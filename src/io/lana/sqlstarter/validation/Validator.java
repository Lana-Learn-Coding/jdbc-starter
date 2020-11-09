package io.lana.sqlstarter.validation;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class Validator {
    public static class ValidationContext {
        private final String input;

        private final Map<Class<?>, Object> parsedInput = new HashMap<>(3);

        private ValidationContext(String input) {
            this.input = input;
        }

        public static ValidationContext of(String input) {
            return new ValidationContext(input);
        }

        public static ValidationContext from(Supplier<String> inputSupplier) {
            return new ValidationContext(inputSupplier.get());
        }

        public <T> T getInputOfType(Class<T> clazz, Function<String, T> parser) {
            return getInputOfType(clazz, parser, "Bad format, cannot parse");
        }

        public <T> T getInputOfType(Class<T> clazz, Function<String, T> parser, String messageOnFail) {
            Object value = parsedInput.get(clazz);
            if (value == null) {
                try {
                    value = parser.apply(input);
                } catch (Exception e) {
                    throw new IllegalArgumentException(messageOnFail, e);
                }
                setParsedInputValue(value);
            }
            return (T) value;
        }

        public <T> T getInputOfType(Class<T> clazz, Supplier<T> orElse) {
            Object value = parsedInput.get(clazz);
            if (value == null) {
                value = orElse.get();
                setParsedInputValue(value);
            }
            return (T) value;
        }

        public <T> void setParsedInputValue(T value) {
            parsedInput.putIfAbsent(value.getClass(), value);
        }

        public String getInput() {
            return input;
        }
    }

    public interface ValidationRule {
        ValidationResult validate(ValidationContext context);
    }

    public static class ValidationResult {
        private static final ValidationResult SUCCESS_RESULT = new ValidationResult();

        private String message;

        private ValidationResult() {
        }

        public static ValidationResult succeed() {
            return SUCCESS_RESULT;
        }

        public static ValidationResult failed(String message) {
            ValidationResult validationResult = new ValidationResult();
            validationResult.message = message != null ? message : "Validation Failed";
            return validationResult;
        }

        public boolean isFailed() {
            return message != null;
        }

        public String getFailedMessage() {
            if (isFailed()) {
                return message;
            }
            return null;
        }
    }

    private final List<ValidationRule> rules;

    private Validator(List<ValidationRule> rules) {
        this.rules = rules;
    }

    public static Validator of(ValidationRule... rules) {
        return new Validator(Arrays.asList(rules));
    }

    public Validator withRules(ValidationRule... rules) {
        List<ValidationRule> ruleList = Arrays.asList(rules);
        ruleList.addAll(this.rules);
        return new Validator(ruleList);
    }

    public ValidationResult validate(String string) {
        return validate(ValidationContext.of(string));
    }

    public ValidationResult validate(Supplier<String> inputSupplier) {
        return validate(ValidationContext.from(inputSupplier));
    }

    public ValidationResult validate(ValidationContext validationContext) {
        for (ValidationRule rule : rules) {
            try {
                ValidationResult result = rule.validate(validationContext);
                if (result.isFailed()) {
                    return result;
                }
            } catch (IllegalArgumentException e) {
                return ValidationResult.failed(e.getMessage());
            }
        }
        return ValidationResult.succeed();
    }
}
