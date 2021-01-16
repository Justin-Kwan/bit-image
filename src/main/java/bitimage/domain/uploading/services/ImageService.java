package bitimage.domain.uploading.services;

import bitimage.domain.common.entities.EntityID;
import bitimage.domain.sharedkernel.entities.Image;
import bitimage.domain.uploading.commands.CreateImageCmd;
import bitimage.domain.uploading.entities.FileUrl;
import bitimage.domain.uploading.entities.ImageMetadata;
import bitimage.domain.uploading.entities.Tag;
import bitimage.domain.uploading.events.ImagesUploadedEvent;
import bitimage.domain.uploading.exceptions.ImageAlreadyExistsException;
import bitimage.domain.uploading.exceptions.ImageNotFoundException;
import bitimage.domain.uploading.exceptions.UserNotFoundException;
import bitimage.domain.uploading.ports.IEventPublisher;
import bitimage.domain.uploading.ports.IImageStore;
import bitimage.domain.uploading.ports.IUserStore;
import bitimage.storage.exceptions.StorageObjectAlreadyExistsException;
import bitimage.storage.exceptions.StorageObjectNotFoundException;
import bitimage.storage.exceptions.StorageObjectReferenceException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImageService {

  private final IImageStore imageStore;
  private final IUserStore userStore;
  private final IEventPublisher eventPublisher;

  public ImageService(
      IImageStore imageStore, IUserStore userStore, IEventPublisher eventPublisher) {
    this.imageStore = imageStore;
    this.userStore = userStore;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Generates presigned aws urls to upload a variable number of images. Confirmation should take
   * place after uploading images to aws presigned urls.
   *
   * <p>Uploaded images that are not confirmed as uploaded will expire (eventually be deleted).
   */
  public List<FileUrl> generateImageUploadUrls(int imageUploadCount, String providedUserID)
      throws Exception {
    final var userID = EntityID.CreateNew(providedUserID);
    this.assertUserExists(userID);

    final List<FileUrl> uploadUrls =
        IntStream.range(0, imageUploadCount)
            .parallel()
            .mapToObj(i -> this.imageStore.generateImageUploadUrl(userID))
            .collect(Collectors.toList());

    return uploadUrls;
  }

  /**
   * Confirms that all images have been uploaded to image upload urls. At most, 5 images can be
   * created per request.
   *
   * <p>Image upload confirmation should store user provided images metadata and publish an event
   * indicating all images have been uploaded.
   *
   * <p>Cron job could handle removal of orphaned images that pass potential race condition.
   */
  public List<Image> confirmImagesUploaded(List<CreateImageCmd> cmds) throws Exception {
    final var userID = EntityID.CreateNew(cmds.get(0).userID);
    this.assertUserExists(userID);

    final List<Image> images = cmds.stream().map(this::createImage).collect(Collectors.toList());

    try {
      final List<Image> uploadedImages = this.imageStore.addImages(images);

      this.eventPublisher.<ImagesUploadedEvent>publish(new ImagesUploadedEvent(uploadedImages));

      return uploadedImages;
    } catch (StorageObjectNotFoundException e) {
      throw new ImageNotFoundException();
    } catch (StorageObjectAlreadyExistsException e) {
      throw new ImageAlreadyExistsException();
    } catch (StorageObjectReferenceException e) {
      throw new UserNotFoundException();
    }
  }

  private void assertUserExists(EntityID userID) throws UserNotFoundException, Exception {
    if (!this.userStore.doesUserExist(userID)) {
      throw new UserNotFoundException();
    }
  }

  public Image createImage(CreateImageCmd cmd) {
    final var imageID = EntityID.CreateNew(cmd.imageID);
    final var userID = EntityID.CreateNew(cmd.userID);

    final ImageMetadata imageMetadata = this.imageStore.getReceivedImageMetadata(userID, imageID);

    if (imageMetadata.isNull() || imageMetadata.isCorrupt(cmd.providedImageHash)) {
      throw new ImageNotFoundException();
    }

    final List<Tag> imageTags =
        cmd.imageTagNames.stream().map(Tag::CreateNew).collect(Collectors.toList());

    final var image =
        new Image.Builder(imageID, cmd.imageName, userID)
            .withPrivacyStatus(cmd.isImagePrivate)
            .withMetadata(imageMetadata)
            .withTags(imageTags)
            .build();

    return image;
  }

  public List<Image> getAllUserImages(String providedUserID) throws Exception {
    final var userID = EntityID.CreateNew(providedUserID);
    final List<Image> images = this.imageStore.getAllUserImages(userID);

    return images;
  }

  public List<Image> searchImagesByName(String providedUserID, String imageName) throws Exception {
    final var userID = EntityID.CreateNew(providedUserID);
    final List<Image> images = this.imageStore.getImagesByName(userID, imageName);

    return images;
  }

  public List<Image> searchImagesByTag(String providedUserID, String tagName) throws Exception {
    final var userID = EntityID.CreateNew(providedUserID);
    final List<Image> images = this.imageStore.getImagesByTag(userID, tagName);

    return images;
  }

  public List<Image> searchImagesByContentLabel(String providedUserID, String contentLabelName)
      throws Exception {
    final var userID = EntityID.CreateNew(providedUserID);
    final List<Image> images = this.imageStore.getImagesByContentLabel(userID, contentLabelName);

    return images;
  }

  public Image getImage(String providedUserID, String providedImageID) throws Exception {
    final var userID = EntityID.CreateNew(providedUserID);
    final var imageID = EntityID.CreateNew(providedImageID);

    final Image image = this.imageStore.getImageByID(userID, imageID);

    if (image.isNull()) {
      throw new ImageNotFoundException();
    }

    return image;
  }

  public void deleteImagesByID(String providedUserID, List<String> providedImageIDs)
      throws Exception {
    final var userID = EntityID.CreateNew(providedUserID);
    final List<EntityID> imageIDs =
        providedImageIDs.stream().map(EntityID::CreateNew).collect(Collectors.toList());

    try {
      this.imageStore.deleteImages(userID, imageIDs);
    } catch (StorageObjectNotFoundException e) {
      throw new ImageNotFoundException();
    }
  }

  public void deleteAllUserImages(String providedUserID) throws Exception {
    final var userID = EntityID.CreateNew(providedUserID);
    this.imageStore.deleteAllUserImages(userID);
  }
}
