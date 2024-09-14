package com.nainuo.time;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nainuo.Beans.commConfigBean;
import com.nainuo.Beans.orderBean;
import com.nainuo.Beans.userSearchGoodsBean;
import com.nainuo.VO.DateTimeToStrUtil;
import com.nainuo.mapper.commConfigMapper;
import com.nainuo.mapper.orderMapper;
import com.nainuo.mapper.userSearchGoodsBeanMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Watchable;
import java.util.List;
import java.util.Map;

/**
 * 定义任务
 */
@Component
public class chatDelLogJob implements Job {
    private static final Log logger = LogFactory.getLog(chatDelLogJob.class);
    @Autowired
    userSearchGoodsBeanMapper userSearchGoodsBeanMapper;
    @Autowired
    commConfigMapper commConfigMapper;
    @Override
    public void execute(JobExecutionContext context)  {
        LambdaQueryWrapper<commConfigBean>wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(commConfigBean::getFlag,1);
        commConfigBean commConfigBean = commConfigMapper.selectOne(wrapper1);
        LambdaQueryWrapper<userSearchGoodsBean>wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userSearchGoodsBean::getIsLogicDel,0);
        List<userSearchGoodsBean> list = userSearchGoodsBeanMapper.selectList(wrapper);
        for (userSearchGoodsBean bean : list) {
            logDelOrder(bean,commConfigBean.getConfigNum());
        }
    }
    private void logDelOrder(userSearchGoodsBean bean,int day) {

        if (DateTimeToStrUtil.getTimeDifference(bean.getCreatTime())>=day){//配置
            bean.setIsLogicDel(1);
            userSearchGoodsBeanMapper.updateById(bean);
            logger.info(bean.toString());
        }
    }
}
