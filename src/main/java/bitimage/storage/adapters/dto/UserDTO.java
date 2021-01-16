package bitimage.storage.adapters.dto;

import java.sql.Timestamp;
import java.util.UUID;

public class UserDTO {
  public UUID id;
  public Timestamp created_at;
  public Timestamp updated_at;
  public int image_upload_count;
  public int image_upload_limit;
}
