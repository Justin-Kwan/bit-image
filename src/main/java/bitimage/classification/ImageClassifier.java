package bitimage.classification;

import bitimage.classification.mappers.ImageClassifierMapper;
import bitimage.classification.rekognition.AwsImageClassifier;
import bitimage.domain.analysis.ports.IImageClassifier;
import bitimage.domain.common.entities.Image;
import bitimage.domain.common.entities.Label;
import bitimage.storage.s3.S3Constants;

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
        this.awsImageClassifier.detectObjectsInImage(fileID, S3Constants.PERMANENT_STORAGE_FOLDER);

    final List<Label> objectLabels = this.mapper.mapToObjectLabels(image.getID(), objectLabelDTOs);

    return objectLabels;
  }

  public List<Label> detectFacesInImage(Image image) {
    final String fileID = this.mapper.mapToFileID(image.getUserID(), image.getID());

    final List<FaceDetail> faceLabelDTOs =
        this.awsImageClassifier.detectFacesInImage(fileID, S3Constants.PERMANENT_STORAGE_FOLDER);

    final List<Label> faceLabels = this.mapper.mapToFaceLabels(image.getID(), faceLabelDTOs);

    return faceLabels;
  }

  public List<Label> detectTextInImage(Image image) {
    final String fileID = this.mapper.mapToFileID(image.getUserID(), image.getID());

    final List<TextDetection> textLabelDTOs =
        this.awsImageClassifier.detectTextInImage(fileID, S3Constants.PERMANENT_STORAGE_FOLDER);

    final List<Label> textLabels = this.mapper.mapToTextLabels(image.getID(), textLabelDTOs);

    return textLabels;
  }

  public List<Label> detectCelebritiesInImage(Image image) {
    final String fileID = this.mapper.mapToFileID(image.getUserID(), image.getID());

    final List<Celebrity> celebrityLabelDTOs =
        this.awsImageClassifier.detectCelebritiesInImage(
            fileID, S3Constants.PERMANENT_STORAGE_FOLDER);

    final List<Label> celebrityLabels =
        this.mapper.mapToCelebrityLabels(image.getID(), celebrityLabelDTOs);

    return celebrityLabels;
  }

  public List<Label> detectUnsafeContentInImage(Image image) {
    final String fileID = this.mapper.mapToFileID(image.getUserID(), image.getID());

    final List<ModerationLabel> unsafeLabelDTOs =
        this.awsImageClassifier.detectUnsafeContentInImage(
            fileID, S3Constants.PERMANENT_STORAGE_FOLDER);

    final List<Label> unsafeLabels =
        this.mapper.mapToUnsafeContentLabels(image.getID(), unsafeLabelDTOs);

    return unsafeLabels;
  }
}
