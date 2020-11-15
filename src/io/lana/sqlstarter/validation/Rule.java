package io.lana.sqlstarter.validation;

import io.lana.sqlstarter.dao.BaseDAO;
import io.lana.sqlstarter.validation.Validator.ValidationRule;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static io.lana.sqlstarter.validation.Validator.*;

public class Rule {
    private Rule() {
    }

    public static ValidationRule nullable(ValidationRule rule) {
        return context -> context.getInput() != null ? rule.validate(context) : ValidationResult.succeed();
    }

    public static ValidationRule notEmpty(String msg) {
        return context -> {
            String input = context.getInput();
            if (input != null && !input.isEmpty()) {
                return ValidationResult.succeed();
            }
            return ValidationResult.failed(msg);
        };
    }

    public static ValidationRule min(String msg, Integer min) {
        return nullable(context -> {
            Integer input = context.getInputOfType(Integer.class, Integer::parseInt);
            return input >= min ? ValidationResult.succeed() : ValidationResult.failed(msg + " " + min);
        });
    }

    public static ValidationRule min(String msg, Double min) {
        return nullable(context -> {
            Double input = context.getInputOfType(Double.class, Double::parseDouble);
            return input >= min ? ValidationResult.succeed() : ValidationResult.failed(msg + " " + min);
        });
    }

    public static ValidationRule doubleNum(String msg) {
        return nullable(context -> {
            try {
                Double input = Double.parseDouble(context.getInput());
                context.setParsedInputValue(input);
                return ValidationResult.succeed();
            } catch (Exception e) {
                return ValidationResult.failed(msg);
            }
        });
    }

    public static ValidationRule integer(String msg) {
        return nullable(context -> {
            try {
                Integer input = Integer.parseInt(context.getInput());
                context.setParsedInputValue(input);
                return ValidationResult.succeed();
            } catch (Exception e) {
                return ValidationResult.failed(msg);
            }
        });
    }

    public static ValidationRule bool(String msg) {
        return nullable(context -> {
            String input = context.getInput().trim();
            if (input.equals("true") || input.equals("false")) {
                return ValidationResult.succeed();
            }
            return ValidationResult.failed(msg);
        });
    }

    public static <T> ValidationRule unique(String msg, BaseDAO<?> dao, String col, Class<T> type) {
        return nullable(context -> {
            T input = context.getInputOfType(type, s -> valueOf(s, type));
            return dao.exist(col + " =?", input)
                ? ValidationResult.failed(msg)
                : ValidationResult.succeed();
        });
    }

    public static <T> ValidationRule exist(String msg, BaseDAO<?> dao, String col, Class<T> type) {
        return nullable(context -> {
            T input = context.getInputOfType(type, s -> valueOf(s, type));
            return dao.exist(col + " =?", input)
                ? ValidationResult.succeed()
                : ValidationResult.failed(msg);
        });
    }

    private static <T> T valueOf(String s, Class<T> clazz) {
        try {
            Method valueOf = clazz.getMethod("valueOf", String.class);
            return (T) valueOf.invoke(clazz, s);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }
}
