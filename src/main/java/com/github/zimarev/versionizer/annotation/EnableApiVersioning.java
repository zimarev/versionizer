package com.github.zimarev.versionizer.annotation;


import com.github.zimarev.versionizer.configuration.VersionedApiConfiguration;
import com.github.zimarev.versionizer.strategy.PathPrependingVersionizerStrategy;
import com.github.zimarev.versionizer.strategy.VersionizerStrategy;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(VersionedApiConfiguration.class)
public @interface EnableApiVersioning {
    double defaultVersion() default 1.0;

    Class<? extends VersionizerStrategy> strategy() default PathPrependingVersionizerStrategy.class;
}
