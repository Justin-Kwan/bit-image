package bitimage.domain.uploading.entities;

public class FileSize {

  private final double sizeBytes;

  private static final double BYTES_PER_MB = 1000000;
  private static final double MAX_SIZE_BYTES = 2000000;
  private static final int MAX_DECIMAL_PLACES = 2;

  private FileSize(double sizeBytes) {
    this.sizeBytes = sizeBytes;
  }

  public static FileSize CreateFromBytes(double sizeBytes) {
    return new FileSize(sizeBytes);
  }

  public double toMb() {
    final double unroundedSizeMb = sizeBytes / FileSize.BYTES_PER_MB;

    final double roundedSizeMb =
        this.roundDecimalPlaces(unroundedSizeMb, FileSize.MAX_DECIMAL_PLACES);

    return roundedSizeMb;
  }

  public double toBytes() {
    return this.sizeBytes;
  }

  public double getSizeBytes() {
    return this.sizeBytes;
  }

  public boolean doesExceedLimit() {
    return this.sizeBytes > MAX_SIZE_BYTES;
  }

  private double roundDecimalPlaces(double num, int places) {
    double scale = Math.pow(10, places);
    return Math.round(num * scale) / scale;
  }
}
