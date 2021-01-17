package com.jacky.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacky.entity.MailMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * @author jacky
 * @time 2021-01-17 21:36
 * @discription 消息服务
 *
 *          JMS的消息类型支持以下几种：
 *          1)TextMessage：文本消息；
 *          2)BytesMessage：二进制消息；
 *          3)MapMessage：包含多个Key-Value对的消息；
 *          4)ObjectMessage：直接序列化Java对象的消息；
 *          5)StreamMessage：一个包含基本类型序列的消息。
 *
 *          注意：Artemis消息服务器默认配置下会自动创建Queue，因此不必手动创建一个名为jms/queue/mail的Queue，
 *          但不是所有的消息服务器都会自动创建Queue，生产环境的消息服务器通常会关闭自动创建功能，需要手动创建Queue。
 *
 *          再注意到MailMessage是我们自己定义的一个JavaBean，真正的JMS消息是创建的TextMessage，它的内容是JSON。
 */
@Component
public class MessagingService {

    /**
     * ObjectMapper provides functionality for reading and writing JSON, either to and from basic POJOs (Plain Old Java Objects),
     * or to and from a general-purpose JSON Tree Model (JsonNode), as well as related functionality for performing conversions.
     */
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JmsTemplate jmsTemplate;

    /**
     * 通过JmsTemplate创建一个TextMessage并发送到名称为jms/queue/mail的Queue。
     * @param msg
     * @throws Exception
     */
    public void sendMailMessage(MailMessage msg) throws Exception {
        String text = objectMapper.writeValueAsString(msg);
        jmsTemplate.send("jms/queue/mail", new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(text);
            }
        });
    }
}
