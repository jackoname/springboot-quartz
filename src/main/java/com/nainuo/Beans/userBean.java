/**
 * @Author：ouyhjian
 * @Package：com.nainuo.wx.mp.myBusiness.horeBusiness.horeBeans
 * @Project：wx-mp
 * @name：usersBean
 * @Date：2024/3/31 13:08
 * @Filename：usersBean
 */
package com.nainuo.Beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("tbi_user_info")

public class userBean {
    @TableId(value = "user_id", type = IdType.AUTO)
        Long userId;//用户id
    String openId;//微信id
    String signOid;//加密微信id
    String trueName;//真实姓名
    String aliPayNo;//支付宝账号
    Long createTimeSign;//创建时间戳
    Date createTime;//创建时间
    Double preCommissionRate;// 个人佣金比例

    BigDecimal alreadyGetMoney;//已经提现余额
    BigDecimal accountBalance;//账户余额

    String remark;//备注
    int isUse;//是否有效

    String username;//登录用户名
    String password;//用户密码
    int sex;//性别

    String appId;//gzhid
    String tbId;//淘宝id
    public userBean(String openId, String appId){
        this.openId =openId;
        this.appId =appId;
    }
}
