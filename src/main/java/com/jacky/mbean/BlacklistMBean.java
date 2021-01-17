package com.jacky.mbean;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author jacky
 * @time 2021-01-17 23:15
 * @discription BlacklistMBean首先是一个标准的Spring管理的Bean，
 *              其次，添加了@ManagedResource表示这是一个MBean，将要被注册到JMX。
 *
 *              在Spring中使用JMX需要：
 *                  通过@EnableMBeanExport启用自动注册MBean；
 *                  编写MBean并实现管理属性和管理操作。
 */

@Component
//objectName指定了这个MBean的名字，通常以company:name=Xxx来分类MBean。
@ManagedResource(objectName = "sample:name=blacklist", description = "Blacklist of IP addresses")
public class BlacklistMBean {

    private Set<String> ips = new HashSet<>();

    /**
     * 对于属性，使用@ManagedAttribute注解标注。上述MBean只有get属性，没有set属性，说明这是一个只读属性。
     * @return
     */
    @ManagedAttribute(description = "Get IP addresses in blacklist")
    public String[] getBlacklist() {
        return ips.toArray(new String[ips.size()]);
    }

    /**
     * 对于操作，使用@ManagedOperation注解标准。下述MBean定义了两个操作：addBlacklist()和removeBlacklist()，
     * 其他方法如shouldBlock()不会被暴露给JMX。
     * @param ip
     */
    @ManagedOperation
    @ManagedOperationParameter(name = "ip", description = "Target IP address that will be added to blacklist")
    public void addBlacklist(String ip) {
        ips.add(ip);
    }

    @ManagedOperation
    @ManagedOperationParameter(name = "ip", description = "Target IP address that will be removed from blacklist")
    public void removeBlacklist(String ip) {
        ips.remove(ip);
    }

    /**
     * 不会被暴露给JMX。
     * @param ip
     * @return
     */
    public boolean shouldBlock(String ip) {
        return ips.contains(ip);
    }
}
