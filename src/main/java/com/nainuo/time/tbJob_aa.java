/**
 * @Author：ouyhjian
 * @Package：com.nainuo.time
 * @Project：springboot-quartz
 * @name：tbJob
 * @Date：2024/4/6 20:17
 * @Filename：tbJob
 */
package com.nainuo.time;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nainuo.Beans.orderBean;
import com.nainuo.Beans.syncConfigBean;
import com.nainuo.Beans.userBean;
import com.nainuo.Beans.userSearchGoodsBean;
import com.nainuo.VO.*;
import com.nainuo.mapper.orderMapper;
import com.nainuo.mapper.syncConfigMapper;
import com.nainuo.mapper.userMapper;
import com.nainuo.mapper.userSearchGoodsBeanMapper;
import lombok.SneakyThrows;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.nainuo.VO.constant.tbParm;
import static com.nainuo.VO.constant.tbUrl;

@Component
public class tbJob_aa implements Job {

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

    public void execute(JobExecutionContext context)  {

        JobDataMap jobDataMap = context.getJobDetail()
                .getJobDataMap();

        LambdaQueryWrapper<syncConfigBean> syncwrapper = new LambdaQueryWrapper();
        syncwrapper.eq(syncConfigBean::getAppType,0);
        syncConfigBean syncConfigBean = syncConfigMapper.selectOne(syncwrapper);
        int hour = syncConfigBean.getHour();
        int minute =syncConfigBean.getMinute();

        LambdaQueryWrapper<userSearchGoodsBean> wrapper = new LambdaQueryWrapper();

        wrapper.eq(userSearchGoodsBean::getAppType,0).
                eq(userSearchGoodsBean::getOrderBindFlag ,0);
        List<userSearchGoodsBean> userSearchGoodsBeans = userSearchGoodsBeanMapper.selectList(wrapper);

        for (int i = 0; i < userSearchGoodsBeans.size(); i++) {
            userSearchGoodsBean ub = userSearchGoodsBeans.get(i);
            String startTime = DateTimeToStrUtil.addDateTimeToStr(ub.getCreatTime(), 0, 0, 0, 0);
            String endTime = DateTimeToStrUtil.addDateTimeToStr(ub.getCreatTime(), 0, hour, minute, 0);
            String res = HttpUtils.sendHttpPostRequest(tbUrl, stringUtils.toStrMapJson(tbParm
                    .replace("*", startTime).concat(endTime)));
            BigDecimal rate = BigDecimal.valueOf(ub.getUserCommissionRate() * ub.getCommissionRate());

            JSONArray jsonArray = JSONObject.parseObject(res).getJSONObject("data")
                    .getJSONObject("results").getJSONArray("publisher_order_dto");

            int len = 0;
            if (Objects.nonNull(jsonArray))
                len = jsonArray.size();

            if (len > 0) {
               // System.out.println(len+"-------------*-*-*-*-*-");
                if (Objects.nonNull(ub.getTbId())) {
                    for (int j = 0; j < jsonArray.size(); j++) {
                        JSONObject jb = jsonArray.getJSONObject(j);

                        String tradeParentId = jb.getString("trade_parent_id");
                        String  trasix = tradeParentId.substring(tradeParentId.length() - 6);
                        if (ub.getTbpId().equals(jb.getString("adzone_id"))
                                &&
                                ub.getTbId().equals(trasix)
                                &&
                                ub.getGoodId().split("-")[1].equals(
                                        jb.getString("item_id").split("-")[1]
                                )
//                                &&
//                        ub.getTitle().equals(jb.getString("item_title"))
                        )
                        {
                            orderBean tob = new orderBean();
                            tob.setUserId(ub.getUId());
                            tob.setOpenId(ub.getOpenId());
                            tob.setAppId(ub.getAppId());
                            tob.setAppName(ub.getAppName());
                            tob.setSgId(ub.getSgId());
                            tob.setStartTime(startTime);
                            tob.setEndTime(endTime);
                            tob.setUpdateTime(new Date());
                            tob.setAppType(0);
                            tob.setSignOid(ub.getSignOid());
                            tob.setOrderNo(jb.getString("trade_id"));
                            tob.setPId(jb.getString("adzone_id"));
                            tob.setGoodId(jb.getString("item_id"));
                            tob.setTitle(jb.getString("item_title"));

                            tob.setRealPrice(jb.getBigDecimal("alipay_total_price"));
                            tob.setGoodPic(jb.getString("item_img"));
                            tob.setCreatTime(jb.getDate("tk_create_time"));

                            tob.setOrderPayTime(jb.getDate("tk_paid_time"));

                            tob.setCommissionRate(jb.getBigDecimal("total_commission_rate").divide(new BigDecimal(100)));

                            tob.setCommission(jb.getBigDecimal("pub_share_pre_fee_for_commission"));
                            tob.setOrderStatus(jb.getInteger("tk_status"));
                            tob.setUserCommission(rate.multiply(tob.getCommission()));
                            tob.setMyCommission(tob.getCommission().subtract(
                                    tob.getUserCommission()
                            ));

                            String statu ="";
                            if (tob.getOrderStatus()==3) statu ="订单结算";
                            if (tob.getOrderStatus()==12) statu ="订单付款";
                            if (tob.getOrderStatus()==13) statu ="订单失效";
                            if (tob.getOrderStatus()==14) statu ="订单成功";
                            tob.setOrderStatusDec(statu);
                            tob.setGoodsSign("-");

                            orderMapper.insert(tob);

                            ub.setOrderBindFlag("1");
                            ub.setUpdateTime(new Date());
                            ub.setUpdateTimeSign(TimestampUtils.convertToDateTimestamp(ub.getUpdateTime()));
                            userSearchGoodsBeanMapper.updateById(ub);

                            jsonArray.remove(jb);
                            break;
                        }
                    }

                } else {
                    for (int j = 0; j < jsonArray.size(); j++) {
                        JSONObject jb = jsonArray.getJSONObject(j);

                        String tradeParentId = jb.getString("trade_parent_id");
                        String  trasix = tradeParentId.substring(tradeParentId.length() - 6);
                     //   System.out.println(jb.toJSONString());
                     boolean pd =   (ub.getTbpId().equals(jb.getString("adzone_id"))
//                                &&
//                                ub.getTitle().equals(jb.getString("item_title"))
                                &&
                                ub.getGoodId().split("-")[1].equals(jb.getString("item_id").split("-")[1]
                                )
                        );

//                        System.out.println((ub.getTbpId().equals(jb.getString("adzone_id"))+"  "+ub.getTbpId()+" : "+jb.getString("adzone_id")));
//                        System.out.println((ub.getGoodId().split("-")[1].equals(jb.getString("item_title"))+"  "+ub.getTitle()+" : "+jb.getString("item_title")));
//                        System.out.println((ub.getGoodId().split("-")[1].equals(jb.getString("item_id").split("-")[1])+"  "+ub.getGoodId().split("-")[1]+" : "+jb.getString("item_id").split("-")[1]));
                        if (pd)

                        {
                            LambdaQueryWrapper<userBean> wrap = new LambdaQueryWrapper<>();
                            wrap.eq(userBean::getUserId,ub.getUId());
                            userBean tub = userMapper.selectOne(wrap);
                            tub.setTbId(trasix);
                            userMapper.updateById(tub);
                            orderBean tob = new orderBean();
                            tob.setUserId(ub.getUId());
                            tob.setOpenId(ub.getOpenId());
                            tob.setAppId(ub.getAppId());
                            tob.setAppName(ub.getAppName());
                            tob.setSgId(ub.getSgId());
                            tob.setStartTime(startTime);
                            tob.setEndTime(endTime);
                            tob.setUpdateTime(new Date());
                            tob.setAppType(0);
                            tob.setSignOid(ub.getSignOid());
                            tob.setOrderNo(jb.getString("trade_id"));
                            tob.setPId(jb.getString("adzone_id"));
                            tob.setGoodId(jb.getString("item_id"));
                            tob.setTitle(jb.getString("item_title"));
                            //System.out.println(jb.getBigDecimal("pay_price")+"][]");
                            tob.setRealPrice(jb.getBigDecimal("alipay_total_price"));
                            tob.setGoodPic(jb.getString("item_img"));
                            tob.setCreatTime(jb.getDate("tk_create_time"));

                            tob.setOrderPayTime(jb.getDate("tk_paid_time"));

                            tob.setCommissionRate(jb.getBigDecimal("total_commission_rate").divide(new BigDecimal(100)));

                            tob.setCommission(jb.getBigDecimal("pub_share_pre_fee_for_commission"));
                            tob.setOrderStatus(jb.getInteger("tk_status"));
                            tob.setUserCommission(rate.multiply(tob.getCommission()));
                            tob.setMyCommission(tob.getCommission().subtract(
                                    tob.getUserCommission()
                            ));

                            String statu ="";
                            if (tob.getOrderStatus()==3) statu ="订单结算";
                            if (tob.getOrderStatus()==12) statu ="订单付款";
                            if (tob.getOrderStatus()==13) statu ="订单失效";
                            if (tob.getOrderStatus()==14) statu ="订单成功";
                            tob.setOrderStatusDec(statu);
                            tob.setGoodsSign("-");
                            orderMapper.insert(tob);
                            ub.setOrderBindFlag("1");
                            ub.setUpdateTime(new Date());
                            ub.setUpdateTimeSign(TimestampUtils.convertToDateTimestamp(ub.getUpdateTime()));
                            userSearchGoodsBeanMapper.updateById(ub);
                            jsonArray.remove(jb);
                            break;
                        }
                    }
                }

            }
        }
        Log.info(JSON.toJSONString(jobDataMap),this.getClass().getName());
    }

}
