package ru.akirakozov.sd.refactoring.command;

import ru.akirakozov.sd.refactoring.dao.ProductDao;
import ru.akirakozov.sd.refactoring.render.Renderer;
import ru.akirakozov.sd.refactoring.view.MinQueryView;

import javax.servlet.http.HttpServletRequest;

public class MinPriceProductCommand implements Command {
    private final ProductDao productDao;

    public MinPriceProductCommand(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public Renderer executeAndGetRenderer(HttpServletRequest request) {
        return productDao.findWithMinPrice().map(MinQueryView::new).orElseGet(MinQueryView::new);
    }
}
