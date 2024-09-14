/**
 * @Author：ouyhjian
 * @Package：com.nainuo.time
 * @Project：springboot-quartz
 * @name：orderSyncJob
 * @Date：2024/8/8 22:36
 * @Filename：orderSyncJob
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
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.nainuo.VO.constant.*;
import static com.nainuo.VO.constant.jdParm;
import static com.nainuo.VO.stringUtils.str2short;

@Component
public class orderSyncJob implements Job {
    @Autowired
    orderMapper orderMapper;
    @Autowired
    syncConfigMapper syncConfigMapper;
    @Autowired
    userSearchGoodsBeanMapper userSearchGoodsBeanMapper;
    @Autowired
    userMapper userMapper;
    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LambdaQueryWrapper<syncConfigBean> syncwrapper = new LambdaQueryWrapper();
        syncwrapper.eq(syncConfigBean::getAppType,0);
        syncConfigBean syncConfigBean = syncConfigMapper.selectOne(syncwrapper);
        int hour = syncConfigBean.getHour();
        int minute =syncConfigBean.getMinute();
        int second = syncConfigBean.getSecond();

        String endTime = DateTimeToStrUtil.addDateTimeToStr(new Date(), 0, 0, 0, 0);
        String startTime= DateTimeToStrUtil.addDateTimeToStr(new Date(), 0, hour, minute, second);
        jdSync(startTime,endTime);
        tbSync(startTime,endTime);
        pddSync(String.valueOf(TimestampUtils.convertToDateTimestamp(DateTimeToStrUtil.addDateTimeToDate(new Date(),0,hour,minute,second))),
                String.valueOf(TimestampUtils.convertToDateTimestamp(new Date())));
        orderBind();
    }

    @SneakyThrows
    private void pddSync(String startTimeSign, String endTimeSign) {
//        startTimeSign ="1726308000";
//        endTimeSign="1726315200";
  //      System.out.println(startTimeSign+"--"+endTimeSign);
        String res =HttpUtils.sendHttpPostRequest(pddUrl, stringUtils.toStrMapJson
                (pddParm.replace("*", startTimeSign).concat(endTimeSign)));

        JSONObject jsonres = JSON.parseObject(res).getJSONObject("order_list_get_response");
        int size = jsonres.getInteger("total_count");
        if (size>0) {
            JSONArray results = jsonres.getJSONArray("order_list");

            if (Objects.nonNull(results)) {
                for (int i = 0; i < results.size(); i++) {
                    JSONObject tem = results.getJSONObject(i);//订单明细
                    String uidsid = tem.getString("custom_parameters");
                    String uuid = JSON.parseObject(uidsid).getString("sid");

                    String tradeId = tem.getString("order_sn");//订单号
                    Integer tkStatus = tem.getInteger("order_status");
                    Integer skuNum = 0;

                    if (Objects.nonNull(tradeId) && isHaveOrderNo(tradeId, skuNum)) {//订单不存在可以同步
                        String tradeParentId = tem.getString("goods_id");//父订单
                        String adzoneId = tem.getString("p_id");
                //订单状态：0-已支付；1-已成团；2-确认收货；3- ；4-审核失败（不可提现）；5-已经结算 ;10-已处罚
                        String statuDec = tem.getString("order_status_desc");
                        int orderStatusSys = 0;

                        if (tkStatus == -1) {
                            statuDec = "待付款";
                            orderStatusSys = 1;
                        }
                        if (tkStatus == 0 || tkStatus == 1||tkStatus == 2||tkStatus == 3) {
                            statuDec = "订单付款";
                            orderStatusSys = 1;
                        }
                        if (tkStatus == 4 || tkStatus == 10) {
                            statuDec = "订单失效";
                            orderStatusSys = -1;
                        }
                        if (tkStatus == 5) {
                            statuDec = "订单成功";
                            orderStatusSys = 2;
                        }


                        String goodSign = tem.getString("goods_sign");//物料编码
                        String itemTitle = tem.getString("goods_name");//商品标题
                        String itemImg = tem.getString("goods_thumbnail_url");//商品图片
                        String itemId = tem.getString("goods_id");//商品签名

                        BigDecimal alipayTotalPrice = tem.getBigDecimal("goods_price").divide(new BigDecimal(100));//支付价格
                        BigDecimal pubSharePreFeeForCommission = tem.getBigDecimal("promotion_amount").divide(new BigDecimal(100));//总佣金

                        long tkCreateTimeSign = tem.getLong("order_create_time");//订单创建时间戳
                        Date tkCreateTime = TimestampUtils.convertToUtilDate(tkCreateTimeSign);//订单创建时间


                        long tbPaidTimeSign = tem.getLong("order_pay_time");
                        Date tbPaidTime = TimestampUtils.convertToUtilDate(tbPaidTimeSign);//商品支付时间

                        String startTimeStr = DateTimeToStrUtil.addDateTimeToStr(tkCreateTime,0,0,0,0);
                        String endTimeStr = startTimeStr;


                        saveOrder(tradeId, tradeParentId,
                                adzoneId, tkStatus,
                                statuDec, itemId,
                                itemTitle, itemImg,
                                goodSign, alipayTotalPrice,
                                pubSharePreFeeForCommission, tkCreateTime,
                                tkCreateTimeSign, tbPaidTime,
                                tbPaidTimeSign, startTimeStr,
                                endTimeStr, orderStatusSys,
                                2, "拼多多", skuNum, uuid);
                    } else {
                        Log.info("pdd --- 订单号 [ " + tradeId + " ] 已存在！ ", this.getClass().getName());
                    }

                }
            } else {
                Log.info("pdd --- 时间段 [ " + TimestampUtils.convertToUtilDate(Long.parseLong(startTimeSign)) + "-" + TimestampUtils.convertToUtilDate(Long.parseLong(endTimeSign)) + " ] 无订单", this.getClass().getName());
            }
        }else {
            Log.info("pdd --- 时间段 [ " + TimestampUtils.convertToUtilDate(Long.parseLong(startTimeSign)) + "-" + TimestampUtils.convertToUtilDate(Long.parseLong(endTimeSign)) + " ] 无订单", this.getClass().getName());
        }
    }

    @SneakyThrows
    private void jdSync(String startTime, String endTime) {

        String res = HttpUtils.sendHttpPostRequest(jdUrl, stringUtils.toStrMapJson(jdParm
                .replace("*", startTime).concat(endTime)));
        JSONArray results = JSONObject.parseObject(res).getJSONArray("data");
        if (Objects.nonNull(results)&& !results.isEmpty()){
            for (int i = 0; i < results.size(); i++) {
                JSONObject tem = results.getJSONObject(i);//订单明细
                String tradeId = tem.getString("orderId");//订单号
                Integer tkStatus = tem.getInteger("validCode");
                Integer skuNum = tem.getInteger("skuNum");
                String uuid = tem.getString("positionId");
                if(tkStatus!=2&&Objects.nonNull(tradeId)&&isHaveOrderNo(tradeId,skuNum)){//订单不存在可以同步
                    String tradeParentId = tem.getString("parentId");//父订单
                    String adzoneId = tem.getString("unionId");

                    String statuDec ="";
                    int orderStatusSys = 0;

                    if (tkStatus==15) {statuDec ="待付款";orderStatusSys = 1;}
                    if (tkStatus==16||tkStatus==2) { statuDec ="订单付款";orderStatusSys = 1;}
                    if(!(tkStatus==16||tkStatus==17||tkStatus==15||tkStatus==2)){statuDec ="订单失效"; orderStatusSys = -1;}
                    if (tkStatus==17) {statuDec ="订单成功";orderStatusSys = 2;}


                    String  goodSign= tem.getString("itemId");//物料编码
                    String itemTitle = tem.getString("skuName");//商品标题
                    String itemImg = tem.getJSONObject("goodsInfo").getString("imageUrl");//商品图片
                    String itemId = tem.getJSONObject("goodsInfo").getString("productId");//商品签名

                    BigDecimal alipayTotalPrice = tem.getBigDecimal("estimateCosPrice");//支付价格
                    BigDecimal pubSharePreFeeForCommission = tem.getBigDecimal("estimateFee");//总佣金

                    Date tkCreateTime = tem.getDate("orderTime");//订单创建时间
                    long tkCreateTimeSign = TimestampUtils.convertToDateTimestamp(tkCreateTime);//订单创建时间戳
                    Date tbPaidTime = tem.getDate("modifyTime");//商品支付时间
                    long tbPaidTimeSign = TimestampUtils.convertToDateTimestamp(tbPaidTime);
                    String startTimeStr = tem.getString("orderTime");
                    String endTimeStr = tem.getString("orderTime");


                    saveOrder(tradeId,tradeParentId,
                            adzoneId,tkStatus,
                            statuDec,itemId,
                            itemTitle,itemImg,
                            goodSign,alipayTotalPrice,
                            pubSharePreFeeForCommission,tkCreateTime,
                            tkCreateTimeSign,tbPaidTime,
                            tbPaidTimeSign,startTimeStr,
                            endTimeStr, orderStatusSys,
                            1,"京东",skuNum,uuid);
                }else {
                    Log.info( "jd ---  订单号 [ "+tradeId+" ] 已存在！ ",this.getClass().getName());
                }

            }
        }
        else {
            Log.info(" jd --- 时间段 [ "+startTime+"-"+endTime+" ] 无订单",this.getClass().getName());
        }
    }

    @SneakyThrows
    private void tbSync(String startTime, String endTime) {
//        startTime ="2024-09-14 15:04:04";
//        endTime ="2024-09-14 17:00:00";
        String res = HttpUtils.sendHttpPostRequest(tbUrl, stringUtils.toStrMapJson(tbParm.replace("*", startTime).concat(endTime)));
        //判断订单号
        JSONArray results = null;
        try {
            results=   JSONObject.parseObject(res).
                            getJSONObject("data").
                            getJSONObject("results").
                            getJSONArray("publisher_order_dto");
        }catch (Exception e){

        }

        if (Objects.nonNull(results)){
            for (int i = 0; i < results.size(); i++) {
                JSONObject tem = results.getJSONObject(i);//订单明细
                String tradeId = tem.getString("trade_id");//订单号

                if(isHaveOrderNo(tradeId,0)){//订单不存在可以同步
                    String tradeParentId = tem.getString("trade_parent_id");//父订单
                    String adzoneId = tem.getString("adzone_id");
                    Integer tkStatus = tem.getInteger("tk_status");

                    String statuDec ="";
                    int orderStatusSys = 0;
                    if (tkStatus==3) {statuDec ="订单结算";orderStatusSys = 2;}
                    if (tkStatus==12){ statuDec ="订单付款";orderStatusSys = 1;}
                    if (tkStatus == 13) {statuDec ="订单失效"; orderStatusSys = -1;}
                    if (tkStatus==14) {statuDec ="订单成功";orderStatusSys = 2;}

                    String itemId = tem.getString("item_id");//物料编码
                    String itemTitle = tem.getString("item_title");//商品标题
                    String itemImg = tem.getString("item_img");//商品图片
                    String goodSign = "-";//商品签名

                    BigDecimal alipayTotalPrice = tem.getBigDecimal("alipay_total_price");//支付价格
                    BigDecimal pubSharePreFeeForCommission = tem.getBigDecimal("pub_share_pre_fee_for_commission");//总佣金

                    Date tkCreateTime = tem.getDate("tk_create_time");//订单创建时间
                    long tkCreateTimeSign = TimestampUtils.convertToDateTimestamp(tkCreateTime);//订单创建时间戳
                    Date tbPaidTime = tem.getDate("tb_paid_time");//商品支付时间
                    long tbPaidTimeSign = TimestampUtils.convertToDateTimestamp(tbPaidTime);
                    String startTimeStr = tem.getString("tk_create_time");
                    String endTimeStr = tem.getString("tk_create_time");


                    saveOrder(tradeId,tradeParentId,
                            adzoneId,tkStatus,
                            statuDec,itemId,
                            itemTitle,itemImg,
                            goodSign,alipayTotalPrice,
                            pubSharePreFeeForCommission,tkCreateTime,
                            tkCreateTimeSign,tbPaidTime,
                            tbPaidTimeSign,startTimeStr,
                            endTimeStr, orderStatusSys, 0, "淘宝", 0, "-");
                }else {
                    Log.info("tb --- 订单号 [ "+tradeId+" ] 已存在！ ",this.getClass().getName());
                }

            }
        }
        else {
            Log.info(" tb --- 时间段 [ "+startTime+"-"+endTime+" ] 无订单",this.getClass().getName());
        }
    }

    private void saveOrder(String tradeId, String tradeParentId,
                           String adzoneId, Integer tkStatus,
                           String statuDec, String itemId,
                           String itemTitle, String itemImg,
                           String goodSign, BigDecimal alipayTotalPrice,
                           BigDecimal pubSharePreFeeForCommission, Date tkCreateTime,
                           long tkCreateTimeSign, Date tbPaidTime,
                           long tbPaidTimeSign, String startTimeStr,
                           String endTimeStr, int orderStatusSys,
                           int appType, String appName, Integer skuNum, String uuid) {

                            orderBean bean = new orderBean();
                            bean.setOrderNo(tradeId);
                            bean.setOrderParentNo(tradeParentId);
                            bean.setPId(adzoneId);
                            bean.setOrderStatus(tkStatus);
                            bean.setOrderStatusDec(statuDec);
                            bean.setGoodId(itemId);
                            bean.setTitle(itemTitle);
                            bean.setGoodPic(itemImg);
                            bean.setGoodsSign(goodSign);
                            bean.setRealPrice(alipayTotalPrice);
                            bean.setCommission(pubSharePreFeeForCommission);
                            bean.setCreatTime(tkCreateTime);
                            bean.setCreatTimeSign(tkCreateTimeSign);
                            bean.setOrderPayTime(tbPaidTime);
                            bean.setOrderPayTimeSign(tbPaidTimeSign);
                            bean.setStartTime(startTimeStr);
                            bean.setEndTime(endTimeStr);
                            bean.setOrderStatusSys(orderStatusSys);
                            bean.setAppType(appType);
                            bean.setAppName(appName);
                            bean.setUserId(-1L);
                            bean.setSkuNum(skuNum);
                            bean.setUuid(uuid);
                            //Log.info(bean.toString(),this.getClass().getName());
                            orderMapper.insert(bean);
    }


    private boolean isHaveOrderNo(String tradeId,Integer skuNum) {
        LambdaQueryWrapper<orderBean> syncwrapper = new LambdaQueryWrapper();
        syncwrapper.eq(orderBean::getOrderNo,tradeId).eq(orderBean::getSkuNum,skuNum);
        return !Objects.nonNull(orderMapper.selectOne(syncwrapper));
    }

   private void orderBind(){
       LambdaQueryWrapper<orderBean> bindwrapper = new LambdaQueryWrapper();
       bindwrapper.eq(orderBean::getUserId,-1).ne(orderBean::getOrderStatusSys,-4);
       List<orderBean> list = orderMapper.selectList(bindwrapper);
       orderBindJd(list.stream().filter(orderBean-> orderBean.getAppType()==1)
               .collect(Collectors.toList()));

       orderBindTb(list.stream().filter(orderBean-> orderBean.getAppType()==0)
               .collect(Collectors.toList()));

       orderBindPdd(list.stream().filter(orderBean-> orderBean.getAppType()==2)
               .collect(Collectors.toList()));

   }

    private void orderBindPdd(List<orderBean> list) {
        LambdaQueryWrapper<userSearchGoodsBean> pdds = new LambdaQueryWrapper<>();
        pdds.eq(userSearchGoodsBean::getAppType,2).eq(userSearchGoodsBean::getOrderBindFlag,"0")
                .eq(userSearchGoodsBean::getStatus,0).eq(userSearchGoodsBean::getIsDel,0);
        List<userSearchGoodsBean> userSearchGoodsBeansList = userSearchGoodsBeanMapper.selectList(pdds);
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                orderBean tem = list.get(i);
                List<userSearchGoodsBean> temubs = userSearchGoodsBeansList.stream().filter(
                        userSearchGoodsBean -> userSearchGoodsBean.getCreatTime().before(tem.getCreatTime())
                ).collect(Collectors.toList());
                if (!temubs.isEmpty()) {
                    for (int j = 0; j < temubs.size(); j++) {
                        userSearchGoodsBean temub = temubs.get(j);
                        if(temub.getUuid().equals(tem.getUuid())){
                            addBindInfo(temub,tem);
                            break;
                        }
                    }

                }
            }
        }
    }

    private void orderBindTb(List<orderBean> list) {
        LambdaQueryWrapper<userSearchGoodsBean> tbs = new LambdaQueryWrapper<>();
        tbs.eq(userSearchGoodsBean::getAppType,0).eq(userSearchGoodsBean::getOrderBindFlag,"0")
                .eq(userSearchGoodsBean::getStatus,0).eq(userSearchGoodsBean::getIsDel,0);
        List<userSearchGoodsBean> userSearchGoodsBeansList = userSearchGoodsBeanMapper.selectList(tbs);
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                orderBean tem = list.get(i);
                List<userSearchGoodsBean> temubs = userSearchGoodsBeansList.stream().filter(
                        userSearchGoodsBean -> userSearchGoodsBean.getCreatTime().before(tem.getCreatTime())
                ).collect(Collectors.toList());
                String orderNo = tem.getOrderNo();
                String tbp = orderNo.substring(orderNo.length()-6,orderNo.length());

                if (!temubs.isEmpty()) {
                    int count1=0;
                    int count2=0;
                    for (int k = 0; k < temubs.size(); k++) {
                        userSearchGoodsBean temub = temubs.get(k);
                        //MQ86yot7t8moKQgOGi5gMT2t6-XngZXvVt0K3mBm9ouw
                        //JbtMUqkGMQvagTN5zSvta-XngZXvVt0K3mBm9ouw
                        //495329

                        if (Objects.nonNull(temub.getTbId())){
                            if (temub.getTbpId().equals(tem.getPId())
                            && temub.getTbId().equals(tbp)
                                    &&
                                    temub.getGoodId().split("-")[1].
                                            equals(tem.getGoodId().split("-")[1])
                            ) {
                                count1++;
                            }
                        }
                       if(
                              temub.getTbpId().equals(tem.getPId())
                                &&
                                temub.getGoodId().split("-")[1].
                                        equals(tem.getGoodId().split("-")[1])){

                            count2++;
                        }

                    }

                    if (count1==1||count2==1) {
                        Log.info("数量： "+count1 +"-- "+count2,this.getClass().getName());

                        for (int j = 0; j < temubs.size(); j++) {
                            userSearchGoodsBean temub = temubs.get(j);
                            if (temub.getTbpId().equals(tem.getPId())
                                    &&
                                    temub.getGoodId().split("-")[1].
                                            equals(tem.getGoodId().split("-")[1])) {
                                if (count2==1){
                                    setTbid(temub.getAppId(),temub.getUId(),tbp);
                                }
                                addBindInfo(temub, tem);
                                break;
                            }
                        }
                    }else {
                        tem.setOrderStatusSys(-4);
                        tem.setOrderStatusDec("无记录或记录大于1，无法绑定！~数量"+count1 +"-- "+count2);
                        orderMapper.updateById(tem);

                        Log.info("tb --- 无记录或记录大于1，无法绑定！~数量： "+count1 +"-- "+count2,this.getClass().getName());
                    }
                }
            }
        }
    }


    private void orderBindJd(List<orderBean> list) {
        LambdaQueryWrapper<userSearchGoodsBean> jds = new LambdaQueryWrapper<>();
        jds.eq(userSearchGoodsBean::getAppType,1).eq(userSearchGoodsBean::getOrderBindFlag,"0")
                .eq(userSearchGoodsBean::getStatus,0).eq(userSearchGoodsBean::getIsDel,0);
        List<userSearchGoodsBean> userSearchGoodsBeansList = userSearchGoodsBeanMapper.selectList(jds);
       if (!list.isEmpty()) {
           for (int i = 0; i < list.size(); i++) {
               orderBean tem = list.get(i);
               List<userSearchGoodsBean> temubs = userSearchGoodsBeansList.stream().filter(
                       userSearchGoodsBean -> userSearchGoodsBean.getCreatTime().before(tem.getCreatTime())
               ).collect(Collectors.toList());
               if (!temubs.isEmpty()) {
                   for (int j = 0; j < temubs.size(); j++) {
                       userSearchGoodsBean temub = temubs.get(j);
                       if(temub.getUuid().equals(tem.getUuid())&&temub.getGoodId().equals(tem.getGoodId())){
                           addBindInfo(temub,tem);
                           break;
                       }
                   }

               }
           }
       }
    }

    //@Transactional
    private void addBindInfo(userSearchGoodsBean temub, orderBean tem) {
//        Log.info(temub.toString(),this.getClass().getName());
    tem.setUserId(temub.getUId());
    tem.setSgId(temub.getSgId());
    tem.setOpenId(temub.getOpenId());
    tem.setSignOid(temub.getSignOid());
    tem.setCommissionRate(BigDecimal.valueOf(temub.getCommissionRate()));
    tem.setUserCommissionRate(BigDecimal.valueOf(temub.getUserCommissionRate()));

    BigDecimal userCommission = tem.getCommission().multiply(tem.getCommissionRate()).multiply(tem.getUserCommissionRate());
    BigDecimal myCommission =  tem.getCommission().subtract(userCommission);

    tem.setUserCommission(userCommission);
    tem.setMyCommission(myCommission);
    tem.setAppId(temub.getAppId());

    temub.setOrderBindFlag("-1");
    temub.setStatus(1);
    temub.setStatusDec("已绑定");

    userSearchGoodsBeanMapper.updateById(temub);
    orderMapper.updateById(tem);

    }

    private void setTbid(String appId, Long uId, String tbp) {
        LambdaQueryWrapper<userBean> userBeanLambdaQueryWrapper =new LambdaQueryWrapper<>();
        userBeanLambdaQueryWrapper.eq(userBean::getAppId,appId)
                .eq(userBean::getUserId,uId);
        userBean tub = userMapper.selectOne(userBeanLambdaQueryWrapper);
        tub.setTbId(tbp);
        userMapper.updateById(tub);
    }

}
