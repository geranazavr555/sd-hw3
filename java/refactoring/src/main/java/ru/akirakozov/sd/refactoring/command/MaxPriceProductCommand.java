package ru.akirakozov.sd.refactoring.command;

import ru.akirakozov.sd.refactoring.dao.ProductDao;
import ru.akirakozov.sd.refactoring.render.Renderer;
import ru.akirakozov.sd.refactoring.view.MaxQueryView;

import javax.servlet.http.HttpServletRequest;

public class MaxPriceProductCommand implements Command {
    private final ProductDao productDao;

    public MaxPriceProductCommand(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public Renderer executeAndGetRenderer(HttpServletRequest request) {
        return productDao.findWithMaxPrice().map(MaxQueryView::new).orElseGet(MaxQueryView::new);
    }
}
