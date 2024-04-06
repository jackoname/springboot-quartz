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

}
