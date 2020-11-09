package io.lana.sqlstarter.dao;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QueryResult<T> {
    private final List<T> resultList = new ArrayList<>();

    public static class MappingConfig {
        private boolean autoSnakeCase = true;

        private String prefix = "";

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public boolean isAutoSnakeCase() {
            return autoSnakeCase;
        }

        public void setAutoSnakeCase(boolean autoSnakeCase) {
            this.autoSnakeCase = autoSnakeCase;
        }
    }

    private QueryResult(ResultSet resultSet, Class<T> clazz) {
        this(resultSet, clazz, new MappingConfig());
    }


    private QueryResult(ResultSet resultSet, Class<T> clazz, MappingConfig config) {
        try {
            while (resultSet.next()) {
                if (isPrimitive(clazz)) {
                    resultList.add(resultSet.getObject(1, clazz));
                    continue;
                }

                T row = clazz.getConstructor().newInstance();
                PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz, Object.class).getPropertyDescriptors();
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    String fieldName = propertyDescriptor.getName();
                    Class<?> fieldType = propertyDescriptor.getPropertyType();

                    String colName = toColumnName(fieldName, config);
                    Object colValue = resultSet.getObject(colName, fieldType);
                    Method setter = propertyDescriptor.getWriteMethod();
                    setter.invoke(row, colValue);
                }
                resultList.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot extract result set", e);
        }
    }

    private boolean isPrimitive(Class<?> clazz) {
        return String.class.isAssignableFrom(clazz) ||
               Number.class.isAssignableFrom(clazz) ||
               Boolean.class.isAssignableFrom(clazz);
    }

    public static <T> QueryResult<T> of(ResultSet resultSet, Class<T> clazz) {
        return new QueryResult<>(resultSet, clazz);
    }

    public static <T> QueryResult<T> of(ResultSet resultSet, Class<T> clazz, MappingConfig config) {
        return new QueryResult<>(resultSet, clazz, config);
    }

    public T firstResult() {
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    public Optional<T> firstResultOptional() {
        return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList.get(0));
    }

    public List<T> list() {
        return new ArrayList<>(resultList);
    }

    private String toColumnName(String fieldName, MappingConfig config) {
        fieldName = config.getPrefix() + fieldName;
        return config.isAutoSnakeCase() ? CaseUtils.toSnakeCase(fieldName) : fieldName;
    }
}
