package bitimage.uploading.services;

import bitimage.shared.entities.EntityID;
import bitimage.shared.entities.Image;
import bitimage.uploading.commands.CreateImageCmd;
import bitimage.uploading.entities.FileUrl;
import bitimage.uploading.entities.ImageMetadata;
import bitimage.uploading.entities.Tag;
import bitimage.uploading.events.ImagesUploadedEvent;
import bitimage.uploading.exceptions.ImageAlreadyExistsException;
import bitimage.uploading.exceptions.ImageNotFoundException;
import bitimage.uploading.exceptions.UserNotFoundException;
import bitimage.uploading.ports.EventPublisher;
import bitimage.uploading.ports.ImageStore;
import bitimage.uploading.ports.UserStore;
import bitimage.storage.exceptions.StorageObjectAlreadyExistsException;
import bitimage.storage.exceptions.StorageObjectNotFoundException;
import bitimage.storage.exceptions.StorageObjectReferenceException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImageUploadService
{

    private final ImageStore imageStore;
    private final UserStore userStore;
    private final EventPublisher eventPublisher;

    public ImageUploadService(
            ImageStore imageStore,
            UserStore userStore,
            EventPublisher eventPublisher)
    {
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
            throws Exception
    {
        EntityID userID = EntityID.CreateNew(providedUserID);
        assertUserExists(userID);

        List<FileUrl> uploadUrls = IntStream.range(0, imageUploadCount)
                .parallel()
                .mapToObj(i -> imageStore.generateImageUploadUrl(userID))
                .collect(Collectors.toList());

        return uploadUrls;
    }

    /**
     * Confirms that all images have been uploaded to image
     * upload urls. At most, 5 images can be created per request.
     *
     * <p>Image upload confirmation should store user provided
     * images metadata and publish an event indicating all images
     * have been uploaded.
     *
     * <p>Cron job could handle removal of orphaned images that
     * pass potential race condition.
     */
    public List<Image> confirmImagesUploaded(List<CreateImageCmd> cmds)
            throws Exception
    {
        EntityID userID = EntityID.CreateNew(cmds.get(0).userID);
        assertUserExists(userID);

        List<Image> images = cmds.stream().map(this::createImage).collect(Collectors.toList());

        try {
            List<Image> uploadedImages = imageStore.addImages(images);
            eventPublisher.publish(new ImagesUploadedEvent(uploadedImages));

            return uploadedImages;
        }
        catch (StorageObjectNotFoundException e) {
            throw new ImageNotFoundException();
        }
        catch (StorageObjectAlreadyExistsException e) {
            throw new ImageAlreadyExistsException();
        }
        catch (StorageObjectReferenceException e) {
            throw new UserNotFoundException();
        }
    }

    private void assertUserExists(EntityID userID)
            throws Exception
    {
        if (!userStore.doesUserExist(userID)) {
            throw new UserNotFoundException();
        }
    }

    public Image createImage(CreateImageCmd cmd)
    {
        EntityID imageID = EntityID.CreateNew(cmd.imageID);
        EntityID userID = EntityID.CreateNew(cmd.userID);

        ImageMetadata imageMetadata = imageStore.getReceivedImageMetadata(userID, imageID);

        if (imageMetadata.isNull() || imageMetadata.isCorrupt(cmd.providedImageHash)) {
            throw new ImageNotFoundException();
        }

        List<Tag> imageTags = cmd.imageTagNames.stream()
                .map(Tag::CreateNew)
                .collect(Collectors.toList());

        return new Image.Builder(imageID, cmd.imageName, userID)
                .withPrivacyStatus(cmd.isImagePrivate)
                .withMetadata(imageMetadata)
                .withTags(imageTags)
                .build();
    }

    public List<Image> getAllUserImages(String providedUserID)
            throws Exception
    {
        EntityID userID = EntityID.CreateNew(providedUserID);
        return imageStore.getAllUserImages(userID);
    }

    public List<Image> getAllPublicImages()
            throws Exception
    {
        return imageStore.getAllPublicImages();
    }

    public List<Image> searchImagesByName(String providedUserID, String imageName)
            throws Exception
    {
        EntityID userID = EntityID.CreateNew(providedUserID);
        return imageStore.getImagesByName(userID, imageName);
    }

    public List<Image> searchImagesByTag(String providedUserID, String tagName)
            throws Exception
    {
        EntityID userID = EntityID.CreateNew(providedUserID);
        return imageStore.getImagesByTag(userID, tagName);
    }

    public List<Image> searchImagesByContentLabel(String providedUserID, String contentLabelName)
            throws Exception
    {
        EntityID userID = EntityID.CreateNew(providedUserID);
        return imageStore.getImagesByContentLabel(userID, contentLabelName);
    }

    public Image getImage(String providedUserID, String providedImageID)
            throws Exception
    {
        EntityID userID = EntityID.CreateNew(providedUserID);
        EntityID imageID = EntityID.CreateNew(providedImageID);

        Image image = imageStore.getImageByID(userID, imageID);

        if (image.isNull()) {
            throw new ImageNotFoundException();
        }

        return image;
    }

    public void deleteImagesByID(String providedUserID, List<String> providedImageIDs)
            throws Exception
    {
        EntityID userID = EntityID.CreateNew(providedUserID);
        List<EntityID> imageIDs = providedImageIDs.stream()
                .map(EntityID::CreateNew)
                .collect(Collectors.toList());

        try {
            imageStore.deleteImages(userID, imageIDs);
        }
        catch (StorageObjectNotFoundException e) {
            throw new ImageNotFoundException();
        }
    }

    public void deleteAllUserImages(String providedUserID)
            throws Exception
    {
        EntityID userID = EntityID.CreateNew(providedUserID);
        imageStore.deleteAllUserImages(userID);
    }
}
