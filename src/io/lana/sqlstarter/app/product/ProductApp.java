package io.lana.sqlstarter.app.product;

import io.lana.sqlstarter.LanguageBundle;
import io.lana.sqlstarter.dao.ConnectionUtils;
import io.lana.sqlstarter.menu.Menu;
import io.lana.sqlstarter.menu.command.Command;
import io.lana.sqlstarter.validation.Input;
import io.lana.sqlstarter.validation.Rule;
import io.lana.sqlstarter.validation.Validator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ProductApp {
    private static final Scanner sc = new Scanner(System.in);

    private static final Input input = Input.using(sc::nextLine);

    private static final String LOCALE_PATH = "io.lana.sqlstarter.app.product.locale.Menu";

    private static ResourceBundle lang = LanguageBundle.getBundle(LOCALE_PATH);

    public static void main(String[] args) {
        try (Connection connection = ConnectionUtils.getConnection()) {
            ProductDAO productDAO = new ProductDAO(connection);

            Menu menu = new Menu(sc);
            menu.addCommand(Command.of("1", () -> lang.getString("menu.1"), () -> importProduct(productDAO)));
            menu.addCommand(Command.of("2", () -> lang.getString("menu.2"), () -> exportProduct(productDAO)));
            menu.addCommand(Command.of("3", () -> lang.getString("menu.3"), () -> showAllProduct(productDAO)));
            menu.addCommand(Command.of("4", () -> lang.getString("menu.4"), () -> updateProduct(productDAO)));
            menu.addCommand(Command.of("5", () -> lang.getString("menu.5"), ProductApp::changeLanguage));
            menu.addCommand(Command.of("6", () -> lang.getString("menu.6"), menu::stop));

            menu.setTitle(() -> lang.getString("menu.title"));
            menu.setAskOptionMessage(() -> lang.getString("menu.select"));
            menu.setInvalidOptionMessage(() -> lang.getString("menu.invalid-option"));
            menu.run(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void importProduct(ProductDAO productDAO) {
        System.out.println(lang.getString("input.code"));
        Integer code = input.until(Rule.integer(lang.getString("validation.integer")),
            Rule.min(lang.getString("validation.min"), 1)).getAs(Integer.class);

        Optional<Product> product = productDAO.findOne(code);
        if (!product.isPresent()) {
            System.out.println(lang.getString("error.product-not-found") + ". " + lang.getString("action.create"));
            productDAO.save(inputProduct(code));
            printAll(Collections.singletonList(productDAO.findOne(code).get()));
            return;
        }
        updateQuantity(productDAO, product.get(), false);
        printAll(Collections.singletonList(product.get()));
    }

    public static void exportProduct(ProductDAO productDAO) {
        Map<Product, Integer> reportMap = new HashMap<>();
        do {
            System.out.println(lang.getString("input.code"));
            Integer code = input.until(Rule.integer(lang.getString("validation.integer")),
                Rule.min(lang.getString("validation.min"), 1)).getAs(Integer.class);
            Optional<Product> product = productDAO.findOne(code);
            if (!product.isPresent()) {
                System.out.println(lang.getString("error.product-not-found"));
                continue;
            }

            int quantity = product.get().getQuantity();
            updateQuantity(productDAO, product.get(), true);
            printAll(Collections.singletonList(product.get()));
            reportMap.put(product.get(), quantity - product.get().getQuantity());
        } while (askYesNo(lang.getString("input.continue-export")));

        boolean printReport = askYesNo(lang.getString("action.print-report"));
        if (printReport) {
            printReport(reportMap);
        }
    }

    private static void printReport(Map<Product, Integer> reportMap) {
        Path cwd = Paths.get(".").toAbsolutePath().normalize();
        System.out.println(lang.getString("input.filename"));
        String filename = input.until(Validator.ValidationRule.from(lang.getString("error.file-exist"),
            input -> input.getInput().trim().isEmpty() || !Files.exists(cwd.resolve(input.getInput())))).get();

        if (filename.isEmpty()) {
            filename = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        }

        filename = cwd.resolve(filename).toString();
        String border = Product.BORDER + "----------+\n";
        String format = Product.FORMAT + " %8s |\n";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(border);
            writer.write(String.format(format, lang.getString("product.code"), lang.getString("product.name"),
                lang.getString("product.producer"), lang.getString("product.quantity"), lang.getString("product.price"),
                "VAT", lang.getString("lang.exported")));
            writer.write(border);
            for (Product product : reportMap.keySet()) {
                writer.write(String.format(format, product.getCode(), product.getName(), product.getProducer(),
                    product.getQuantity(), Product.PRICE_FORMATTER.format(product.getPrice()), product.getVat(), reportMap.get(product)));
            }
            writer.write(border);
            System.out.println(lang.getString("lang.exported") + " " + filename);
            writer.flush();
        } catch (Exception e) {
            System.out.println(lang.getString("error.cannot-write-file"));
        }
    }

    private static void updateQuantity(ProductDAO productDAO, Product product, boolean exporting) {
        System.out.println(lang.getString("input.quantity"));
        Integer quantity = input.until(Rule.integer(lang.getString("validation.integer")),
            Rule.min(lang.getString("validation.min"), 1)).getAs(Integer.class);
        if (exporting) {
            if (quantity > product.getQuantity()) {
                System.out.println(lang.getString("error.not-enough-product"));
                return;
            }
            product.setQuantity(product.getQuantity() - quantity);
        } else {
            product.setQuantity(product.getQuantity() + quantity);
        }
        productDAO.update(product);
    }

    private static Product inputProduct(Integer code) {
        Product product = new Product();
        product.setCode(code);
        System.out.println(lang.getString("input.name"));
        product.setName(input.until(Rule.notEmpty(lang.getString("validation.not-empty"))).get());
        System.out.println(lang.getString("input.producer"));
        product.setProducer(input.until(Rule.notEmpty(lang.getString("validation.not-empty"))).get());
        System.out.println(lang.getString("input.quantity"));
        product.setQuantity(input.until(Rule.integer(lang.getString("validation.integer")),
            Rule.min(lang.getString("validation.min"), 1)).getAs(Integer.class));
        System.out.println(lang.getString("input.price"));
        product.setPrice(input.until(Rule.doubleNum(lang.getString("validation.doubleNum")),
            Rule.min(lang.getString("validation.min"), 0D)).getAs(Double.class));
        System.out.println(lang.getString("input.vat"));
        product.setVat(input.until(Rule.integer(lang.getString("validation.integer")),
            Rule.min(lang.getString("validation.min"), 0)).getAs(Integer.class));
        return product;
    }

    public static void updateProduct(ProductDAO productDAO) {
        System.out.println(lang.getString("input.code"));
        Integer code = input.until(Rule.integer(lang.getString("validation.integer")),
            Rule.min(lang.getString("validation.min"), 1)).getAs(Integer.class);

        if (!productDAO.exist(code)) {
            System.out.println(lang.getString("error.product-not-found"));
            return;
        }
        System.out.println(lang.getString("action.update"));
        productDAO.update(inputProduct(code));
    }

    public static void showAllProduct(ProductDAO productDAO) {
        List<Product> products = productDAO.findAll();
        String sort = askOneOf(lang.getString("action.sort"), "code", "name", "price", "quantity");
        switch (sort) {
            case "code":
                products = products.stream()
                    .sorted(Comparator.comparingInt(Product::getCode))
                    .collect(Collectors.toList());
                break;
            case "name":
                products = products.stream()
                    .sorted(Comparator.comparing(Product::getName))
                    .collect(Collectors.toList());
                break;
            case "price":
                products = products.stream()
                    .sorted(Comparator.comparingDouble(Product::getPrice))
                    .collect(Collectors.toList());
                break;
            case "quantity":
                products = products.stream()
                    .sorted(Comparator.comparingInt(Product::getQuantity))
                    .collect(Collectors.toList());
                break;
            default:
                System.out.println(lang.getString("error.sort-not-found"));
        }
        printAll(products);
    }

    private static void printAll(List<Product> products) {
        StringBuilder builder = new StringBuilder(Product.BORDER);
        builder.append("\n");
        builder.append(String.format(Product.FORMAT, lang.getString("product.code"), lang.getString("product.name"),
            lang.getString("product.producer"), lang.getString("product.quantity"), lang.getString("product.price"), "VAT"));
        builder.append("\n");
        builder.append(Product.BORDER);
        builder.append("\n");
        products.forEach((product) -> {
            builder.append(product.toString());
            builder.append("\n");
        });
        builder.append(Product.BORDER);
        System.out.println(builder.toString());
    }

    private static boolean askYesNo(String message) {
        System.out.println(message + "[y/N]:");
        String yn = input.get();
        return yn.equals("y") || yn.equals("Y");
    }

    private static String askOneOf(String message, String... options) {
        while (true) {
            System.out.println(message + "[" + String.join("/", options) + "]:");
            String selected = input.get();
            if (Arrays.asList(options).contains(selected.trim())) {
                return selected;
            }
            System.out.println(lang.getString("error.sort-not-found"));
        }
    }

    public static void changeLanguage() {
        System.out.println(lang.getString("conf.lang"));
        String language = input.until(Validator.ValidationRule.from(lang.getString("conf.error.lang-not-found"),
            context -> context.getInput().equals("en") || context.getInput().equals("vi"))).get();

        if (language.equals("vi")) {
            LanguageBundle.setLocale("vi", "VN");
        } else {
            LanguageBundle.setLocale("en", "US");
        }
        lang = LanguageBundle.getBundle(LOCALE_PATH);
    }
}
