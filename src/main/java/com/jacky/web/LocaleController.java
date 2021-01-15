package com.jacky.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * @author jacky
 * @time 2021-01-14 23:25
 * @discription 允许用户手动切换Locale，编写一个LocaleController来实现该功能：
 *
 *              多语言支持需要从HTTP请求中解析用户的Locale，然后针对不同Locale显示不同的语言；
 *              Spring MVC应用程序通过MessageSource和LocaleResolver，配合View实现国际化。
 */

@Controller
public class LocaleController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    LocaleResolver localeResolver;

    @GetMapping("/locale/{lo}")
    public String setLocale(@PathVariable("lo") String lo, HttpServletRequest request, HttpServletResponse response) {
        // 根据传入的lo创建Locale实例
        Locale locale = null;
        //Returns the index within this string of the first occurrence of the specified character.
        int pos = lo.indexOf('_');
        if (pos > 0) {
            String lang = lo.substring(0, pos);
            String country = lo.substring(pos + 1);
            locale = new Locale(lang, country);
        } else {
            locale = new Locale(lo);
        }

        // 设定此Locale
        localeResolver.setLocale(request, response, locale);
        logger.info("locale is set to {{}.", locale);
        //刷新页面
        //Returns the value of the specified request header as a String.
        String referer = request.getHeader("Referer");
        System.out.println("header is {}." + referer);
        return "redirect:" + (referer == null ? "/" : referer);
    }
}
