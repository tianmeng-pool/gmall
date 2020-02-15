package com.lq.gmall.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 *VO:(view object/value object):视图对象
 *  1）、List<User>:把专门交给前段的数据封装成VO</>
 *  2）、User{};用户给我提交的封装成VO往下传
 *
 *  request ---> 提交的VO
 *  response ---> 返回的VO
 *DAO:(datavase access object)：数据库访问对象；专门用来对数据库进行crud的对象；xxxMapper
 *POJO:(plain old java object)：古老的单纯的Java对象。===>JavaBean(封装数据的)
 *DO:(data object)：数据对象---POJO===》(database object):数据库对象（专门用来封装数据库表的实体类）
 *TO:(Transfer object):传输对象
 *  1)、服务之间互调，为了数据传输封装对象
 *  2)、aService{
 *      user;
 *      movie
 *  }
 *  bService(用户名和电影名 UserMovieTo(userName,movieName))
 * DTO:(Dtat Transfer Object):数据传输对象
 */

/**
 * 如果引入的依赖，引入一个自动配置场景
 * 1）、这个场景自动配置默认生效，我们就必须配置它
 * 2）、不想配置
 *      1.引入的时候排除这个依赖
 *      2.排除掉这个场景的自动配置类
 */

/**
 * 排除数据源的依赖
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GmallAdminWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallAdminWebApplication.class, args);
    }

}
