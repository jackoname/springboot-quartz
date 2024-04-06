package com.nainuo.config;


import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SchedulerConfig {

    @Autowired
    private Scheduler scheduler;
//  创建定时器
    public void add(String jobName, Class<? extends Job> jobClass, Map<String, Object> jobDataMap, String cronExpression) {
        try {
            if (scheduler.isStarted()) {
                System.out.println("Scheduler 已经启动");
            } else {
                System.out.println("Scheduler 还未启动");
            }
            log.info("调用");
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobName)
                    .usingJobData(new JobDataMap(jobDataMap))
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobDetail)
                    .withIdentity(jobName + "Trigger")
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

//    停止定时任务
    public void stop(String jobName) {
        try {
            scheduler.unscheduleJob(new TriggerKey(jobName + "Trigger"));
            scheduler.deleteJob(new JobKey(jobName));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void rescheduleJob(String jobName, String newCronExpression) {
        try {
            TriggerKey triggerKey = new TriggerKey(jobName + "Trigger");

            // 停止当前触发器
            scheduler.unscheduleJob(triggerKey);

            // 删除当前作业
            scheduler.deleteJob(new JobKey(jobName));

            // 重新创建定时任务
            add(jobName, (Class<? extends Job>) Class.forName("InvokeJob"), new HashMap<>(), newCronExpression);
        } catch (SchedulerException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
