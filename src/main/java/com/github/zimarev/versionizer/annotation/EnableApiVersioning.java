package com.github.zimarev.versionizer.annotation;


import com.github.zimarev.versionizer.configuration.VersionedApiConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(VersionedApiConfiguration.class)
public @interface EnableApiVersioning {
}
