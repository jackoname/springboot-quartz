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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.nainuo.VO.constant.*;

@Component
public class dyJob implements Job {
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
        syncwrapper.eq(syncConfigBean::getAppType,3);
        syncConfigBean syncConfigBean = syncConfigMapper.selectOne(syncwrapper);
        int hour = syncConfigBean.getHour();
        int minute =syncConfigBean.getMinute();
        //  Map<String,Object> map = JSON.parseObject(String.valueOf(jobDataMap.get("info")),new TypeReference<Map<String,Object>>(){}.getType());
        //  System.out.println(jobDataMap);
        LambdaQueryWrapper<userSearchGoodsBean> wrapper = new LambdaQueryWrapper();


        wrapper.eq(userSearchGoodsBean::getAppType,3).
                eq(userSearchGoodsBean::getOrderBindFlag ,0);

        List<userSearchGoodsBean> userSearchGoodsBeans = userSearchGoodsBeanMapper.selectList(wrapper);

        if(userSearchGoodsBeans.size()>0){
            for (int i = 0; i < userSearchGoodsBeans.size(); i++) {
                userSearchGoodsBean ub = userSearchGoodsBeans.get(i);
                String startTime = DateTimeToStrUtil.addDateTimeToStr(ub.getCreatTime(), 0, 0, 0, 0);
                String endTime = DateTimeToStrUtil.addDateTimeToStr(ub.getCreatTime(), 0, hour, minute, 0);
                String res = HttpUtils.sendHttpPostRequest(dyUrl, stringUtils.toStrMapJson(dyParm
                        .replace("*", startTime).concat(endTime)));
                BigDecimal rate = BigDecimal.valueOf(ub.getUserCommissionRate() * ub.getCommissionRate());
                JSONArray jbres = JSONObject.parseObject(res).getJSONObject("data").getJSONArray("list");
                int len = 0;
                if (Objects.nonNull(jbres.size()))
                    len =jbres.size();
                if (len>0){
                    for (int i1 = 0; i1 < jbres.size(); i1++) {
                        JSONObject jb  = jbres.getJSONObject(i1);
                        boolean is = (
                                ub.getGoodId().equals(jb.getString("product_id"))
                                        &&
                                        ub.getTitle().equals(jb.getString("product_name"))
                                        &&
                                        ub.getUuid().equals(jb.getString("external_info").split("_")[1])
                        );
                        if (is){

                            orderBean job = new orderBean();
                            job.setUserId(ub.getUId());
                            job.setOpenId(ub.getOpenId());
                            job.setAppId(ub.getAppId());
                            job.setAppName(ub.getAppName());
                            job.setSgId(ub.getSgId());
                            job.setStartTime(startTime);
                            job.setEndTime(endTime);
                            job.setUpdateTime(new Date());
                            job.setAppType(3);
                            job.setSignOid(ub.getSignOid());
                            job.setOrderNo(jb.getString("order_id"));
                            job.setPId("-");

                            /**
                             {
                             "ads_estimated_commission": 0.07,
                             "ads_real_commission": 0,
                             "commission_rate": 4,
                             "estimated_total_commission": 0.08,
                             "external_info": "1861241_2711559_0200",
                             "flow_point": "REFUND",
                             "item_num": 0,3675460992311099847
                             "media_type_name": "ProductDetail",
                             "order_id": "6928960059399149150",
                             "pay_goods_amount": 1.7,
                             "pay_success_time": "2024-04-18 23:23:30",
                             "product_id": "3675460992311099847",
                             "product_img": "https://p3-aio.ecombdimg.com/obj/ecom-shop-material/jliNJeUJ_m_681a8dc2f66cf684d015117798ec2d50_sx_332833_www800-800",
                             "product_name": "【0.1起/100只】100只大卷装包邮穿绳垃圾袋家用加厚加大黑色塑料袋",
                             "real_commission": 0.07,【0.1起/100只】100只大卷装包邮穿绳垃圾袋家用加厚加大黑色塑料袋
                             "refund_time": "2024-04-18 23:23:46",
                             "settle_time": "",
                             "settled_goods_amount": 0,
                             "settled_tech_service_fee": 0,
                             "shop_name": "",
                             "total_pay_amount": 2.6,
                             "update_time": "2024-04-18 23:23:53"
                             }
                             */
                            job.setGoodId(jb.getString("product_id"));
                            job.setTitle(jb.getString("product_name"));

                            job.setRealPrice(jb.getBigDecimal("total_pay_amount"));
                            job.setGoodPic(jb.getString("product_img"));
                            job.setCreatTime(jb.getDate("pay_success_time"));

                            job.setOrderPayTime(jb.getDate("pay_success_time"));

                            job.setCommissionRate(jb.getBigDecimal("commission_rate").divide(new BigDecimal(100)));

                            job.setCommission( jb.getBigDecimal("estimated_total_commission"));

                            job.setUserCommission(rate.multiply(job.getCommission()));
                            job.setMyCommission(job.getCommission().subtract(
                                    job.getUserCommission()
                            ));


                            job.setOrderStatusDec(jb.getString("flow_point"));
                            String statu =job.getOrderStatusDec();
                            int a = 0;
                            if (statu.equals("PAY_SUCC"))a=12;
                            if (statu.equals("REFUND"))a=13;
                            if (statu.equals("CONFIRM"))a=12;
                            if (statu.equals("SETTLE"))a=3;
                            job.setOrderStatus(a);

                            job.setGoodsSign("-");

                            orderMapper.insert(job);

                            ub.setOrderBindFlag("1");
                            ub.setUpdateTime(new Date());
                            ub.setUpdateTimeSign(TimestampUtils.convertToDateTimestamp(ub.getUpdateTime()));
                            userSearchGoodsBeanMapper.updateById(ub);
                            jbres.remove(jb);
                            break;
                        }
                    }
                }

            }
        }
    }


}
