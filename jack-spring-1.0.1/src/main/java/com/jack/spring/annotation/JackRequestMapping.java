package com.jack.spring.annotation;

import java.lang.annotation.*;

/**
 * Created by Jack
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JackRequestMapping {
    String value() default  "";

}
