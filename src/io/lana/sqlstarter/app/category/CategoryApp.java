package io.lana.sqlstarter.app.category;

import io.lana.sqlstarter.dao.ConnectionUtils;
import io.lana.sqlstarter.menu.Command;
import io.lana.sqlstarter.menu.Menu;
import io.lana.sqlstarter.validation.Input;
import io.lana.sqlstarter.validation.Rule;
import io.lana.sqlstarter.validation.Validator;

import java.sql.Connection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

public class CategoryApp {
    private static final String LOCALE_PATH = "io.lana.sqlstarter.app.category.locale.Menu";

    private static ResourceBundle menuLang = ResourceBundle.getBundle(LOCALE_PATH, new Locale("en", "US"));

    private static final Scanner sc = new Scanner(System.in);

    private static final Input input = Input.using(sc::nextLine);

    private static long seq = 1;

    public static void main(String[] args) {
        try (Connection connection = ConnectionUtils.getConnection()) {
            CategoryDAO categoryDAO = new CategoryDAO(connection);
            seq = categoryDAO.getLatestId() + 1;

            Menu menu = new Menu(sc, menuLang.getString("menu.title"));
            menu.addCommand(Command.of("1", menuLang.getString("menu.1"), () -> showAllCategory(categoryDAO)));
            menu.addCommand(Command.of("2", menuLang.getString("menu.2"), () -> showAllCategoryByName(categoryDAO)));
            menu.addCommand(Command.of("3", menuLang.getString("menu.3"), () -> createNewCategory(categoryDAO)));
            menu.addCommand(Command.of("4", menuLang.getString("menu.4"), () -> deleteCategory(categoryDAO)));
            menu.addCommand(Command.of("5", menuLang.getString("menu.5"), () -> updateCategory(categoryDAO)));
            menu.addCommand(Command.of("6", menuLang.getString("menu.6"), () -> changeLanguage(menu)));
            menu.addCommand(Command.of("7", menuLang.getString("menu.7"), menu::stop));

            menu.run(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showAllCategory(CategoryDAO repo) {
        printAll(repo.findAll());
    }

    private static void showAllCategoryByName(CategoryDAO repo) {
        System.out.println(menuLang.getString("app.op.query.name-to-find"));
        String name = input.until(Rule.notEmpty()).get();
        printAll(repo.findByName(name));
    }

    private static void createNewCategory(CategoryDAO dao) {
        Category category = inputCategory(dao);
        category.setId((int) seq);
        dao.save(category);
        seq++;
    }

    private static void updateCategory(CategoryDAO dao) {
        System.out.println(menuLang.getString("app.op.query.id-to-update"));
        Integer id = input.until(Rule.integer(), Rule.exist(dao, "id", Integer.class)).getAs(Integer.class);

        Category category = inputCategory(dao);
        category.setId(id);
        dao.update(category);
    }

    private static Category inputCategory(CategoryDAO dao) {
        while (true) {
            Category category = new Category();
            System.out.println(menuLang.getString("category.input.name"));
            category.setName(input.until(Rule.notEmpty()).get());
            System.out.println(menuLang.getString("category.input.parent-id"));
            category.setParentId(input.until(Rule.integer()).getAs(Integer.class));
            System.out.println(menuLang.getString("category.input.status"));
            category.setStatus(input.until(Rule.bool()).getAs(Boolean.class));

            if (category.getParentId() != 0 && !dao.exist(category.getParentId())) {
                System.out.println(menuLang.getString("category.input.error.parent-id-not-found"));
                continue;
            }

            if (dao.existByName(category.getName())) {
                System.out.println(menuLang.getString("category.input.error.name-duplicated"));
                continue;
            }

            if (category.getParentId() == 0) category.setParentId(null);
            return category;
        }
    }

    private static void deleteCategory(CategoryDAO categoryDAO) {
        System.out.println(menuLang.getString("app.op.query.id-to-delete"));
        Integer id = input.until(Rule.integer()).getAs(Integer.class);
        if (categoryDAO.exist(id)) {
            categoryDAO.delete(id);
            System.out.println(menuLang.getString("app.op.ok"));
            return;
        }
        System.out.println(menuLang.getString("category.input.error.not-found"));
    }

    private static void changeLanguage(Menu menu) {
        System.out.println(menuLang.getString("app.conf.input.lang"));
        String lang = input.until(Validator.ValidationRule.from(menuLang.getString("app.conf.error.language-not-found"),
                context -> context.getInput().equals("en") || context.getInput().equals("vi"))).get();

        if (lang.equals("vi")) {
            menuLang = ResourceBundle.getBundle(LOCALE_PATH, new Locale("vi", "VN"));
        } else {
            menuLang = ResourceBundle.getBundle(LOCALE_PATH, new Locale("en", "US"));
        }

        menu.setTitle(menuLang.getString("menu.title"));
        menu.getCommand("1").setDescription(menuLang.getString("menu.1"));
        menu.getCommand("2").setDescription(menuLang.getString("menu.2"));
        menu.getCommand("3").setDescription(menuLang.getString("menu.3"));
        menu.getCommand("4").setDescription(menuLang.getString("menu.4"));
        menu.getCommand("5").setDescription(menuLang.getString("menu.5"));
        menu.getCommand("6").setDescription(menuLang.getString("menu.6"));
        menu.getCommand("7").setDescription(menuLang.getString("menu.7"));
    }

    private static void printAll(List<Category> categories) {
        categories.forEach(System.out::println);
    }
}
