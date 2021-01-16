package com.jacky.webSocket;

import com.jacky.web.UserController;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Collections;

/**
 * @author jacky
 * @time 2021-01-15 16:54
 * @discription 注意到我们在注册WebSocket时还传入了一个ChatHandshakeInterceptor，
 *             这个类实际上可以从HttpSessionHandshakeInterceptor继承，
 *             它的主要作用是在WebSocket建立连接后，把HttpSession的一些属性复制到WebSocketSession，例如，用户的登录信息等：
 *             这样，在ChatHandler中，可以从WebSocketSession.getAttributes()中获取到复制过来的属性。
 */
@Component
public class ChatHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    //// 指定从HttpSession复制属性到WebSocketSession:
    public ChatHandshakeInterceptor() {
        super(Collections.singletonList(UserController.KEY_USER));
    }
}
