package io.lana.sqlstarter.validation;

import io.lana.sqlstarter.validation.Validator.ValidationRule;

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
}
