package com.lq.gmall.search;

import com.lq.gmall.vo.es.SearchParam;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private JestClient jestClient;

    @Autowired
    private SearchProductService searchProductService;

    @Test
    void contextLoads() throws IOException {

        Search search = new Search.Builder("").addIndex("product").addType("info").build();

        SearchResult execute = jestClient.execute(search);

        System.out.println(execute.getTotal());

    }

    @Test
    public void searchTest(){
        SearchSourceBuilder builder = new SearchSourceBuilder();

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        queryBuilder.filter();

        queryBuilder.must();

        builder.query(queryBuilder);

        AggregationBuilder aggregationBuilder = AggregationBuilders.terms("");

        builder.aggregation(aggregationBuilder);

        String toString = builder.toString();

        System.out.println(toString);
    }

    @Test
    public void dslTest(){

        SearchParam searchParam = new SearchParam();

        String keyWord = "手机";
        searchParam.setKeyword(keyWord);

        searchProductService.searchProduct(searchParam);
    }

}
