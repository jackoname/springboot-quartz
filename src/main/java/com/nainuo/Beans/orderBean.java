/**
 * @Author：ouyhjian
 * @Package：com.nainuo.Beans
 * @Project：springboot-quartz
 * @name：orderBean
 * @Date：2024/4/6 10:47
 * @Filename：orderBean
 */
package com.nainuo.Beans;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@Data
@NoArgsConstructor
@TableName(value = "tbi_order_info")
public class orderBean {
    @TableId(value = "order_id", type = IdType.AUTO)
    Long orderId;//自增id
    Long userId;//用户id
    Long sgId;//查询记录ID
    String openId;//微信id
    String signOid;//加密后的微信id
    String orderNo;//订单号
    String orderNoSon;//子订单号
    String goodId;//商品id
    String goodsSign;//加密商品id
    String title;//商品标题
    String goodPic;//商品主图
    BigDecimal realPrice;//到手价
    BigDecimal commission;//总佣金
    BigDecimal UserCommission;//用户佣金
    BigDecimal myCommission;//我的佣金
    BigDecimal CommissionRate;//总佣金比例
    BigDecimal UserCommissionRate;//用户佣金
    int orderStatus;//订单状态
    String orderStatusDec;//订单状态描述
    Date creatTime;//创建时间
    Long creatTimeSign;//创建时间戳
    Date orderPayTime;//订单支付时间
    Long orderPayTimeSign;//订单支付时间时间戳
    String appId;//gzhid
    String appName;//gzhid
    String pId;
    int orderStatusSys;
    Date updateTime;//更新时间
    Long updateTimeSign;//更新时间戳
    int appType;
    String startTime;//查询开始
    String endTime;//查询结束
    Date complateTime;//订单支付时间
    Long complateTimeSign;//订单支付时间时间戳
    String orderParentNo;
    int skuNum;
    String uuid;

}

