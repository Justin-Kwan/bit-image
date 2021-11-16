package bitimage.transport.middleware;

public interface TokenChecker<T>
{
    String doAuthCheck(T headers)
            throws Exception;
}
