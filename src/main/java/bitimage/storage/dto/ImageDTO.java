package bitimage.storage.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImageDTO
{
    public UUID id;
    public UUID user_id;

    public String name;
    public String hash_md5;
    public String file_format;
    public double size_bytes;

    public Timestamp created_at;
    public Timestamp updated_at;

    public List<TagDTO> tag_dtos;
    public List<LabelDTO> label_dtos;

    public boolean is_null;
    public boolean is_private;

    public ImageDTO()
    {
        this.is_null = false;
        this.tag_dtos = new ArrayList<>();
        this.label_dtos = new ArrayList<>();
    }

    public ImageDTO asNull()
    {
        is_null = true;
        return this;
    }

    public boolean isNull()
    {
        return is_null;
    }
}
