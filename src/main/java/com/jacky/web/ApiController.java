package com.jacky.web;

import com.jacky.entity.User;
import com.jacky.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jacky
 * @time 2021-01-01 01:04
 * @discription 在Web应用中，除了需要使用MVC给用户显示页面外，还有一类API接口，我们称之为
 *              API接口，称之为REST，通常输入输出都是JSON，便于第三方调用或者使用页面JavaScript与之交互。
 *              @GetMapping和@PostMapping都支持指定输入和输出的格式。
 *
 *              直接用Spring的Controller配合一大堆注解写REST太麻烦了，
 *              因此，Spring还额外提供了一个@RestController注解，使用@RestController替代@Controller后，每个方法自动变成API接口方法。
 *
 *              使用@RestController可以方便地编写REST服务，Spring默认使用JSON作为输入和输出。
 *              要控制序列化和反序列化，可以使用Jackson提供的@JsonIgnore和@JsonProperty注解。
 */

//@Controller
//类中的方法自动变成API接口方法
@RestController
@RequestMapping("/api")
/**
 * CORS可以控制指定域的页面JavaScript能否访问API。
 * 使用@CrossOrigin注解，可以在@RestController的class级别或方法级别定义一个@CrossOrigin
 *  上述定义在ApiController处的@CrossOrigin指定了只允许来自local.liaoxuefeng.com跨域访问，
 * 允许多个域访问需要写成数组形式，例如origins = {"http://a.com", "https://www.b.com"})。如果要允许任何域访问，写成origins = "*"即可。
 * 如果有多个REST Controller都需要使用CORS，那么，每个Controller都必须标注@CrossOrigin注解。
 */
@CrossOrigin(origins = "http://local.liaoxuefeng.com:8080")
public class ApiController {

    @PostMapping(value = "/rest",
                //声明能接收的类型
                consumes = "application/json;charset=UTF-8",
                //输出的类型
                produces = "application/json;charset=UTF-8")
    //表示返回的String无需额外处理，直接作为输出内容写入HttpServletResponse
    @ResponseBody
    //@RequestBody将输入的JSON直接被Spring反序列化为User这个JavaBean
    public String rest(@RequestBody User user){
        return "{\"restSupport\":true}";
    }

    @Autowired
    UserService userService;

    @GetMapping("/users")
    public List<User> users(){
        return userService.getUsers();
    }

    @GetMapping("/users/{id}")
    public User user(@PathVariable("id") long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/signin")
    public Map<String, Object> signin(@RequestBody SignInRequest signinRequest) {
        try {
            User user = userService.signin(signinRequest.email, signinRequest.password);
            Map<String, Object> model = new HashMap<>();
            model.put("user", user);
            return model;
        } catch (Exception e) {
            Map<String, Object> model = new HashMap<>();
            model.put("error", "SIGNIN_FAILED");
            model.put("message", e.getMessage());
            return model;
        }
    }
    public static class SignInRequest {
        public String email;
        public String password;
    }
}
