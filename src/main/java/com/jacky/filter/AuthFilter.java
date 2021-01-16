package com.jacky.filter;

import com.jacky.entity.User;
import com.jacky.service.UserService;
import com.jacky.web.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author jacky
 * @time 2021-01-12 22:35
 * @discription 允许用户使用Basic模式进行用户验证，即在HTTP请求中添加头Authorization: Basic email:password
 *              在Spring中创建的这个AuthFilter是一个普通Bean，Servlet容器并不知道，所以它不会起作用。
 *
 *              如果我们直接在web.xml中声明这个AuthFilter，注意到AuthFilter的实例将由Servlet容器而不是Spring容器初始化，
 *              因此，@Autowire根本不生效，用于登录的UserService成员变量永远是null。
 *
 *              所以，得通过一种方式，让Servlet容器实例化的Filter，间接引用Spring容器实例化的AuthFilter。
 *              Spring MVC提供了一个DelegatingFilterProxy，专门来干这个事情。（见web.xml）
 *
 *              实现原理：
 *                  Servlet容器从web.xml中读取配置，实例化DelegatingFilterProxy，注意命名是authFilter；
 *                  Spring容器通过扫描@Component实例化AuthFilter。
*               当DelegatingFilterProxy生效后，它会自动查找注册在ServletContext上的Spring容器，
 *               再试图从容器中查找名为authFilter的Bean，也就是我们用@Component声明的AuthFilter。
 *
 *               注意：Basic认证模式并不安全，本节只用来作为使用Filter的示例。
 *
 *               当一个Filter作为Spring容器管理的Bean存在时，可以通过DelegatingFilterProxy间接地引用它并使其生效。
 */

//@Component
public class AuthFilter implements Filter {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        try {
            authenticateByHeader(req);
        } catch (RuntimeException e) {
            logger.warn("login by authorization header failed.", e);
        }
        chain.doFilter(request, response);
    }

    private void authenticateByHeader(HttpServletRequest req) throws UnsupportedEncodingException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            logger.info("try authenticate by authorization header...");
            String up = new String(Base64.getDecoder().decode(authHeader.substring(6)), StandardCharsets.UTF_8);
            int pos = up.indexOf(':');
            if (pos > 0) {
                String email = URLDecoder.decode(up.substring(0, pos), String.valueOf(StandardCharsets.UTF_8));
                String password = URLDecoder.decode(up.substring(pos + 1), String.valueOf(StandardCharsets.UTF_8));
                User user = userService.signin(email, password);
                req.getSession().setAttribute(UserController.KEY_USER, user);
                logger.info("user {} login by authorization header ok.", email);
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
