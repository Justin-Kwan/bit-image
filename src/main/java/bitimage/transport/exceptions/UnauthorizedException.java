package bitimage.transport.exceptions;

public class UnauthorizedException
        extends RuntimeException
{
    public UnauthorizedException()
    {
        super("Request is unauthorized");
    }
}
