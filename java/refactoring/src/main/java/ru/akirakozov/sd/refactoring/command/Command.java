package ru.akirakozov.sd.refactoring.command;

import ru.akirakozov.sd.refactoring.render.Renderer;

import javax.servlet.http.HttpServletRequest;

public interface Command {
    Renderer executeAndGetRenderer(HttpServletRequest request);
}
