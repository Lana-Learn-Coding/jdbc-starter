package io.lana.sqlstarter;

import io.lana.sqlstarter.menu.Command;
import io.lana.sqlstarter.menu.Menu;
import io.lana.sqlstarter.menu.ValidationUtils;
import io.lana.sqlstarter.model.Category;
import io.lana.sqlstarter.repo.CategoryRepo;
import io.lana.sqlstarter.repo.ConnectionUtils;

import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

public class App {
    private static final Scanner sc = new Scanner(System.in);

    private static long seq = 1;

    public static void main(String[] args) {
        try (Connection connection = ConnectionUtils.getConnection()) {
            CategoryRepo categoryRepo = new CategoryRepo(connection);
            seq = categoryRepo.getLatestId() + 1;

            Menu menu = new Menu(sc, "Category Menu");
            menu.addCommand(Command.of("1", "Show all category", () -> showAllCategory(categoryRepo)));
            menu.addCommand(Command.of("2", "Show category by name", () -> showAllCategoryByName(categoryRepo)));
            menu.addCommand(Command.of("3", "Create new category ", () -> createNewCategory(categoryRepo)));
            menu.addCommand(Command.of("6", "Exit", menu::stop));

            menu.run(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAllCategory(CategoryRepo repo) {
        printAll(repo.findAll());
    }

    public static void showAllCategoryByName(CategoryRepo repo) {
        System.out.println("Enter name: ");
        String name = ValidationUtils.enforceNotEmpty(sc);
        printAll(repo.findByName(name));
    }

    public static void createNewCategory(CategoryRepo repo) {
        while (true) {
            Category category = new Category();
            System.out.println("Enter category name");
            category.setName(ValidationUtils.enforceNotEmpty(sc));
            System.out.println("Enter parent id");
            category.setParentId(ValidationUtils.enforceInteger(sc));
            System.out.println("Enter status");
            category.setStatus(ValidationUtils.enforceBoolean(sc));

            if (category.getParentId() != 0 && !repo.exist(category.getParentId())) {
                System.out.println("parent id not exist, try again");
                continue;
            }

            if (repo.existByName(category.getName())) {
                System.out.println("name already picked, try again");
                continue;
            }

            if (category.getParentId() == 0) category.setParentId(null);
            category.setId((int) seq);
            repo.save(category);
            seq++;
            return;
        }

    }

    private static void printAll(List<Category> categories) {
        categories.forEach(System.out::println);
    }
}
