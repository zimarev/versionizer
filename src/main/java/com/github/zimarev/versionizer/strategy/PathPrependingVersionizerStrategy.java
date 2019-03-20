package com.github.zimarev.versionizer.strategy;

import com.github.zimarev.versionizer.annotation.VersionedApi;
import com.github.zimarev.versionizer.dto.VersionedApiConfigHolder;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

public class PathPrependingVersionizerStrategy extends AbstractConfigurableVersionizerStrategy {

    @Override
    public RequestMappingInfo getVersionedMapping(final Method method, final Class<?> handlerType) {
        final VersionedApiConfigHolder configuration = getMergedConfiguration(
                findMergedAnnotation(handlerType, VersionedApi.class),
                findMergedAnnotation(method, VersionedApi.class)
        );
        return RequestMappingInfo
                .paths(configuration.getPathVersion())
                .build();
    }
}
