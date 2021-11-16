package bitimage.classification;

import bitimage.classification.mappers.ImageClassifierMapper;
import bitimage.classification.rekognition.AwsImageClassifier;
import bitimage.shared.entities.Image;
import bitimage.shared.entities.Label;
import bitimage.storage.s3.S3Constants;

import com.amazonaws.services.rekognition.model.Celebrity;
import com.amazonaws.services.rekognition.model.FaceDetail;
import com.amazonaws.services.rekognition.model.ModerationLabel;
import com.amazonaws.services.rekognition.model.TextDetection;

import java.util.List;

public class ImageClassifier
        implements bitimage.analysis.ports.ImageClassifier
{
    private final AwsImageClassifier awsImageClassifier;
    private final ImageClassifierMapper mapper;

    public ImageClassifier(
            AwsImageClassifier awsImageClassifier,
            ImageClassifierMapper mapper)
    {
        this.awsImageClassifier = awsImageClassifier;
        this.mapper = mapper;
    }

    public List<Label> detectObjectsInImage(Image image)
    {
        String fileID = mapper.mapToFileID(image.getUserID(), image.getID());

        List<com.amazonaws.services.rekognition.model.Label> objectLabelDTOs =
                awsImageClassifier.detectObjectsInImage(
                        fileID,
                        S3Constants.PERMANENT_STORAGE_FOLDER);

        return mapper.mapToObjectLabels(image.getID(), objectLabelDTOs);
    }

    public List<Label> detectFacesInImage(Image image)
    {
        String fileID = mapper.mapToFileID(image.getUserID(), image.getID());

        List<FaceDetail> faceLabelDTOs = awsImageClassifier.detectFacesInImage(
                fileID,
                S3Constants.PERMANENT_STORAGE_FOLDER);

        return mapper.mapToFaceLabels(image.getID(), faceLabelDTOs);
    }

    public List<Label> detectTextInImage(Image image)
    {
        String fileID = mapper.mapToFileID(image.getUserID(), image.getID());

        List<TextDetection> textLabelDTOs = awsImageClassifier.detectTextInImage(
                fileID,
                S3Constants.PERMANENT_STORAGE_FOLDER);

        return mapper.mapToTextLabels(image.getID(), textLabelDTOs);
    }

    public List<Label> detectCelebritiesInImage(Image image)
    {
        String fileID = mapper.mapToFileID(image.getUserID(), image.getID());

        List<Celebrity> celebrityLabelDTOs = awsImageClassifier
                .detectCelebritiesInImage(
                        fileID,
                        S3Constants.PERMANENT_STORAGE_FOLDER);

        return mapper.mapToCelebrityLabels(image.getID(), celebrityLabelDTOs);
    }

    public List<Label> detectUnsafeContentInImage(Image image)
    {
        String fileID = mapper.mapToFileID(image.getUserID(), image.getID());

        List<ModerationLabel> unsafeLabelDTOs = awsImageClassifier
                .detectUnsafeContentInImage(
                        fileID,
                        S3Constants.PERMANENT_STORAGE_FOLDER);

        return mapper.mapToUnsafeContentLabels(image.getID(), unsafeLabelDTOs);
    }
}
