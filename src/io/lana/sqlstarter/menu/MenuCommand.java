package io.lana.sqlstarter.menu;

public interface MenuCommand {
    String getActivator();

    String getDescription();

    MenuCommandExecutor getExecutor();
}
