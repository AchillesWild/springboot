package com.achilles.wild.server.common.aop.log;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreField {

    String[] value() default {};
}
