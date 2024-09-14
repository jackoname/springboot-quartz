package com.nainuo.VO;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class stringUtils {
    public static String extractShortUrl(String input,String stastr,String  endstr) {
        int startIndex = input.indexOf(stastr);
        if (startIndex >= 0) {
            startIndex += stastr.length();
            int endIndex = input.indexOf(endstr, startIndex);
            if (endIndex > startIndex) {
                return input.substring(startIndex, endIndex).trim();
            }
        }
        return "没有找到或者格式不正确";
    }
    public static String toStrMapJson (String input) {
        Map<String,String> parms= new HashMap<>();
        if(input.contains("#&#")){
        String[] mapstr = input.split("#&#");
      //  System.out.println(mapstr[0]+"----  ****----"+mapstr[1]);

        for (String s : mapstr) {
            String[] split = s.split("#-#");
            parms.put(split[0],split[1]);
        }
      //  JSONObject json = new JSONObject(parms);
        Log.info(JSON.toJSONString(parms),"");
        }
        else {
            String[] split = input.split("#-#");
            parms.put(split[0],split[1]);
        }
      //  System.out.println(JSON.toJSONString(parms));
        return JSON.toJSONString(parms);
    }

    public static String str2JsonObj (String jsonStr) {


        // 将JSON字符串转换为JSONObject
        JSONObject jsonObject = JSON.parseObject(jsonStr);

        // 获取data数组
        JSONArray jsonArray = jsonObject.getJSONArray("data");

        // 遍历数组并取值
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            System.out.println(item.getString("name"));
        }
    	return null;
    }

    public static String str2short (String str,int len,int sublen) {
    	if(str.length() > len)
    	return str.substring(0, str.length() - sublen);
    	else return null;
    }

}
