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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GetProductsServletTest {
    private static Database database;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeClass
    public static void initDbTempDir() {
        database = TestDbUtil.acquireTestDb(GetProductsServletTest.class);
    }

    @AfterClass
    public static void removeDbTempDir() {
       TestDbUtil.releaseTestDb(GetProductsServletTest.class);
    }

    @Before
    public void initTestDb() {
        TestDbUtil.initTestDb(database);
    }

    private void addProduct(Product product) {
        TestDbUtil.addProduct(database, product);
    }

    private List<Product> callServletWithValidationAndGetItems() {
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
            new GetProductsServlet(new ProductDaoImpl(database)).doGet(request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertTrue(contentTypeSet[0]);
        assertTrue(statusSet[0]);

        return parseAndValidateHtml(stringWriter.getBuffer().toString().trim());
    }

    private List<Product> parseAndValidateHtml(String htmlResponse) {
        htmlResponse = htmlResponse.replaceAll("(\r)?\n", "");

        assertTrue(htmlResponse.startsWith("<html>"));
        assertTrue(htmlResponse.endsWith("</html>"));

        htmlResponse = htmlResponse.substring("<html>".length(), htmlResponse.length() - "</html>".length()).trim();
        assertTrue(htmlResponse.startsWith("<body>"));
        assertTrue(htmlResponse.endsWith("</body>"));

        htmlResponse = htmlResponse.substring("<body>".length(), htmlResponse.length() - "</body>".length()).trim();

        if (htmlResponse.isEmpty())
            return Collections.emptyList();

        List<Product> result = new ArrayList<>();
        for (String item : htmlResponse.split("</br>")) {
            String[] nameAndPrice = item.split("\t");
            result.add(new Product(nameAndPrice[0], Integer.parseInt(nameAndPrice[1])));
        }

        return result;
    }

    @Test
    public void testEmpty() {
        List<Product> products = callServletWithValidationAndGetItems();
        assertTrue(products.isEmpty());
    }

    @Test
    public void testSimple() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("abacaba", 555));
        products.add(new Product("cabab", 1337));
        products.add(new Product("aaaaaaa", 15432));
        products.forEach(this::addProduct);

        List<Product> returnedProducts = callServletWithValidationAndGetItems();
        assertEquals(products, returnedProducts);
    }

    @Test
    public void stress() {
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < 100; i++)
            products.add(new Product(
                    UUID.randomUUID().toString(),
                    ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE)
            ));

        products.forEach(this::addProduct);

        List<Product> returnedProducts = callServletWithValidationAndGetItems();
        assertEquals(products, returnedProducts);
    }
}
