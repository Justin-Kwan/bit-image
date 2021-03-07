package bitimage.domain.uploading.entities;

import bitimage.domain.common.entities.EntityID;
import bitimage.domain.uploading.exceptions.ImageFormatInvalidException;
import bitimage.domain.uploading.exceptions.ImageSizeExceededException;
import bitimage.regexp.RegexPatterns;

public class ImageMetadata {

  private String hash;
  private final FileSize size;
  private final String fileFormat;

  protected ImageMetadata(FileSize size, String fileFormat) {
    this.size = size;
    this.fileFormat = fileFormat;
  }

  public static ImageMetadata CreateNew(EntityID imageID, FileSize imageSize, String fileFormat) {
    if (ImageMetadata.isBadImageFormat(fileFormat)) {
      throw new ImageFormatInvalidException(imageID, fileFormat);
    }

    if (imageSize.doesExceedLimit()) {
      throw new ImageSizeExceededException(imageID, imageSize);
    }

    return new ImageMetadata(imageSize, fileFormat);
  }

  /**
   * Secures an image's metadata with an MD5 hash object, prevents overwriting of image metadata's
   * hash if already set.
   */
  public ImageMetadata secureWithHash(String hash) {
    final boolean isAlreadySecuredWithHash = this.hash != null;

    if (isAlreadySecuredWithHash) {
      return this;
    }

    this.hash = hash;
    return this;
  }

  public String getHash() {
    return this.hash;
  }

  public FileSize getSize() {
    return this.size;
  }

  public String getFileFormat() {
    return this.fileFormat;
  }

  /**
   * The list of accepted mime image formats can be found here
   * https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
   */
  public static boolean isBadImageFormat(String imageFormat) {
    return !imageFormat.equals("png")
        && !imageFormat.equals("gif")
        && !imageFormat.equals("bmp")
        && !imageFormat.equals("jpg")
        && !imageFormat.equals("jpeg")
        && !imageFormat.equals("tiff")
        && !imageFormat.equals("webp")
        && !imageFormat.equals("svg+xml")
        && !imageFormat.equals("vnd.microsoft.icon");
  }

  public boolean isCorrupt(String providedHash) {
    final boolean isBadHash =
        !hash.matches(RegexPatterns.HASH_MD5) || !this.hash.equals(providedHash);

    return isBadHash;
  }

  public boolean isNull() {
    return false;
  }
}
