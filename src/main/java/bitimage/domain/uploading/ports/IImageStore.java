package bitimage.domain.uploading.ports;

import bitimage.domain.common.entities.EntityID;
import bitimage.domain.common.entities.Image;
import bitimage.domain.uploading.entities.FileUrl;
import bitimage.domain.uploading.entities.ImageMetadata;

import java.util.List;

public interface IImageStore
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
