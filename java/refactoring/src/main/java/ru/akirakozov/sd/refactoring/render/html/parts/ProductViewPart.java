package ru.akirakozov.sd.refactoring.render.html.parts;

import ru.akirakozov.sd.refactoring.model.Product;

public class ProductViewPart extends HtmlViewPart {
    private final Product product;

    public ProductViewPart(Product product) {
        this.product = product;
    }

    @Override
    public String getHtml() {
        return product.getName() + "\t" + product.getPrice() + "</br>";
    }

    public static ProductViewPart of(Product product) {
        return new ProductViewPart(product);
    }
}
