package com.jacky;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ServletLoader;
import com.mitchellbosecke.pebble.spring.extension.SpringExtension;
import com.mitchellbosecke.pebble.spring.servlet.PebbleViewResolver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.File;

/**
 * @author jacky
 * @time 2020-12-25 19:57
 * @discription SpringMVC服务配置类
 *              使用Spring MVC时，整个Web应用程序按如下顺序启动：
 *                  启动Tomcat服务器；
 *                  Tomcat读取web.xml并初始化DispatcherServlet；
 *                  DispatcherServlet创建IoC容器并自动注册到ServletContext中。
 */
@Configuration
@ComponentScan
@EnableWebMvc  //启用Spring MVC
@EnableTransactionManagement
@PropertySource("classpath:/jdbc.properties")
public class WebConfig {

    public static void main(String[] args) throws Exception{
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.getInteger("port", 8080));
        tomcat.getConnector();
        Context ctx = tomcat.addWebapp("", new File("src/main/webapp").getAbsolutePath());
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(
                new DirResourceSet(resources, "/WEB-INF/classes", new File("target/classes").getAbsolutePath(), "/"));
        ctx.setResources(resources);
        tomcat.start();
        tomcat.getServer().await();
    }

    // -- Mvc configuration ---------------------------------------------------

    /**
     * WebMvcConfigurer并不是必须的，但我们在这里创建一个默认的WebMvcConfigurer，只覆写addResourceHandlers()，
     * 目的是让Spring MVC自动处理静态文件，并且映射路径为/static/**
     * @return WebMvcConfigurer
     */
    @Bean
    WebMvcConfigurer createWebMvcConfigurer(@Autowired HandlerInterceptor[] interceptors) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                for (HandlerInterceptor interceptor : interceptors){
                    registry.addInterceptor(interceptor);
                }
            }
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/static/**").addResourceLocations("/static/");
            }

            /**
             * 定义一个全局CORS配置，全称Cross-Origin Resource Sharing，是HTML5规范定义的如何跨域访问资源。
             * 如果A站的JavaScript访问B站API的时候，B站能够返回响应头Access-Control-Allow-Origin: http://a.com，
             *  那么，浏览器就允许A站的JavaScript访问B站的API。
             *
             *  注意到跨域访问能否成功，取决于B站是否愿意给A站返回一个正确的Access-Control-Allow-Origin响应头，所以决定权永远在提供API的服务方手中。
             *
             *  还可使用Spring提供的CorsFilter。不过，推荐使用上述方式。
             *
             *  也可以使用@CrossOrigin注解，详见ApiController类
             * @param registry
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://local.liaoxuefeng.com:8080")
                        .allowedMethods("GET", "POST")
                        .maxAge(3600);
            }
        };
    }

    // -- pebble view configuration -------------------------------------------

    /**
     * 另一个必须要创建的Bean是ViewResolver，因为Spring MVC允许集成任何模板引擎，使用哪个模板引擎，就实例化一个对应的ViewResolver
     * ViewResolver通过指定prefix和suffix来确定如何查找View。
     * 下述配置使用Pebble引擎，指定模板文件存放在/WEB-INF/tempaltes/目录下。
     * @param servletContext servletContext
     * @return ViewResolver
     */
    @Bean
    ViewResolver createViewResolver(@Autowired ServletContext servletContext) {
        PebbleEngine engine = new PebbleEngine.Builder().autoEscaping(true)
                // cache:
                .cacheActive(false)
                // loader:
                .loader(new ServletLoader(servletContext))
                // extension:
                .extension(new SpringExtension())
                // build:
                .build();
        PebbleViewResolver viewResolver = new PebbleViewResolver();
        viewResolver.setPrefix("/WEB-INF/templates/");
        viewResolver.setSuffix("");
        viewResolver.setPebbleEngine(engine);
        return viewResolver;
    }

    // -- jdbc configuration --------------------------------------------------

    @Value("${jdbc.url}")
    String jdbcUrl;

    @Value("${jdbc.username}")
    String jdbcUsername;

    @Value("${jdbc.password}")
    String jdbcPassword;

    @Bean
    DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(jdbcUsername);
        config.setPassword(jdbcPassword);
        config.addDataSourceProperty("autoCommit", "false");
        config.addDataSourceProperty("connectionTimeout", "5");
        config.addDataSourceProperty("idleTimeout", "60");
        return new HikariDataSource(config);
    }

    @Bean
    JdbcTemplate createJdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    PlatformTransactionManager createTxManager(@Autowired DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
