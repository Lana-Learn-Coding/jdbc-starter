package io.lana.sqlstarter.app.product;

import io.lana.sqlstarter.dao.ConnectionUtils;
import io.lana.sqlstarter.menu.Command;
import io.lana.sqlstarter.menu.Menu;
import io.lana.sqlstarter.validation.Input;
import io.lana.sqlstarter.validation.Rule;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ProductApp {
    private static final Scanner sc = new Scanner(System.in);
    private static final Input input = Input.using(sc::nextLine);

    public static void main(String[] args) {
        try (Connection connection = ConnectionUtils.getConnection()) {
            ProductDAO productDAO = new ProductDAO(connection);

            Menu menu = new Menu(sc, "Product Management");
            menu.addCommand(Command.of("1", "Import product", () -> importProduct(productDAO)));
            menu.addCommand(Command.of("2", "Export product", () -> exportProduct(productDAO)));
            menu.addCommand(Command.of("3", "Show all product", () -> showAllProduct(productDAO)));
            menu.addCommand(Command.of("4", "Update product", () -> updateProduct(productDAO)));
            menu.addCommand(Command.of("5", "Exit", menu::stop));

            menu.run(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void importProduct(ProductDAO productDAO) {
        System.out.println("Enter code:");
        Integer code = input.until(Rule.integer(), Rule.min(1)).getAs(Integer.class);

        Optional<Product> product = productDAO.findOne(code);
        if (!product.isPresent()) {
            System.out.println("Product not found, creating new one:");
            productDAO.save(inputProduct(code));
            return;
        }
        updateQuantity(productDAO, product.get(), false);
    }

    public static void exportProduct(ProductDAO productDAO) {
        System.out.println("Enter code");
        Integer code = input.until(Rule.integer(), Rule.min(1)).getAs(Integer.class);
        Optional<Product> product = productDAO.findOne(code);
        if (!product.isPresent()) {
            System.out.println("Product not found");
            return;
        }
        updateQuantity(productDAO, product.get(), true);
    }

    private static void updateQuantity(ProductDAO productDAO, Product product, boolean exporting) {
        System.out.println("Enter quantity: ");
        Integer quantity = input.until(Rule.integer(), Rule.min(1), Rule.max(product.getQuantity())).getAs(Integer.class);
        if (exporting) {
            product.setQuantity(product.getQuantity() - quantity);
        } else {
            product.setQuantity(product.getQuantity() + quantity);
        }
        productDAO.update(product);
        System.out.println("ok");
    }

    private static Product inputProduct(Integer code) {
        Product product = new Product();
        product.setCode(code);
        System.out.println("Enter name of product");
        product.setName(input.until(Rule.notEmpty()).get());
        System.out.println("Enter producer of product");
        product.setProducer(input.until(Rule.notEmpty()).get());
        System.out.println("Enter quantity of product");
        product.setQuantity(input.until(Rule.integer(), Rule.min(0)).getAs(Integer.class));
        System.out.println("Enter price of product");
        product.setPrice(input.until(Rule.doubleNum(), Rule.min(0D)).getAs(Double.class));
        System.out.println("Enter vat of product");
        product.setVat(input.until(Rule.integer(), Rule.min(0)).getAs(Integer.class));
        return product;
    }

    public static void updateProduct(ProductDAO productDAO) {
        System.out.println("Enter code:");
        Integer code = input.until(Rule.integer(), Rule.min(0)).getAs(Integer.class);

        if (!productDAO.exist(code)) {
            System.out.println("Product not found!");
            return;
        }
        System.out.println("Update product: ");
        productDAO.update(inputProduct(code));
    }

    public static void showAllProduct(ProductDAO productDAO) {
        printAll(productDAO.findAll());
    }

    private static void printAll(List<Product> products) {
        products.forEach(System.out::println);
    }
}
