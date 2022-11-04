package ru.akirakozov.sd.refactoring;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.akirakozov.sd.refactoring.db.Database;
import ru.akirakozov.sd.refactoring.servlet.AddProductServlet;
import ru.akirakozov.sd.refactoring.servlet.GetProductsServlet;
import ru.akirakozov.sd.refactoring.servlet.QueryServlet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author akirakozov
 */
public class Main {
    private static final Properties properties = new Properties();

    public static void main(String[] args) throws Exception {
        properties.load(Main.class.getResourceAsStream("/db.properties"));
        String dbConnectionUrl = properties.getProperty("connection_url");

        try (Connection c = DriverManager.getConnection(dbConnectionUrl)) {
            String sql = "CREATE TABLE IF NOT EXISTS PRODUCT" +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    " NAME           TEXT    NOT NULL, " +
                    " PRICE          INT     NOT NULL)";
            Statement stmt = c.createStatement();

            stmt.executeUpdate(sql);
            stmt.close();
        }

        Server server = new Server(8081);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        Database database = new Database(dbConnectionUrl);
        context.addServlet(new ServletHolder(new AddProductServlet(database)), "/add-product");
        context.addServlet(new ServletHolder(new GetProductsServlet(database)),"/get-products");
        context.addServlet(new ServletHolder(new QueryServlet(database)),"/query");

        server.start();
        server.join();
    }
}
