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
@TableName(value = "tci_comm_config")
public class commConfigBean {
    @TableId(value = "id", type = IdType.AUTO)
    int id;//自增id
    int flag;//
    int configNum;//
    String appId;//gzhid
    String configStr;//
   // String pId;
    String shopType;//
    String remark;
    Date configTime1;//
    Date configTime2;//


}

