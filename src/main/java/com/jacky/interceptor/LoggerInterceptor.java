package com.jacky.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * @author jacky
 * @time 2021-01-13 22:22
 * @discription 如果只基于SpringMVC开发应用程序，可以使用SpringMVC提供的一种类似Filter的拦截器：Interceptor。
 *              和Filter相比，Interceptor拦截范围不是后续整个处理流程，而是仅针对Controller拦截。
 *
 *              Interceptor的拦截范围其实就是Controller方法，它实际上就相当于基于AOP的方法拦截。
 *              因为Interceptor只拦截Controller方法，所以要注意，返回ModelAndView后，后续对View的渲染就脱离了Interceptor的拦截范围。
 *
 *              使用Interceptor的好处是Interceptor本身是Spring管理的Bean，因此注入任意Bean都非常简单。
 *              此外，可以应用多个Interceptor，并通过简单的@Order指定顺序。
 *
 *              一个Interceptor必须实现HandlerInterceptor接口，可以选择实现preHandle()、postHandle()和afterCompletion()方法。
 *
 *              最后，要让拦截器生效，我们在WebMvcConfigurer中注册所有的Interceptor：
 */

//@Order(1)
//@Component
public class LoggerInterceptor implements HandlerInterceptor {

    final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * preHandle()是Controller方法调用前执行
     * 在preHandle()中，也可以直接处理响应，然后返回false表示无需调用Controller方法继续处理了，通常在认证或者安全检查失败时直接返回错误响应。
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.info("preHandle {}...", request.getRequestURI());
        if (request.getParameter("debug") != null) {
            PrintWriter pw = response.getWriter();
            pw.write("<p>DEBUG MODE</p>");
            pw.flush();
            return false;
        }
        return true;
    }

    /**
     * postHandle()是Controller方法正常返回后执行
     * 在postHandle()中，因为捕获了Controller方法返回的ModelAndView，
     * 所以可以继续往ModelAndView里添加一些通用数据，很多页面需要的全局数据如Copyright信息等都可以放到这里，无需在每个Controller方法中重复添加。
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        logger.info("postHandle {}.", request.getRequestURI());
        if (modelAndView != null) {
            modelAndView.addObject("__time__", LocalDateTime.now());
        }
    }

    /**
     * afterCompletion()无论Controller方法是否抛异常都会执行，参数ex就是Controller方法抛出的异常（未抛出异常是null）。
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.info("afterCompletion {}: exception = {}", request.getRequestURI(), ex);
    }
}
