package io.lana.sqlstarter.validation;

import io.lana.sqlstarter.validation.Validator.ValidationResult;
import io.lana.sqlstarter.validation.Validator.ValidationRule;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class Input {
    protected final Supplier<String> inputSupplier;

    private Input(Supplier<String> inputSupplier) {
        this.inputSupplier = inputSupplier;
    }

    public static Input using(Supplier<String> inputSupplier) {
        return new Input(inputSupplier);
    }

    public OnceValidationInput once(ValidationRule... rules) {
        return new OnceValidationInput(inputSupplier, rules);
    }

    public LoopingValidationInput until(ValidationRule... rules) {
        return new LoopingValidationInput(inputSupplier, rules);
    }

    public String get() {
        return inputSupplier.get();
    }

    public <T> T getAs(Class<T> clazz) {
        String input = get();
        if (input == null) {
            return null;
        }
        try {
            Method valueOf = clazz.getMethod("valueOf", String.class);
            return (T) valueOf.invoke(clazz, input);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    public <T> T get(Function<String, T> transformer) {
        return transformer.apply(get());
    }

    public static class LoopingValidationInput extends Input {
        private final Validator validator;

        private LoopingValidationInput(Supplier<String> inputSupplier, ValidationRule... rules) {
            super(inputSupplier);
            validator = Validator.of(rules);
        }

        public String get() {
            String input = inputSupplier.get();
            ValidationResult validationResult = validator.validate(input);
            if (validationResult.isFailed()) {
                System.out.println(validationResult.getFailedMessage());
                return get();
            }
            return input;
        }
    }

    public static class OnceValidationInput extends Input {
        private final Validator validator;

        private OnceValidationInput(Supplier<String> inputSupplier, ValidationRule... rules) {
            super(inputSupplier);
            validator = Validator.of(rules);
        }

        @Override
        public String get() {
            String input = inputSupplier.get();
            ValidationResult validationResult = validator.validate(input);
            if (validationResult.isFailed()) {
                System.out.println(validationResult.getFailedMessage());
                return null;
            }
            return input;
        }

        public Optional<String> getOptional() {
            return Optional.ofNullable(get());
        }

        public <T> Optional<T> getOptional(Function<String, T> transformer) {
            return getOptional().map(transformer);
        }

        public <T> Optional<T> getAsOptional(Class<T> clazz) {
            return Optional.ofNullable(getAs(clazz));
        }
    }
}
