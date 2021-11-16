package bitimage.transport.controllers;

import bitimage.transport.exceptions.HttpExceptionHandler;
import bitimage.transport.middleware.ITokenChecker;
import io.micronaut.http.HttpResponse;

public abstract class BaseController
{
    protected final ITokenChecker tokenChecker;

    protected BaseController(ITokenChecker tokenChecker)
    {
        this.tokenChecker = tokenChecker;
    }

    /**
     * Wraps service core exception handling and translation to http responses.
     */
    protected HttpResponse<Object> handleRequest(IRequestHandler requestHandler)
    {
        HttpResponse<Object> httpResponse;

        try {
            httpResponse = requestHandler.fn();
        }
        catch (Exception e) {
            httpResponse = HttpExceptionHandler.handle(e);
        }

        return httpResponse;
    }
}

interface IRequestHandler
{
    HttpResponse<Object> fn()
            throws Exception;
}
