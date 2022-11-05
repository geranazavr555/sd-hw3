package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.command.*;
import ru.akirakozov.sd.refactoring.dao.ProductDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author akirakozov
 */
public class QueryServlet extends ApplicationServlet {
    private final Map<String, Command> commandMap;

    public QueryServlet(ProductDao productDao) {
        this.commandMap = new HashMap<>();
        commandMap.put("max", new MaxPriceProductCommand(productDao));
        commandMap.put("min", new MinPriceProductCommand(productDao));
        commandMap.put("sum", new SumPriceCommand(productDao));
        commandMap.put("count", new CountProductsCommand(productDao));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");

        if (commandMap.containsKey(command)) {
            setupResponse(commandMap.get(command).executeAndGetRenderer(request).render(), response);
        } else {
            setupResponse("Unknown command: " + command, response);
        }
    }
}
