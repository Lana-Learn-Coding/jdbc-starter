package io.lana.sqlstarter.app.category;

import io.lana.sqlstarter.conn.BaseDAO;
import io.lana.sqlstarter.conn.QueryResult;

import java.sql.*;
import java.util.List;

public class CategoryDAO extends BaseDAO<Category> {
    private final Connection connection;

    public CategoryDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    protected Connection getConnection() {
        return this.connection;
    }

    @Override
    protected String getTableName() {
        return "category";
    }

    public List<Category> findByName(String name) {
        return findAll("name like ?", "%" + name + "%");
    }

    public void update(Category category) {
        updateAll(category, "id = ?", category.getId());
    }

    public void delete(Integer id) {
        deleteAll("id = ?", id);
    }

    public boolean existByName(String name) {
        return exist("name = ?", name);
    }

    public boolean exist(Integer id) {
        return exist("id = ?", id);
    }

    public Long getLatestId() {
        if (count() == 0) {
            return 0L;
        }
        return (long) ((int) QueryResult.of(executeQuery("SELECT id FROM category ORDER BY id DESC"), Integer.class)
                .firstResult());
    }
}
