package com.nainuo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @Date：2024/4/6
 * @Author：ouyhjian
 * @return：
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuartzAddDto {

    private String quartzName;

    private String corn;

    private Map<String,Object> argument;
}
