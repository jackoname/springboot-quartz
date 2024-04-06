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

@Data
@TableName("sync_config")
public class syncConfigBean {
    @TableId(value = "id", type = IdType.AUTO)
    int id;
    int appType;
    int minute;
    int hour;

}
