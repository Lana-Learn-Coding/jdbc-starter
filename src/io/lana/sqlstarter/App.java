package io.lana.sqlstarter;

import io.lana.sqlstarter.menu.Command;
import io.lana.sqlstarter.menu.Menu;
import io.lana.sqlstarter.repo.conn.PostgresConnection;
import io.lana.sqlstarter.utils.ValidationUtils;
import io.lana.sqlstarter.model.Category;
import io.lana.sqlstarter.repo.CategoryDAO;

import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

public class App {
    private static final Scanner sc = new Scanner(System.in);

    private static long seq = 1;

    public static void main(String[] args) {
        try (Connection connection = PostgresConnection.INSTANCE.getConnection()) {
            CategoryDAO categoryDAO = new CategoryDAO(connection);
            seq = categoryDAO.getLatestId() + 1;

            Menu menu = new Menu(sc, "Category Menu");
            menu.addCommand(Command.of("1", "Show all category", () -> showAllCategory(categoryDAO)));
            menu.addCommand(Command.of("2", "Show category by name", () -> showAllCategoryByName(categoryDAO)));
            menu.addCommand(Command.of("3", "Create new category", () -> createNewCategory(categoryDAO)));
            menu.addCommand(Command.of("4", "Delete category", () -> deleteCategory(categoryDAO)));
            menu.addCommand(Command.of("5", "Update existing category", () -> updateCategory(categoryDAO)));
            menu.addCommand(Command.of("6", "Exit", menu::stop));

            menu.run(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAllCategory(CategoryDAO repo) {
        printAll(repo.findAll());
    }

    public static void showAllCategoryByName(CategoryDAO repo) {
        System.out.println("Enter name: ");
        String name = ValidationUtils.enforceNotEmpty(sc);
        printAll(repo.findByName(name));
    }

    public static void createNewCategory(CategoryDAO repo) {
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

    private static void deleteCategory(CategoryDAO categoryDAO) {
        System.out.println("Enter id to delete");
        Integer id = ValidationUtils.enforceInteger(sc);
        if (categoryDAO.exist(id)) {
            categoryDAO.delete(id);
            System.out.println("ok");
            return;
        }
        System.out.println("Not found");
    }

    private static void updateCategory(CategoryDAO categoryDAO) {
        System.out.println("Enter id to update");
        Integer id = ValidationUtils.enforceInteger(sc);
        if (!categoryDAO.exist(id)) {
            System.out.println("Not found");
        }
        while (true) {
            Category category = new Category();
            System.out.println("Enter category name");
            category.setName(ValidationUtils.enforceNotEmpty(sc));
            System.out.println("Enter parent id");
            category.setParentId(ValidationUtils.enforceInteger(sc));
            System.out.println("Enter status");
            category.setStatus(ValidationUtils.enforceBoolean(sc));

            if (category.getParentId() != 0 && !categoryDAO.exist(category.getParentId())) {
                System.out.println("parent id not exist, try again");
                continue;
            }

            if (categoryDAO.existByName(category.getName())) {
                System.out.println("name already picked, try again");
                continue;
            }

            if (category.getParentId() == 0) category.setParentId(null);
            category.setId(id);
            categoryDAO.update(category);
            return;
        }
    }

    private static void printAll(List<Category> categories) {
        categories.forEach(System.out::println);
    }
}
