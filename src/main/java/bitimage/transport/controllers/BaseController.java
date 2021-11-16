package bitimage.transport.controllers;

import bitimage.transport.exceptions.HttpExceptionHandler;
import bitimage.transport.middleware.TokenChecker;
import io.micronaut.http.HttpResponse;

public abstract class BaseController
{
    protected final TokenChecker tokenChecker;

    protected BaseController(TokenChecker tokenChecker)
    {
        this.tokenChecker = tokenChecker;
    }

    /**
     * Wraps service core exception handling and translation to http responses.
     */
    protected HttpResponse<Object> handleRequest(RequestHandler requestHandler)
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

interface RequestHandler
{
    HttpResponse<Object> fn()
            throws Exception;
}
