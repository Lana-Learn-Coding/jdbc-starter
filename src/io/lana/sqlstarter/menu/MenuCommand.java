package io.lana.sqlstarter.menu;

public interface MenuCommand {
    String getActivator();

    String getDescription();

    void setDescription(String description);

    MenuCommandExecutor getExecutor();
}
