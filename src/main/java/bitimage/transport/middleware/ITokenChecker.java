package bitimage.transport.middleware;

public interface ITokenChecker<T>
{
    String doAuthCheck(T headers)
            throws Exception;
}
