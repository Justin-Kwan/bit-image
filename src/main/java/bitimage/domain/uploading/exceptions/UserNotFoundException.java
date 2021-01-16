package bitimage.domain.uploading.exceptions;

public class UserNotFoundException extends IllegalArgumentException {
  public UserNotFoundException() {
    super("User with provided id not found");
  }
}
