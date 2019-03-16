package com.github.zimarev.versionizer.strategy;

import com.github.zimarev.versionizer.annotation.VersionedApi;
import com.github.zimarev.versionizer.configuration.VersionizerConfiguration;
import com.github.zimarev.versionizer.dto.VersionedApiConfigHolder;
import com.github.zimarev.versionizer.dto.VersionedMappingDataHolder;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Set;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

public class HeaderFieldVersionizerStrategy extends AbstractConfigurableVersionizerStrategy {
    private static final String DEFAULT_HEADER_NAME = "X-API-VERSION";

    private final String HEADER_NAME;

    public HeaderFieldVersionizerStrategy(VersionizerConfiguration configuration) {
        this(configuration, DEFAULT_HEADER_NAME);
    }

    public HeaderFieldVersionizerStrategy(VersionizerConfiguration configuration, String headerName) {
        super(configuration);
        this.HEADER_NAME = headerName;
    }

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
        return RequestMappingInfo
                .paths()
                .headers(HEADER_NAME + "=" + configuration.getVersion())
                .build();
    }
}
