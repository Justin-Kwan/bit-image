package bitimage.storage.postgres.resultset;

import bitimage.storage.dto.ImageDTO;
import bitimage.storage.dto.LabelDTO;
import bitimage.storage.dto.TagDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.sql.ResultSet;
import java.util.List;

public class ImageExpandedViewResultSetMapper
        extends ImageResultSetMapper
{
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public ImageExpandedViewResultSetMapper()
    {
        super();
    }

    public ImageDTO mapRowToDTO(ResultSet results)
            throws Exception
    {
        ImageDTO imageDTO = super.mapRowToDTO(results);

        JsonNode tagsJson = JSON_MAPPER.readTree(results.getString("tags"));
        ObjectReader tagsReader = JSON_MAPPER.readerFor(new TypeReference<List<TagDTO>>() {});
        List<TagDTO> tagDTOs = tagsReader.readValue(tagsJson);

        JsonNode labelsJson = JSON_MAPPER.readTree(results.getString("content_labels"));
        ObjectReader labelsReader = JSON_MAPPER.readerFor(new TypeReference<List<LabelDTO>>() {});
        List<LabelDTO> labelDTOs = labelsReader.readValue(labelsJson);

        imageDTO.tag_dtos = tagDTOs;
        imageDTO.label_dtos = labelDTOs;

        return imageDTO;
    }
}
