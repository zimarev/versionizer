package com.github.zimarev.versionizer.dto;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class VersionedMappingDataHolder {
    private final double version;
    private final Method method;
    private final Class<?> handlerType;
}
