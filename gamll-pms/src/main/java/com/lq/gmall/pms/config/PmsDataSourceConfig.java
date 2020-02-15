package com.lq.gmall.pms.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import io.shardingjdbc.core.api.MasterSlaveDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import javax.sql.DataSource;
import java.io.File;

/**
 * @author tianmeng
 * @date 2020/2/10
 */
@Configuration
public class PmsDataSourceConfig {

    /**
     * 配置主从数据源
     * @return
     * @throws Exception
     */
    @Bean
    public DataSource dataSource() throws Exception{

        File file = ResourceUtils.getFile("classpath:sharding-jdbc.yml");

        DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(file);

        return dataSource;
    }

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
         paginationInterceptor.setOverflow(true);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        // paginationInterceptor.setLimit(500);
        return paginationInterceptor;
    }

}
