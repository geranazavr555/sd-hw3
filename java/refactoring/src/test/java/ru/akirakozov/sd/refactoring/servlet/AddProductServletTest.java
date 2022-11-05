package ru.akirakozov.sd.refactoring.servlet;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.akirakozov.sd.refactoring.dao.impl.ProductDaoImpl;
import ru.akirakozov.sd.refactoring.db.Database;
import ru.akirakozov.sd.refactoring.model.Product;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddProductServletTest {
    private static Path tempDir;
    private static Database database;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private void executeUpdate(String sql) {
        database.executeUpdate(sql);
    }

    private List<Product> executeSelectAll() {
        return database.executeQuery("SELECT * FROM PRODUCT", resultSet -> {
            List<Product> result = new ArrayList<>();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int price = resultSet.getInt("price");
                result.add(new Product(name, price));
            }
            return result;
        });
    }

    @BeforeClass
    public static void initDbTempDir() {
        try {
            tempDir = Files.createTempDirectory(AddProductServletTest.class.getSimpleName());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        database = new Database("jdbc:sqlite:" + tempDir.resolve("tmp.db"));
    }

    @AfterClass
    public static void removeDbTempDir() {
        try {
            Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Before
    public void initTestDb() {
        executeUpdate("DROP TABLE IF EXISTS PRODUCT");
        executeUpdate("CREATE TABLE PRODUCT" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " NAME           TEXT    NOT NULL, " +
                " PRICE          INT     NOT NULL)");
    }

    private void callServletWithValidation(Product product) {
        when(request.getParameter("name")).thenReturn(product.getName());
        when(request.getParameter("price")).thenReturn(String.valueOf(product.getPrice()));

        boolean[] contentTypeSet = new boolean[]{false};
        boolean[] statusSet = new boolean[]{false};

        doAnswer(answer -> {
            String contentType = answer.getArgument(0);
            assertEquals("text/html", contentType);
            contentTypeSet[0] = true;
            return null;
        }).when(response).setContentType(anyString());

        doAnswer(answer -> {
            int statusCode = answer.getArgument(0);
            assertEquals(HttpServletResponse.SC_OK, statusCode);
            statusSet[0] = true;
            return null;
        }).when(response).setStatus(anyInt());

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        try {
            when(response.getWriter()).thenReturn(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try {
            new AddProductServlet(new ProductDaoImpl(database)).doGet(request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertTrue(contentTypeSet[0]);
        assertTrue(statusSet[0]);
        assertEquals("OK", stringWriter.getBuffer().toString().trim());
    }

    @Test
    public void addOne() {
        Product product = new Product("product1", 123);
        callServletWithValidation(product);
        List<Product> products = executeSelectAll();
        assertEquals(1, products.size());
        assertEquals(product, products.get(0));
    }

    @Test
    public void addMany() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("abacaba", 52654));
        products.add(new Product("fdsergqe", 5494));
        products.add(new Product("asdfq", 69871));
        products.add(new Product("asdfwegebvw", 26489));

        for (int i = 0; i < products.size(); i++) {
            callServletWithValidation(products.get(i));

            List<Product> dbProducts = executeSelectAll();
            assertEquals(i + 1, dbProducts.size());
            assertEquals(products.subList(0, i + 1), dbProducts);
        }
    }

    @Test
    public void stress() {
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < 100; i++)
            products.add(new Product(
                    UUID.randomUUID().toString(),
                    ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE)
            ));

        products.forEach(this::callServletWithValidation);

        List<Product> dbProducts = executeSelectAll();
        assertEquals(products, dbProducts);
    }
}
