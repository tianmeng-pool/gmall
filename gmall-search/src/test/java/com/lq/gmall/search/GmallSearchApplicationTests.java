package com.lq.gmall.search;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private JestClient jestClient;

    @Test
    void contextLoads() throws IOException {

        Search search = new Search.Builder("").addIndex("product").addType("info").build();

        SearchResult execute = jestClient.execute(search);

        System.out.println(execute.getTotal());

    }

}
