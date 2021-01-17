package com.jacky.entity;

import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * @author jacky
 * @time 2021-01-17 21:37
 * @discription 邮件消息实体
 */
public class MailMessage {

    public static enum Type {
        REGISTRATION, SIGNIN;
    }

    public Type type;

    public String email;

    public String name;

    public long timestamp;

    public static MailMessage registration(String email, String name) {
        MailMessage msg = new MailMessage();
        msg.email = email;
        msg.name = name;
        msg.type = Type.REGISTRATION;
        msg.timestamp = System.currentTimeMillis();
        return msg;
    }
}
