package com.jacky.webSocket;

/**
 * @author jacky
 * @time 2021-01-15 16:36
 * @discription 发送的消息是序列化后的JSON，可以用ChatMessage表示
 */
public class ChatMessage extends ChatText{
    public long timestamp;
    public String name;

    public ChatMessage() {

    }

    public ChatMessage(String name, String text) {
        this.timestamp = System.currentTimeMillis();
        this.name = name;
        this.text = text;
    }
}

class ChatText{
    public String text;
}
