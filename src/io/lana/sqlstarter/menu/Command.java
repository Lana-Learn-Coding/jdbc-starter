package io.lana.sqlstarter.menu;

public class Command implements MenuCommand {
    private final MenuCommandExecutor executor;

    private final String activator;

    private String description;

    private Command(String activator, String description, MenuCommandExecutor executor) {
        this.executor = executor;
        this.activator = activator;
        this.description = description;
    }

    public static Command of(String activator, String description, MenuCommandExecutor executor) {
        return new Command(activator, description, executor);
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
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public MenuCommandExecutor getExecutor() {
        return this.executor;
    }
}
