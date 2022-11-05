package ru.akirakozov.sd.refactoring.command;

import ru.akirakozov.sd.refactoring.dao.ProductDao;
import ru.akirakozov.sd.refactoring.render.Renderer;
import ru.akirakozov.sd.refactoring.view.GetProductsView;

import javax.servlet.http.HttpServletRequest;

public class GetProductsCommand implements Command {
    private final ProductDao productDao;

    public GetProductsCommand(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public Renderer executeAndGetRenderer(HttpServletRequest request) {
        return new GetProductsView(productDao.findAll());
    }
}
