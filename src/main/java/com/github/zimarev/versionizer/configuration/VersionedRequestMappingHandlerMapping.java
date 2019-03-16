package com.github.zimarev.versionizer.configuration;

import com.github.zimarev.versionizer.annotation.VersionedApi;
import com.github.zimarev.versionizer.dto.VersionedMappingDataHolder;
import com.github.zimarev.versionizer.strategy.VersionizerStrategy;
import org.springframework.lang.Nullable;
import org.springframework.util.StringValueResolver;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

import static com.github.zimarev.versionizer.Utils.notNull;
import static java.util.Optional.ofNullable;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

public class VersionedRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    private static final String SCOPED_TARGET_NAME_PREFIX = "scopedTarget.";
    private static final ThreadLocal<Boolean> versioning = InheritableThreadLocal.withInitial(() -> true);
    private static final Map<RequestMappingInfo, Set<VersionedMappingDataHolder>> versionedMappingsMap = new WeakHashMap<>();

    private final VersionizerConfiguration configuration;
    private final VersionizerStrategy strategy;

    @Nullable
    private StringValueResolver embeddedValueResolver;

    public VersionedRequestMappingHandlerMapping(final VersionizerConfiguration configuration) {
        this.configuration = configuration;
        this.strategy = configuration.getStrategy();
    }

    @Override
    protected void initHandlerMethods() {
        for (String beanName : getCandidateBeanNames()) {
            if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX)) {
                processCandidateBean(beanName);
            }
        }
        initUnversionedMethods();
        handlerMethodsInitialized(getHandlerMethods());
    }

    protected void initUnversionedMethods() {
        versioning.set(false);
        for (String beanName : getCandidateBeanNames()) {
            if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX)) {
                processCandidateBean(beanName);
            }
        }

        versionedMappingsMap.forEach((mapping, infoSet) -> {
            final VersionedMappingDataHolder best = strategy.decideBestMapping(infoSet);
            registerHandlerMethod(best.getHandlerType(), best.getMethod(), mapping);
        });
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(final Method method, final Class<?> handlerType) {
        if (versioning.get()) {
            return processVersionedMapping(method, handlerType);
        } else {
            return processUnversionedMapping(method, handlerType);
        }
    }

    protected RequestMappingInfo getCommonRequestMappingInfo(final Class<?> handlerType, RequestMappingInfo info) {
        final RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
        if (typeInfo != null) {
            info = strategy.combineVersionizedMapping(info, typeInfo);
        }
        final String prefix = getPathPrefix(handlerType);
        if (prefix != null) {
            info = strategy.combineVersionizedMapping(info, RequestMappingInfo.paths(prefix).build());
        }
        return info;
    }

    protected RequestMappingInfo processUnversionedMapping(final Method method, final Class<?> handlerType) {
        final VersionedApi annotation = getVersionedAnnotation(handlerType, method);
        final RequestMappingInfo info = createRequestMappingInfo(method);
        if (annotation != null && info != null) {
            final RequestMappingInfo mapping = getCommonRequestMappingInfo(handlerType, info);
            versionedMappingsMap.computeIfAbsent(mapping, k -> new HashSet<>())
                    .add(new VersionedMappingDataHolder(getVersion(method, handlerType), method, handlerType));
        }
        // return null anyway, so to process everything later
        return null;
    }

    protected VersionedApi getVersionedAnnotation(final Class<?> handlerType, final Method method) {
        return Optional.ofNullable(findMergedAnnotation(method, VersionedApi.class))
                .orElseGet(() -> findMergedAnnotation(handlerType, VersionedApi.class));
    }

    protected RequestMappingInfo processVersionedMapping(final Method method, final Class<?> handlerType) {
        RequestMappingInfo info = createRequestMappingInfo(method);
        if (info != null) {
            info = getCommonRequestMappingInfo(handlerType, info);
            final RequestMappingInfo versionedMapping = strategy.getVersionedMapping(method, handlerType);
            if (versionedMapping != null) {
                info = strategy.combineVersionizedMapping(info, versionedMapping);
            }
        }
        return info;
    }

    protected double getVersion(final Method method, final Class<?> handler) {
        return getVersion(
                findMergedAnnotation(handler, VersionedApi.class),
                findMergedAnnotation(method, VersionedApi.class)
        );
    }

    protected double getVersion(final VersionedApi handler, final VersionedApi method) {
        return notNull(configuration.getDefaultVersion(),
                ofNullable(method).map(VersionedApi::version).orElse(null),
                ofNullable(handler).map(VersionedApi::version).orElse(null)
        );
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
        final RequestMapping requestMapping = findMergedAnnotation(element, RequestMapping.class);
        final RequestCondition<?> condition = (element instanceof Class ?
                getCustomTypeCondition((Class<?>) element) : getCustomMethodCondition((Method) element));
        return requestMapping != null ? createRequestMappingInfo(requestMapping, condition) : null;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        super.setEmbeddedValueResolver(resolver);
        this.embeddedValueResolver = resolver;
    }
}
