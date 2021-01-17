package com.jacky.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacky.entity.MailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * @author jacky
 * @time 2021-01-17 21:37
 * @discription 邮件消息监听器
 */

@Component
public class MailMessageListener {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MailService mailService;

    /**
     * @JmsListener指定了Queue的名称，因此，凡是发到此Queue的消息都会被这个onMailMessageReceived()方法处理，
     * 方法参数是JMS的Message接口，
     * 我们通过强制转型为TextMessage并提取JSON，反序列化后获得自定义的JavaBean，也就获得了发送邮件所需的所有信息。
     * @param message
     * @throws Exception
     */
    @JmsListener(destination = "jms/queue/mail", concurrency = "10")
    public void onMailMessageReceived(Message message) throws Exception {
        logger.info("received message: " + message);
        if (message instanceof TextMessage) {
            String text = ((TextMessage) message).getText();
            MailMessage mm = objectMapper.readValue(text, MailMessage.class);
            mailService.sendRegistrationMail(mm);
        } else {
            logger.error("unable to process non-text message!");
        }
    }
}
