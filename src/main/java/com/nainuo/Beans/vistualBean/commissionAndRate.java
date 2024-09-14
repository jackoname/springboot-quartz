/**
 * @Author：ouyhjian
 * @Package：com.nainuo.wx.mp.myBusiness.horeBusiness.horeBeans.vistualBean
 * @Project：wx-mp
 * @name：commissionAndRate
 * @Date：2024/4/1 23:33
 * @Filename：commissionAndRate
 */
package com.nainuo.Beans.vistualBean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class commissionAndRate {
BigDecimal commissiom; // 佣金
Double userrate; //用户比例
Double comissionRate; //分成比例

}

