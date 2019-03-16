package com.github.zimarev.versionizer.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@ComponentScan(basePackages = "com.github.zimarev.versionizer")
public class VersionedApiConfiguration extends WebMvcConfigurationSupport {

    @Override
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new VersionedRequestMappingHandlerMapping(versionizerConfiguration());
    }

    // fixme: extract somewhere
    @Bean
    @ConditionalOnMissingBean(VersionizerConfiguration.class)
    public VersionizerConfiguration versionizerConfiguration() {
        return new VersionizerConfigurationBean();
    }
}
