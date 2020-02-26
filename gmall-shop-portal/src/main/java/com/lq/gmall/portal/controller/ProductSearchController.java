package com.lq.gmall.portal.controller;

/**
 * @author tianmeng
 * @date 2020/2/26
 */

import com.alibaba.dubbo.config.annotation.Reference;
import com.lq.gmall.search.SearchProductService;
import com.lq.gmall.vo.es.SearchParam;
import com.lq.gmall.vo.es.SearchResponse;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 检索服务
 */
@RestController
@RequestMapping
@CrossOrigin
@Slf4j
@Api(tags = "ProductSearchController", description = "商品检索")
public class ProductSearchController {

    @Reference
    private SearchProductService searchProductService;

    @GetMapping("/search")
    public SearchResponse searchProduct(@RequestBody SearchParam searchParam){

        SearchResponse searchResponse = searchProductService.searchProduct(searchParam);

        return searchResponse;
    }

}
