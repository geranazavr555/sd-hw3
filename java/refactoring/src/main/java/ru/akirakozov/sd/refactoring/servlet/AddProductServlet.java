package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.command.AddProductCommand;
import ru.akirakozov.sd.refactoring.dao.ProductDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author akirakozov
 */
public class AddProductServlet extends ApplicationServlet {
    private final ProductDao productDao;

    public AddProductServlet(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setupResponse(new AddProductCommand(productDao).executeAndGetRenderer(request).render(), response);
    }
}
