package io.lana.sqlstarter;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageBundle {
    private static Locale locale = new Locale("en", "US");

    private LanguageBundle() {
    }

    public static ResourceBundle getBundle(String path) {
        return ResourceBundle.getBundle(path, locale);
    }

    public static void setLocale(Locale locale) {
        LanguageBundle.locale = locale;
    }

    public static void setLocale(String lang, String country) {
        setLocale(new Locale(lang, country));
    }
}
