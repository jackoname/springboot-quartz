/**
 * @Author：ouyhjian
 * @Package：com.nainuo.wx.mp.myBusiness.horeBusiness.horeBeans
 * @Project：wx-mp
 * @name：wordConfigBean
 * @Date：2024/4/12 23:46
 * @Filename：wordConfigBean
 */
package com.nainuo.Beans.vistualBean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tci_msg_config")
public class msgConfigBean {
    @TableId(value = "id", type = IdType.AUTO)
    int id;
    int msgType;
    String appId;
    String msg;
    String msgDes;

}
