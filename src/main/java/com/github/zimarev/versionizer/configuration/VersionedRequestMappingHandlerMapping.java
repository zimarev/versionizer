package com.github.zimarev.versionizer.configuration;

import com.github.zimarev.versionizer.annotation.VersionedApi;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringValueResolver;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Predicate;

import static com.github.zimarev.versionizer.Utils.notNull;
import static java.util.Optional.ofNullable;

public class VersionedRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    @Nullable
    private StringValueResolver embeddedValueResolver;

    @Override
    protected RequestMappingInfo getMappingForMethod(final Method method, final Class<?> handlerType) {
        RequestMappingInfo info = createRequestMappingInfo(method);
        if (info != null) {
            RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
            if (typeInfo != null) {
                info = typeInfo.combine(info);
            }
            String prefix = getPathPrefix(handlerType);
            if (prefix != null) {
                info = RequestMappingInfo.paths(prefix).build().combine(info);
            }
            final RequestMappingInfo finalInfo = info;
            final RequestMappingInfo versionedMapping = getVersionedMapping(method, handlerType);
            return ofNullable(versionedMapping)
                    .map(mapping -> mapping.combine(finalInfo))
                    .orElse(finalInfo);
        }
        return null;
    }

    // todo: move to strategy
    protected RequestMappingInfo getVersionedMapping(final Method method, final Class<?> handlerType) {
        final VersionedApiConfigHolder configuration = getMergedConfiguration(
                AnnotatedElementUtils.findMergedAnnotation(handlerType, VersionedApi.class),
                AnnotatedElementUtils.findMergedAnnotation(method, VersionedApi.class)
        );
        return buildMappingInfo(configuration);
    }

    protected RequestMappingInfo buildMappingInfo(final VersionedApiConfigHolder config) {
        return RequestMappingInfo
                .paths(config.getPathVersion())
                .build();
    }

    protected VersionedApiConfigHolder getMergedConfiguration(final VersionedApi cl, final VersionedApi mth) {
        final VersionedApiConfigHolder config = new VersionedApiConfigHolder();
        config.setVersion(notNull(1.0,
                ofNullable(cl).map(VersionedApi::version).orElse(null),
                ofNullable(mth).map(VersionedApi::version).orElse(null)
        ));
        return config;
    }

    protected String getPathPrefix(Class<?> handlerType) {
        for (Map.Entry<String, Predicate<Class<?>>> entry : getPathPrefixes().entrySet()) {
            if (entry.getValue().test(handlerType)) {
                String prefix = entry.getKey();
                if (this.embeddedValueResolver != null) {
                    prefix = this.embeddedValueResolver.resolveStringValue(prefix);
                }
                return prefix;
            }
        }
        return null;
    }

    protected RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        RequestCondition<?> condition = (element instanceof Class ?
                getCustomTypeCondition((Class<?>) element) : getCustomMethodCondition((Method) element));
        return (requestMapping != null ? createRequestMappingInfo(requestMapping, condition) : null);
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        super.setEmbeddedValueResolver(resolver);
        this.embeddedValueResolver = resolver;
    }
}
