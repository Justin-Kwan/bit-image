package bitimage.storage.dto;

import java.sql.Timestamp;
import java.util.UUID;

public class TagDTO
{
    public UUID id;
    public String name;

    public Timestamp created_at;
    public Timestamp updated_at;
}
