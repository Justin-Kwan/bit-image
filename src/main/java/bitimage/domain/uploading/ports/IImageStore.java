package bitimage.domain.uploading.ports;

import bitimage.domain.common.entities.EntityID;
import bitimage.domain.sharedkernel.entities.Image;
import bitimage.domain.uploading.entities.FileUrl;
import bitimage.domain.uploading.entities.ImageMetadata;
import java.util.List;

public interface IImageStore {
  public List<Image> addImages(List<Image> images) throws Exception;

  public List<Image> getAllUserImages(EntityID userID) throws Exception;

  public List<Image> getImagesByName(EntityID userID, String imageName) throws Exception;

  public List<Image> getImagesByTag(EntityID userID, String tagName) throws Exception;

  public List<Image> getImagesByContentLabel(EntityID userID, String contentLabelName)
      throws Exception;

  public Image getImageByID(EntityID userID, EntityID imageID) throws Exception;

  public void deleteImages(EntityID userID, List<EntityID> imageIDs) throws Exception;

  public void deleteAllUserImages(EntityID userID) throws Exception;

  public FileUrl generateImageUploadUrl(EntityID uploaderUserID);

  public ImageMetadata getReceivedImageMetadata(EntityID userID, EntityID imageID);
}
