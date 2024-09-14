/**
 * @Author：ouyhjian
 * @Package：com.nainuo.time.orderStatusSync
 * @Project：springboot-quartz
 * @name：orderStatusSync
 * @Date：2024/5/17 19:28
 * @Filename：orderStatusSync
 */
package com.nainuo.time;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nainuo.Beans.commissionPlanBean;
import com.nainuo.Beans.orderBean;
import com.nainuo.Beans.userBean;
import com.nainuo.Beans.userSearchGoodsBean;
import com.nainuo.Beans.vistualBean.commissionAndRate;
import com.nainuo.VO.*;
import com.nainuo.mapper.commissionPlanMapper;
import com.nainuo.mapper.orderMapper;
import com.nainuo.mapper.syncConfigMapper;
import com.nainuo.mapper.userMapper;
import com.nainuo.tools.getUserPub;
import lombok.SneakyThrows;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.nainuo.VO.constant.tbParm;
import static com.nainuo.VO.constant.tbUrl;

@Component
public class orderCheckSyncJob implements Job {
    @Autowired
    orderMapper orderMapper;
    @Autowired
    syncConfigMapper syncConfigMapper;
    @Autowired
    userMapper userMapper;
    @Autowired
    getUserPub getUserPub;
    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LambdaQueryWrapper<orderBean> orderBeanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        String startTime =  DateTimeToStrUtil.addDateTimeToStr(jobExecutionContext.getScheduledFireTime(),-1,-3,0,0);
        String endTime =  DateTimeToStrUtil.addDateTimeToStr(jobExecutionContext.getScheduledFireTime(),-1,0,0,0);
//        startTime = "2024-05-17 12:35:00";
//        endTime  = "2024-05-17 13:35:00";
        System.out.println(startTime);
        System.out.println(endTime);
        String res = HttpUtils.sendHttpPostRequest(tbUrl, stringUtils.toStrMapJson(tbParm.replace("*", startTime).concat(endTime)));
        JSONArray results =  JSONObject.parseObject(res).getJSONObject("data")
                .getJSONObject("results").getJSONArray("publisher_order_dto");
        System.out.println(results);
        if (Objects.nonNull(results)&&results.size()>0){
            for (int i = 0; i < results.size(); i++) {
                JSONObject jb = results.getJSONObject(i);
               orderBeanLambdaQueryWrapper.eq(orderBean::getOrderNo,jb.getString("trade_parent_id"));
                int len = 0;
                        len = orderMapper.selectList(orderBeanLambdaQueryWrapper).size();
                        if (len==0){
                            savaUsgb(jb);
                            break;
                        }
            }
        }
    }

//"JobExecutionContext: trigger: 'DEFAULT.orderCheckSyncJobTrigger job: DEFAULT.orderCheckSyncJob fireTime: 'Fri May 17 21:11:00 CST 2024 scheduledFireTime: Fri May 17 21:11:00 CST 2024 previousFireTime: 'Fri May 17 21:10:00 CST 2024 nextFireTime: Fri May 17 21:12:00 CST 2024 isRecovering: false refireCount: 0"
private void savaUsgb(JSONObject tem) {


    orderBean ob = new orderBean();
    ob.setAppId("gh_5e0c4f9779fa");
    ob.setSignOid("-null-");
    ob.setOpenId("-null-");
    ob.setUserId(99L);
    ob.setSgId(-1L);
    ob.setAppName("淘宝平台");
    ob.setAppType(0);
    /*----------------------------------------*/

    commissionAndRate commissionByCommAnAppid = getUserPub.getCommissionByCommAnAppid("oUW-h6e3RGzlukAoQjcmhh3HrhFg", tem.getBigDecimal("pub_share_pre_fee_for_commission"));
    BigDecimal rate = BigDecimal.valueOf(commissionByCommAnAppid.getUserrate()*commissionByCommAnAppid.getComissionRate());
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
    ob.setPId(tem.getString("adzone_id"));
    ob.setOrderStatus(tem.getInteger("tk_status"));
    String statu ="";
    if (ob.getOrderStatus()==3) statu ="订单结算";
    if (ob.getOrderStatus()==12) statu ="订单付款";
    if (ob.getOrderStatus()==13) statu ="订单失效";
    if (ob.getOrderStatus()==14) statu ="订单成功";
    ob.setOrderStatusDec(statu);
    orderMapper.insert(ob);
}

}
