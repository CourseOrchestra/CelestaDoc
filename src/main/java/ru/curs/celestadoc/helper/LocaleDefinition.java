package ru.curs.celestadoc.helper;

import java.util.Arrays;
import java.util.Locale;

public enum LocaleDefinition {
    DOCEN("doc-ru", Locale.ENGLISH),
    DOCRU("doc-en", new Locale("ru"));

    private Locale locale;
    private String value;

    LocaleDefinition(String value, Locale locale) {
        this.locale = locale;
        this.value = value;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getValue() {
        return value;
    }

    public static LocaleDefinition getLocaleDefinition(String lang) {
        return Arrays
                .stream(values())
                .filter(definition -> definition.getValue().equalsIgnoreCase(lang))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("There is not such locale for %s", lang)));
    }
}