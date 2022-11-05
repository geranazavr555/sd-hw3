package ru.akirakozov.sd.refactoring.command;

import ru.akirakozov.sd.refactoring.dao.ProductDao;
import ru.akirakozov.sd.refactoring.render.Renderer;
import ru.akirakozov.sd.refactoring.view.SumQueryView;

import javax.servlet.http.HttpServletRequest;

public class SumPriceCommand implements Command {
    private ProductDao productDao;

    public SumPriceCommand(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public Renderer executeAndGetRenderer(HttpServletRequest request) {
        return new SumQueryView(productDao.findSumPrice());
    }
}
