/**
 * @Author：ouyhjian
 * @Package：com.nainuo.tools
 * @Project：springboot-quartz
 * @name：getUserPub
 * @Date：2024/5/20 21:44
 * @Filename：getUserPub
 */
package com.nainuo.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nainuo.Beans.commissionPlanBean;
import com.nainuo.Beans.userBean;
import com.nainuo.Beans.vistualBean.commissionAndRate;
import com.nainuo.mapper.commissionPlanMapper;
import com.nainuo.mapper.userMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Component
public class getUserPub {
    @Autowired
    commissionPlanMapper commissionPlanMapper;
    @Autowired
    userMapper userMapper;
    public commissionAndRate getCommissionByCommAnAppid(String opid, BigDecimal commssion) {
        Double rate = null;
        Double commissionRate =null;

        LambdaQueryWrapper<userBean> uw = new LambdaQueryWrapper<>();
        uw.eq(userBean::getOpenId, opid);

        userBean uwser = userMapper.selectOne(uw);

        if (Objects.isNull(uwser.getPreCommissionRate())) {
            String appid = uwser.getAppId();
            List<commissionPlanBean> list = getCommissionPlanByAppid(appid);
            commissionRate = list.get(0).getCommissionRate();
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i).getLowPrice().compareTo(commssion) < 0 && list.get(i).getMaxPrice().compareTo(commssion) >= 0) {
                    rate = list.get(i).getUserComRate();
                }
            }

        } else {
            rate = uwser.getPreCommissionRate();

        }
        BigDecimal com =   commssion.multiply(BigDecimal.valueOf(rate)).multiply(BigDecimal.valueOf(commissionRate));
        commissionAndRate commissionAndRate = new commissionAndRate(com.setScale(2, RoundingMode.DOWN),
                rate,commissionRate);
        return commissionAndRate;
    }

    public List<commissionPlanBean> getCommissionPlanByAppid(String appid) {
        LambdaQueryWrapper<commissionPlanBean> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(commissionPlanBean::getAppId, appid);
        List<commissionPlanBean> commissionPlanBeans = commissionPlanMapper.selectList(wrapper);
        return commissionPlanBeans;
    }
}
