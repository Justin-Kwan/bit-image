package bitimage.domain.uploading.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException() {
    super("User with provided id already exists");
  }
}
