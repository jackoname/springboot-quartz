/**
 * @Author：ouyhjian
 * @Package：com.nainuo.VO
 * @Project：springboot-quartz
 * @name：constant
 * @Date：2024/4/6 15:13
 * @Filename：constant
 */
package com.nainuo.VO;

public class constant {
    public  static String host ="http://localhost:8081" ;
    public  static  String pddUrl = host.concat("/nainuo/pdd/pddOrderOP/getPddOrder");

    public  static  String pddParm = "start_update_time#-#*#&#end_update_time#-#";



    //tb
    public  static  String tbUrl = host.concat("/nainuo/tb/orderOP/getTbOrder");

    public  static  String tbParm = "url#-#https://openapi.dataoke.com/api/tb-service/get-order-details#&#startTime#-#*#&#endTime#-#";
//jd
   public  static  String jdUrl = host.concat("/nainuo/jd/orderOP/syncJdOrder");

    public  static  String jdParm = "type#-#1#&#key#-#fb9d3a21926c0282984d049b580b0f1140dd31d0ec922b0f1d14f4926660a559802bbac09eb0e338#&#url#-#https://openapi.dataoke.com/api/dels/jd/order/get-official-order-list#&#startTime#-#*#&#endTime#-#";

   /* {
        "startTime": "2024-04-18 23:50:20",
            "endTime": "2024-04-19 24:00:08",
            "url": "https://openapiv2.dataoke.com/open-api/tiktok/order-list"
    }*/
   public  static  String dyUrl = host.concat("/nainuo/dy/orderOP/getDyOrder");

    public  static  String dyParm = "url#-#https://openapiv2.dataoke.com/open-api/tiktok/order-list#&#startTime#-#*#&#endTime#-#";



    public static  String tbLineParms = "{\n" +
            "    \"url\": \"https://openapi.dataoke.com/api/dels/spider/list-tip-off\",\n" +
            "    \"platform\": \"0\",\n" +
            "    \"cid\": \"\",\n" +
            "    \"play\": \"\",\n" +
            "    \"pageId\": \"1\",\n" +
            "    \"pageSize\": \"100\",\n" +
            "    \"version\": \"v5.0.0\"\n" +
            "}";
    public static  String jdLineParms = "{\n" +
            "    \"url\": \"https://openapi.dataoke.com/api/dels/spider/list-tip-off\",\n" +
            "    \"platform\": \"1\",\n" +
            "    \"cid\": \"\",\n" +
            "    \"play\": \"\",\n" +
            "    \"pageId\": \"1\",\n" +
            "    \"pageSize\": \"100\",\n" +
            "    \"version\": \"v5.0.0\"\n" +
            "}";
    public static  String tb3_9Parms = " {\n" +
            " \"url\": \"https://openapi.dataoke.com/api/goods/nine/op-goods-list\",\n" +
            " \"platform\":\"TB3.9\",\n" +
            " \"nineCid\":\"1\",\n" +
            " \"pageId\":\"1\",\n" +
            " \"pageSize\":\"100\",\n" +
            " \"version\":\"v3.0.0\"\n" +
            " }";
    public static  String tb9_9Parms = " {\n" +
            " \"url\": \"https://openapi.dataoke.com/api/goods/nine/op-goods-list\",\n" +
            " \"platform\":\"TB9.9\",\n" +
            " \"nineCid\":\"2\",\n" +
            " \"pageId\":\"1\",\n" +
            " \"pageSize\":\"100\",\n" +
            " \"version\":\"v3.0.0\"\n" +
            " }";

    public static  String jd9_9Parms = "{\n" +
            "     \"url\": \"https://openapi.dataoke.com/api/dels/jd/column/list-nines\",\n" +
            "     \"platform\":\"JD9.9\",\n" +
            "     \"pageId\":\"1\",\n" +
            "     \"pageSize\":\"100\",\n" +
            "     \"version\":\"v1.0.0\",\n" +
            "     \"sort\":\"1\"\n" +
            " }";
    public static  String wxArtUrl =  host.concat("/nainuo/dtk/commOP/userMsg");
}
