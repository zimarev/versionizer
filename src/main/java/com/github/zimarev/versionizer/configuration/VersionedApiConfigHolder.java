package com.github.zimarev.versionizer.configuration;

import lombok.Data;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@Data
public class VersionedApiConfigHolder {
    private static final String versionPrefix = "v";
    private final DecimalFormatSymbols symbols;
    private final DecimalFormat decimalFormat;

    private double version;

    public VersionedApiConfigHolder() {
        symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        decimalFormat = new DecimalFormat("#0.0", symbols);
    }

    public String getPathVersion() {
        return versionPrefix + decimalFormat.format(getVersion());
    }
}
