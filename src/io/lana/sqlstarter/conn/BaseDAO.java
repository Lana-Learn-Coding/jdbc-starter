package io.lana.sqlstarter.conn;

import io.lana.sqlstarter.utils.CaseUtils;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class BaseDAO<T> {

    private final Class<T> clazz;

    protected abstract Connection getConnection();

    protected abstract String getTableName();

    @SuppressWarnings("unchecked")
    protected BaseDAO() {
        this.clazz = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public void save(T entity) {
        List<Object> fieldValues = getFieldsValue(entity);
        String paramPlaceholder = fieldValues.stream().map(val -> "?").collect(Collectors.joining(", "));
        String fieldNames = String.join(", ", getFieldsName(entity));

        String sql = "INSERT INTO " + getTableName() + "(" + fieldNames + ") VALUES (" + paramPlaceholder + ")";
        executeUpdate(sql, fieldValues.toArray());
    }

    public Optional<T> findOne(String condition, Object... params) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + condition + " LIMIT 1";
        QueryResult<T> result = QueryResult.of(executeQuery(sql, params), clazz);
        return result.firstResultOptional();
    }

    public List<T> findAll(String condition, Object... params) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + condition;
        QueryResult<T> result = QueryResult.of(executeQuery(sql, params), clazz);
        return result.list();
    }

    public List<T> findAll() {
        String sql = "SELECT * FROM " + getTableName();
        QueryResult<T> result = QueryResult.of(executeQuery(sql), clazz);
        return result.list();
    }

    public void updateAll(T entity, String condition, Object... params) {
        List<Object> fieldValueParams = getFieldsValue(entity);
        fieldValueParams.addAll(Arrays.asList(params));
        String sql = "UPDATE " + getTableName() + " SET " + getColumnsUpdateMapping(entity) + " WHERE " + condition;
        executeUpdate(sql, fieldValueParams.toArray());
    }

    public void deleteAll(String condition, Object... params) {
        String sql = "DELETE FROM " + getTableName() + " WHERE " + condition;
        executeUpdate(sql, params);
    }

    public long count(String condition, Object... params) {
        String sql = "SELECT count(*) FROM " + getTableName() + " WHERE " + condition;
        QueryResult<Long> result = QueryResult.of(executeQuery(sql, params), Long.class);
        return result.firstResult();
    }

    public long count() {
        String sql = "SELECT count(*) FROM " + getTableName();
        QueryResult<Long> result = QueryResult.of(executeQuery(sql), Long.class);
        return result.firstResult();
    }

    public boolean exist(String condition, Object... params) {
        String sql = "SELECT 1 FROM " + getTableName() + " WHERE " + condition;
        QueryResult<Integer> result = QueryResult.of(executeQuery(sql, params), Integer.class);
        return result.firstResultOptional().isPresent();
    }

    protected ResultSet executeQuery(String sql) {
        try {
            return getConnection().prepareStatement(sql).executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected ResultSet executeQuery(String sql, Object... params) {
        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            return statement.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected int executeUpdate(String sql, Object... params) {
        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<Object> getFieldsValue(Object object) {
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

    protected List<String> getFieldsName(Object object) {
        Class<?> clazz = object.getClass();
        try {
            List<String> names = new ArrayList<>();
            for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(clazz, Object.class).getPropertyDescriptors()) {
                names.add(CaseUtils.toSnakeCase(propertyDescriptor.getName()));
            }
            return names;
        } catch (Exception e) {
            throw new RuntimeException("Cannot get fields of type: " + clazz.getName());
        }
    }

    protected String getColumnsUpdateMapping(Object object) {
        Class<?> clazz = object.getClass();
        try {
            return Arrays.stream(Introspector.getBeanInfo(clazz, Object.class).getPropertyDescriptors())
                    .map(FeatureDescriptor::getName)
                    .map(CaseUtils::toSnakeCase)
                    .map(name -> name + "=?")
                    .collect(Collectors.joining(","));
        } catch (IntrospectionException e) {
            throw new RuntimeException("Cannot get fields of type: " + clazz.getName());
        }
    }
}
