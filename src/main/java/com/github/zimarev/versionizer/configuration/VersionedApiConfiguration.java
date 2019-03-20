package com.github.zimarev.versionizer.configuration;

import com.github.zimarev.versionizer.annotation.EnableApiVersioning;
import com.github.zimarev.versionizer.strategy.VersionizerStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Configuration
@ComponentScan(basePackages = "com.github.zimarev.versionizer")
public class VersionedApiConfiguration extends WebMvcConfigurationSupport {

    @Override
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new VersionedRequestMappingHandlerMapping(versionizerConfiguration());
    }

    @Bean
    @ConditionalOnMissingBean(VersionizerConfiguration.class)
    public VersionizerConfiguration versionizerConfiguration() {
        final VersionizerConfigurationBean configuration = new VersionizerConfigurationBean();
        Optional.ofNullable(getApplicationContext())
                .map(ctx -> ctx.getBeansWithAnnotation(EnableApiVersioning.class))
                .map(Map::entrySet)
                .map(Set::stream)
                .orElseGet(Stream::empty).findFirst()
                .map(Map.Entry::getValue)
                .map(bean -> AnnotationUtils.findAnnotation(bean.getClass(), EnableApiVersioning.class))
                .ifPresent(annotation -> {
                    configuration.setDefaultVersion(getDefaultVersion(annotation));
                    configuration.setStrategy(getVersionizerStrategy(annotation));
                });
        return configuration;
    }

    protected double getDefaultVersion(final EnableApiVersioning annotation) {
        final double version = annotation.defaultVersion();
        if (version < 0.0) {
            throw new IllegalArgumentException("API version cannot be less than 0");
        }
        return version;
    }

    protected VersionizerStrategy getVersionizerStrategy(EnableApiVersioning annotation) {
        try {
            final VersionizerStrategy strategy = annotation.strategy().newInstance();
            strategy.setDefaultVersion(getDefaultVersion(annotation));
            return strategy;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Cannot create versionizer strategy from " + annotation.strategy());
        }
    }
}
