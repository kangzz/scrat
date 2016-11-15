package com.scrat.framework.log.utils;

import org.slf4j.Logger;

/**
 * 描述：日志工具类
 * 作者 ：kangzz
 * 日期 ：2016-11-15 11:08:07
 */
public class LogUtil {

    /**
     * 工具类 私有化其构造，防止通过new创建对象
     * @from sonar
     */
    private LogUtil(){

    }

    /**
     * trace level
     * @param logger
     * @param s
     * @param objs
     */
    public static void trace(Logger logger,String s,Object... objs){
        if(logger.isTraceEnabled()){
            logger.trace(s, objs);
        }
    }
    /**
     * Debug level
     * @param logger
     * @param s
     * @param objs
     */
    public static void debug(Logger logger,String s,Object... objs){
        if(logger.isDebugEnabled()){
            logger.debug(s,objs);
        }
    }

    /**
     * info level
     * @param logger
     * @param s
     * @param objs
     */
    public static void info(Logger logger,String s,Object... objs){
        if(logger.isInfoEnabled()){
            logger.info(s, objs);
        }
    }

    /**
     * warn level
     * @param logger
     * @param s
     * @param objs
     */
    public static void warn(Logger logger,String s,Object... objs){
        if(logger.isWarnEnabled()){
            logger.warn(s, objs);
        }
    }

    /**
     * error level
     * @param logger
     * @param s
     * @param objs
     */
    public static void error(Logger logger,String s,Object... objs){
        if(logger.isErrorEnabled()){
            logger.error(s,objs);
        }
    }
    
   
}
