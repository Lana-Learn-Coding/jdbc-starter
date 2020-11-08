package io.lana.sqlstarter.app.product;

import io.lana.sqlstarter.conn.BaseDAO;

import java.sql.Connection;
import java.util.Optional;

public class ProductDAO extends BaseDAO<Product> {

    private final Connection connection;

    public ProductDAO(Connection connection) {
        this.connection = connection;
    }

    public Optional<Product> findOne(Integer code) {
        return findOne("code = ?", code);
    }

    public void update(Product product) {
        updateAll(product, "code = ?", product.getCode());
    }

    public boolean exist(Integer code) {
        return exist("code = ?", code);
    }

    @Override
    protected Connection getConnection() {
        return connection;
    }

    @Override
    protected String getTableName() {
        return "product";
    }
}
