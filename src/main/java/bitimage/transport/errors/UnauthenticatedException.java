package bitimage.transport.errors;

public class UnauthenticatedException extends RuntimeException {
  public UnauthenticatedException() {
    super("Request is unauthorized");
  }
}
