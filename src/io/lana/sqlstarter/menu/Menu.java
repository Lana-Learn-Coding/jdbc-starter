package io.lana.sqlstarter.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Supplier;

public class Menu {
    private final Map<String, MenuCommand> commandMap = new HashMap<>();

    private final Scanner scanner;

    private boolean isRunning = false;

    private Supplier<String> title = () -> "Menu";

    private Supplier<String> askOptionMessage = () -> "Select an option";

    private Supplier<String> invalidOptionMessage = () -> "Invalid option";

    public Menu(Scanner sc) {
        this.scanner = sc;
    }

    public Menu(Scanner sc, Supplier<String> titleGenerator) {
        this.scanner = sc;
        this.title = titleGenerator;
    }

    public Menu(Scanner sc, String title) {
        this.scanner = sc;
        this.title = () -> title;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    private void setRunning(boolean running) {
        this.isRunning = running;
    }

    public void setTitle(Supplier<String> title) {
        this.title = title;
    }

    public void setAskOptionMessage(Supplier<String> askOptionMessage) {
        this.askOptionMessage = askOptionMessage;
    }

    public void setInvalidOptionMessage(Supplier<String> invalidOptionMessage) {
        this.invalidOptionMessage = invalidOptionMessage;
    }

    public String getTitle() {
        return this.title.get();
    }


    public void addCommand(MenuCommand command) {
        commandMap.putIfAbsent(command.getActivator(), command);
    }

    public void setTitle(String title) {
        this.title = () -> title;
    }

    public void setTitleGenerator(Supplier<String> titleGenerator) {
        this.title = titleGenerator;
    }

    public MenuCommand getCommand(String key) {
        return commandMap.get(key);
    }

    public void run() {
        run(true);
    }

    public void run(boolean loop) {
        setRunning(true);
        do {
            System.out.println(getTitle());
            commandMap.forEach((key, command) -> {
                System.out.println("[" + key + "] " + command.getDescription());
            });
            System.out.print(askOptionMessage.get() + " ");
            String key = scanner.nextLine();
            if (commandMap.containsKey(key)) {
                commandMap.get(key).getExecutor().exec();
            } else {
                System.out.println(invalidOptionMessage.get());
            }
            System.out.println();
        } while (loop && isRunning());

    }

    public void stop() {
        setRunning(false);
    }
}
