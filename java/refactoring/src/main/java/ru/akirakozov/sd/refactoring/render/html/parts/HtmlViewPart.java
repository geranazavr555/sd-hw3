package ru.akirakozov.sd.refactoring.render.html.parts;

public abstract class HtmlViewPart {
    public abstract String getHtml();

    @Override
    public String toString() {
        return getHtml();
    }

    public static HtmlViewPart of(Object value) {
        return new HtmlViewPart() {
            @Override
            public String getHtml() {
                return String.valueOf(value);
            }
        };
    }
}
