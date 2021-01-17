package bitimage.transport.errors;

public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException() {
    super("Request is unauthorized");
  }
}
