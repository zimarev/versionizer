package com.github.zimarev.versionizer.strategy;

import com.github.zimarev.versionizer.annotation.VersionedApi;
import com.github.zimarev.versionizer.configuration.VersionizerConfiguration;
import com.github.zimarev.versionizer.dto.VersionedApiConfigHolder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.github.zimarev.versionizer.Utils.notNull;
import static java.util.Optional.ofNullable;

@Getter
@RequiredArgsConstructor
public abstract class AbstractConfigurableVersionizerStrategy implements VersionizerStrategy {
    private final VersionizerConfiguration configuration;

    protected VersionedApiConfigHolder getMergedConfiguration(final VersionedApi cl, final VersionedApi mth) {
        final VersionedApiConfigHolder config = new VersionedApiConfigHolder();
        config.setVersion(getVersion(mth, cl));
        return config;
    }

    protected double getVersion(final VersionedApi handler, final VersionedApi method) {
        return notNull(getConfiguration().getDefaultVersion(),
                ofNullable(method).map(VersionedApi::version).orElse(null),
                ofNullable(handler).map(VersionedApi::version).orElse(null)
        );
    }
}
