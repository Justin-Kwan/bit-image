package bitimage.domain.uploading.exceptions;

public class ImageNotFoundException extends IllegalArgumentException {
  public ImageNotFoundException() {
    super("Image with provided id not found");
  }
}
