package com.jack.spring.annotation;

import java.lang.annotation.*;

/**
 * Created by Jack
 */
// @Target 这个注解可能在哪里出现TYPE、FIELD、METHOD。。。
@Target({ElementType.FIELD})
// @Retention  保留多久  SOURCE < CLASS < RUNTIME
/**
 * source：注解只保留在源文件，当Java文件编译成class文件的时候，注解被遗弃；被编译器忽略
 * class：注解被保留到class文件，但jvm加载class文件时候被遗弃，这是默认的生命周期
 * runtime：注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在
 * 一般如果需要在运行时去动态获取注解信息，那只能用 RUNTIME 注解；
 * 如果要在编译时进行一些预处理操作，比如生成一些辅助代码（如 ButterKnife），就用 CLASS注解；
 * 如果只是做一些检查性的操作，比如 @Override 和 @SuppressWarnings，则可选用 SOURCE 注解。
 */
@Retention(RetentionPolicy.RUNTIME)
//生成javadoc的时候就会把@Documented注解给显示出来。
@Documented
public @interface JackAutowired {
    String value() default  "";
}
