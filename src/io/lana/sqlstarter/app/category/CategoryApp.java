package io.lana.sqlstarter.app.category;

import io.lana.sqlstarter.LanguageBundle;
import io.lana.sqlstarter.dao.ConnectionUtils;
import io.lana.sqlstarter.menu.Menu;
import io.lana.sqlstarter.menu.command.Command;
import io.lana.sqlstarter.validation.Input;
import io.lana.sqlstarter.validation.Rule;
import io.lana.sqlstarter.validation.Validator;

import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

public class CategoryApp {
    private static final String LOCALE_PATH = "io.lana.sqlstarter.app.category.locale.Menu";

    private static ResourceBundle lang = LanguageBundle.getBundle(LOCALE_PATH);

    private static final Scanner sc = new Scanner(System.in);

    private static final Input input = Input.using(sc::nextLine);

    private static long seq = 1;

    public static void main(String[] args) {
        try (Connection connection = ConnectionUtils.getConnection()) {
            CategoryDAO categoryDAO = new CategoryDAO(connection);
            seq = categoryDAO.getLatestId() + 1;

            Menu menu = new Menu(sc, () -> lang.getString("menu.title"));
            menu.addCommand(Command.of("1", () -> lang.getString("menu.1"), () -> showAllCategory(categoryDAO)));
            menu.addCommand(Command.of("2", () -> lang.getString("menu.2"), () -> showAllCategoryByName(categoryDAO)));
            menu.addCommand(Command.of("3", () -> lang.getString("menu.3"), () -> createNewCategory(categoryDAO)));
            menu.addCommand(Command.of("4", () -> lang.getString("menu.4"), () -> deleteCategory(categoryDAO)));
            menu.addCommand(Command.of("5", () -> lang.getString("menu.5"), () -> updateCategory(categoryDAO)));
            menu.addCommand(Command.of("6", () -> lang.getString("menu.6"), () -> changeLanguage(menu)));
            menu.addCommand(Command.of("7", () -> lang.getString("menu.7"), menu::stop));

            menu.run(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showAllCategory(CategoryDAO repo) {
        printAll(repo.findAll());
    }

    private static void showAllCategoryByName(CategoryDAO repo) {
        System.out.println(lang.getString("app.op.query.name-to-find"));
        String name = input.until(Rule.notEmpty(lang.getString("validation.not-empty"))).get();
        printAll(repo.findByName(name));
    }

    private static void createNewCategory(CategoryDAO dao) {
        Category category = inputCategory(dao);
        category.setId((int) seq);
        dao.save(category);
        seq++;
    }

    private static void updateCategory(CategoryDAO dao) {
        System.out.println(lang.getString("app.op.query.id-to-update"));
        Integer id = input.until(Rule.integer(lang.getString("validation.integer")),
            Rule.exist(lang.getString("validation.exist"), dao, "id", Integer.class)).getAs(Integer.class);

        Category category = inputCategory(dao);
        category.setId(id);
        dao.update(category);
    }

    private static Category inputCategory(CategoryDAO dao) {
        while (true) {
            Category category = new Category();
            System.out.println(lang.getString("category.input.name"));
            category.setName(input.until(Rule.notEmpty(lang.getString("validation.not-empty"))).get());
            System.out.println(lang.getString("category.input.parent-id"));
            category.setParentId(input.until(Rule.integer(lang.getString("validation.integer"))).getAs(Integer.class));
            System.out.println(lang.getString("category.input.status"));
            category.setStatus(input.until(Rule.bool(lang.getString("validation.bool"))).getAs(Boolean.class));

            if (category.getParentId() != 0 && !dao.exist(category.getParentId())) {
                System.out.println(lang.getString("category.input.error.parent-id-not-found"));
                continue;
            }

            if (dao.existByName(category.getName())) {
                System.out.println(lang.getString("category.input.error.name-duplicated"));
                continue;
            }

            if (category.getParentId() == 0) category.setParentId(null);
            return category;
        }
    }

    private static void deleteCategory(CategoryDAO categoryDAO) {
        System.out.println(lang.getString("app.op.query.id-to-delete"));
        Integer id = input.until(Rule.integer(lang.getString("validation.integer"))).getAs(Integer.class);
        if (categoryDAO.exist(id)) {
            categoryDAO.delete(id);
            System.out.println(lang.getString("app.op.ok"));
            return;
        }
        System.out.println(lang.getString("category.input.error.not-found"));
    }

    private static void changeLanguage(Menu menu) {
        System.out.println(lang.getString("app.conf.input.lang"));
        String lang = input.until(Validator.ValidationRule.from(CategoryApp.lang.getString("app.conf.error.language-not-found"),
            context -> context.getInput().equals("en") || context.getInput().equals("vi"))).get();

        if (lang.equals("vi")) {
            LanguageBundle.setLocale("vi", "VN");
        } else {
            LanguageBundle.setLocale("en", "US");
        }
        CategoryApp.lang = LanguageBundle.getBundle(LOCALE_PATH);
    }

    private static void printAll(List<Category> categories) {
        categories.forEach(System.out::println);
    }
}
