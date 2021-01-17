package com.jacky.service;

import com.jacky.entity.MailMessage;
import com.jacky.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;

/**
 * @author jacky
 * @time 2021-01-16 11:04
 * @discription
 */

@Component
public class MailService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${smtp.from}")
    String from;

    @Autowired
    JavaMailSender mailSender;

    /**
     * 邮件内容直接发送给用户
     * @param user
     */
    public void setRegistrationMail(User user) {
        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(from);
            helper.setTo(user.getEmail());
            helper.setSubject("Welcome to Java course!");
            String html = String.format("<p>Hi, %s,</p><p>Welcome to Java course!</p><p>Sent at %s</p>",  user.getName(), LocalDateTime.now());
            helper.setText(html, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 邮件内容先发送到JMS
     * @param mm
     */
    public void sendRegistrationMail(MailMessage mm) {
        logger.info("[send mail] sending registration mail to {}...", mm.email);
        // TODO: simulate a long-time task:
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        logger.info("[send mail] registration mail was sent to {}.", mm.email);
    }

}
