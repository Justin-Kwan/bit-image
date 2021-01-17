package bitimage.domain.uploading.exceptions;

import bitimage.domain.common.entities.EntityID;

public class ImageFormatInvalidException extends RuntimeException {
  public ImageFormatInvalidException(EntityID imageID, String imageFormat) {
    super(
        "Uploaded image with id '%s' is of '%s' format, which is not supported"
            .formatted(imageID.toString(), imageFormat));
  }
}
