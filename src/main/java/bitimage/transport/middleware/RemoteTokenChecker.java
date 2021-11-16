package bitimage.transport.middleware;

import bitimage.regexp.RegexPatterns;
import bitimage.transport.exceptions.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpHeaders;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.Objects;

public class RemoteTokenChecker
        implements TokenChecker<HttpHeaders>
{
    private static final String REQUEST_TOKEN_BODY = "{ \"token\": \"%s\" }";
    private static final String RESPONSE_USER_ID = "user_id";
    private static final String RESPONSE_IS_USER_AUTHORIZED = "is_user_authorized";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private final String remoteApiHostPort;
    private final String remoteApiMediaType;

    public RemoteTokenChecker(TokenCheckerEnv env)
    {
        this.remoteApiHostPort = env.getRemoteTokenCheckerHostPort();
        this.remoteApiMediaType = env.getRemoteTokenCheckerRequestMediaType();
    }

    /**
     * Calls remote CAS auth service to verify token
     * is valid, then returns user id of token.
     */
    public String doAuthCheck(HttpHeaders headers)
            throws UnauthorizedException
    {
        try {
            return doTokenCheck(headers);
        }
        catch (Exception e) {
            throw new UnauthorizedException();
        }
    }

    private String doTokenCheck(HttpHeaders headers)
            throws UnauthorizedException, IOException
    {
        assertHeadersContainToken(headers);

        String authToken = headers.getAuthorization().get();
        RequestBody tokenCheckRequest = getTokenCheckRequestBody(authToken);
        String tokenCheckResponse = fetchAuthCheck(tokenCheckRequest);

        if (!isUserAuthorized(tokenCheckResponse)) {
            throw new UnauthorizedException();
        }

        return getTokenUserID(tokenCheckResponse);
    }

    private static void assertHeadersContainToken(HttpHeaders headers)
    {
        if (!headers.contains(HttpHeaders.AUTHORIZATION)) {
            throw new UnauthorizedException();
        }
    }

    private RequestBody getTokenCheckRequestBody(String authToken)
    {
        MediaType requestMediaType = MediaType.get(remoteApiMediaType);
        // "Bearer token..." -> "token..."
        String cleanedAuthToken = authToken.split(RegexPatterns.SPACE)[1];

        return RequestBody.create(
                requestMediaType,
                String.format(REQUEST_TOKEN_BODY, cleanedAuthToken));
    }

    private String fetchAuthCheck(RequestBody tokenCheckRequest)
            throws IOException
    {
        Request req = new Request.Builder()
                .url(remoteApiHostPort)
                .post(tokenCheckRequest)
                .build();

        return Objects.requireNonNull(
                HTTP_CLIENT
                        .newCall(req)
                        .execute()
                        .body())
                .string();
    }

    private static boolean isUserAuthorized(String tokenCheckResponse)
            throws IOException
    {
        return JSON_MAPPER
                .readTree(tokenCheckResponse)
                .get(RESPONSE_IS_USER_AUTHORIZED)
                .booleanValue();
    }

    private static String getTokenUserID(String tokenCheckResponse)
            throws IOException
    {
        return JSON_MAPPER
                .readTree(tokenCheckResponse)
                .get(RESPONSE_USER_ID)
                .textValue();
    }
}
