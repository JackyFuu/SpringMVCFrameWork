package com.jacky.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * @author jacky
 * @time 2021-01-14 23:06
 * @discription  要在View中使用MessageSource加上Locale输出多语言，
 *                  我们通过编写一个MvcInterceptor，把相关资源注入到ModelAndView中：
 */

@Component
public class MvcInterceptor implements HandlerInterceptor {

    @Autowired
    LocaleResolver localeResolver;

    @Autowired
    //// 注意注入的MessageSource名称是i18n:
    @Qualifier("i18n")
    MessageSource messageSource;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null){
            //解析用户的Locale
            Locale locale = localeResolver.resolveLocale(request);
            //放入Model
            modelAndView.addObject("__messageSource__", messageSource);
            modelAndView.addObject("__locale__", locale);
        }
    }
}
