package com.lq.gmall.pms.config;

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

}
