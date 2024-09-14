/**
 * @Author：ouyhjian
 * @Package：com.nainuo.controller
 * @Project：springboot-quartz
 * @name：apiController
 * @Date：2024/5/7 21:17
 * @Filename：apiController
 */
package com.nainuo.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nainuo.Beans.wxArticleBean;
import com.nainuo.mapper.wxArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/nainuo")
@CrossOrigin
@ResponseBody
public class apiController {
    @Autowired
    wxArticleMapper  wxArticleMapper;
    @GetMapping("/getMyLineGoods")
public String getMyLineGoods(){
        Date date1 = new Date();
        date1.setHours(0);
        date1.setMinutes(0);
        date1.setSeconds(0);


        LambdaQueryWrapper<wxArticleBean> wrapper =new LambdaQueryWrapper<>();
        wrapper.gt(wxArticleBean::getCreateTime,date1);
        List<wxArticleBean> wxArticleBeans = wxArticleMapper.selectList(wrapper);
        List<String> res = new ArrayList<>();
        for (int i = 0; i < wxArticleBeans.size(); i++) {
            res.add(wxArticleBeans.get(i).getContent());
        }
        return JSON.toJSONString(res);
    }

    public String getTest(){
        Date date1 = new Date();
        date1.setHours(0);
        date1.setMinutes(0);
        date1.setSeconds(0);


        LambdaQueryWrapper<wxArticleBean> wrapper =new LambdaQueryWrapper<>();
        wrapper.gt(wxArticleBean::getCreateTime,date1);
        List<wxArticleBean> wxArticleBeans = wxArticleMapper.selectList(wrapper);
        List<String> res = new ArrayList<>();
        for (int i = 0; i < wxArticleBeans.size(); i++) {
            res.add(wxArticleBeans.get(i).getContent());
        }
        return JSON.toJSONString(res);
    }
}
