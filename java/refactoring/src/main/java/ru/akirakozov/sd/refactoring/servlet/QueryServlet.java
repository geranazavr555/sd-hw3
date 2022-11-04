package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.db.Database;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author akirakozov
 */
public class QueryServlet extends HttpServlet {
    private final Database db;

    public QueryServlet(Database db) {
        this.db = db;
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");

        if ("max".equals(command)) {
            db.executeQuery("SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1", resultSet -> {
                response.getWriter().println("<html><body>");
                response.getWriter().println("<h1>Product with max price: </h1>");

                while (resultSet.next()) {
                    String  name = resultSet.getString("name");
                    int price  = resultSet.getInt("price");
                    response.getWriter().println(name + "\t" + price + "</br>");
                }
                response.getWriter().println("</body></html>");
                return null;
            });
        } else if ("min".equals(command)) {
            db.executeQuery("SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1", resultSet -> {
                response.getWriter().println("<html><body>");
                response.getWriter().println("<h1>Product with min price: </h1>");

                while (resultSet.next()) {
                    String  name = resultSet.getString("name");
                    int price  = resultSet.getInt("price");
                    response.getWriter().println(name + "\t" + price + "</br>");
                }
                response.getWriter().println("</body></html>");
                return null;
            });
        } else if ("sum".equals(command)) {
            db.executeQuery("SELECT SUM(price) FROM PRODUCT", resultSet -> {
                response.getWriter().println("<html><body>");
                response.getWriter().println("Summary price: ");

                if (resultSet.next()) {
                    response.getWriter().println(resultSet.getInt(1));
                }
                response.getWriter().println("</body></html>");
                return null;
            });
        } else if ("count".equals(command)) {
            db.executeQuery("SELECT COUNT(*) FROM PRODUCT", resultSet -> {
                response.getWriter().println("<html><body>");
                response.getWriter().println("Number of products: ");

                if (resultSet.next()) {
                    response.getWriter().println(resultSet.getInt(1));
                }
                response.getWriter().println("</body></html>");
                return null;
            });
        } else {
            response.getWriter().println("Unknown command: " + command);
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
