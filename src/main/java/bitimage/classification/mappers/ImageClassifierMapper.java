package bitimage.classification.mappers;

import bitimage.domain.common.entities.EntityID;
import bitimage.domain.common.entities.Label;
import com.amazonaws.services.rekognition.model.Celebrity;
import com.amazonaws.services.rekognition.model.Emotion;
import com.amazonaws.services.rekognition.model.FaceDetail;
import com.amazonaws.services.rekognition.model.Gender;
import com.amazonaws.services.rekognition.model.ModerationLabel;
import com.amazonaws.services.rekognition.model.TextDetection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImageClassifierMapper
{
    private static final String FILE_ID_PREFIX = "users/%s/%s";
    private static final String SMILE_LABEL_NAME = "smile";

    private static final String TEXT_CONTENT_CATEGORY = "text";
    private static final String FACE_CONTENT_CATEGORY = "face";
    private static final String OBJECT_CONTENT_CATEGORY = "object";
    private static final String CELEBRITY_CONTENT_CATEGORY = "celebrity";
    private static final String UNSAFE_CONTENT_CATEGORY = "unsafe content";

    public String mapToFileID(EntityID userID, EntityID imageID)
    {
        return String.format(
                FILE_ID_PREFIX,
                userID,
                imageID.toString());
    }

    public List<Label> mapToObjectLabels(
            EntityID imageID,
            List<com.amazonaws.services.rekognition.model.Label> objectLabelDTOs)
    {
        return objectLabelDTOs.stream()
                .map(objectLabelDTO -> mapToObjectLabel(imageID, objectLabelDTO))
                .collect(Collectors.toList());
    }

    public Label mapToObjectLabel(
            EntityID imageID,
            com.amazonaws.services.rekognition.model.Label objectLabelDTO)
    {
        Label label = Label.CreateNew(imageID, objectLabelDTO.getName());

        label.setContentCategory(OBJECT_CONTENT_CATEGORY);
        label.setConfidenceScore(objectLabelDTO.getConfidence());

        return label;
    }

    public List<Label> mapToTextLabels(
            EntityID imageID,
            List<TextDetection> textLabelDTOs)
    {
        return textLabelDTOs.stream()
                .map(textLabelDTO -> mapToTextLabel(imageID, textLabelDTO))
                .collect(Collectors.toList());
    }

    public Label mapToTextLabel(EntityID imageID, TextDetection textLabelDTO)
    {
        Label label = Label.CreateNew(imageID, textLabelDTO.getDetectedText());

        label.setContentCategory(TEXT_CONTENT_CATEGORY);
        label.setConfidenceScore(textLabelDTO.getConfidence());

        return label;
    }

    public List<Label> mapToCelebrityLabels(
            EntityID imageID,
            List<Celebrity> celebrityLabelDTOs)
    {
        return celebrityLabelDTOs.stream()
                .map(celebrityLabelDTO -> mapToCelebrityLabel(imageID, celebrityLabelDTO))
                .collect(Collectors.toList());
    }

    public Label mapToCelebrityLabel(EntityID imageID, Celebrity celebrityLabelDTO)
    {
        Label label = Label.CreateNew(imageID, celebrityLabelDTO.getName());

        label.setContentCategory(CELEBRITY_CONTENT_CATEGORY);
        label.setConfidenceScore(celebrityLabelDTO.getMatchConfidence());

        return label;
    }

    public List<Label> mapToUnsafeContentLabels(
            EntityID imageID,
            List<ModerationLabel> unsafeContentLabelDTOs)
    {
        return unsafeContentLabelDTOs.stream()
                .map(unsafeContentLabelDTO -> mapToUnsafeContentLabel(imageID, unsafeContentLabelDTO))
                .collect(Collectors.toList());
    }

    public Label mapToUnsafeContentLabel(
            EntityID imageID,
            ModerationLabel unsafeContentLabelDTO)
    {
        Label label = Label.CreateNew(imageID, unsafeContentLabelDTO.getName());

        label.setContentCategory(UNSAFE_CONTENT_CATEGORY);
        label.setConfidenceScore(unsafeContentLabelDTO.getConfidence());

        return label;
    }

    public List<Label> mapToFaceLabels(EntityID imageID, List<FaceDetail> faceDetailDTOs)
    {
        List<Label> labels = new ArrayList<Label>();

        for (FaceDetail faceDetailDTO : faceDetailDTOs) {
            boolean isFaceSmiling = faceDetailDTO.getSmile().getValue();

            if (isFaceSmiling) {
                labels.add(mapToSmileLabel(imageID, faceDetailDTO));
            }

            labels.add(mapToSmileLabel(imageID, faceDetailDTO));
            labels.add(mapToGenderLabel(imageID, faceDetailDTO));
            labels.addAll(mapToEmotionLabels(imageID, faceDetailDTO));
        }

        return labels;
    }

    private Label mapToSmileLabel(EntityID imageID, FaceDetail faceDetailDTO)
    {
        Label label = Label.CreateNew(imageID, SMILE_LABEL_NAME);

        label.setContentCategory(FACE_CONTENT_CATEGORY);
        label.setConfidenceScore(faceDetailDTO.getSmile().getConfidence());

        return label;
    }

    private Label mapToGenderLabel(EntityID imageID, FaceDetail faceDetailDTO)
    {
        Gender faceGender = faceDetailDTO.getGender();
        Label label = Label.CreateNew(imageID, faceGender.getValue());

        label.setContentCategory(FACE_CONTENT_CATEGORY);
        label.setConfidenceScore(faceGender.getConfidence());

        return label;
    }

    private List<Label> mapToEmotionLabels(EntityID imageID, FaceDetail faceDetailDTO)
    {
        List<Emotion> faceEmotions = faceDetailDTO.getEmotions();

        return faceEmotions.stream()
                .map(faceEmotion -> mapToEmotionLabel(imageID, faceEmotion))
                .collect(Collectors.toList());
    }

    private Label mapToEmotionLabel(EntityID imageID, Emotion emotionDTO)
    {
        Label label = Label.CreateNew(imageID, emotionDTO.getType());

        label.setContentCategory(FACE_CONTENT_CATEGORY);
        label.setConfidenceScore(emotionDTO.getConfidence());

        return label;
    }
}
