/**
 * @Author：ouyhjian
 * @Package：com.nainuo.time
 * @Project：springboot-quartz
 * @name：tbJob
 * @Date：2024/4/6 20:17
 * @Filename：tbJob
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
import lombok.SneakyThrows;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.nainuo.VO.constant.*;

@Component
public class tbJob implements Job {

    @Autowired
    com.nainuo.mapper.orderMapper orderMapper;
    @Autowired
    com.nainuo.mapper.userSearchGoodsBeanMapper userSearchGoodsBeanMapper;
    @Autowired
    com.nainuo.mapper.syncConfigMapper syncConfigMapper;

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext context)  {
        JobDataMap jobDataMap = context.getJobDetail()
                .getJobDataMap();
        JobDetail jobDetail = context.getJobDetail();

        //  System.out.println("jobDetail中有什么: " + JSON.toJSONString(jobDetail));
        LambdaQueryWrapper<syncConfigBean> syncwrapper = new LambdaQueryWrapper();
        syncwrapper.eq(syncConfigBean::getAppType,0);
        syncConfigBean syncConfigBean = syncConfigMapper.selectOne(syncwrapper);
        int hour = syncConfigBean.getHour();
        int minute =syncConfigBean.getMinute();
        //  Map<String,Object> map = JSON.parseObject(String.valueOf(jobDataMap.get("info")),new TypeReference<Map<String,Object>>(){}.getType());
        //  System.out.println(jobDataMap);
        LambdaQueryWrapper<userSearchGoodsBean> wrapper = new LambdaQueryWrapper();



        wrapper.eq(userSearchGoodsBean::getAppType,0).
                eq(userSearchGoodsBean::getOrderBindFlag ,0);

        List<userSearchGoodsBean> userSearchGoodsBeans = userSearchGoodsBeanMapper.selectList(wrapper);

        if (userSearchGoodsBeans.size()>0) {

        for (int i = 0; i < userSearchGoodsBeans.size(); i++) {
            String res = null;
            String endTime = null;
            Date startTime = null;
            startTime = userSearchGoodsBeans.get(i).getCreatTime();
            endTime = DateTimeToStrUtil.addDateTimeToStr
                    (userSearchGoodsBeans.get(i).getCreatTime(), 0, hour, minute, 0);
           // System.out.println(endTime);
            String startTimestr =DateTimeToStrUtil.addDateTimeToStr
                    (startTime, 0, 0, 0, 0);

            res = HttpUtils.sendHttpPostRequest(tbUrl, stringUtils.toStrMapJson
                    (tbParm.replace("*", startTimestr).concat(endTime)));

            JSONObject jsonres = JSON.parseObject(res).getJSONObject("data");

            JSONArray jsonArray = jsonres.getJSONObject("results").getJSONArray("publisher_order_dto");
            int size =0;
            if (Objects.nonNull(jsonArray))
                size= jsonArray.size();
            userSearchGoodsBean usb = userSearchGoodsBeans.get(i);
            if (size > 0) {
                String goodId = usb.getGoodId();//商品id
               // BigDecimal realp = usb.getRealPrice();//商品id
                List<String> resstr = new ArrayList<>();
                for (int i1 = 0; i1 < jsonArray.size(); i1++) {
                    JSONObject rjb = jsonArray.getJSONObject(i1);
                    if(Objects.nonNull(rjb))
                    if (rjb.getString("item_id").contains(usb.getGoodId().split("-")[1])){
                        resstr.add(rjb.toJSONString());
                    }
                }

                if (resstr.size()==1){

                    orderBean orderBean = new orderBean();
                    JSONObject rj = JSONObject.parseObject(resstr.get(0));


                        //     System.out.println(uuid);
                        userSearchGoodsBean ub = userSearchGoodsBeans.get(i);
                        BigDecimal rate = BigDecimal.valueOf(ub.getUserCommissionRate() * ub.getCommissionRate());
                        orderBean.setOrderNo(rj.getString("trade_id"));
                        orderBean.setPId(rj.getString("adzone_id"));
                        orderBean.setGoodId(rj.getString("item_id"));
                        orderBean.setTitle(rj.getString("item_title"));
                        orderBean.setStartTime(startTimestr);
                        orderBean.setEndTime(endTime);
                        orderBean.setRealPrice(rj.getBigDecimal("pay_price"));

                        orderBean.setGoodPic(rj.getString("item_img"));

                        orderBean.setCreatTime(rj.getDate("tk_create_time"));
                        orderBean.setCreatTimeSign(TimestampUtils.convertToDateTimestamp(orderBean.getCreatTime()));

                        orderBean.setOrderPayTime(rj.getDate("tk_paid_time"));
                        orderBean.setOrderPayTimeSign(TimestampUtils.convertToDateTimestamp(orderBean.getOrderPayTime()));
                        orderBean.setCommissionRate(rj.getBigDecimal("total_commission_rate").divide(new BigDecimal(100)));

                        orderBean.setCommission(rj.getBigDecimal("total_commission_fee"));

                        orderBean.setOrderStatus(rj.getInteger("tk_status"));
                        String statu ="";
                        if (orderBean.getOrderStatus()==3) statu ="订单结算";
                        if (orderBean.getOrderStatus()==12) statu ="订单付款";
                        if (orderBean.getOrderStatus()==13) statu ="订单失效";
                        if (orderBean.getOrderStatus()==14) statu ="订单成功";
                        orderBean.setOrderStatusDec(statu);
                       orderBean.setGoodsSign("-");
                        orderBean.setSgId(ub.getSgId());
                        orderBean.setUserId(ub.getUId());
                        orderBean.setOpenId(ub.getOpenId());
                        orderBean.setSignOid(ub.getSignOid());
                        orderBean.setAppName("淘宝");
                        orderBean.setAppType(0);

                        orderBean.setUserCommission(rate.multiply(orderBean.getCommission()));

                        orderBean.setMyCommission(orderBean.getCommission().subtract(
                                orderBean.getUserCommission()
                        ));
                        orderBean.setUpdateTime(new Date());
                        orderBean.setUpdateTimeSign(TimestampUtils.convertToDateTimestamp(orderBean.getUpdateTime()));
                        //orderBean.setUserCommissionRate();
                       // orderBean.getComplateTime(rj.getDate(""));
                        //已付款：指订单已付款，但还未确认收货 已收货：指订单已确认收货，但商家佣金未支付 已结算：指订单已确认收货，且商家佣金已支付成功 已失效：指订单关闭/订单佣金小于0.01元，订单关闭主要有：1）买家超时未付款； 2）买家付款前，买家/卖家取消了订单；3）订单付款后发起售中退款成功；3：订单结算，12：订单付款， 13：订单失效，14：订单成功
                        usb.setOrderBindFlag("1");
                        usb.setUpdateTime(new Date());
                        usb.setUpdateTimeSign(TimestampUtils.convertToDateTimestamp(usb.getUpdateTime()));
                        orderBean.setAppId(usb.getAppId());

                        System.out.println(orderBean);
                        orderMapper.insert(orderBean);
                        userSearchGoodsBeanMapper.updateById(usb);

            }

    //        System.out.println(res);
            }
        }
        Log.info(JSON.toJSONString(jobDataMap),this.getClass().getName());

    }

    }

}
