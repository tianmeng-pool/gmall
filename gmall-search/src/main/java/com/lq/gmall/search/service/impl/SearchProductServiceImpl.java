package com.lq.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lq.gmall.constant.EsConstant;
import com.lq.gmall.search.SearchProductService;
import com.lq.gmall.vo.es.SearchParam;
import com.lq.gmall.vo.es.SearchResponse;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

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

        log.debug("检索的dsl语句为:{}",buildDsl);

        //2.构建检索
        Search search = new Search.Builder("").addIndex(EsConstant.ES_PRODUCT_INDEX)
                .addType(EsConstant.ES_PRODUCT_INDEX_INFO)
                .build();
        try {
            SearchResult execute = jestClient.execute(search);

            SearchResponse searchResponse = buildSearchResponse(execute);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //3.对检索的数据进行处理，把SearchResult转为SearchResponse

        return null;
    }

    private SearchResponse buildSearchResponse(SearchResult execute) {

        return null;
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
        //sourceBuilder.aggregation(null);

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
