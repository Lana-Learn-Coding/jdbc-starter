package io.lana.sqlstarter.menu.command;

import io.lana.sqlstarter.menu.MenuCommandExecutor;

import java.util.function.Supplier;

public class DynamicDescriptionCommand extends Command {
    private final Supplier<String> description;

    DynamicDescriptionCommand(String activator, Supplier<String> descriptionGenerator, MenuCommandExecutor executor) {
        super(activator, descriptionGenerator.get(), executor);
        description = descriptionGenerator;
    }

    @Override
    public String getDescription() {
        return description.get();
    }
}
