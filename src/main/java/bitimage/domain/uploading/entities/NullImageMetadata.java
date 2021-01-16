package bitimage.domain.uploading.entities;

public class NullImageMetadata extends ImageMetadata {

  public NullImageMetadata() {
    super(null, null);
  }

  @Override
  public boolean isCorrupt(String providedHash) {
    return true;
  }

  @Override
  public boolean isNull() {
    return true;
  }
}
