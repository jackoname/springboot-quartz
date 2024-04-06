package com.nainuo.VO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
    private static  Logger log = null;
    public static void error(String msg, Object obj) {
        try {
            log =  LoggerFactory.getLogger(obj.getClass());
            log.error("{}",msg);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void debug(String msg, Object obj) {
        try {
            log =  LoggerFactory.getLogger(obj.getClass());
            log.debug("{}",msg);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void info(String msg, String cla) {
        try {
            log =  LoggerFactory.getLogger(cla);
            log.info("{}",msg);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void warn(String msg, Object obj) {
        try {
            log =  LoggerFactory.getLogger(obj.getClass());
            log.warn("{}",msg);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}

