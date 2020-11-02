package io.lana.sqlstarter.utils;

import java.util.Scanner;
import java.util.function.Function;

public class ValidationUtils {
    public static String enforceInput(Scanner sc, Function<String, String> validator) {
        String input = sc.nextLine();
        String error = validator.apply(input);
        if (error == null) {
            return input;
        }
        System.out.println("Invalid: " + error + ", enter again:");
        return enforceInput(sc, validator);
    }

    public static Integer enforceInteger(Scanner sc) {
        return enforceInteger(sc, false);
    }

    public static Integer enforceInteger(Scanner sc, boolean nullable) {
        String input = sc.nextLine();
        if (nullable && input.equals("null")) {
            return null;
        }
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
            System.out.println("Bad integer format, enter again:");
            return enforceInteger(sc, nullable);
        }
    }

    public static Boolean enforceBoolean(Scanner sc) {
        String input = enforceInput(sc, bool -> bool.equals("true") || bool.equals("false") ? null : "Bad boolean format");
        return input.equals("true");
    }

    public static String enforceNotEmpty(Scanner sc) {
        return enforceInput(sc, (name) -> name.isEmpty() ? "name must not empty" : null);
    }
}
