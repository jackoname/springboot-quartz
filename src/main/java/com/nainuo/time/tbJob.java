/**
 * @Author：ouyhjian
 * @Package：com.nainuo.time
 * @Project：springboot-quartz
 * @name：tbJob
 * @Date：2024/4/6 20:17
 * @Filename：tbJob
 */
package com.nainuo.time;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nainuo.Beans.orderBean;
import com.nainuo.Beans.syncConfigBean;
import com.nainuo.Beans.userBean;
import com.nainuo.Beans.userSearchGoodsBean;
import com.nainuo.Beans.vistualBean.commissionAndRate;
import com.nainuo.VO.*;
import com.nainuo.mapper.orderMapper;
import com.nainuo.mapper.syncConfigMapper;
import com.nainuo.mapper.userMapper;
import com.nainuo.mapper.userSearchGoodsBeanMapper;
import com.nainuo.tools.getUserPub;
import lombok.SneakyThrows;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.nainuo.VO.constant.*;

@Component
public class tbJob implements Job {
    @Autowired
    orderMapper orderMapper;
    @Autowired
    userSearchGoodsBeanMapper userSearchGoodsBeanMapper;
    @Autowired
    syncConfigMapper syncConfigMapper;
    @Autowired
    userMapper userMapper;
    @Autowired
    getUserPub getUserPub;
    @SneakyThrows
    @Override
    public void execute(JobExecutionContext context)  {
        LambdaQueryWrapper<syncConfigBean> syncwrapper = new LambdaQueryWrapper();
        syncwrapper.eq(syncConfigBean::getAppType,0);
        syncConfigBean syncConfigBean = syncConfigMapper.selectOne(syncwrapper);
        int hour = syncConfigBean.getHour();
        int minute =syncConfigBean.getMinute();

        //1.搜索记录统计
        LambdaQueryWrapper<userSearchGoodsBean> wrapperUsgb =  new LambdaQueryWrapper<>();
        wrapperUsgb.eq(userSearchGoodsBean::getAppType,0).eq(userSearchGoodsBean::getOrderBindFlag,0);
        List<userSearchGoodsBean> userSearchGoodsBeanList = userSearchGoodsBeanMapper.selectList(wrapperUsgb);

        for (int i = 0; i < userSearchGoodsBeanList.size(); i++) {
            userSearchGoodsBean usb = userSearchGoodsBeanList.get(i);
            String startTime = DateTimeToStrUtil.addDateTimeToStr(usb.getCreatTime(), 0, 0, 0, 0);
            String endTime = DateTimeToStrUtil.addDateTimeToStr(usb.getCreatTime(), 0, hour, minute, 0);

            String res = HttpUtils.sendHttpPostRequest(tbUrl, stringUtils.toStrMapJson(tbParm.replace("*", startTime).concat(endTime)));
            try {

               JSONArray results =  JSONObject.parseObject(res).getJSONObject("data")
                       .getJSONObject("results").getJSONArray("publisher_order_dto");
               if (Objects.nonNull(results)&&results.size()>0){

                for (int j = 0; j < results.size(); j++) {

                    JSONObject tem = results.getJSONObject(j);

                  //  System.out.println(tem+"---------------");
                    String  trasix = tem.getString("trade_parent_id").
                            substring(tem.getString("trade_parent_id").length() - 6);


                    if (Objects.nonNull(usb.getTbId())){
                        if(usb.getTbId().equals(trasix)
                    && usb.getTbpId().equals(tem.getString("adzone_id"))
                                &&(usb.getGoodId().contains(tem.getString("item_id").split("-")[1])

                        )
                    ){
                            savaUsgb(tem,usb,null);
                           // results.remove(tem);
                           // userSearchGoodsBeanList.remove(i);
                            break;
                    }
                    }
                    else if (
                            usb.getTbpId().equals(tem.getString("adzone_id"))
                    && usb.getGoodId().contains(tem.getString("item_id").split("-")[1])
                    ){
                        savaUsgb(tem,usb,trasix);
                       //  results.remove(tem);
                       //  userSearchGoodsBeanList.remove(i);
                        break;
                    }

                       // System.out.println("----------------------------------");

                }
               }
               else {
                   //System.out.println("----------------------------------");
               }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        for (int i = 0; i < userSearchGoodsBeanList.size(); i++) {
            userSearchGoodsBean userSearchGoodsBean = userSearchGoodsBeanList.get(i);
            Date dateBefore24Hours = new Date(userSearchGoodsBean.getCreatTimeSign()*1000+ (24 * 60 * 60 * 1000L));
            System.out.println(dateBefore24Hours.toString());
            // 比较两个时间点
            boolean is24h = dateBefore24Hours.after(new Date());
            if (!is24h){
             userSearchGoodsBean.setOrderBindFlag("-1");
             userSearchGoodsBeanMapper.updateById(userSearchGoodsBean);
            }

        }

    }

    private void savaUsgb(JSONObject tem, userSearchGoodsBean usb, String trasix) {

        orderBean ob = new orderBean();
        BigDecimal rate = BigDecimal.valueOf(usb.getUserCommissionRate() * usb.getCommissionRate());
     //   commissionAndRate pubSharePreFeeForCommission = getUserPub.getCommissionByCommAnAppid(ob.getOpenId(), tem.getBigDecimal("pub_share_pre_fee_for_commission"));
        ob.setAppId(usb.getAppId());
        ob.setSignOid(usb.getSignOid());
        ob.setOpenId(usb.getOpenId());
        ob.setUserId(usb.getUId());
        ob.setSgId(usb.getSgId());
        ob.setAppName(usb.getAppName());
        ob.setAppType(usb.getAppType());

        /*----------------------------------------*/
        ob.setCommissionRate(tem.getBigDecimal("total_commission_rate").divide(new BigDecimal(100)));
        ob.setCommission(tem.getBigDecimal("pub_share_pre_fee_for_commission"));//总佣金
        ob.setUserCommission(rate.multiply(ob.getCommission()));//用户佣金
        ob.setMyCommission(ob.getCommission().subtract(ob.getUserCommission()));//用户佣金
        ob.setUserCommissionRate(rate);//用户比例
        ob.setGoodId(tem.getString("item_id"));
        ob.setGoodPic(tem.getString("item_img"));
        ob.setGoodsSign("-");
        ob.setRealPrice(tem.getBigDecimal("alipay_total_price"));
        ob.setTitle(tem.getString("item_title"));

        ob.setCreatTime(tem.getDate("tk_create_time"));
        ob.setCreatTimeSign(TimestampUtils.convertToDateTimestamp(ob.getCreatTime()));

        ob.setOrderPayTime(tem.getDate("tb_paid_time"));
        ob.setOrderPayTimeSign(TimestampUtils.convertToDateTimestamp(ob.getOrderPayTime()));

        ob.setOrderPayTime(tem.getDate("tb_paid_time"));
        ob.setOrderPayTimeSign(TimestampUtils.convertToDateTimestamp(ob.getOrderPayTime()));

        ob.setStartTime(tem.getString("tk_create_time"));
        ob.setEndTime(tem.getString("tk_create_time"));
        ob.setOrderNo(tem.getString("trade_parent_id"));
        ob.setOrderNoSon(tem.getString("trade_id"));
        ob.setPId(tem.getString("adzone_id"));
        ob.setOrderStatus(tem.getInteger("tk_status"));
        String statu ="";
        if (ob.getOrderStatus()==3) statu ="订单结算";
        if (ob.getOrderStatus()==12) statu ="订单付款";
        if (ob.getOrderStatus()==13) statu ="订单失效";
        if (ob.getOrderStatus()==14) statu ="订单成功";
        ob.setOrderStatusDec(statu);

        orderMapper.insert(ob);
        System.out.println("-----------------------ob------------inert-------------------------");
        System.out.println(ob);
        if (Objects.nonNull(trasix)){
            LambdaQueryWrapper<userBean> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(userBean::getOpenId,usb.getOpenId());
            userBean userBean = userMapper.selectOne(wrapper);
            userBean.setTbId(trasix);
            userMapper.updateById(userBean);
            System.out.println("--------------------userBean---------------inert-------------------------");
            System.out.println(userBean);
        }
        usb.setOrderBindFlag("1");
        usb.setCreatTime(new Date());
        userSearchGoodsBeanMapper.updateById(usb);

    }

}
