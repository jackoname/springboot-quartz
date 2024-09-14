/**
 * @Author：ouyhjian
 * @Package：com.nainuo.time
 * @Project：springboot-quartz
 * @name：jdJob
 * @Date：2024/4/18 0:50
 * @Filename：jdJob
 */
package com.nainuo.time;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nainuo.Beans.orderBean;
import com.nainuo.Beans.syncConfigBean;
import com.nainuo.Beans.userSearchGoodsBean;
import com.nainuo.VO.DateTimeToStrUtil;
import com.nainuo.VO.HttpUtils;
import com.nainuo.VO.TimestampUtils;
import com.nainuo.VO.stringUtils;
import com.nainuo.mapper.orderMapper;
import com.nainuo.mapper.syncConfigMapper;
import com.nainuo.mapper.userMapper;
import com.nainuo.mapper.userSearchGoodsBeanMapper;
import lombok.SneakyThrows;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.nainuo.VO.constant.*;

@Component
public class jdJob implements Job {
    @Autowired
    orderMapper orderMapper;
    @Autowired
    userSearchGoodsBeanMapper userSearchGoodsBeanMapper;
    @Autowired
    syncConfigMapper syncConfigMapper;
    @Autowired
    userMapper userMapper;
    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        LambdaQueryWrapper<syncConfigBean> syncwrapper = new LambdaQueryWrapper();
        syncwrapper.eq(syncConfigBean::getAppType,1);
        syncConfigBean syncConfigBean = syncConfigMapper.selectOne(syncwrapper);
        int hour = syncConfigBean.getHour();
        int minute =syncConfigBean.getMinute();
        //  Map<String,Object> map = JSON.parseObject(String.valueOf(jobDataMap.get("info")),new TypeReference<Map<String,Object>>(){}.getType());
        //  System.out.println(jobDataMap);
        LambdaQueryWrapper<userSearchGoodsBean> wrapper = new LambdaQueryWrapper();


        wrapper.eq(userSearchGoodsBean::getAppType,1).
                eq(userSearchGoodsBean::getOrderBindFlag ,0);

        List<userSearchGoodsBean> userSearchGoodsBeans = userSearchGoodsBeanMapper.selectList(wrapper);

        if(userSearchGoodsBeans.size()>0){
            for (int i = 0; i < userSearchGoodsBeans.size(); i++) {
                userSearchGoodsBean ub = userSearchGoodsBeans.get(i);
                String startTime = DateTimeToStrUtil.addDateTimeToStr(ub.getCreatTime(), 0, 0, 0, 0);
                String endTime = DateTimeToStrUtil.addDateTimeToStr(ub.getCreatTime(), 0, hour, minute, 0);
                String res = HttpUtils.sendHttpPostRequest(jdUrl, stringUtils.toStrMapJson(jdParm
                        .replace("*", startTime).concat(endTime)));
                BigDecimal rate = BigDecimal.valueOf(ub.getUserCommissionRate() * ub.getCommissionRate());
                JSONArray jbres = JSONObject.parseObject(res).getJSONArray("data");
                int len = 0;
                if (Objects.nonNull(jbres.size()))
                    len =jbres.size();
                if (len>0){
                    for (int i1 = 0; i1 < jbres.size(); i1++) {
                        JSONObject jb  = jbres.getJSONObject(i);
                        if (
                                ub.getGoodsSign().equals(jb.getJSONObject("shopId").getString("shopId"))
                        &&
                                ub.getTitle().equals(jb.getString("skuName"))
                                &&
                                        ub.getUuid().equals(jb.getString("positionId"))
                        ){

                            orderBean job = new orderBean();
                            job.setUserId(ub.getUId());
                            job.setOpenId(ub.getOpenId());
                            job.setAppId(ub.getAppId());
                            job.setAppName(ub.getAppName());
                            job.setSgId(ub.getSgId());
                            job.setStartTime(startTime);
                            job.setEndTime(endTime);
                            job.setUpdateTime(new Date());
                            job.setAppType(1);
                            job.setSignOid(ub.getSignOid());
                            job.setOrderNo(jb.getString("orderId"));
                            job.setPId(jb.getString("pid"));
                            job.setGoodId(jb.getString("itemId"));
                            job.setTitle(jb.getString("skuName"));

                            job.setRealPrice(jb.getBigDecimal("estimateCosPrice"));
                            job.setGoodPic(jb.getJSONObject("goodsInfo").getString("imageUrl"));
                            job.setCreatTime(jb.getDate("orderTime"));

                            job.setOrderPayTime(jb.getDate("modifyTime"));

                            job.setCommissionRate(jb.getBigDecimal("commissionRate").divide(new BigDecimal(100)));

                            job.setCommission( job.getRealPrice().multiply(job.getCommissionRate()));
                            job.setOrderStatus(jb.getInteger("validCode"));
                            job.setUserCommission(rate.multiply(job.getCommission()));
                            job.setMyCommission(job.getCommission().subtract(
                                    job.getUserCommission()
                            ));

                            String statu ="";
                            int a =job.getOrderStatus();

                            if (a==15) statu ="待付款";
                            if (a==16) statu ="订单付款";
                            if (a==17) statu ="订单成功";
                            if(!(a==16||a==17||a==15)) statu ="订单失效";

                            job.setOrderStatusDec(statu);


                            job.setGoodsSign("-");

                            orderMapper.insert(job);

                            ub.setOrderBindFlag("1");
                            ub.setUpdateTime(new Date());
                            ub.setUpdateTimeSign(TimestampUtils.convertToDateTimestamp(ub.getUpdateTime()));
                            userSearchGoodsBeanMapper.updateById(ub);
                            jbres.remove(jb);
                        }
                    }
                }

            }
        }
    }


}
