package io.lana.sqlstarter;

import io.lana.sqlstarter.menu.Command;
import io.lana.sqlstarter.menu.Menu;
import io.lana.sqlstarter.repo.conn.PostgresConnection;
import io.lana.sqlstarter.utils.ValidationUtils;
import io.lana.sqlstarter.model.Category;
import io.lana.sqlstarter.repo.CategoryDAO;

import java.sql.Connection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

public class App {
    private static final String LOCALE_PATH = "io.lana.sqlstarter.locale.Menu";

    private static ResourceBundle menuLang = ResourceBundle.getBundle(LOCALE_PATH, new Locale("en", "US"));

    private static final Scanner sc = new Scanner(System.in);

    private static long seq = 1;

    public static void main(String[] args) {
        try (Connection connection = PostgresConnection.INSTANCE.getConnection()) {
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
        String name = ValidationUtils.enforceNotEmpty(sc);
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
        Integer id = ValidationUtils.enforceInteger(sc);
        if (!dao.exist(id)) {
            System.out.println(menuLang.getString("category.input.error.not-found"));
        }
        Category category = inputCategory(dao);
        category.setId(id);
        dao.update(category);
    }

    private static Category inputCategory(CategoryDAO dao) {
        while (true) {
            Category category = new Category();
            System.out.println(menuLang.getString("category.input.name"));
            category.setName(ValidationUtils.enforceNotEmpty(sc));
            System.out.println(menuLang.getString("category.input.parent-id"));
            category.setParentId(ValidationUtils.enforceInteger(sc));
            System.out.println(menuLang.getString("category.input.status"));
            category.setStatus(ValidationUtils.enforceBoolean(sc));

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
        Integer id = ValidationUtils.enforceInteger(sc);
        if (categoryDAO.exist(id)) {
            categoryDAO.delete(id);
            System.out.println(menuLang.getString("app.op.ok"));
            return;
        }
        System.out.println(menuLang.getString("category.input.error.not-found"));
    }

    private static void changeLanguage(Menu menu) {
        System.out.println(menuLang.getString("app.conf.input.lang"));
        String lang = ValidationUtils.enforceInput(sc, (input) ->
                input.equals("en") || input.equals("vi")
                        ? null
                        : menuLang.getString("app.conf.error.language-not-found")
        );

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
