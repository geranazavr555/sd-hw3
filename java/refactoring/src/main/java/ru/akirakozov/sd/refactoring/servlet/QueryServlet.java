package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.dao.ProductDao;
import ru.akirakozov.sd.refactoring.view.CountQueryView;
import ru.akirakozov.sd.refactoring.view.MaxQueryView;
import ru.akirakozov.sd.refactoring.view.MinQueryView;
import ru.akirakozov.sd.refactoring.view.SumQueryView;

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
            setupResponse(
                    productDao.findWithMaxPrice().map(MaxQueryView::new).orElseGet(MaxQueryView::new).render(),
                    response
            );
        } else if ("min".equals(command)) {
            setupResponse(
                    productDao.findWithMinPrice().map(MinQueryView::new).orElseGet(MinQueryView::new).render(),
                    response
            );
        } else if ("sum".equals(command)) {
            setupResponse(new SumQueryView(productDao.findSumPrice()).render(), response);
        } else if ("count".equals(command)) {
            setupResponse(new CountQueryView(productDao.findCount()).render(), response);
        } else {
            setupResponse("Unknown command: " + command, response);
        }
    }
}
