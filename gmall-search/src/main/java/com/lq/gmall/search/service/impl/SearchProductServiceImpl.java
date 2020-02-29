package com.lq.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.lq.gmall.constant.EsConstant;
import com.lq.gmall.search.SearchProductService;
import com.lq.gmall.to.es.EsProduct;
import com.lq.gmall.vo.es.SearchParam;
import com.lq.gmall.vo.es.SearchResponse;
import com.lq.gmall.vo.es.SearchResponseAttrVo;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tianmeng
 * @date 2020/2/25
 */
@Slf4j
@Service
@Component
public class SearchProductServiceImpl implements SearchProductService {

    @Autowired
    private JestClient jestClient;

    /**
     * 数据检索
     * @return
     */
    @Override
    public SearchResponse searchProduct(SearchParam searchParam){

        //1.构建检索语句，dsl语句
        String buildDsl = buildDsl(searchParam);

        log.error("检索的dsl语句为:{}",buildDsl);

        //2.构建检索
        Search search = new Search.Builder(buildDsl).addIndex(EsConstant.ES_PRODUCT_INDEX)
                .addType(EsConstant.ES_PRODUCT_INDEX_INFO)
                .build();
        SearchResult execute = null;
        try {
            execute = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //3.对检索的数据进行处理，把SearchResult转为SearchResponse
        SearchResponse searchResponse = buildSearchResponse(execute);

        //设置分页条件
        searchResponse.setPageNum(searchParam.getPageNum());
        searchResponse.setPageSize(searchParam.getPageSize());

        return searchResponse;
    }

    private SearchResponse buildSearchResponse(SearchResult execute) {

        SearchResponse searchResponse = new SearchResponse();

        MetricAggregation aggregations = execute.getAggregations();

        //searchResponse.setBrand();//设置品牌数据
        //封装品牌数据
        TermsAggregation brand_agg = aggregations.getTermsAggregation("brand_agg");

        List<String> brandNames = new ArrayList<>();

        brand_agg.getBuckets().forEach((bucket) -> {
            String keyAsString = bucket.getKeyAsString();
            brandNames.add(keyAsString);
        });

        SearchResponseAttrVo brand_AttrVo = new SearchResponseAttrVo();
        brand_AttrVo.setName("品牌");
        brand_AttrVo.setValue(brandNames);

        searchResponse.setBrand(brand_AttrVo);
        //====================品牌数据封装完成==========================

        //searchResponse.setCatelog();//设置分类
        //封装分类数据
        TermsAggregation category_agg = aggregations.getTermsAggregation("category_agg");
        List<String> cate_list = new ArrayList<>();
        category_agg.getBuckets().forEach((cate_Bucket) -> {

            Map<String,String> map = new HashMap<>();

            String cateGoryName = cate_Bucket.getKeyAsString();

            //获取category_agg下面的子聚合
            TermsAggregation categoryId_agg = cate_Bucket.getTermsAggregation("categoryId_agg");
            String cateGoryId = categoryId_agg.getBuckets().get(0).getKeyAsString();

            //把id和name存入map
            map.put("id",cateGoryId);
            map.put("name",cateGoryName);

            //把map转为json字符串传给前段，json字符串里面包含id和name
            String s = JSON.toJSONString(map);

            cate_list.add(s);
        });
        SearchResponseAttrVo cate_Attrvo = new SearchResponseAttrVo();
        //setValue()里面有id和name，就不需要设置setProductId()的值
        cate_Attrvo.setName("分类");
        cate_Attrvo.setValue(cate_list);

        searchResponse.setCatelog(cate_Attrvo);
        //==================分类数据封装完成================================

        //设置总记录数
        searchResponse.setTotal(execute.getTotal());
        //=================总记录数封装完成=========================

        //searchResponse.setAttrs();//设置筛选属性
        //获取总聚合下面的子聚合下面的terms聚合
        TermsAggregation attrNames = aggregations.getChildrenAggregation("attr_agg")
                .getTermsAggregation("attrName_agg");
        List<SearchResponseAttrVo> attrVos = new ArrayList<>();
        List<String> list = new ArrayList<>();
        //遍历terms聚合下的所有数据
        attrNames.getBuckets().forEach((att_bucket) -> {
            SearchResponseAttrVo vo = new SearchResponseAttrVo();
            String attr_Name = att_bucket.getKeyAsString();
            vo.setName(attr_Name);
            //获取该terms聚合下的两个字聚合
            TermsAggregation attrId_agg = att_bucket.getTermsAggregation("attrId_agg");
            vo.setProductAttributeId(Long.parseLong(attrId_agg.getBuckets().get(0).getKeyAsString()));
            TermsAggregation attrValue_agg = att_bucket.getTermsAggregation("attrValue_agg");
            List<TermsAggregation.Entry> buckets = attrValue_agg.getBuckets();
            //遍历属性值的buckets，把buckets里面的数据封装到list中
            buckets.forEach((valueBucket) -> {
                String attrValue = valueBucket.getKeyAsString();
                list.add(attrValue);
            });
            //封装属性名对应的属性值
            vo.setValue(list);
            attrVos.add(vo);
        });

        searchResponse.setAttrs(attrVos);
        //==================筛选属性封装完成=========================

        //searchResponse.setProducts();//封装商品数据
        List<SearchResult.Hit<EsProduct, Void>> hits = execute.getHits(EsProduct.class);
        List<EsProduct> esProducts = new ArrayList<>();
        hits.forEach((hit) -> {
            EsProduct source = hit.source;
            //提取高亮结果
            String title = hit.highlight.get("skuProductInfos.skuTitle").get(0);
            //设置高亮结果
            source.setName(title);

            esProducts.add(source);
        });

        searchResponse.setProducts(esProducts);
        //================商品数据封装完成====================

        return searchResponse;
    }

    /**
     * 构建dsl语句
     * @param searchParam
     * @return
     */
    private String buildDsl(SearchParam searchParam) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //1.查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1检索
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            //根据dsl语句来构建检索
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("skuProductInfos.skuTitle", searchParam.getKeyword());
            NestedQueryBuilder skuProductInfos = QueryBuilders.nestedQuery("skuProductInfos", matchQueryBuilder, ScoreMode.None);
            boolQuery.must(skuProductInfos);
        }

        //1.2过滤
        if (searchParam.getCatelog3() != null && searchParam.getCatelog3().length > 0 ) {
            //根据三级分类id来过滤(可以有多个三级分类)
            boolQuery.filter(QueryBuilders.termsQuery("productCategoryId",searchParam.getCatelog3()));
        }
        if (searchParam.getBrand() != null && searchParam.getBrand().length > 0) {
            //根据品牌来过滤(可以允许有多个品牌)
            boolQuery.filter(QueryBuilders.termsQuery("brandName.keyword",searchParam.getBrand()));
        }
        if (searchParam.getProps() != null && searchParam.getProps().length > 0) {
            //根据筛选属性过滤
            String[] props = searchParam.getProps();
            for (String prop : props) {
                //先根据:分割prop,获取prop的值
                String[] split = prop.split(":");
                //2:3g-4g;2号属性的值是4g或者3g,用-分隔开
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                boolQueryBuilder.must(QueryBuilders.matchQuery("attrValueList.productAttributeId",split[0]));
                boolQueryBuilder.must(QueryBuilders.termsQuery("attrValueList.value.keyword",split[1].split("-")));
                NestedQueryBuilder attrValueList = QueryBuilders.nestedQuery("attrValueList", boolQueryBuilder, ScoreMode.None);
                boolQuery.filter(attrValueList);
            }

        }
        if (searchParam.getPriceFrom()  != null || searchParam.getPriceTo() != null) {
            //根据价格区间来检索
            RangeQueryBuilder price = QueryBuilders.rangeQuery("price");
            if (searchParam.getPriceFrom() != null) {
                boolQuery.filter(price.gte(searchParam.getPriceFrom()));
            }
            if (searchParam.getPriceTo() != null) {
                boolQuery.filter(price.lte(searchParam.getPriceTo()));
            }
        }

        //1.2.1按照属性过滤，按照品牌过滤、按照分类过滤
        sourceBuilder.query(boolQuery);

        //高亮
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuProductInfos.skuTitle");
            //设置高亮为红色
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        //聚合
        //1.按照品牌聚合
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("brand_agg").field("brandName.keyword");
        aggregationBuilder.subAggregation(AggregationBuilders.terms("brandId").field("brandId"));
        sourceBuilder.aggregation(aggregationBuilder);

        //2.按照分类聚合
        TermsAggregationBuilder category_agg = AggregationBuilders.terms("category_agg").field("productCategoryName.keyword");
        category_agg.subAggregation(AggregationBuilders.terms("categoryId_agg").field("productCategoryId"));
        sourceBuilder.aggregation(category_agg);
        //3.按照属性聚合
        //获取内嵌属性的名字和路径
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrValueList");
        //属性名聚合
        TermsAggregationBuilder attrName_agg = AggregationBuilders.terms("attrName_agg").field("attrValueList.name");
        //聚合属性名所对应的属性值
        attrName_agg.subAggregation(AggregationBuilders.terms("attrValue_agg").field("attrValueList.value.keyword"));
        //聚合属性名所对应的属性名id
        attrName_agg.subAggregation(AggregationBuilders.terms("attrId_agg").field("attrValueList.productAttributeId"));

        attr_agg.subAggregation(attrName_agg);
        sourceBuilder.aggregation(attr_agg);

        //分页
        //从第几页开始
        sourceBuilder.from((searchParam.getPageNum() - 1) * searchParam.getPageSize());
        //每页显示的条数
        sourceBuilder.size(searchParam.getPageSize());

        //排序
        if (!StringUtils.isEmpty(searchParam.getOrder())) {
            // 0：综合排序  1：销量  2：价格
            // order=1:asc  排序规则
            String order = searchParam.getOrder();
            String[] split = order.split(":");
            if (split[0].equals("0")) {
                //默认综合排序，不做处理
            }
            //按照销量来排序
            if (split[0].equals("1")) {
                FieldSortBuilder sale = SortBuilders.fieldSort("sale");
                if (split[1].equalsIgnoreCase("asc")) {
                    //按照升序来排列
                    sale.order(SortOrder.ASC);
                } else {
                    //按照降序来排列
                    sale.order(SortOrder.DESC);
                }
                sourceBuilder.sort(sale);
            }
            //按照价格来排序
            if (split[0].equals("2")) {
                FieldSortBuilder price = SortBuilders.fieldSort("price");
                if (split[1].equalsIgnoreCase("asc")) {
                    price.order(SortOrder.ASC);
                } else {
                    price.order(SortOrder.DESC);
                }
                sourceBuilder.sort(price);
            }

        }

        String toString = sourceBuilder.toString();

        return toString;
    }
}
