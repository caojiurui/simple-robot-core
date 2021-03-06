/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  simple-robot-core
 * File     SimbotTODO.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 *
 */
package com.forte.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.CONSTRUCTOR;

/**
 *
 * 给开发者我自己看的。
 * 标注一些可能需要大改的地方.
 * 简单来说就是开发者的备忘录.
 *
 * 非运行时，不会被加载
 * Created by lcy on 2020/8/19.
 *
 * @author ForteScarlet
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, TYPE, METHOD, PARAMETER, PACKAGE, CONSTRUCTOR})
public @interface SimbotTODO {
	String[] value() default {};
}
