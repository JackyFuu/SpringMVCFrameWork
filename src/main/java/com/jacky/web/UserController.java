package com.jacky.web;

import com.jacky.entity.User;
import com.jacky.service.MailService;
import com.jacky.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jacky
 * @time 2020-12-25 23:10
 * @discription Controller类
 *                  1、@GetMapping GET请求
 *                  2、@PostMapping POST请求
 *                  3、@RequestParam 需要接收的HTTP参数
 *                      如果方法参数需要传入HttpServletRequest、HttpServletResponse或者HttpSession，
 *                      直接添加这个类型的参数即可，Spring MVC会自动按类型传入。
 *                  4、返回的ModelAndView通常包含View的路径和一个Map作为Model，但也可以没有Model
 *                  5、返回重定向时既可以写new ModelAndView("redirect:/signin")，也可以直接返回String
 *                  6、如果在方法内部直接操作HttpServletResponse发送响应，返回null表示无需进一步处理。
 *                  7、对URL进行分组，每组对应一个Controller是一种很好的组织形式，
 *                      并可以在Controller的class定义出添加URL前缀
 */

@Controller
public class UserController {

    public static final String KEY_USER = "__user__";

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;

    @Autowired
    MailService mailService;

    /**
     * 在Controller中，Spring MVC还允许定义基于@ExceptionHandler注解的异常处理方法。
     * 异常处理方法没有固定的方法签名，可以传入Exception、HttpServletRequest等，返回值可以是void，也可以是ModelAndView，
     * 下述代码通过@ExceptionHandler(RuntimeException.class)表示当发生RuntimeException的时候，就自动调用此方法处理。
     *
     * 可以编写多个错误处理方法，每个方法针对特定的异常。例如，处理LoginException使得页面可以自动跳转到登录页。
     *
     * 使用ExceptionHandler时，要注意它仅作用于当前的Controller，
     * 即ControllerA中定义的一个ExceptionHandler方法对ControllerB不起作用。如果我们有很多Controller，
     * 每个Controller都需要处理一些通用的异常，例如LoginException，思考一下应该怎么避免重复代码？
     * @param ex
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleUnknowException(Exception ex){
        Map<String, Object> model = new HashMap<>();
        model.put("error", ex.getClass().getSimpleName());
        model.put("message", ex.getMessage());
        return new ModelAndView("500.html", model);
    }

    /**
     * 测试基于@ExceptionHandler注解的异常处理方法。
     * @return
     */
    @GetMapping("/testExceptionHandler")
    public ModelAndView testExceptionHandler() {
        throw new RuntimeException();
    }

    @GetMapping("/")
    public ModelAndView index(HttpSession session) {
        User user = (User) session.getAttribute(KEY_USER);
        Map<String, Object> model = new HashMap<>();
        if (user != null) {
            model.put("user", model);
        }
        return new ModelAndView("index.html", model);
    }

    @GetMapping("/register")
    public ModelAndView register() {
        return new ModelAndView("register.html");
    }

    @PostMapping("/register")
    public ModelAndView doRegister(@RequestParam("email") String email, @RequestParam("password") String password,
                                   @RequestParam("name") String name) {
        try {
            User user = userService.register(email, password, name);
            logger.info("user registered: {}", user.getEmail());
            mailService.setRegistrationMail(user);
        } catch (RuntimeException e) {
            Map<String, Object> model = new HashMap<>();
            model.put("email", email);
            model.put("error", "Register failed");
            return new ModelAndView("register.html", model);
        }
        return new ModelAndView("redirect:/signin");
    }

    @GetMapping("/signin")
    public ModelAndView signin(HttpSession session) {
        User user = (User) session.getAttribute(KEY_USER);
        if (user != null) {
            return new ModelAndView("redirect:/profile");
        }
        return new ModelAndView("signin.html");
    }

    @PostMapping("/signin")
    public ModelAndView doSignin(@RequestParam("email") String email, @RequestParam("password") String password,
                                 HttpSession session) {
        try {
            User user = userService.signin(email, password);
            session.setAttribute(KEY_USER, user);
        } catch (RuntimeException e) {
            Map<String, Object> model = new HashMap<>();
            model.put("email", email);
            model.put("error", "Signin failed");
            return new ModelAndView("signin.html", model);
        }
        return new ModelAndView("redirect:/profile");
    }

    @GetMapping("/profile")
    public ModelAndView profile(HttpSession session) {
        User user = (User) session.getAttribute(KEY_USER);
        if (user == null) {
            return new ModelAndView("redirect:/signin");
        }
        Map<String, Object> model = new HashMap<>();
        model.put("user", user);
        return new ModelAndView("profile.html", model);
    }

    @GetMapping("/signout")
    public String signout(HttpSession session) {
        session.removeAttribute(KEY_USER);
        return "redirect:/signin";
    }
}
