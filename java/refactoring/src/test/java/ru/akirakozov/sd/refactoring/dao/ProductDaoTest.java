package ru.akirakozov.sd.refactoring.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.akirakozov.sd.refactoring.dao.impl.ProductDaoImpl;
import ru.akirakozov.sd.refactoring.db.Database;
import ru.akirakozov.sd.refactoring.model.Product;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductDaoTest {
    private static final String INSERT_SQL_PREFIX = "INSERT INTO PRODUCT (NAME, PRICE) VALUES";
    private static final String FIND_SQL_PREFIX = "SELECT * FROM PRODUCT";
    private static final String FIND_COUNT_SQL = "SELECT COUNT(*) FROM PRODUCT";
    private static final String FIND_SUM_PRICE_SQL = "SELECT SUM(price) FROM PRODUCT";
    private static final String ORDER_CLAUSE = "ORDER BY PRICE";
    private static final String DESC_CLAUSE = "DESC";
    private static final String LIMIT_CLAUSE = "LIMIT 1";

    @Mock
    private Database database;

    private List<Product> products;

    private ProductDao productDao;

    @Before
    public void init() {
        products = new ArrayList<>();

        doAnswer(answer -> {
            String sql = answer.getArgument(0, String.class);
            String itemSql = sql.substring(
                    sql.indexOf("(", INSERT_SQL_PREFIX.length() + 1) + 1,
                    sql.lastIndexOf(")")
            );

            String[] tokens = itemSql.split("\\s*,\\s*");
            String name = tokens[0].substring(1, tokens[0].length() - 1);
            long price = Long.parseLong(tokens[1]);

            products.add(new Product(name, price));

            return null;
        }).when(database).executeUpdate(startsWith(INSERT_SQL_PREFIX));

        doAnswer(answer -> products.size()).when(database).executeQuery(eq(FIND_COUNT_SQL), any());

        doAnswer(answer -> products.stream().mapToLong(Product::getPrice).sum())
                .when(database)
                .executeQuery(eq(FIND_SUM_PRICE_SQL), any());

        doAnswer(answer -> {
            String sql = answer.getArgument(0, String.class);
            sql = sql.substring(FIND_SQL_PREFIX.length() + 1);

            if (sql.isEmpty())
                return Collections.unmodifiableList(new ArrayList<>(products));

            if (!sql.startsWith(ORDER_CLAUSE))
                throw new UnsupportedOperationException("Operation is not mocked");

            sql = sql.substring(ORDER_CLAUSE.length() + 1);

            boolean desc = false;
            if (sql.startsWith(DESC_CLAUSE)) {
                desc = true;
                sql = sql.substring(DESC_CLAUSE.length() + 1);
            }

            if (!LIMIT_CLAUSE.equals(sql))
                throw new UnsupportedOperationException("Operation is not mocked");

            Comparator<Product> comparator = Comparator.comparing(Product::getPrice);
            if (desc)
                comparator = comparator.reversed();

            return products.stream().min(comparator);

        }).when(database).executeQuery(startsWith(FIND_SQL_PREFIX), any());

        productDao = new ProductDaoImpl(database);
    }

    @Test
    public void testInsert() {
        List<Product> expected = new ArrayList<>();
        expected.add(new Product("abcde", 555));
        expected.add(new Product("qwerty", 123));

        expected.forEach(productDao::insert);

        assertEquals(expected, products);
    }

    @Test
    public void testEmptyCount() {
        int count = productDao.findCount();
        assertEquals(0, count);
    }

    @Test
    public void testEmptyAll() {
        assertTrue(productDao.findAll().isEmpty());
    }

    @Test
    public void testEmptyMax() {
        assertFalse(productDao.findWithMaxPrice().isPresent());
    }

    @Test
    public void testEmptyMin() {
        assertFalse(productDao.findWithMinPrice().isPresent());
    }

    @Test
    public void testEmptySum() {
        assertEquals(0L, productDao.findSumPrice());
    }

    @Test
    public void testSimpleAll() {
        products.add(new Product("abcde", 555));
        products.add(new Product("qwerty", 123));
        assertEquals(products, productDao.findAll());
    }

    @Test
    public void testSimpleMax() {
        products.add(new Product("abcde", 555));
        products.add(new Product("qwerty", 123));
        Optional<Product> product = productDao.findWithMaxPrice();
        assertTrue(product.isPresent());
        assertEquals(products.get(0), product.get());
    }

    @Test
    public void testSimpleMin() {
        products.add(new Product("abcde", 555));
        products.add(new Product("qwerty", 123));
        Optional<Product> product = productDao.findWithMinPrice();
        assertTrue(product.isPresent());
        assertEquals(products.get(1), product.get());
    }

    @Test
    public void testSimpleCount() {
        products.add(new Product("abcde", 555));
        products.add(new Product("qwerty", 123));
        assertEquals(2, productDao.findCount());
    }

    @Test
    public void testSimpleSum() {
        products.add(new Product("abcde", 555));
        products.add(new Product("qwerty", 123));
        assertEquals(678L, productDao.findSumPrice());
    }
    
    @Test
    public void stress() {
        List<Product> expectedProducts = new ArrayList<>();

        for (int i = 0; i < 100; i++)
            expectedProducts.add(new Product(
                    UUID.randomUUID().toString(),
                    ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE)
            ));

        for (int i = 0; i < 100; i++) {
            productDao.insert(expectedProducts.get(i));
            assertEquals(expectedProducts.subList(0, i + 1), productDao.findAll());
            assertEquals(
                    expectedProducts.subList(0, i + 1).stream().max(Comparator.comparing(Product::getPrice)),
                    productDao.findWithMaxPrice()
            );
            assertEquals(
                    expectedProducts.subList(0, i + 1).stream().min(Comparator.comparing(Product::getPrice)),
                    productDao.findWithMinPrice()
            );
            assertEquals(
                    expectedProducts.subList(0, i + 1).stream().mapToLong(Product::getPrice).sum(),
                    productDao.findSumPrice()
            );
            assertEquals(i + 1, productDao.findCount());
        }
    }
}
