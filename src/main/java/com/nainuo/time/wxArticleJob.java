package com.nainuo.time;

import com.nainuo.Beans.wxArticleBean;
import com.nainuo.VO.HttpUtils;
import com.nainuo.mapper.QuartzMapper;
import com.nainuo.mapper.wxArticleMapper;
import lombok.SneakyThrows;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.nainuo.VO.constant.*;


/**
 * @Date：2024/4/6
 * @Author：ouyhjian
 * @return：
 */
@Component
public class wxArticleJob implements Job {
    @Autowired
    wxArticleMapper wxArticleMapper;
    private static final Log logger = LogFactory.getLog(InvokeJob.class);


    @Override
    public void execute(JobExecutionContext context)  {
        wxArticleBean wxArticleBean = new wxArticleBean();
        String dec =null;
        String content = null;
        int type = 0;//1,2,3,4,5
        logger.info("------------------------淘宝线报启动-----------------------");
        dec ="淘宝线报";
        type =1;
        content = getArticle(tbLineParms);
        System.out.println(content);
        wxArticleBean.setRemark(dec);
        wxArticleBean.setContent(content);
        wxArticleBean.setType(type);
        wxArticleBean.setCreateTime(new Date());
        wxArticleMapper.insert(wxArticleBean);
        logger.info("------------------------京东线报启动-----------------------");
        wxArticleBean = new wxArticleBean();
        dec ="京东线报";
        type =2;
        content = getArticle(jdLineParms);
        System.out.println(content);
        wxArticleBean.setRemark(dec);
        wxArticleBean.setContent(content);
        wxArticleBean.setType(type);
        wxArticleBean.setCreateTime(new Date());
        wxArticleMapper.insert(wxArticleBean);
        logger.info("------------------------淘宝3.9启动-----------------------");
        wxArticleBean = new wxArticleBean();
        dec ="淘宝3.9";
        type =3;
        content = getArticle(tb3_9Parms);
        System.out.println(content);
        wxArticleBean.setRemark(dec);
        wxArticleBean.setContent(content);
        wxArticleBean.setType(type);
        wxArticleBean.setCreateTime(new Date());
        wxArticleMapper.insert(wxArticleBean);
        logger.info("------------------------淘宝9.9启动-----------------------");
        wxArticleBean = new wxArticleBean();
        dec ="淘宝9.9";
        type =4;
        content = getArticle(tb9_9Parms);
        System.out.println(content);
        wxArticleBean.setRemark(dec);
        wxArticleBean.setContent(content);
        wxArticleBean.setType(type);
        wxArticleBean.setCreateTime(new Date());
        wxArticleMapper.insert(wxArticleBean);
        logger.info("------------------------京东9.9启动-----------------------");
        wxArticleBean = new wxArticleBean();
        dec ="京东9.9";
        type =5;
        content = getArticle(jd9_9Parms);
        System.out.println(content);
        wxArticleBean.setRemark(dec);
        wxArticleBean.setContent(content);
        wxArticleBean.setType(type);
        wxArticleBean.setCreateTime(new Date());
        wxArticleMapper.insert(wxArticleBean);

    }

    @SneakyThrows
    public  String getArticle (String parm){
        return HttpUtils.sendHttpPostRequest(wxArtUrl,parm);
    }
}

