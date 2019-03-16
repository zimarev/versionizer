package com.github.zimarev.versionizer.configuration;

import com.github.zimarev.versionizer.strategy.VersionizerStrategy;

public interface VersionizerConfiguration {
    double getDefaultVersion();

    VersionizerStrategy getStrategy();
}
