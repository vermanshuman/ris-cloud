package it.nexera.ris.web.handlers;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

public class ExceptionHandlerFactoryEx extends ExceptionHandlerFactory {
    private ExceptionHandlerFactory exceptionHandlerFactory;

    public ExceptionHandlerFactoryEx(
            ExceptionHandlerFactory exceptionHandlerFactory) {
        System.out.println("Initializing ExceptionHandlerFactoryEx");
        this.exceptionHandlerFactory = exceptionHandlerFactory;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        ExceptionHandler result = exceptionHandlerFactory.getExceptionHandler();
        result = new ExceptionHandlerEx(result);
        return result;
    }
}
