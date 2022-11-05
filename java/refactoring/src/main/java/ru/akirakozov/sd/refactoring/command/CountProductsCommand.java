package ru.akirakozov.sd.refactoring.command;

import ru.akirakozov.sd.refactoring.dao.ProductDao;
import ru.akirakozov.sd.refactoring.render.Renderer;
import ru.akirakozov.sd.refactoring.view.CountQueryView;

import javax.servlet.http.HttpServletRequest;

public class CountProductsCommand implements Command {
    private final ProductDao productDao;

    public CountProductsCommand(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public Renderer executeAndGetRenderer(HttpServletRequest request) {
        return new CountQueryView(productDao.findCount());
    }
}
