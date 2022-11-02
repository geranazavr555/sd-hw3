package ru.akirakozov.sd.refactoring.servlet;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryServletTest {
    private static String testDbUrl;
    private static Path tempDir;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private void executeUpdate(String sql) {
        try (Connection connection = DriverManager.getConnection(testDbUrl)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Product> executeSelectAll() {
        try (Connection c = DriverManager.getConnection(testDbUrl)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCT");
            List<Product> result = new ArrayList<>();
            while (rs.next()) {
                String  name = rs.getString("name");
                int price  = rs.getInt("price");
                result.add(new Product(name, price));
            }
            rs.close();
            stmt.close();
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeClass
    public static void initDbTempDir() {
        try {
            tempDir = Files.createTempDirectory(AddProductServletTest.class.getSimpleName());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        testDbUrl = "jdbc:sqlite:" + tempDir.resolve("tmp.db");
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

    private void addProduct(Product product) {
        executeUpdate(String.format("INSERT INTO PRODUCT(NAME, PRICE) VALUES ('%s', %d)", product.name, product.price));
    }

    private String callServletWithValidationAndGetResult(String command) {
        when(request.getParameter("command")).thenReturn(command);

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
            new QueryServlet(testDbUrl).doGet(request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertTrue(contentTypeSet[0]);
        assertTrue(statusSet[0]);

        return parseAndValidateHtml(stringWriter.getBuffer().toString().trim());
    }

    private String parseAndValidateHtml(String htmlResponse) {
        htmlResponse = htmlResponse.replaceAll("(\r)?\n", "");

        assertTrue(htmlResponse.startsWith("<html>"));
        assertTrue(htmlResponse.endsWith("</html>"));

        htmlResponse = htmlResponse.substring("<html>".length(), htmlResponse.length() - "</html>".length()).trim();
        assertTrue(htmlResponse.startsWith("<body>"));
        assertTrue(htmlResponse.endsWith("</body>"));

        return htmlResponse.substring("<body>".length(), htmlResponse.length() - "</body>".length()).trim();
    }

    @Test
    public void testEmptyMax() {
        String result = callServletWithValidationAndGetResult("max");
        assertEquals("<h1>Product with max price: </h1>", result);
    }

    @Test
    public void testEmptyMin() {
        String result = callServletWithValidationAndGetResult("min");
        assertEquals("<h1>Product with min price: </h1>", result);
    }

    @Test
    public void testEmptySum() {
        String result = callServletWithValidationAndGetResult("sum");
        assertEquals("Summary price: 0", result);
    }

    @Test
    public void testEmptyCount() {
        String result = callServletWithValidationAndGetResult("count");
        assertEquals("Number of products: 0", result);
    }

    private TestData prepareData(int count) {
        TestData data = new TestData();

        for (int i = 0; i < count; i++) {
            data.products.add(new Product(
                    UUID.randomUUID().toString(),
                    ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE)
            ));
        }

        data.products.forEach(this::addProduct);

        return data;
    }

    @Test
    public void testSimple() {
        TestData testData = prepareData(5);

        String result = callServletWithValidationAndGetResult("count");
        assertEquals("Number of products: " + testData.products.size(), result);

        result = callServletWithValidationAndGetResult("sum");
        assertEquals("Summary price: " + testData.getPriceSum(), result);

        result = callServletWithValidationAndGetResult("min");
        assertEquals("<h1>Product with min price: </h1>" + testData.getMinPriceProduct() + "</br>", result);

        result = callServletWithValidationAndGetResult("max");
        assertEquals("<h1>Product with max price: </h1>" + testData.getMaxPriceProduct() + "</br>", result);
    }

    @Test
    public void stress() {
        TestData testData = prepareData(1000);

        String result = callServletWithValidationAndGetResult("count");
        assertEquals("Number of products: " + testData.products.size(), result);

        result = callServletWithValidationAndGetResult("sum");
        assertEquals("Summary price: " + testData.getPriceSum(), result);

        result = callServletWithValidationAndGetResult("min");
        assertEquals("<h1>Product with min price: </h1>" + testData.getMinPriceProduct() + "</br>", result);

        result = callServletWithValidationAndGetResult("max");
        assertEquals("<h1>Product with max price: </h1>" + testData.getMaxPriceProduct() + "</br>", result);
    }

    private static class TestData {
        private final List<Product> products = new ArrayList<>();

        private int getPriceSum() {
            return products.stream().mapToInt(product -> product.price).sum();
        }

        private Product getMinPriceProduct() {
            return products.stream().min(Comparator.comparing(product -> product.price)).orElse(null);
        }

        private Product getMaxPriceProduct() {
            return products.stream().max(Comparator.comparing(product -> product.price)).orElse(null);
        }

        private int count() {
            return products.size();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestData)) return false;
            TestData testData = (TestData) o;
            return products.equals(testData.products);
        }

        @Override
        public int hashCode() {
            return Objects.hash(products);
        }
    }

    @SuppressWarnings("NewClassNamingConvention")
    private static class Product {
        private final String name;
        private final int price;

        private Product(String name, int price) {
            this.name = name;
            this.price = price;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Product)) return false;
            Product product = (Product) o;
            return price == product.price && name.equals(product.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, price);
        }

        @Override
        public String toString() {
            return name + "\t" + price;
        }
    }
}
