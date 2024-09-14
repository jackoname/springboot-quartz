package com.nainuo.Beans;

/**
 * @Author：ouyhjian
 * @Package：com.nainuo.wx.mp.myBusiness.horeBusiness.horeBeans
 * @Project：wx-mp
 * @name：commissionPlanBean
 * @Date：2024/3/31 14:45
 * @Filename：commissionPlanBean
 */


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
@Data
@TableName("tci_commission_plan")

public class commissionPlanBean {
    @TableId(value = "commission_id", type = IdType.AUTO)
    Integer commissionId;
    Double commissionRate;
    BigDecimal maxPrice;
    BigDecimal lowPrice;
    Double userComRate;
    Integer commissionType;
    int orderMun;
    int planFlag;
    int isUse;
    String commissionPlanDes;
    String appId;//gzhid
}
