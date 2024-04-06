package com.nainuo.time;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.nainuo.mapper.orderMapper;
import com.nainuo.mapper.userSearchGoodsBeanMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 定义任务
 */
@Component
public class InvokeJob implements Job {

    private static final Log logger = LogFactory.getLog(InvokeJob.class);

    @Autowired
    orderMapper orderMapper;
    @Autowired
    userSearchGoodsBeanMapper userSearchGoodsBeanMapper;

    @Override
    public void execute(JobExecutionContext context)  {
        JobDataMap jobDataMap = context.getJobDetail()
                .getJobDataMap();
        JobDetail jobDetail = context.getJobDetail();
        System.out.println("jobDetail中有什么: " + JSON.toJSONString(jobDetail));

        Map<String,Object> map = JSON.parseObject(String.valueOf(jobDataMap.get("info")),new TypeReference<Map<String,Object>>(){}.getType());
        logger.info(JSON.toJSONString(jobDataMap));


    }
}