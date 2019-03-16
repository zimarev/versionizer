package com.github.zimarev.versionizer.configuration;

import com.github.zimarev.versionizer.strategy.PathPrependingVersionizerStrategy;
import lombok.Data;

@Data
public class VersionizerConfigurationBean implements VersionizerConfiguration {

    private double defaultVersion = 1.0;

    private PathPrependingVersionizerStrategy strategy = new PathPrependingVersionizerStrategy(this);
}
