package bitimage.storage.mappers;

import bitimage.shared.entities.Label;
import bitimage.storage.dto.LabelDTO;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

public class LabelStoreMapper
{
    public List<LabelDTO> mapToLabelDTOs(List<Label> labels)
    {
        return labels.stream()
                .map(LabelStoreMapper::mapToLabelDTO)
                .collect(Collectors.toList());
    }

    private static LabelDTO mapToLabelDTO(Label label)
    {
        LabelDTO labelDTO = new LabelDTO();

        labelDTO.id = label.getID().toUUID();
        labelDTO.image_id = label.getImageID().toUUID();
        labelDTO.name = label.getName();
        labelDTO.content_category = label.getContentCategory();
        labelDTO.label_confidence_score = label.getConfidenceScore();
        labelDTO.created_at = Timestamp.from(label.getDateTimeCreated());
        labelDTO.updated_at = Timestamp.from(label.getDateTimeUpdated());

        return labelDTO;
    }
}
