package com.jacky.interceptor;

import com.jacky.entity.User;
import com.jacky.service.UserService;
import com.jacky.web.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author jacky
 * @time 2021-01-13 22:39
 * @discription 添加一个AuthInterceptor，用于替代使用AuthFilter进行Basic认证的功能。
 *              注：对Interceptor的介绍，在LoggerInterceptor中体现。
 */

//@Order(2)
//@Component
public class AuthInterceptor implements HandlerInterceptor {

    final Logger logger = LoggerFactory.getLogger(getClass());

    //这个AuthInterceptor是由Spring容器直接管理的，因此注入UserService非常方便。
    @Autowired
    UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.info("pre authenticate {}...", request.getRequestURI());
        try {
            authenticateByHeader(request);
        } catch (RuntimeException e) {
            logger.warn("login by authorization header failed.", e);
        }
        return true;
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
}
