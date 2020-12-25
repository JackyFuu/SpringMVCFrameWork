package com.jacky;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author jacky
 * @time 2020-12-25 19:57
 * @discription SpringMVC服务配置类
 */
@Configuration
@ComponentScan
@EnableWebMvc  //启用Spring MVC
@EnableTransactionManagement
@PropertySource("classpath:/jdbc.properties")
public class WebConfig {
}
