/**
 * @Author：ouyhjian
 * @Package：com.nainuo.Beans
 * @Project：springboot-quartz
 * @name：userSearchGoodsBean
 * @Date：2024/4/6 10:48
 * @Filename：userSearchGoodsBean
 */
package com.nainuo.Beans;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("chat_and_recommendation")

public class userSearchGoodsBean {
    @TableId(value = "sg_id", type = IdType.AUTO)
    Long sgId;//主键
    @TableField(value = "u_id")
    Long uId;//用户主键
    String openId;//微信id
    String signOid;//加密后的微信id
    String fromUserMsg;//用户发送的信息
    String toUserMsg;//回复给用户的信息
    String goodId;//商品id
    String title;//商品标题
    String goodPic;//商品主图
    String appId;//公众号id
    BigDecimal realPrice;//到手价
    BigDecimal commission;//总佣金
    BigDecimal UserCommission;//用户佣金
    BigDecimal myCommission;//我的佣金
    Double CommissionRate;//总佣金比例
    Double userCommissionRate;//总佣金比例
    String goodsSign;//加密商品id
    Date creatTime;//创建时间
    Long creatTimeSign;//创建时间戳
    Date outDateTime;//失效时间
    Long outDateTimeSign;//失效时间戳
    String shareLink; //分享的链接
    String appName;//平台
    String orderBindFlag;//订单绑定标志
    int appType;//1,2,3
    int isUserBuy;//用户是否下单购买
    int isUse;//链接是否在有效期内
    int isDel;//是否逻辑删除
    int comparePricePdd;// 拼多多判定比价

    String uuid;
    Date updateTime;//更新时间
    Long updateTimeSign;//更新时间戳

}

