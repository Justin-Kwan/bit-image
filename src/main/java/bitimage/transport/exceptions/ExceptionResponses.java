package bitimage.transport.exceptions;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;

public class ExceptionResponses
{
    public static HttpResponse<Object> unauthorizedAccess(String message)
    {
        String responseBody = String.format(
                """
                {
                    "error": "Unauthorized access",
                    "message": "%s",
                    "detail": "Ensure that the provided JSON web token is valid"
                }
                """,
                message);

        return HttpResponse
                .status(HttpStatus.UNAUTHORIZED)
                .body(responseBody);
    }

    public static HttpResponse<Object> forbiddenAccess(String message)
    {
        String responseBody = String.format(
                """
                {
                    "error": "Forbidden access",
                    "message": "%s",
                    "detail": "Ensure that the provided user id is correct"
                }
                """,
                message);

        return HttpResponse
                .status(HttpStatus.FORBIDDEN)
                .body(responseBody);
    }

    public static HttpResponse<Object> resourceNotFound(String message)
    {
        String responseBody = String.format(
                """
                {
                    "error": "Requested resource not found",
                    "message": "%s",
                    "detail": "Ensure that the provided resource id and user id are correct"
                }
                """,
                message);

        return HttpResponse
                .status(HttpStatus.NOT_FOUND)
                .body(responseBody);
    }

    public static HttpResponse<Object> resourceAlreadyExists(String message)
    {
        String responseBody = String.format(
                """
                {
                    "error": "Conflict occurred, resource already exists",
                    "message": "%s",
                    "detail": "Ensure that the provided resource id and user id are correct"
                }
                """,
                message);

        return HttpResponse
                .status(HttpStatus.CONFLICT)
                .body(responseBody);
    }

    public static HttpResponse<Object> resourceMediaTypeUnsupported(String message)
    {
        String responseBody = String.format(
                """
                {
                    "error": "Resource media type is unsupported",
                    "message": "%s",
                    "detail": "Ensure that the provided resource media type is correct"
                }
                """,
                message);

        return HttpResponse
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(responseBody);
    }

    public static HttpResponse<Object> resourceSizeExceedsLimit(String message)
    {
        String responseBody = String.format(
                """
                {
                    "error": "Resource size exceeds upload limit",
                    "message": "%s",
                    "detail": "Ensure that the provided resource is within size limit"
                }""",
                message);

        return HttpResponse
                .status(HttpStatus.REQUEST_ENTITY_TOO_LARGE)
                .body(responseBody);
    }

    public static HttpResponse<Object> internalServerError()
    {
        String responseBody = """
            {
                "error": "Internal server error occurred",
                "message": "No message",
                "detail": "No detail"
            }
        """;

        return HttpResponse
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(responseBody);
    }
}
