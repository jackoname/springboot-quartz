package com.nainuo.service;

import com.nainuo.dto.QuartzAddDto;

public interface QuartzService {

    /**
     * 在Quartz中添加定时任务
     */
    public boolean add(QuartzAddDto quartzAddDto);

    /**
     * 停止Quartz中的定时任务
     * @param name
     * @return
     */
    public boolean stop(String name);

}
