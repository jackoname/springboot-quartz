/**
 * @Author：ouyhjian
 * @Package：com.nainuo.Beans
 * @Project：springboot-quartz
 * @name：syncConfigBean
 * @Date：2024/4/6 15:39
 * @Filename：syncConfigBean
 */
package com.nainuo.Beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tbi_wx_article")
public class wxArticleBean {
    @TableId(value = "id", type = IdType.AUTO)
    int id;
    int type;
    Date createTime;
    String content;
    String remark;
    String appId;
    String openId;

}
