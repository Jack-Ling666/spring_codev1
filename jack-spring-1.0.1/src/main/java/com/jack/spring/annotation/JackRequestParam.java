package com.jack.spring.annotation;

import java.lang.annotation.*;

/**
 * Created by Jack
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JackRequestParam {
    String value() default  "";
}
