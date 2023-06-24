package com.hablutzel.spwing.annotations;

import java.lang.annotation.*;



@Documented
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SVWFComponentFactory {

    String componentMethod() default "addSVWFComponents";
}
