/**
 * @Author：ouyhjian
 * @Package：com.nainuo.time
 * @Project：springboot-quartz
 * @name：pddJob
 * @Date：2024/4/6 11:27
 * @Filename：pddJob
 */
package com.nainuo.time;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nainuo.Beans.orderBean;
import com.nainuo.Beans.syncConfigBean;
import com.nainuo.Beans.userSearchGoodsBean;
import com.nainuo.VO.*;
import com.nainuo.mapper.orderMapper;
import com.nainuo.mapper.syncConfigMapper;
import com.nainuo.mapper.userSearchGoodsBeanMapper;
import lombok.SneakyThrows;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.nainuo.VO.constant.pddParm;
import static com.nainuo.VO.constant.pddUrl;

@Component
public class pddJob implements Job {

    @Autowired
    orderMapper orderMapper;
    @Autowired
    userSearchGoodsBeanMapper userSearchGoodsBeanMapper;
    @Autowired
    syncConfigMapper syncConfigMapper;

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext context)  {
        JobDataMap jobDataMap = context.getJobDetail()
                .getJobDataMap();
        JobDetail jobDetail = context.getJobDetail();

      //  System.out.println("jobDetail中有什么: " + JSON.toJSONString(jobDetail));
        LambdaQueryWrapper<syncConfigBean> syncwrapper = new LambdaQueryWrapper();
        syncwrapper.eq(syncConfigBean::getAppType,2);
        syncConfigBean syncConfigBean = syncConfigMapper.selectOne(syncwrapper);
        int hour = syncConfigBean.getHour();
        int minute =syncConfigBean.getMinute();
      //  Map<String,Object> map = JSON.parseObject(String.valueOf(jobDataMap.get("info")),new TypeReference<Map<String,Object>>(){}.getType());
       //  System.out.println(jobDataMap);
        LambdaQueryWrapper<userSearchGoodsBean> wrapper = new LambdaQueryWrapper();



        wrapper.eq(userSearchGoodsBean::getAppType,2).
                eq(userSearchGoodsBean::getOrderBindFlag ,0);

       List<userSearchGoodsBean> userSearchGoodsBeans = userSearchGoodsBeanMapper.selectList(wrapper);


        for (int i = 0; i < userSearchGoodsBeans.size(); i++) {
            String res =  null;
            String endTime = null;
            String startTime = null;
            startTime = userSearchGoodsBeans.get(i).getCreatTimeSign().toString();
            endTime = DateTimeToStrUtil.addDateTimeToStr
                    (userSearchGoodsBeans.get(i).getCreatTime(),0,hour,minute,0);
            System.out.println(endTime);
            endTime = String.valueOf(TimestampUtils.convertToDateTimestamp(new DateTime(endTime)));
            res = HttpUtils.sendHttpPostRequest(pddUrl, stringUtils.toStrMapJson
                    (pddParm.replace("*",startTime).concat(endTime)));

            JSONObject jsonres = JSON.parseObject(res).getJSONObject("order_list_get_response");
            int size = jsonres.getInteger("total_count");

            if (size>0){
                String uuid =  userSearchGoodsBeans.get(i).getUuid();

                JSONArray jsonArray = jsonres.getJSONArray("order_list");
                for (int i1 = 0; i1 < jsonArray.size(); i1++) {
                    orderBean orderBean = new orderBean();
                    JSONObject rj = jsonArray.getJSONObject(i1);
                    String uidsid = rj.getString("custom_parameters");
                    String sid = JSON.parseObject(uidsid).getString("sid");
                    int st = rj.getInteger("order_status");
                   // System.out.println(sid);
                    if (uuid.equals(sid)) {
                   //     System.out.println(uuid);
                        userSearchGoodsBean ub = userSearchGoodsBeans.get(i);
                        BigDecimal rate = BigDecimal.valueOf(ub.getUserCommissionRate()*ub.getCommissionRate());
                        orderBean.setOrderNo(rj.getString("order_sn"));
                        orderBean.setPId(rj.getString("pid"));
                        orderBean.setGoodId(rj.getString("goods_id"));
                        orderBean.setTitle(rj.getString("goods_name"));
                        orderBean.setStartTime(startTime);
                        orderBean.setEndTime(endTime);
                        orderBean.setRealPrice(rj.getBigDecimal("goods_price").divide(new BigDecimal(100)));

                        orderBean.setGoodPic(rj.getString("goods_thumbnail_url"));
                        orderBean.setCreatTimeSign(Long.valueOf(rj.getString("order_create_time")));
                        orderBean.setCreatTime(TimestampUtils.convertToUtilDate(orderBean.getCreatTimeSign()));

                        orderBean.setOrderPayTimeSign(Long.valueOf(rj.getString("order_pay_time")));
                        orderBean.setOrderPayTime(TimestampUtils.convertToUtilDate(orderBean.getOrderPayTimeSign()));

                        orderBean.setCommissionRate(rj.getBigDecimal("promotion_rate").divide(new BigDecimal(100)));
                        orderBean.setCommission(rj.getBigDecimal("promotion_amount").divide(new BigDecimal(100)));
                        orderBean.setOrderStatus(rj.getInteger("order_status"));
                        orderBean.setOrderStatusDec(rj.getString("order_status_desc"));
                        orderBean.setGoodsSign(rj.getString("goods_sign"));
                        orderBean.setSgId(ub.getSgId());
                        orderBean.setUserId(ub.getUId());
                        orderBean.setOpenId(ub.getOpenId());
                        orderBean.setSignOid(ub.getSignOid());
                        orderBean.setAppName("拼多多");
                        orderBean.setAppType(2);

                        orderBean.setUserCommission(rate.multiply(orderBean.getCommission()));

                        orderBean.setMyCommission(orderBean.getCommission().subtract(
                                orderBean.getUserCommission()
                        ));
                        orderBean.setUpdateTime(new Date());
                        orderBean.setUpdateTimeSign(TimestampUtils.convertToDateTimestamp(orderBean.getUpdateTime()));
                        //orderBean.setUserCommissionRate();
                        userSearchGoodsBean usb = userSearchGoodsBeans.get(i);
                        //订单状态：0-已支付；1-已成团；2-确认收货；3- ；4-审核失败（不可提现）；5-已经结算 ;10-已处罚
                        usb.setOrderBindFlag("1");
                        usb.setUpdateTime(new Date());
                        usb.setUpdateTimeSign(TimestampUtils.convertToDateTimestamp(usb.getUpdateTime()));
                        orderBean.setAppId(usb.getAppId());

                        System.out.println(orderBean);
                        orderMapper.insert(orderBean);
                        userSearchGoodsBeanMapper.updateById(usb);
                        break;
                    }
                }
            }

            System.out.println(res);
        }
        Log.info(JSON.toJSONString(jobDataMap),this.getClass().getName());

    }
}
