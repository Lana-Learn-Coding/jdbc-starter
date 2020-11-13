package io.lana.sqlstarter.menu.command;

import io.lana.sqlstarter.menu.MenuCommand;
import io.lana.sqlstarter.menu.MenuCommandExecutor;

import java.util.function.Supplier;

public class Command implements MenuCommand {
    private final MenuCommandExecutor executor;

    private final String activator;

    private final String description;

    protected Command(String activator, String description, MenuCommandExecutor executor) {
        this.executor = executor;
        this.activator = activator;
        this.description = description;
    }

    public static Command of(String activator, String description, MenuCommandExecutor executor) {
        return new Command(activator, description, executor);
    }

    public static Command of(String activator, Supplier<String> descriptionGenerator, MenuCommandExecutor executor) {
        return new DynamicDescriptionCommand(activator, descriptionGenerator, executor);
    }


    public static Command of(String activator, MenuCommandExecutor executor) {
        return new Command(activator, "No description", executor);
    }

    @Override
    public String getActivator() {
        return this.activator;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public MenuCommandExecutor getExecutor() {
        return this.executor;
    }
}
