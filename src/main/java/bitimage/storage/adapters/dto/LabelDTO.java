package bitimage.storage.adapters.dto;

import java.sql.Timestamp;
import java.util.UUID;

public class LabelDTO {
  public UUID id;
  public UUID image_id;
  public String name;
  public String content_category;
  public double label_confidence_score;
  public Timestamp created_at;
  public Timestamp updated_at;
}
