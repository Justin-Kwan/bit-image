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

public class ImageExpandedViewResultSetMapper extends ImageResultSetMapper {

  public ImageExpandedViewResultSetMapper() {
    super();
  }

  public ImageDTO mapRowToDTO(ResultSet results) throws Exception {
    final var imageDTO = super.mapRowToDTO(results);
    final var jsonMapper = new ObjectMapper();
    ObjectReader objectReader;

    final JsonNode tagsJson = jsonMapper.readTree(results.getString("tags"));
    objectReader = jsonMapper.readerFor(new TypeReference<List<TagDTO>>() {});
    final List<TagDTO> tagDTOs = objectReader.readValue(tagsJson);

    final JsonNode labelsJson = jsonMapper.readTree(results.getString("content_labels"));
    objectReader = jsonMapper.readerFor(new TypeReference<List<LabelDTO>>() {});
    final List<LabelDTO> labelDTOs = objectReader.readValue(labelsJson);

    imageDTO.tag_dtos = tagDTOs;
    imageDTO.label_dtos = labelDTOs;

    return imageDTO;
  }
}
