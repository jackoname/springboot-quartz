/**
 * @Author：ouyhjian
 * @Package：com.nainuo.time.orderStatusSync
 * @Project：springboot-quartz
 * @name：orderStatusSync
 * @Date：2024/5/17 19:28
 * @Filename：orderStatusSync
 */
package com.nainuo.time;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nainuo.Beans.orderBean;
import com.nainuo.VO.HttpUtils;
import com.nainuo.VO.Log;
import com.nainuo.VO.stringUtils;
import com.nainuo.mapper.orderMapper;
import com.nainuo.mapper.syncConfigMapper;
import com.nainuo.mapper.userMapper;
import lombok.SneakyThrows;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.nainuo.VO.constant.tbParm;
import static com.nainuo.VO.constant.tbUrl;

@Component
public class orderStatusSyncJob implements Job {
    @Autowired
    orderMapper orderMapper;
    @Autowired
    syncConfigMapper syncConfigMapper;
    @Autowired
    userMapper userMapper;
    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        LambdaQueryWrapper<orderBean> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(orderBean::getAppType,0).and(i-> i.eq(orderBean::getOrderStatus,12).or().eq(orderBean::getOrderStatus,14));
        List<orderBean> orderList = orderMapper.selectList(wrapper);
        for (int i = 0; i < orderList.size(); i++) {
            orderBean orderBean = orderList.get(i);
            String res = HttpUtils.sendHttpPostRequest(tbUrl, stringUtils.toStrMapJson(tbParm.replace("*", orderBean.getStartTime()).concat(orderBean.getEndTime())));
            JSONArray results =  JSONObject.parseObject(res).getJSONObject("data")
                    .getJSONObject("results").getJSONArray("publisher_order_dto");
            if (Objects.nonNull(results)&&results.size()>0){
                for (int j = 0; j < results.size(); j++) {
                    JSONObject jb = results.getJSONObject(i);
                    if (orderBean.getOrderNo().equals(jb.getString("trade_parent_id"))){
                        Log.info("---------one-------------",this.getClass().getName());
                        try {
                            if (orderBean.getOrderStatus() != jb.getInteger("tk_status")) {
                                Log.info("---------two-------------",this.getClass().getName());
                                String statu = "";
                                orderBean.setOrderStatus(Integer.parseInt(jb.getString("tk_status")));
                                if (orderBean.getOrderStatus() == 3) statu = "订单结算";
                                if (orderBean.getOrderStatus() == 13) statu = "订单失效";
                                if (orderBean.getOrderStatus() == 14) statu = "确认收货";
                                orderBean.setOrderStatusDec(statu);
                                orderBean.setUpdateTime(jb.getDate("modified_time"));
                                if (orderBean.getOrderStatus() == 3)
                                    orderBean.setCreatTime(jb.getDate("tk_earning_time"));
                                orderMapper.updateById(orderBean);
                                Log.info(orderBean.toString(),this.getClass().getName());
                                break;
                            }
                        }catch (Exception e){}
                    }
                }
            }
            Log.info("---------three-------------",this.getClass().getName());
        }
    }



}
