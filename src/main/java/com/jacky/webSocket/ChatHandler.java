package com.jacky.webSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacky.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jacky
 * @time 2021-01-15 16:18
 * @discription 和处理普通HTTP请求不同，没法用一个方法处理一个URL。
 *  Spring提供了TextWebSocketHandler和BinaryWebSocketHandler分别处理文本消息和二进制消息，
 *  这里我们选择文本消息作为聊天室的协议
 */
@Component
public class ChatHandler extends TextWebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ChatHistory chatHistory;

    @Autowired
    ObjectMapper objectMapper;

    //保存所有Client的WebSocket会话实例
    //用实例变量clients持有当前所有的WebSocketSession是为了广播，即向所有用户推送同一消息。
    private Map<String, WebSocketSession> clients = new ConcurrentHashMap<>();

    /**
     * 当浏览器请求一个WebSocket连接后，如果成功建立连接，Spring会自动调用afterConnectionEstablished()方法
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        /*
        //新会话根据ID放入Map:
        clients.put(session.getId(), session);
        // 每个WebSocket会话以WebSocketSession表示，且已分配唯一ID,
        // 和WebSocket相关的数据，例如用户名称等，均可放入关联的getAttributes()中。
        session.getAttributes().put("name", "Guest1");
        //super.afterConnectionEstablished(session);
        */
        clients.put(session.getId(), session);
        String name = null;
        User user = (User) session.getAttributes().get("__user__");
        if (user != null) {
            name = user.getName();
        } else {
            name = initGuestName();
        }
        session.getAttributes().put("name", name);
        logger.info("websocket connection established: id = {}, name = {}", session.getId(), name);
        // 把历史消息发给新用户:
        List<ChatMessage> list = chatHistory.getHistory();
        session.sendMessage(toTextMessage(list));
        // 添加系统消息并广播:
        ChatMessage msg = new ChatMessage("SYSTEM MESSAGE", name + " joined the room.");
        chatHistory.addToHistory(msg);
        broadcastMessage(msg);
    }

    /**
     * 任何原因导致WebSocket连接中断时，Spring会自动调用afterConnectionClosed方法
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        clients.remove(session.getId());
        logger.info("websocket connection closed: id = {}, close-status = {}", session.getId(), status);
    }

    private AtomicInteger guestNumber = new AtomicInteger();

    private String initGuestName() {
        return "Guest" + this.guestNumber.incrementAndGet();
    }

    /**
     * 每收到一个用户的消息后，我们就需要广播给所有用户;
     * 如果要推送给指定的几个用户，那就需要在clients中根据条件查找出某些WebSocketSession，然后发送消息。
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String s = message.getPayload();
        if (s.isEmpty()){
            return;
        }
        String name = (String) session.getAttributes().get("name");
        ChatText chat = objectMapper.readValue(s, ChatText.class);
        ChatMessage msg = new ChatMessage(name, chat.text);
        chatHistory.addToHistory(msg);
        broadcastMessage(msg);
    }

    /**
     * 广播消息
     * @param chatMessage
     * @throws IOException
     */
    public void broadcastMessage(ChatMessage chatMessage) throws IOException{
        TextMessage message = toTextMessage(Collections.singletonList(chatMessage));
        for (String id : clients.keySet()){
            WebSocketSession session = clients.get(id);
            session.sendMessage(message);
        }
    }

    private TextMessage toTextMessage(List<ChatMessage> messages) throws IOException {
        String json = objectMapper.writeValueAsString(messages);
        return new TextMessage(json);
    }

    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
    }
}
