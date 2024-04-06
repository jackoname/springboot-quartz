package com.nainuo.service.impl;

import com.nainuo.config.SchedulerConfig;
import com.nainuo.dto.QuartzAddDto;
import com.nainuo.service.QuartzService;
import org.quartz.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author whf
 * @date 2024年03月18日 8:09
 */
@Service
public class QuartzServiceImpl implements QuartzService {

    @Autowired
    private SchedulerConfig schedulerConfig;

    @Autowired
    private StringRedisTemplate redisTemplate;


    /**
     * 在Quartz中添加定时任务
     *
     * @param quartzAddDto
     */
    @Override
    public boolean add(QuartzAddDto quartzAddDto) {
//       获取Redis中的String属性
        ValueOperations<String,String> operations = redisTemplate.opsForValue();
//        根据key查询Redis中是否包含 Quartz Job的名字
        if(StringUtils.hasText(operations.get(quartzAddDto.getQuartzName()))) {
            return false;
        }
//        如果没有 就在Redis中添加  Qrautz Job的名字 后续就可以用作查询
        operations.set(quartzAddDto.getQuartzName(), quartzAddDto.getCorn());
        Class<? extends Job> Job = null;
        try {
            Job = (Class<? extends Job>) Class.forName("com.nainuo.time.InvokeJob");
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
//        添加到定时任务
        schedulerConfig.add(quartzAddDto.getQuartzName(),
                Job,
                quartzAddDto.getArgument(),
                quartzAddDto.getCorn());
        return true;
    }

    /**
     * 停止Quartz中的定时任务
     *
     * @param name
     * @return
     */
    @Override
    public boolean stop(String name) {
        ValueOperations<String,String> operations = redisTemplate.opsForValue();
        if(StringUtils.hasText(operations.get(name))){
            redisTemplate.delete(name);
            schedulerConfig.stop(name);
            return true;
        }
        return false;
    }


}
