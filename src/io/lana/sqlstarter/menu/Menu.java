package io.lana.sqlstarter.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

public class Menu {
    private final Map<String, MenuCommand> commandMap = new HashMap<>();

    private final Scanner scanner;

    private boolean isRunning = false;

    private String title = "Menu";

    public Menu(Scanner sc) {
        this.scanner = sc;
    }

    public Menu(Scanner sc, String title) {
        this.scanner = sc;
        this.title = title;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    private void setRunning(boolean running) {
        this.isRunning = running;
    }


    public String getTitle() {
        return this.title;
    }


    public void addCommand(MenuCommand command) {
        commandMap.putIfAbsent(command.getActivator(), command);
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
            System.out.print("Select an option: ");
            String key = scanner.nextLine();
            if (commandMap.containsKey(key)) {
                commandMap.get(key).getExecutor().exec();
            } else {
                System.out.println("Invalid option!");
            }
            System.out.println();
        } while (loop && isRunning());

    }

    public void stop() {
        setRunning(false);
    }
}
