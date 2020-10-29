package io.lana.sqlstarter.repo;

import io.lana.sqlstarter.model.Category;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CategoryRepo {
    private final Connection connection;

    public CategoryRepo(Connection connection) {
        this.connection = connection;
    }

    public List<Category> findAll() {
        String sql = "SELECT * FROM category";
        QueryResult<Category> result = QueryResult.of(executeQuery(sql), Category.class);
        return result.list();
    }

    public List<Category> findByName(String name) {
        String sql = "SELECT * from category where name like %?%";
        QueryResult<Category> result = QueryResult.of(executeQuery(sql, name), Category.class);
        return result.list();
    }

    public Optional<Category> findOne(Integer id) {
        String sql = "SELECT * FROM category where id = ?";
        QueryResult<Category> result = QueryResult.of(executeQuery(sql), Category.class);
        return result.firstResultOptional();
    }

    public void save(Category category) {
        List<Object> fieldValues = this.getFieldsValue(category);
        String sql = "INSERT INTO category VALUES (?, ?, ?, ?)";
        executeQuery(sql, fieldValues.toArray());
    }

    public void update(Category category) {
        List<Object> fieldValues = this.getFieldsValue(category);
        fieldValues.add(category.getId());
        String sql = "UPDATE category SET " + this.getColumnsUpdateMapping(Category.class) + " WHERE id=?";
        executeQuery(sql, fieldValues.toArray());
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM category WHERE id = ?";
        executeQuery(sql, id);
    }

    public boolean existByName(String name) {
        String sql = "SELECT 1 FROM category where name = ?";
        QueryResult<Integer> result = QueryResult.of(executeQuery(sql, name), Integer.class);
        return result.firstResultOptional().isPresent();
    }

    public boolean exist(Integer id) {
        String sql = "SELECT 1 FROM category where id = ?";
        QueryResult<Integer> result = QueryResult.of(executeQuery(sql, id), Integer.class);
        return result.firstResultOptional().isPresent();
    }

    public Long getLatestId() {
        String sql = "SELECT count(*) FROM category";
        return QueryResult.of(executeQuery(sql), Long.class).firstResult();
    }

    private ResultSet executeQuery(String sql) {
        try {
            return connection.prepareStatement(sql).executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ResultSet executeQuery(String sql, Object... params) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            return statement.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Object> getFieldsValue(Object object) {
        Class<?> clazz = object.getClass();
        try {
            List<Object> values = new ArrayList<>();
            for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(clazz, Object.class).getPropertyDescriptors()) {
                values.add(propertyDescriptor.getReadMethod().invoke(object));
            }
            return values;
        } catch (Exception e) {
            throw new RuntimeException("Cannot get fields of type: " + clazz.getName());
        }
    }

    public String getColumnsUpdateMapping(Class<?> clazz) {
        try {
            return Arrays.stream(Introspector.getBeanInfo(clazz).getPropertyDescriptors())
                    .map(FeatureDescriptor::getName)
                    .map(CaseUtils::toSnakeCase)
                    .map(name -> name + "=?")
                    .collect(Collectors.joining(","));
        } catch (IntrospectionException e) {
            throw new RuntimeException("Cannot get fields of type: " + clazz.getName());
        }
    }
}
