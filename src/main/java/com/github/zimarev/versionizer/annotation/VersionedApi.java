package com.github.zimarev.versionizer.annotation;


import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface VersionedApi {
    double version() default 1.0;
}
