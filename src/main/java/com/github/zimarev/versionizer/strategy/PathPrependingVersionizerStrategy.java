package com.github.zimarev.versionizer.strategy;

import com.github.zimarev.versionizer.annotation.VersionedApi;
import com.github.zimarev.versionizer.configuration.VersionizerConfiguration;
import com.github.zimarev.versionizer.dto.VersionedApiConfigHolder;
import com.github.zimarev.versionizer.dto.VersionedMappingDataHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Set;

import static com.github.zimarev.versionizer.Utils.notNull;
import static java.util.Optional.ofNullable;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

@RequiredArgsConstructor
public class PathPrependingVersionizerStrategy implements VersionizerStrategy {
    private final VersionizerConfiguration configuration;

    @Override
    public VersionedMappingDataHolder decideBestMapping(final Set<VersionedMappingDataHolder> infoSet) {
        return infoSet.stream().sorted(Comparator.comparing(VersionedMappingDataHolder::getVersion).reversed())
                .filter(holder -> holder.getVersion() > 0) // todo: move to validation phase
                .findFirst().orElse(null);
    }

    @Override
    public RequestMappingInfo getVersionedMapping(final Method method, final Class<?> handlerType) {
        final VersionedApiConfigHolder configuration = getMergedConfiguration(
                findMergedAnnotation(handlerType, VersionedApi.class),
                findMergedAnnotation(method, VersionedApi.class)
        );
        return buildMappingInfo(configuration);
    }

    protected VersionedApiConfigHolder getMergedConfiguration(final VersionedApi cl, final VersionedApi mth) {
        final VersionedApiConfigHolder config = new VersionedApiConfigHolder();
        config.setVersion(getVersion(mth, cl));
        return config;
    }

    protected double getVersion(final VersionedApi handler, final VersionedApi method) {
        return notNull(configuration.getDefaultVersion(),
                ofNullable(method).map(VersionedApi::version).orElse(null),
                ofNullable(handler).map(VersionedApi::version).orElse(null)
        );
    }

    protected RequestMappingInfo buildMappingInfo(final VersionedApiConfigHolder config) {
        return RequestMappingInfo
                .paths(config.getPathVersion())
                .build();
    }
}
