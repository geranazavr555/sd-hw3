package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.command.CountProductsCommand;
import ru.akirakozov.sd.refactoring.command.MaxPriceProductCommand;
import ru.akirakozov.sd.refactoring.command.MinPriceProductCommand;
import ru.akirakozov.sd.refactoring.command.SumPriceCommand;
import ru.akirakozov.sd.refactoring.dao.ProductDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author akirakozov
 */
public class QueryServlet extends ApplicationServlet {
    private final ProductDao productDao;

    public QueryServlet(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");

        if ("max".equals(command)) {
            setupResponse(new MaxPriceProductCommand(productDao).executeAndGetRenderer(request).render(), response);
        } else if ("min".equals(command)) {
            setupResponse(new MinPriceProductCommand(productDao).executeAndGetRenderer(request).render(), response);
        } else if ("sum".equals(command)) {
            setupResponse(new SumPriceCommand(productDao).executeAndGetRenderer(request).render(), response);
        } else if ("count".equals(command)) {
            setupResponse(new CountProductsCommand(productDao).executeAndGetRenderer(request).render(), response);
        } else {
            setupResponse("Unknown command: " + command, response);
        }
    }
}
