package com.github.zimarev.versionizer.strategy;

import com.github.zimarev.versionizer.dto.VersionedMappingDataHolder;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;
import java.util.Set;

public interface VersionizerStrategy {

    default RequestMappingInfo combineVersionizedMapping(RequestMappingInfo path, RequestMappingInfo version) {
        return version.combine(path);
    }

    VersionedMappingDataHolder decideBestMapping(Set<VersionedMappingDataHolder> infoSet);

    RequestMappingInfo getVersionedMapping(Method method, Class<?> handlerType);

    double getDefaultVersion();

    void setDefaultVersion(double defaultVersion);
}
