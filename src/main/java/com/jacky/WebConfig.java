package com.jacky;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacky.webSocket.ChatHandler;
import com.jacky.webSocket.ChatHandshakeInterceptor;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Extension;
import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.loader.ServletLoader;
import com.mitchellbosecke.pebble.spring.servlet.PebbleViewResolver;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.jms.ConnectionFactory;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.File;
import java.util.*;

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
//@EnableWebSocket
@EnableJms //Java Message Service
@EnableWebMvc  //启用Spring MVC
@EnableScheduling //启用Scheduler
@EnableMBeanExport  //自动注册MBean(Managed Bean):for JMX(Java Management Extensions)
@EnableTransactionManagement
@PropertySource({"classpath:/jdbc.properties", "classpath:/smtp.properties", "classpath:/jms.properties"})
public class WebConfig {

    final Logger logger = LoggerFactory.getLogger(getClass());

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
            /**
             * 注册Interceptor
             * @param registry
             */
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

    /**
     * 加入Spring WEB对WebSocket的配置：
     * 此实例在内部通过WebSocketHandlerRegistry注册能处理WebSocket的WebSocketHandler，
     * 以及可选的WebSocket拦截器HandshakeInterceptor
     * @param chatHandler
     * @param chatInterceptor
     * @return
     */
    @Bean
    WebSocketConfigurer createWebSocketConfigurer(@Autowired ChatHandler chatHandler,
                                                  @Autowired ChatHandshakeInterceptor chatInterceptor) {
        return new WebSocketConfigurer() {
            @Override
            public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
                // 把URL与指定的WebSocketHandler关联，可关联多个:
                registry.addHandler(chatHandler, "/chat").addInterceptors(chatInterceptor);
            }
        };
    }

    @Bean
    ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return om;
    }

    /**
     * Spring MVC通过LocaleResolver来自动从HttpServletRequest中获取Locale。有多种LocaleResolver的实现类，其中最常用的是CookieLocaleResolver。
     *
     * CookieLocaleResolver从HttpServletRequest中获取Locale时，
     * 1）首先根据一个特定的Cookie判断是否指定了Locale，
     * 2）如果没有，就从HTTP头获取，
     * 3）如果还没有，就返回默认的Locale。
     * @return
     */
    @Bean
    LocaleResolver createLocaleResolver(){
        CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
        cookieLocaleResolver.setDefaultLocale(Locale.ENGLISH);
        cookieLocaleResolver.setDefaultTimeZone(TimeZone.getDefault());
        return cookieLocaleResolver;
    }

    /**
     * 创建一个Spring提供的MessageSource实例，它自动读取所有的.properties文件，并提供一个统一接口来实现“翻译”：
     *
     * ResourceBundleMessageSource会自动根据主文件名自动把所有相关语言的资源文件都读进来。
     *
     * 再注意到Spring容器会创建不只一个MessageSource实例，
     * 我们自己创建的这个MessageSource是专门给页面国际化使用的，因此命名为i18n，不会与其它MessageSource实例冲突。
     * @return
     */
    @Bean("i18n")
    MessageSource createMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 指定文件是UTF-8编码
        messageSource.setDefaultEncoding("UTF-8");
        // 指定主文件名
        messageSource.setBasename("messages");
        return messageSource;
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
    ViewResolver createViewResolver(@Autowired ServletContext servletContext, @Autowired @Qualifier("i18n") MessageSource messageSource) {
        PebbleEngine engine = new PebbleEngine.Builder().autoEscaping(true)
                //设置是否自动执行转义。
                .autoEscaping(true)
                // cache:
                .cacheActive(false)
                // loader:
                .loader(new ServletLoader(servletContext))
                // extension:(添加国际化函数扩展)
                .extension(createExtension(messageSource))
                //.extension(new SpringExtension())
                // build:
                .build();
        PebbleViewResolver viewResolver = new PebbleViewResolver();
        viewResolver.setPrefix("/WEB-INF/templates/");
        viewResolver.setSuffix("");
        viewResolver.setPebbleEngine(engine);
        return viewResolver;
    }

    /**
     * 使用View时，要根据每个特定的View引擎定制国际化函数。在Pebble中，我们可以封装一个国际化函数，名称就是下划线_，改造一下创建ViewResolver的代码：
     * @param messageSource
     * @return
     */
    private Extension createExtension(MessageSource messageSource) {
        return new AbstractExtension() {
            @Override
            public Map<String, Function> getFunctions() {
                Map<String, Function> functionMap = new HashMap<>();
                functionMap.put("_", new Function() {
                    public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
                        String key = (String) args.get("0");
                        List<Object> arguments = this.extractArguments(args);
                        Locale locale = (Locale) context.getVariable("__locale__");
                        return messageSource.getMessage(key, arguments.toArray(), "???" + key + "???", locale);
                    }

                    private List<Object> extractArguments(Map<String, Object> args) {
                        int i = 1;
                        List<Object> arguments = new ArrayList<>();
                        while (args.containsKey(String.valueOf(i))) {
                            Object param = args.get(String.valueOf(i));
                            arguments.add(param);
                            i++;
                        }
                        return arguments;
                    }

                    public List<String> getArgumentNames() {
                        return null;
                    }
                });
                return functionMap;
            }
        };
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

    // -- javamail configuration ----------------------------------------------
    @Bean
    JavaMailSender createJavaMailSender(
            // properties:
            @Value("${smtp.host}") String host,
            @Value("${smtp.port}") int port,
            @Value("${smtp.auth}") String auth,
            @Value("${smtp.username}") String username,
            @Value("${smtp.password}") String password,
            @Value("${smtp.debug:true}") String debug){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", auth);
        if (port == 587){
            properties.put("mail.smtp.starttls.enable", "true");
        }
        if (port == 465){
            properties.put("mail.smtp.socketFactory.port", "465");
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        properties.put("mail.debug", debug);
        return mailSender;
    }
    @Bean
    PlatformTransactionManager createTxManager(@Autowired DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    // -- jms configuration ---------------------------------------------------

    /**
     * 连接消息服务器的连接池
     * 因为我们使用的消息服务器是ActiveMQ Artemis，所以ConnectionFactory的实现类就是消息服务器提供的ActiveMQJMSConnectionFactory，
     * 它需要的参数均由配置文件读取后传入，并设置了默认值。
     * @param uri
     * @param username
     * @param password
     * @return
     */
    @Bean
    ConnectionFactory createJMSConnectionFactory(
            @Value("${jms.uri:tcp://localhost:61616}") String uri,
            @Value("${jms.username:admin}") String username,
            @Value("${jms.password:123456}") String password) {
        logger.info("create JMS connection factory for standalone activemq artemis server...");
        return new ActiveMQJMSConnectionFactory(uri, username, password);
    }

    /**
     * 我们再创建一个JmsTemplate，它是Spring提供的一个工具类，和JdbcTemplate类似，可以简化发送消息的代码：
     * @param connectionFactory
     * @return
     */
    @Bean
    JmsTemplate createJmsTemplate(@Autowired ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

    /**
     * 除了必须指定Bean的名称为jmsListenerContainerFactory外，这个Bean的作用是处理和Consumer相关的Bean。
     * @param connectionFactory
     * @return
     */
    @Bean("jmsListenerContainerFactory")
    DefaultJmsListenerContainerFactory createJmsListenerContainerFactory(
            @Autowired ConnectionFactory connectionFactory){
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }


}
