package bitimage.deeplearning.adapters.classifiers;

import bitimage.deeplearning.adapters.mappers.ImageClassifierMapper;
import bitimage.deeplearning.aws.AwsImageClassifier;
import bitimage.domain.analysis.ports.IImageClassifier;
import bitimage.domain.sharedkernel.entities.Image;
import bitimage.domain.sharedkernel.entities.Label;
import bitimage.storage.aws.AwsConstants;
import com.amazonaws.services.rekognition.model.Celebrity;
import com.amazonaws.services.rekognition.model.FaceDetail;
import com.amazonaws.services.rekognition.model.ModerationLabel;
import com.amazonaws.services.rekognition.model.TextDetection;
import java.util.List;

public class ImageClassifier implements IImageClassifier {

  private final AwsImageClassifier awsImageClassifier;
  private final ImageClassifierMapper mapper;

  public ImageClassifier(AwsImageClassifier awsImageClassifier, ImageClassifierMapper mapper) {
    this.awsImageClassifier = awsImageClassifier;
    this.mapper = mapper;
  }

  public List<Label> detectObjectsInImage(Image image) {
    final String fileID = this.mapper.mapToFileID(image.getUserID(), image.getID());

    final List<com.amazonaws.services.rekognition.model.Label> objectLabelDTOs =
        this.awsImageClassifier.detectObjectsInImage(fileID, AwsConstants.PERMANENT_STORAGE_FOLDER);

    final List<Label> objectLabels = this.mapper.mapToObjectLabels(image.getID(), objectLabelDTOs);

    return objectLabels;
  }

  public List<Label> detectFacesInImage(Image image) {
    final String fileID = this.mapper.mapToFileID(image.getUserID(), image.getID());

    final List<FaceDetail> faceLabelDTOs =
        this.awsImageClassifier.detectFacesInImage(fileID, AwsConstants.PERMANENT_STORAGE_FOLDER);

    final List<Label> faceLabels = this.mapper.mapToFaceLabels(image.getID(), faceLabelDTOs);

    return faceLabels;
  }

  public List<Label> detectTextInImage(Image image) {
    final String fileID = this.mapper.mapToFileID(image.getUserID(), image.getID());

    final List<TextDetection> textLabelDTOs =
        this.awsImageClassifier.detectTextInImage(fileID, AwsConstants.PERMANENT_STORAGE_FOLDER);

    final List<Label> textLabels = this.mapper.mapToTextLabels(image.getID(), textLabelDTOs);

    return textLabels;
  }

  public List<Label> detectCelebritiesInImage(Image image) {
    final String fileID = this.mapper.mapToFileID(image.getUserID(), image.getID());

    final List<Celebrity> celebrityLabelDTOs =
        this.awsImageClassifier.detectCelebritiesInImage(
            fileID, AwsConstants.PERMANENT_STORAGE_FOLDER);

    final List<Label> celebrityLabels =
        this.mapper.mapToCelebrityLabels(image.getID(), celebrityLabelDTOs);

    return celebrityLabels;
  }

  public List<Label> detectUnsafeContentInImage(Image image) {
    final String fileID = this.mapper.mapToFileID(image.getUserID(), image.getID());

    final List<ModerationLabel> unsafeLabelDTOs =
        this.awsImageClassifier.detectUnsafeContentInImage(
            fileID, AwsConstants.PERMANENT_STORAGE_FOLDER);

    final List<Label> unsafeLabels =
        this.mapper.mapToUnsafeContentLabels(image.getID(), unsafeLabelDTOs);

    return unsafeLabels;
  }
}
