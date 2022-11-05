package ru.akirakozov.sd.refactoring.dao.impl;

import ru.akirakozov.sd.refactoring.dao.ProductDao;
import ru.akirakozov.sd.refactoring.db.Database;
import ru.akirakozov.sd.refactoring.model.Product;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ProductDaoImpl implements ProductDao {
    private static final String TABLE_NAME = "PRODUCT";

    private final Database db;

    public ProductDaoImpl(Database db) {
        this.db = db;
    }

    @Override
    public void insert(Product product) {
        db.executeUpdate("INSERT INTO " + TABLE_NAME + " (NAME, PRICE) VALUES " +
                "(\"" + product.getName() + "\"," + product.getPrice() + ")");
    }

    @Override
    public List<Product> findAll() {
        return db.executeQuery(select(""), this::mapToProductList);
    }

    @Override
    public int findCount() {
        return db.executeQuery("SELECT COUNT(*) FROM " + TABLE_NAME, this::mapToInt);
    }

    @Override
    public long findSumPrice() {
        return db.executeQuery("SELECT SUM(price) FROM " + TABLE_NAME, this::mapToLong);
    }

    @Override
    public Optional<Product> findWithMaxPrice() {
        return db.executeQuery(select("ORDER BY PRICE DESC LIMIT 1"), this::mapToProduct);
    }

    @Override
    public Optional<Product> findWithMinPrice() {
        return db.executeQuery(select("ORDER BY PRICE LIMIT 1"), this::mapToProduct);
    }

    private String select(String query) {
        return "SELECT * FROM " + TABLE_NAME + " " + query;
    }

    private long mapToLong(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return resultSet.getLong(1);
        }
        return 0;
    }

    private int mapToInt(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return resultSet.getInt(1);
        }
        return 0;
    }

    private Product internalMapToProduct(ResultSet resultSet) throws SQLException {
        String  name = resultSet.getString("name");
        int price  = resultSet.getInt("price");
        return new Product(name, price);
    }

    private Optional<Product> mapToProduct(ResultSet resultSet) throws SQLException {
        if (resultSet.next())
            return Optional.of(internalMapToProduct(resultSet));
        return Optional.empty();
    }

    private List<Product> mapToProductList(ResultSet resultSet) throws SQLException {
        List<Product> products = new ArrayList<>();
        while (resultSet.next())
            products.add(internalMapToProduct(resultSet));
        return Collections.unmodifiableList(products);
    }
}
