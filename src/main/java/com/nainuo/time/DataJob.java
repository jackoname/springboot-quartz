package com.nainuo.time;

import com.nainuo.mapper.QuartzMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @Date：2024/4/6
 * @Author：ouyhjian
 * @return：
 */
@Component
public class DataJob implements Job {


    private static final Log logger = LogFactory.getLog(InvokeJob.class);

    @Autowired
    private QuartzMapper quartzMapper;
    @Override
    public void execute(JobExecutionContext context)  {

    }
}
