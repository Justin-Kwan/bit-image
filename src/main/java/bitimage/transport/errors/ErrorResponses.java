package bitimage.transport.errors;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;

public class ErrorResponses {

  public static HttpResponse<Object> unauthorizedAccess(String message) {
    final String responseBody = """
      {
          \"error\": \"Unauthorized access\",
          \"message\": \"%s\",
          \"detail\": \"Ensure that the provided JSON web token is valid\"
      }
    """
    .formatted(message);

    return HttpResponse.status(HttpStatus.UNAUTHORIZED).body(responseBody);
  }

  public static HttpResponse<Object> forbiddenAccess(String message) {
    final String responseBody = """
      {
          \"error\": \"Forbidden access\",
          \"message\": \"%s\",
          \"detail\": \"Ensure that the provided user id is correct\"
      }
    """
    .formatted(message);

    return HttpResponse.status(HttpStatus.FORBIDDEN).body(responseBody);
  }

  public static HttpResponse<Object> resourceNotFound(String message) {
    final String responseBody = """
      {
          \"error\": \"Requested resource not found\",
          \"message\": \"%s\",
          \"detail\": \"Ensure that the provided resource id and user id are correct\"
      }
    """
    .formatted(message);

    return HttpResponse.status(HttpStatus.NOT_FOUND).body(responseBody);
  }

  public static HttpResponse<Object> resourceAlreadyExists(String message) {
    final String responseBody ="""
      {
          \"error\": \"Conflict occurred, resource already exists\",
          \"message\": \"%s\",
          \"detail\": \"Ensure that the provided resource id and user id are correct\"
      }
      """
      .formatted(message);

    return HttpResponse.status(HttpStatus.CONFLICT).body(responseBody);
  }

  public static HttpResponse<Object> resourceMediaTypeUnsupported(String message) {
    final String responseBody = """
      {
          \"error\": \"Resource media type is unsupported\",
          \"message\": \"%s\",
          \"detail\": \"Ensure that the provided resource media type is correct\"
      }
    """
    .formatted(message);

    return HttpResponse.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(responseBody);
  }

  public static HttpResponse<Object> resourceSizeExceedsLimit(String message) {
    final String responseBody = """
      {
          \"error\": \"Resource size exceeds upload limit\",
          \"message\": \"%s\",
          \"detail\": \"Ensure that the provided resource is within size limit\"
      }
    """
    .formatted(message);

    return HttpResponse.status(HttpStatus.REQUEST_ENTITY_TOO_LARGE).body(responseBody);
  }

  public static HttpResponse<Object> internalServerError() {
    final String responseBody = """
      {
          \"error\": \"Internal server error occurred\",
          \"message\": \"No message\",
          \"detail\": \"No detail\"
      }
    """;

    return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
  }
}
