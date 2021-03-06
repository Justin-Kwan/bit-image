package bitimage.domain.uploading.exceptions;

import bitimage.domain.common.entities.EntityID;
import bitimage.domain.uploading.entities.FileSize;

public class ImageSizeExceededException extends IllegalArgumentException {
  public ImageSizeExceededException(EntityID imageID, FileSize size) {
    super(
        "Image with id '%s' has size of %.2f MB, which exceeds the max upload size of 2 MB"
            .formatted(imageID.toString(), size.toMb()));
  }
}
