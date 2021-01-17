package com.jacky.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author jacky
 * @time 2021-01-17 21:10
 * @discription 定时任务
 */
@Component
public class TaskService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 执行重复任务
     * initialDelay：定时任务启动延时60秒
     * fixedRate：以60秒的间隔执行任务
     * fixedDelay：
     */
    @Scheduled(initialDelay = 60_000, fixedRate = 60_000)
    public void checkSystemStatusEveryMinute(){
        logger.info("Start check system status...");
    }

    /**
     * cron表达式执行定时任务
     * 每10秒执行一次
     * 实际上它可以取代fixedRate类型的定时任务。
     */
    @Scheduled(cron = "${task.report:*/10 * * * * *")
    public void cronDailyReport() {
        logger.info("Start daily report task...");
    }
}
