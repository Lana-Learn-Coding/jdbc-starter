package io.lana.sqlstarter.repo;

public class CaseUtils {

    private CaseUtils() {
    }

    public static String toSnakeCase(String camelCase) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : camelCase.toCharArray()) {
            char nc = Character.toLowerCase(c);
            if (Character.isUpperCase(c)) {
                stringBuilder.append('_').append(nc);
            } else {
                stringBuilder.append(nc);
            }
        }
        return stringBuilder.toString();
    }
}
