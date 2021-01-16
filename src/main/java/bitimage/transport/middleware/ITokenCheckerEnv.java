package bitimage.transport.middleware;

public interface ITokenCheckerEnv {
  public String getRemoteTokenCheckerHostPort();

  public String getRemoteTokenCheckerRequestBody();

  public String getRemoteTokenCheckerRequestMediaType();
}
