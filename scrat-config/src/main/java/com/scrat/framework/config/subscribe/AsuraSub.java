package com.scrat.framework.config.subscribe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述：注册常量
 * 作者 ：kangzz
 * 日期 ：2016-10-21 17:39:25
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AsuraSub {

}
