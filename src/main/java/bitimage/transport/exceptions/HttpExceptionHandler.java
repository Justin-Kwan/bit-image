package bitimage.transport.exceptions;

import bitimage.domain.uploading.exceptions.ImageAlreadyExistsException;
import bitimage.domain.uploading.exceptions.ImageFormatInvalidException;
import bitimage.domain.uploading.exceptions.ImageNotFoundException;
import bitimage.domain.uploading.exceptions.ImageSizeExceededException;
import bitimage.domain.uploading.exceptions.UserAlreadyExistsException;
import bitimage.domain.uploading.exceptions.UserNotFoundException;
import io.micronaut.http.HttpResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpExceptionHandler
{
    /**
     * Global error handler that maps domain layer
     * exceptions into HTTP API response errors.
     */
    public static HttpResponse<Object> handle(Exception e)
    {
        HttpExceptionHandler.doLogError(e);

        if (e instanceof UnauthorizedException) {
            return ExceptionResponses.unauthorizedAccess(e.getMessage());
        }
        if (e instanceof ImageNotFoundException) {
            return ExceptionResponses.resourceNotFound(e.getMessage());
        }
        if (e instanceof UserNotFoundException) {
            return ExceptionResponses.resourceNotFound(e.getMessage());
        }
        if (e instanceof ImageAlreadyExistsException) {
            return ExceptionResponses.resourceAlreadyExists(e.getMessage());
        }
        if (e instanceof ImageSizeExceededException) {
            return ExceptionResponses.resourceSizeExceedsLimit(e.getMessage());
        }
        if (e instanceof ImageFormatInvalidException) {
            return ExceptionResponses.resourceMediaTypeUnsupported(e.getMessage());
        }
        if (e instanceof UserAlreadyExistsException) {
            return ExceptionResponses.resourceAlreadyExists(e.getMessage());
        }

        return ExceptionResponses.internalServerError();
    }

    /**
     * Logs exception message and entire stack trace on error.
     */
    private static void doLogError(Exception e)
    {
        Logger
                .getLogger("Global transport error logger")
                .log(Level.SEVERE, String.format("Error occurred '%s'", e.getMessage()), e);
    }
}
