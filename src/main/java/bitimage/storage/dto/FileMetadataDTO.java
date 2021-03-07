package bitimage.storage.dto;

public class FileMetadataDTO {
  public String hash_md5;
  public String file_format;
  public long size_bytes;
  public boolean is_null;

  public FileMetadataDTO(String hash_md5, String file_format, long size_bytes) {
    this.hash_md5 = hash_md5;
    this.file_format = file_format;
    this.size_bytes = size_bytes;
  }

  public FileMetadataDTO() {}

  public FileMetadataDTO asNull() {
    this.is_null = true;
    return this;
  }

  public boolean isNull() {
    return this.is_null;
  }
}
