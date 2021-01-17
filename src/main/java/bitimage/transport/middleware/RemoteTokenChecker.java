package bitimage.transport.middleware;

import bitimage.regex.RegexPatterns;
import bitimage.transport.errors.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpHeaders;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RemoteTokenChecker implements ITokenChecker<HttpHeaders> {

  private static final String REQUEST_TOKEN_BODY = "{ \"token\": \"%s\" }";
  private static final String RESPONSE_USER_ID = "user_id";
  private static final String RESPONSE_IS_USER_AUTHORIZED = "is_user_authorized";

  private final String remoteApiHostPort;
  private final String remoteApiMediaType;

  public RemoteTokenChecker(ITokenCheckerEnv env) {
    this.remoteApiHostPort = env.getRemoteTokenCheckerHostPort();
    this.remoteApiMediaType = env.getRemoteTokenCheckerRequestMediaType();
  }

  /** Calls remote CAS auth service to verify token is valid, then returns user id of token. */
  public String doAuthCheck(HttpHeaders headers) throws Exception {
    this.assertHeadersContainToken(headers);

    try {
      final String authToken = headers.getAuthorization().get();
      final RequestBody tokenCheckRequest = this.getTokenCheckRequestBody(authToken);
      final String tokenCheckResponse = this.fetchAuthCheck(tokenCheckRequest);

      if (!this.isUserAuthorized(tokenCheckResponse)) {
        throw new UnauthorizedException();
      }

      return this.getTokenUserID(tokenCheckResponse);
    } catch (Exception e) {
      throw new UnauthorizedException();
    }
  }

  private void assertHeadersContainToken(HttpHeaders headers) {
    if (!headers.contains(HttpHeaders.AUTHORIZATION)) {
      throw new UnauthorizedException();
    }
  }

  private RequestBody getTokenCheckRequestBody(String authToken) {
    final MediaType requestMediaType = MediaType.get(remoteApiMediaType);

    // "Bearer token..." -> "token..."
    final var cleanedAuthToken = authToken.split(RegexPatterns.SPACE)[1];

    final RequestBody tokenCheckRequest =
        RequestBody.create(requestMediaType, REQUEST_TOKEN_BODY.formatted(cleanedAuthToken));

    return tokenCheckRequest;
  }

  private String fetchAuthCheck(RequestBody tokenCheckRequest) throws Exception {
    final var client = new OkHttpClient();

    final var req = new Request.Builder().url(remoteApiHostPort).post(tokenCheckRequest).build();

    final String res = client.newCall(req).execute().body().string();

    return res;
  }

  private boolean isUserAuthorized(String tokenCheckResponse) throws IOException {
    final var jsonMapper = new ObjectMapper();

    final boolean isUserAuthorized =
        jsonMapper.readTree(tokenCheckResponse).get(RESPONSE_IS_USER_AUTHORIZED).booleanValue();

    return isUserAuthorized;
  }

  private String getTokenUserID(String tokenCheckResponse) throws IOException {
    final var jsonMapper = new ObjectMapper();

    final String userID = jsonMapper.readTree(tokenCheckResponse).get(RESPONSE_USER_ID).textValue();

    return userID;
  }
}
