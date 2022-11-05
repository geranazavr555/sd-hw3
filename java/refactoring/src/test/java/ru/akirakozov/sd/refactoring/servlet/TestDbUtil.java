package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.db.Database;
import ru.akirakozov.sd.refactoring.model.Product;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestDbUtil {
    private static final Path tempDir;

    private static Path getDbPath(Class<?> testClass) {
        return tempDir.resolve(testClass.getSimpleName() + ".db");
    }

    public static Database acquireTestDb(Class<?> testClass) {
        return new Database("jdbc:sqlite:" + getDbPath(testClass));
    }

    public static void releaseTestDb(Class<?> testClass) {
        try {
            Files.delete(getDbPath(testClass));
        } catch (IOException ignored) {
            // No operations
        }
    }

    public static void initTestDb(Database database) {
        database.executeUpdate("DROP TABLE IF EXISTS PRODUCT");
        database.executeUpdate("CREATE TABLE PRODUCT" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " NAME           TEXT    NOT NULL, " +
                " PRICE          INT     NOT NULL)");
    }

    public static void addProduct(Database database, Product product) {
        database.executeUpdate(String.format("INSERT INTO PRODUCT(NAME, PRICE) VALUES ('%s', %d)",
                product.getName(), product.getPrice()));
    }

    private TestDbUtil() {
    }

    static {
        try {
            tempDir = Files.createTempDirectory(TestDbUtil.class.getSimpleName());
            tempDir.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
