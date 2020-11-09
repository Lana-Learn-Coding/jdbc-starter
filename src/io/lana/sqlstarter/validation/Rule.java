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

    public static ValidationRule notBlank() {
        return context -> {
            String input = context.getInput();
            if (input != null && !input.trim().isEmpty()) {
                return ValidationResult.succeed();
            }
            return ValidationResult.failed("Must not blank");
        };
    }

    public static ValidationRule notEmpty() {
        return context -> {
            String input = context.getInput();
            if (input != null && !input.isEmpty()) {
                return ValidationResult.succeed();
            }
            return ValidationResult.failed("Must not empty");
        };
    }

    public static ValidationRule min(Integer min) {
        return nullable(context -> {
            Integer input = context.getInputOfType(Integer.class, Integer::parseInt);
            return input >= min ? ValidationResult.succeed() : ValidationResult.failed("Must at least " + min);
        });
    }

    public static ValidationRule min(Double min) {
        return nullable(context -> {
            Double input = context.getInputOfType(Double.class, Double::parseDouble);
            return input >= min ? ValidationResult.succeed() : ValidationResult.failed("Must at least " + min);
        });
    }

    public static ValidationRule max(Integer max) {
        return nullable(context -> {
            Integer input = context.getInputOfType(Integer.class, Integer::parseInt);
            return input <= max ? ValidationResult.succeed() : ValidationResult.failed("Must at at most " + max);
        });
    }

    public static ValidationRule max(Double max) {
        return nullable(context -> {
            Double input = context.getInputOfType(Double.class, Double::parseDouble);
            return input <= max ? ValidationResult.succeed() : ValidationResult.failed("Must at at most " + max);
        });
    }

    public static ValidationRule doubleNum() {
        return nullable(context -> {
            try {
                Double input = Double.parseDouble(context.getInput());
                context.setParsedInputValue(input);
                return ValidationResult.succeed();
            } catch (Exception e) {
                return ValidationResult.failed("Bad double format");
            }
        });
    }

    public static ValidationRule integer() {
        return nullable(context -> {
            try {
                Integer input = Integer.parseInt(context.getInput());
                context.setParsedInputValue(input);
                return ValidationResult.succeed();
            } catch (Exception e) {
                return ValidationResult.failed("Bad integer format");
            }
        });
    }

    public static ValidationRule bool() {
        return nullable(context -> {
            String input = context.getInput().trim();
            if (input.equals("true") || input.equals("false")) {
                return ValidationResult.succeed();
            }
            return ValidationResult.failed("Must be true/false");
        });
    }

    public static <T> ValidationRule unique(BaseDAO<?> dao, String col, Class<T> type) {
        return nullable(context -> {
            T input = context.getInputOfType(type, s -> valueOf(s, type));
            return dao.exist(col + " =?", input)
                    ? ValidationResult.failed("The " + col + " has already taken")
                    : ValidationResult.succeed();
        });
    }

    public static <T> ValidationRule exist(BaseDAO<?> dao, String col, Class<T> type) {
        return nullable(context -> {
            T input = context.getInputOfType(type, s -> valueOf(s, type));
            return dao.exist(col + " =?", input)
                    ? ValidationResult.succeed()
                    : ValidationResult.failed("The " + col + " is not exist");
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
