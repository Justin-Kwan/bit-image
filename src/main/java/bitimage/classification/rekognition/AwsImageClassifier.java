package bitimage.classification.rekognition;

import bitimage.storage.exceptions.IExceptionTranslator;
import bitimage.storage.s3.IAwsEnv;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.Attribute;
import com.amazonaws.services.rekognition.model.Celebrity;
import com.amazonaws.services.rekognition.model.DetectFacesRequest;
import com.amazonaws.services.rekognition.model.DetectFacesResult;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectModerationLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectModerationLabelsResult;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.FaceDetail;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.ModerationLabel;
import com.amazonaws.services.rekognition.model.RecognizeCelebritiesRequest;
import com.amazonaws.services.rekognition.model.RecognizeCelebritiesResult;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.TextDetection;
import java.util.List;

public class AwsImageClassifier {

  private final AmazonRekognition rekognitionClient;
  private final IExceptionTranslator<AmazonServiceException, RuntimeException>
      awsExceptionTranslator;

  private final int MAX_LABELS_GENERATED = 20;
  private final float MIN_LABEL_CONFIDENCE_SCORE = 70F;

  public AwsImageClassifier(
      AmazonRekognition rekognitionClient,
      IExceptionTranslator<AmazonServiceException, RuntimeException> awsExceptionTranslator) {
    this.rekognitionClient = rekognitionClient;
    this.awsExceptionTranslator = awsExceptionTranslator;
  }

  public static AwsImageClassifier CreateNew(
      IAwsEnv env,
      IExceptionTranslator<AmazonServiceException, RuntimeException> awsExceptionTranslator) {
    final var awsCredentials = new BasicAWSCredentials(env.getAwsAccessID(), env.getAwsAccessKey());
    final var rekognitionClient = new AmazonRekognitionClient(awsCredentials);

    final var awsImageClassifier =
        new AwsImageClassifier(rekognitionClient, awsExceptionTranslator);

    return awsImageClassifier;
  }

  public List<Label> detectObjectsInImage(String fileID, String folderName) {
    final var request =
        new DetectLabelsRequest()
            .withImage(this.newRekognitionImage(fileID, folderName))
            .withMaxLabels(this.MAX_LABELS_GENERATED)
            .withMinConfidence(this.MIN_LABEL_CONFIDENCE_SCORE);

    final List<Label> detectedLabels = this.rekognitionClient.detectLabels(request).getLabels();

    return detectedLabels;
  }

  public List<TextDetection> detectTextInImage(String fileID, String folderName) {
    final var request =
        new DetectTextRequest().withImage(this.newRekognitionImage(fileID, folderName));

    final List<TextDetection> detectedTexts =
        this.rekognitionClient.detectText(request).getTextDetections();

    return detectedTexts;
  }

  public List<FaceDetail> detectFacesInImage(String fileID, String folderName) {
    final var request =
        new DetectFacesRequest()
            .withImage(this.newRekognitionImage(fileID, folderName))
            .withAttributes(Attribute.ALL);

    DetectFacesResult result = this.rekognitionClient.detectFaces(request);
    List<FaceDetail> detectedFaces = result.getFaceDetails();

    return detectedFaces;
  }

  public List<Celebrity> detectCelebritiesInImage(String fileID, String folderName) {
    final var request =
        new RecognizeCelebritiesRequest().withImage(this.newRekognitionImage(fileID, folderName));

    RecognizeCelebritiesResult result = this.rekognitionClient.recognizeCelebrities(request);
    List<Celebrity> detectedCelebrities = result.getCelebrityFaces();

    return detectedCelebrities;
  }

  public List<ModerationLabel> detectUnsafeContentInImage(String fileID, String folderName) {
    final var request =
        new DetectModerationLabelsRequest()
            .withImage(this.newRekognitionImage(fileID, folderName))
            .withMinConfidence(this.MIN_LABEL_CONFIDENCE_SCORE);

    DetectModerationLabelsResult result = this.rekognitionClient.detectModerationLabels(request);
    List<ModerationLabel> detectedUnsafeContents = result.getModerationLabels();

    return detectedUnsafeContents;
  }

  private Image newRekognitionImage(String fileID, String folderName) {
    final var s3Object = new S3Object().withName(fileID).withBucket(folderName);

    return new Image().withS3Object(s3Object);
  }
}
