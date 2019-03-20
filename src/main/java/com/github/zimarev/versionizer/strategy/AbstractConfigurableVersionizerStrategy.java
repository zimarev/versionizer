package com.github.zimarev.versionizer.strategy;

import com.github.zimarev.versionizer.annotation.VersionedApi;
import com.github.zimarev.versionizer.dto.VersionedApiConfigHolder;
import com.github.zimarev.versionizer.dto.VersionedMappingDataHolder;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.Set;

import static com.github.zimarev.versionizer.Utils.notNull;
import static java.util.Optional.ofNullable;

@Getter
public abstract class AbstractConfigurableVersionizerStrategy implements VersionizerStrategy {
    @Setter
    private double defaultVersion;

    protected VersionedApiConfigHolder getMergedConfiguration(final VersionedApi cl, final VersionedApi mth) {
        final VersionedApiConfigHolder config = new VersionedApiConfigHolder();
        config.setVersion(getVersion(mth, cl));
        return config;
    }

    protected double getVersion(final VersionedApi handler, final VersionedApi method) {
        return notNull(getDefaultVersion(),
                ofNullable(method).map(VersionedApi::version).orElse(null),
                ofNullable(handler).map(VersionedApi::version).orElse(null)
        );
    }

    @Override
    public VersionedMappingDataHolder decideBestMapping(final Set<VersionedMappingDataHolder> infoSet) {
        return infoSet.stream().max(Comparator.comparing(VersionedMappingDataHolder::getVersion)).orElse(null);
    }
}
