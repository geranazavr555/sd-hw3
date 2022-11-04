package ru.akirakozov.sd.refactoring.db;

import java.sql.*;

public class Database {
    private final String connectionUrl;

    public Database(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public <R> R executeQuery(String sql, CheckedFunction<ResultSet, R> mapper) {
        try (
                Connection connection = DriverManager.getConnection(connectionUrl);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
        ) {
            return mapper.apply(resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int executeUpdate(String sql) {
        try (
                Connection connection = DriverManager.getConnection(connectionUrl);
                Statement statement = connection.createStatement();
        ) {
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }
}
