package com.jacky.webSocket;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author jacky
 * @time 2021-01-15 16:41
 * @discription 聊天历史
 */
@Component
public class ChatHistory {

    final int maxMessages = 100;
    final List<ChatMessage> chatHistory = new ArrayList<>(100);
    final Lock readLock;
    final Lock writeLock;

    /**
     * Creates a new ReentrantReadWriteLock with default (nonfair) ordering properties.
     */
    public ChatHistory() {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }

    /**
     * 获取历史消息的一份拷贝
     * @return 历史消息的拷贝
     */
    public List<ChatMessage> getHistory() {
        this.readLock.lock();
        try {
            //应该返回不可更改的chatHistory，但由于JDK8无List.copyOf()方法，因此此处返回可修改的List。
            return chatHistory;
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * 添加一条消息到历史消息中
     * @param message 需要添加的消息
     */
    public void addToHistory(ChatMessage message){
        this.writeLock.lock();
        try {
            this.chatHistory.add(message);
            if (this.chatHistory.size() > maxMessages){
                this.chatHistory.remove(0);
            }
        } finally {
            this.writeLock.unlock();
        }
    }

}
