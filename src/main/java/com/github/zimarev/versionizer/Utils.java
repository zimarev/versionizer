package com.github.zimarev.versionizer;

import lombok.experimental.UtilityClass;

import javax.validation.constraints.NotNull;

@UtilityClass
public class Utils {
    @SafeVarargs
    public static <T> T notNull(@NotNull T defaultValue, T... objects) {
        for (final T object : objects) {
            if (object != null) {
                return object;
            }
        }
        return defaultValue;
    }
}
