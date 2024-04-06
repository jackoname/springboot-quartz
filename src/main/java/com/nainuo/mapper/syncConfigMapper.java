/**
 * @Author：ouyhjian
 * @Package：com.nainuo.mapper
 * @Project：springboot-quartz
 * @name：syncConfigMapper
 * @Date：2024/4/6 15:41
 * @Filename：syncConfigMapper
 */
package com.nainuo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nainuo.Beans.syncConfigBean;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface syncConfigMapper extends BaseMapper<syncConfigBean> {
}
