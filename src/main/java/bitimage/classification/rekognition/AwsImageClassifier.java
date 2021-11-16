package bitimage.classification.rekognition;

import bitimage.storage.exceptions.ExceptionTranslator;
import bitimage.storage.s3.AwsEnv;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.Attribute;
import com.amazonaws.services.rekognition.model.Celebrity;
import com.amazonaws.services.rekognition.model.DetectFacesRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectModerationLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.FaceDetail;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.ModerationLabel;
import com.amazonaws.services.rekognition.model.RecognizeCelebritiesRequest;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.TextDetection;

import java.util.List;

public class AwsImageClassifier
{
    private static final int MAX_LABELS_GENERATED = 20;
    private static final float MIN_LABEL_CONFIDENCE_SCORE = 70F;

    private final AmazonRekognition rekognitionClient;
    private final ExceptionTranslator<AmazonServiceException, RuntimeException>
            awsExceptionTranslator;

    public AwsImageClassifier(
            AmazonRekognition rekognitionClient,
            ExceptionTranslator<AmazonServiceException, RuntimeException> awsExceptionTranslator)
    {
        this.rekognitionClient = rekognitionClient;
        this.awsExceptionTranslator = awsExceptionTranslator;
    }

    public static AwsImageClassifier CreateNew(
            AwsEnv env,
            ExceptionTranslator<AmazonServiceException, RuntimeException> awsExceptionTranslator)
    {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                env.getAwsAccessID(),
                env.getAwsAccessKey());

        return new AwsImageClassifier(
                new AmazonRekognitionClient(awsCredentials),
                awsExceptionTranslator);
    }

    public List<Label> detectObjectsInImage(String fileID, String folderName)
    {
        DetectLabelsRequest request = new DetectLabelsRequest()
                        .withImage(newRekognitionImage(fileID, folderName))
                        .withMaxLabels(MAX_LABELS_GENERATED)
                        .withMinConfidence(MIN_LABEL_CONFIDENCE_SCORE);

        return rekognitionClient
                .detectLabels(request)
                .getLabels();
    }

    public List<TextDetection> detectTextInImage(String fileID, String folderName)
    {
        DetectTextRequest request = new DetectTextRequest()
                .withImage(newRekognitionImage(fileID, folderName));

        return rekognitionClient
                .detectText(request)
                .getTextDetections();
    }

    public List<FaceDetail> detectFacesInImage(String fileID, String folderName)
    {
        DetectFacesRequest request = new DetectFacesRequest()
                .withImage(newRekognitionImage(fileID, folderName))
                .withAttributes(Attribute.ALL);

        return rekognitionClient
                .detectFaces(request)
                .getFaceDetails();
    }

    public List<Celebrity> detectCelebritiesInImage(String fileID, String folderName)
    {
        RecognizeCelebritiesRequest request = new RecognizeCelebritiesRequest()
                .withImage(newRekognitionImage(fileID, folderName));

        return rekognitionClient
                .recognizeCelebrities(request)
                .getCelebrityFaces();
    }

    public List<ModerationLabel> detectUnsafeContentInImage(String fileID, String folderName)
    {
        DetectModerationLabelsRequest request = new DetectModerationLabelsRequest()
                .withImage(newRekognitionImage(fileID, folderName))
                .withMinConfidence(MIN_LABEL_CONFIDENCE_SCORE);

        return rekognitionClient
                .detectModerationLabels(request)
                .getModerationLabels();
    }

    private Image newRekognitionImage(String fileID, String folderName)
    {
        S3Object s3Object = new S3Object()
                .withName(fileID)
                .withBucket(folderName);

        return new Image().withS3Object(s3Object);
    }
}
