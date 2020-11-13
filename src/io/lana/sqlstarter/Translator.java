package io.lana.sqlstarter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Translator {
    public static String locale = "en_US";

    private static final String DEFAULT_LOCALE = "en_US";

    private final String bundle;

    private Map<String, Map<String, String>> langMap = new HashMap<>();

    private Translator(String bundle) {
        this.bundle = bundle;
        loadDefaultBundle();
    }

    public static Translator ofBundle(String bundle) {
        return new Translator(bundle);
    }

    public static void setLocale(String locale) {
        String[] localeStrings = locale.split("_");
        if (localeStrings.length != 2) {
            throw new IllegalArgumentException("Invalid locale string");
        }
        Translator.locale = locale;
    }

    public String translate(String string) {
        if (locale.equals(DEFAULT_LOCALE)) {
            return string;
        }
        if (!langMap.containsKey(locale)) {
            loadBundle(locale);
        }
        String langKey = langMap.get(DEFAULT_LOCALE).get(string);
        if (langKey == null) {
            throw new IllegalArgumentException("Key is missing: " + string);
        }
        String translated = langMap.get(locale).get(langKey);
        if (translated == null) {
            throw new IllegalArgumentException("Translated value is missing: " + string +
                ", key: " + langKey + ", locale: " + locale);
        }
        return translated;
    }

    private void loadBundle(String localeString) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, getLocaleFromString(localeString));
        Map<String, String> resourceLangMap = new HashMap<>(resourceBundle.keySet().size());
        resourceBundle.keySet().forEach(key -> {
            resourceLangMap.putIfAbsent(key, resourceBundle.getString(key));
        });
        langMap.putIfAbsent(localeString, resourceLangMap);
    }

    private void loadDefaultBundle() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, getLocaleFromString(DEFAULT_LOCALE));
        Map<String, String> defaultLangReverseMap = new HashMap<>(resourceBundle.keySet().size());
        resourceBundle.keySet().forEach(key -> {
            defaultLangReverseMap.putIfAbsent(resourceBundle.getString(key), key);
        });
        langMap.putIfAbsent(DEFAULT_LOCALE, defaultLangReverseMap);
    }

    private Locale getLocaleFromString(String localeString) {
        String[] localeStrings = localeString.split("_");
        return new Locale(localeStrings[0], localeStrings[1]);
    }
}
