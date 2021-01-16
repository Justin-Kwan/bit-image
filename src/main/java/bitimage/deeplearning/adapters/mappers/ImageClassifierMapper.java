package bitimage.deeplearning.adapters.mappers;

import bitimage.domain.common.entities.EntityID;
import bitimage.domain.sharedkernel.entities.Label;
import com.amazonaws.services.rekognition.model.Celebrity;
import com.amazonaws.services.rekognition.model.Emotion;
import com.amazonaws.services.rekognition.model.FaceDetail;
import com.amazonaws.services.rekognition.model.Gender;
import com.amazonaws.services.rekognition.model.ModerationLabel;
import com.amazonaws.services.rekognition.model.TextDetection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImageClassifierMapper {

  private final String FILE_ID_PREFIX = "users/%s/%s";

  private final String SMILE_LABEL_NAME = "smile";

  private final String TEXT_CONTENT_CATEGORY = "text";
  private final String FACE_CONTENT_CATEGORY = "face";
  private final String OBJECT_CONTENT_CATEGORY = "object";
  private final String CELEBRITY_CONTENT_CATEGORY = "celebrity";
  private final String UNSAFE_CONTENT_CATEGORY = "unsafe content";

  public String mapToFileID(EntityID userID, EntityID imageID) {
    return this.FILE_ID_PREFIX.formatted(userID, imageID.toString());
  }

  public List<Label> mapToObjectLabels(
      EntityID imageID, List<com.amazonaws.services.rekognition.model.Label> objectLabelDTOs) {
    return objectLabelDTOs.stream()
        .map(objectLabelDTO -> this.mapToObjectLabel(imageID, objectLabelDTO))
        .collect(Collectors.toList());
  }

  public Label mapToObjectLabel(
      EntityID imageID, com.amazonaws.services.rekognition.model.Label objectLabelDTO) {
    final Label label = Label.CreateNew(imageID, objectLabelDTO.getName());

    label.setContentCategory(this.OBJECT_CONTENT_CATEGORY);
    label.setConfidenceScore(objectLabelDTO.getConfidence());

    return label;
  }

  public List<Label> mapToTextLabels(EntityID imageID, List<TextDetection> textLabelDTOs) {
    return textLabelDTOs.stream()
        .map(textLabelDTO -> this.mapToTextLabel(imageID, textLabelDTO))
        .collect(Collectors.toList());
  }

  public Label mapToTextLabel(EntityID imageID, TextDetection textLabelDTO) {
    final Label label = Label.CreateNew(imageID, textLabelDTO.getDetectedText());

    label.setContentCategory(this.TEXT_CONTENT_CATEGORY);
    label.setConfidenceScore(textLabelDTO.getConfidence());

    return label;
  }

  public List<Label> mapToCelebrityLabels(EntityID imageID, List<Celebrity> celebrityLabelDTOs) {
    return celebrityLabelDTOs.stream()
        .map(celebrityLabelDTO -> this.mapToCelebrityLabel(imageID, celebrityLabelDTO))
        .collect(Collectors.toList());
  }

  public Label mapToCelebrityLabel(EntityID imageID, Celebrity celebrityLabelDTO) {
    final Label label = Label.CreateNew(imageID, celebrityLabelDTO.getName());

    label.setContentCategory(this.CELEBRITY_CONTENT_CATEGORY);
    label.setConfidenceScore(celebrityLabelDTO.getMatchConfidence());

    return label;
  }

  public List<Label> mapToUnsafeContentLabels(
      EntityID imageID, List<ModerationLabel> unsafeContentLabelDTOs) {
    return unsafeContentLabelDTOs.stream()
        .map(unsafeContentLabelDTO -> this.mapToUnsafeContentLabel(imageID, unsafeContentLabelDTO))
        .collect(Collectors.toList());
  }

  public Label mapToUnsafeContentLabel(EntityID imageID, ModerationLabel unsafeContentLabelDTO) {
    final Label label = Label.CreateNew(imageID, unsafeContentLabelDTO.getName());

    label.setContentCategory(this.UNSAFE_CONTENT_CATEGORY);
    label.setConfidenceScore(unsafeContentLabelDTO.getConfidence());

    return label;
  }

  public List<Label> mapToFaceLabels(EntityID imageID, List<FaceDetail> faceDetailDTOs) {
    final var labels = new ArrayList<Label>();

    for (FaceDetail faceDetailDTO : faceDetailDTOs) {
      final boolean isFaceSmiling = faceDetailDTO.getSmile().getValue().booleanValue();

      if (isFaceSmiling) {
        labels.add(this.mapToSmileLabel(imageID, faceDetailDTO));
      }

      labels.add(this.mapToSmileLabel(imageID, faceDetailDTO));
      labels.add(this.mapToGenderLabel(imageID, faceDetailDTO));
      labels.addAll(this.mapToEmotionLabels(imageID, faceDetailDTO));
    }

    return labels;
  }

  private Label mapToSmileLabel(EntityID imageID, FaceDetail faceDetailDTO) {
    final Label label = Label.CreateNew(imageID, this.SMILE_LABEL_NAME);

    label.setContentCategory(this.FACE_CONTENT_CATEGORY);
    label.setConfidenceScore(faceDetailDTO.getSmile().getConfidence());

    return label;
  }

  private Label mapToGenderLabel(EntityID imageID, FaceDetail faceDetailDTO) {
    final Gender faceGender = faceDetailDTO.getGender();
    final Label label = Label.CreateNew(imageID, faceGender.getValue());

    label.setContentCategory(this.FACE_CONTENT_CATEGORY);
    label.setConfidenceScore(faceGender.getConfidence());

    return label;
  }

  private List<Label> mapToEmotionLabels(EntityID imageID, FaceDetail faceDetailDTO) {
    final List<Emotion> faceEmotions = faceDetailDTO.getEmotions();

    return faceEmotions.stream()
        .map(faceEmotion -> this.mapToEmotionLabel(imageID, faceEmotion))
        .collect(Collectors.toList());
  }

  private Label mapToEmotionLabel(EntityID imageID, Emotion emotionDTO) {
    final Label label = Label.CreateNew(imageID, emotionDTO.getType());

    label.setContentCategory(this.FACE_CONTENT_CATEGORY);
    label.setConfidenceScore(emotionDTO.getConfidence());

    return label;
  }
}
