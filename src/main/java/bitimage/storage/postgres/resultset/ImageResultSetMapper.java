package bitimage.storage.postgres.resultset;

import bitimage.storage.dto.ImageDTO;

import java.sql.ResultSet;
import java.util.UUID;

public class ImageResultSetMapper
        extends ResultSetMapper<ImageDTO>
{
    public ImageDTO mapRowToDTO(ResultSet results)
            throws Exception
    {
        ImageDTO imageDTO = new ImageDTO();

        imageDTO.id = (UUID) results.getObject("id");
        imageDTO.name = results.getString("name");
        imageDTO.user_id = (UUID) results.getObject("user_id");
        imageDTO.hash_md5 = results.getString("hash_md5");
        imageDTO.size_bytes = results.getFloat("size_bytes");
        imageDTO.file_format = results.getString("file_format");
        imageDTO.is_private = results.getBoolean("is_private");

        return imageDTO;
    }
}
