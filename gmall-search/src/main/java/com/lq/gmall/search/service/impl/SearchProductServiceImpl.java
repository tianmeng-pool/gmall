package com.lq.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lq.gmall.constant.EsConstant;
import com.lq.gmall.search.SearchProductService;
import com.lq.gmall.vo.es.SearchParam;
import com.lq.gmall.vo.es.SearchResponse;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @author tianmeng
 * @date 2020/2/25
 */
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

        //2.构建检索
        Search search = new Search.Builder("").addIndex(EsConstant.ES_PRODUCT_INDEX)
                .addType(EsConstant.ES_PRODUCT_INDEX_INFO)
                .build();
        try {
            SearchResult execute = jestClient.execute(search);

            SearchResponse searchResponse = new SearchResponse();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //3.对检索的数据进行处理，把SearchResult转为SearchResponse

        return null;
    }

    private String buildDsl(SearchParam searchParam) {

        //1.查询
        //1.1检索
        //1.2过滤
        //1.2.1按照属性过滤，按照品牌过滤、按照分类过滤

        //高亮

        //聚合

        //分页

        return "";
    }
}
