package ru.akirakozov.sd.refactoring;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.akirakozov.sd.refactoring.dao.ProductDao;
import ru.akirakozov.sd.refactoring.dao.impl.ProductDaoImpl;
import ru.akirakozov.sd.refactoring.db.Database;
import ru.akirakozov.sd.refactoring.servlet.AddProductServlet;
import ru.akirakozov.sd.refactoring.servlet.GetProductsServlet;
import ru.akirakozov.sd.refactoring.servlet.QueryServlet;

import java.util.Properties;

/**
 * @author akirakozov
 */
public class Main {
    private static final Properties properties = new Properties();

    public static void main(String[] args) throws Exception {
        properties.load(Main.class.getResourceAsStream("/db.properties"));
        String dbConnectionUrl = properties.getProperty("connection_url");
        Database database = new Database(dbConnectionUrl);
        ProductDao productDao = new ProductDaoImpl(database);

        Server server = new Server(8081);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new AddProductServlet(productDao)), "/add-product");
        context.addServlet(new ServletHolder(new GetProductsServlet(productDao)),"/get-products");
        context.addServlet(new ServletHolder(new QueryServlet(productDao)),"/query");

        server.start();
        server.join();
    }
}
