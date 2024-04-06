/**
 * @Author：ouyhjian
 * @Package：com.nainuo.mapper
 * @Project：springboot-quartz
 * @name：orderMapper
 * @Date：2024/4/6 10:50
 * @Filename：orderMapper
 */
package com.nainuo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nainuo.Beans.orderBean;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface orderMapper extends BaseMapper<orderBean> {
}
