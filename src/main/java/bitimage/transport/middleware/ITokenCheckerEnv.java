package bitimage.transport.middleware;

public interface ITokenCheckerEnv {
  public String getRemoteTokenCheckerHostPort();

  public String getRemoteTokenCheckerRequestMediaType();
}
