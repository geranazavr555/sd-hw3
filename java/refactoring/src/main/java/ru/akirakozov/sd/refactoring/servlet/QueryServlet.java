package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.dao.ProductDao;
import ru.akirakozov.sd.refactoring.db.Database;
import ru.akirakozov.sd.refactoring.model.Product;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

/**
 * @author akirakozov
 */
public class QueryServlet extends HttpServlet {
    private final ProductDao productDao;

    public QueryServlet(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");

        PrintWriter writer;
        try {
            writer = response.getWriter();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        writer.println("<html><body>");

        if ("max".equals(command)) {
            Optional<Product> optionalProduct = productDao.findWithMaxPrice();
            writer.println("<h1>Product with max price: </h1>");
            optionalProduct.ifPresent(product -> writer.println(product.getName() + "\t" + product.getPrice() + "</br>"));

        } else if ("min".equals(command)) {
            Optional<Product> optionalProduct = productDao.findWithMinPrice();
            writer.println("<h1>Product with min price: </h1>");
            optionalProduct.ifPresent(product -> writer.println(product.getName() + "\t" + product.getPrice() + "</br>"));

        } else if ("sum".equals(command)) {
            writer.println("Summary price: ");
            writer.println(productDao.findSumPrice());

        } else if ("count".equals(command)) {
            writer.println("Number of products: ");
            writer.println(productDao.findCount());

        } else {
            writer.println("Unknown command: " + command);
        }

        writer.println("</body></html>");
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

    }
}
