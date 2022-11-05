package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.command.GetProductsCommand;
import ru.akirakozov.sd.refactoring.dao.ProductDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends ApplicationServlet {
    private final GetProductsCommand getProductsCommand;

    public GetProductsServlet(ProductDao productDao) {
        getProductsCommand = new GetProductsCommand(productDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setupResponse(getProductsCommand.executeAndGetRenderer(request).render(), response);
    }
}
