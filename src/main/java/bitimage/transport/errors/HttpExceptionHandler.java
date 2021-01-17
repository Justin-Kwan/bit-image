package bitimage.transport.errors;

import bitimage.domain.uploading.exceptions.ImageAlreadyExistsException;
import bitimage.domain.uploading.exceptions.ImageFormatInvalidException;
import bitimage.domain.uploading.exceptions.ImageNotFoundException;
import bitimage.domain.uploading.exceptions.ImageSizeExceededException;
import bitimage.domain.uploading.exceptions.UserAlreadyExistsException;
import bitimage.domain.uploading.exceptions.UserNotFoundException;
import io.micronaut.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpExceptionHandler {

  /** Global error handler that maps domain layer exceptions into http api response errors. */
  public static HttpResponse<Object> handle(Exception e) {
    HttpExceptionHandler.doLogError(e);

    if (e instanceof UnauthorizedException) {
      return ErrorResponses.unauthorizedAccess(e.getMessage());
    }
    if (e instanceof ImageNotFoundException) {
      return ErrorResponses.resourceNotFound(e.getMessage());
    }
    if (e instanceof UserNotFoundException) {
      return ErrorResponses.resourceNotFound(e.getMessage());
    }
    if (e instanceof ImageAlreadyExistsException) {
      return ErrorResponses.resourceAlreadyExists(e.getMessage());
    }
    if (e instanceof ImageSizeExceededException) {
      return ErrorResponses.resourceSizeExceedsLimit(e.getMessage());
    }
    if (e instanceof ImageFormatInvalidException) {
      return ErrorResponses.resourceMediaTypeUnsupported(e.getMessage());
    }
    if (e instanceof UserAlreadyExistsException) {
      return ErrorResponses.resourceAlreadyExists(e.getMessage());
    }

    return ErrorResponses.internalServerError();
  }

  /** Logs exception message and entire stack trace on error. */
  private static void doLogError(Exception e) {
    final String logErrorMessage = "Error occurred '%s'".formatted(e.getMessage());
    Logger.getLogger("Global transport error logger").log(Level.SEVERE, logErrorMessage, e);
  }
}
