package bitimage.uploading.ports;

import bitimage.shared.entities.EntityID;
import bitimage.shared.entities.Image;
import bitimage.uploading.entities.FileUrl;
import bitimage.uploading.entities.ImageMetadata;

import java.util.List;

public interface ImageStore
{
    List<Image> addImages(List<Image> images)
            throws Exception;

    List<Image> getAllPublicImages()
            throws Exception;

    List<Image> getAllUserImages(EntityID userID)
            throws Exception;

    List<Image> getImagesByName(EntityID userID, String imageName)
            throws Exception;

    List<Image> getImagesByTag(EntityID userID, String tagName)
            throws Exception;

    List<Image> getImagesByContentLabel(EntityID userID, String contentLabelName)
            throws Exception;

    Image getImageByID(EntityID userID, EntityID imageID)
            throws Exception;

    void deleteImages(EntityID userID, List<EntityID> imageIDs)
            throws Exception;

    void deleteAllUserImages(EntityID userID)
            throws Exception;

    FileUrl generateImageUploadUrl(EntityID uploaderUserID);

    ImageMetadata getReceivedImageMetadata(EntityID userID, EntityID imageID);
}
