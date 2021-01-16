package bitimage.transport.middleware;

public interface ITokenChecker<T> {
  public String doAuthCheck(T headers) throws Exception;
}
