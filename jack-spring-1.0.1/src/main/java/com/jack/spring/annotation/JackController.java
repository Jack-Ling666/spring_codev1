package com.jack.spring.annotation;

import java.lang.annotation.*;

/**
 * Created by Jack
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JackController {
    String value() default "";
}
//@Target({ElementType.TYPE})
//@Retention(RetentionPolicy.RUNTIME)
//@Documented
//public @interface JackController {
//
//    String value() default  "";
//}
