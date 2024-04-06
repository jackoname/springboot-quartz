package com.nainuo.controller;

import com.nainuo.config.SchedulerConfig;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author whf
 * @date 2024年03月15日 15:13
 */
@RequestMapping("quartz")
@RestController
public class QuartzController {

    @Autowired
    private SchedulerConfig dynamicSchedulerConfig;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private Scheduler scheduler;

    @PostMapping("stop/{jobName}")
    public String stop(@PathVariable String jobName){
        dynamicSchedulerConfig.stop(jobName);
        return "成功";
    }


    @PostMapping("add")
    public String add(@RequestBody Map<String,Object> parms){
      //  HashMap<String,Object> map = new HashMap<>();
      //  parms.put("key","value");
      //  System.out.println(parms.toString());
        try {
            Class<? extends Job> Job = (Class<? extends Job>)Class.forName("com.nainuo.time."+parms.get("clan"));
            dynamicSchedulerConfig.add(String.valueOf(parms.get("job_name")),
                    Job,
                    parms
                    ,parms.get("time").toString());
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return "成功";
    }

    @PostMapping("index")
    public String index(){

        return "成功";
    }

}
