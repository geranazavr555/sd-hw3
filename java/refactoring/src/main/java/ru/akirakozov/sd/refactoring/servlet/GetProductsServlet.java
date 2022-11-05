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
    private final ProductDao productDao;

    public GetProductsServlet(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setupResponse(new GetProductsCommand(productDao).executeAndGetRenderer(request).render(), response);
    }
}
