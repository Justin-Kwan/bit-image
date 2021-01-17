package bitimage.storage.adapters.datastores;

import bitimage.domain.common.entities.EntityID;
import bitimage.domain.sharedkernel.entities.Image;
import bitimage.domain.sharedkernel.entities.NullImage;
import bitimage.domain.uploading.entities.FileUrl;
import bitimage.domain.uploading.entities.ImageMetadata;
import bitimage.domain.uploading.entities.NullImageMetadata;
import bitimage.domain.uploading.ports.IImageStore;
import bitimage.storage.adapters.dto.FileDTO;
import bitimage.storage.adapters.dto.FileMetadataDTO;
import bitimage.storage.adapters.dto.ImageDTO;
import bitimage.storage.adapters.mappers.ImageStoreMapper;
import bitimage.storage.aws.AwsConstants;
import bitimage.storage.aws.IFileSystem;
import bitimage.storage.dao.DAOFactory;
import bitimage.storage.dao.ImageDAO;
import java.util.List;

public class ImageStore implements IImageStore {

  private final DAOFactory daoFactory;
  private final IFileSystem fileSystem;
  private final ImageStoreMapper mapper;

  public ImageStore(DAOFactory daoFactory, IFileSystem fileSystem, ImageStoreMapper mapper) {
    this.daoFactory = daoFactory;
    this.fileSystem = fileSystem;
    this.mapper = mapper;
  }

  public FileUrl generateImageUploadUrl(EntityID userID) {
    final var newImageID = EntityID.CreateNew();
    String newFileID = this.mapper.mapToFileID(userID, newImageID);

    final String imageUploadUrl =
        this.fileSystem.generateFileUploadUrl(newFileID, AwsConstants.TEMPORARY_STORAGE_FOLDER);

    return new FileUrl(imageUploadUrl, newImageID);
  }

  public ImageMetadata getReceivedImageMetadata(EntityID userID, EntityID imageID) {
    final String fileID = this.mapper.mapToFileID(userID, imageID);

    final FileMetadataDTO fileMetadataDTO =
        this.fileSystem.getFileMetadata(fileID, AwsConstants.TEMPORARY_STORAGE_FOLDER);

    if (fileMetadataDTO.isNull()) {
      return new NullImageMetadata();
    }

    final ImageMetadata imageMetadata = this.mapper.mapToImageMetadata(imageID, fileMetadataDTO);

    return imageMetadata;
  }

  public List<Image> addImages(List<Image> images) throws Exception {
    final List<FileDTO> fileDTOs = this.mapper.mapToFileDTOs(images);

    // first move received images into permanent storage folder
    this.fileSystem.moveFilesToFolder(
        fileDTOs, AwsConstants.TEMPORARY_STORAGE_FOLDER, AwsConstants.PERMANENT_STORAGE_FOLDER);

    // then store image data in rdbms
    final ImageDAO imageDAO = this.daoFactory.getImageDAO();
    final List<ImageDTO> imageDTOs = this.mapper.mapToImageDTOs(images);

    imageDAO.insertImages(imageDTOs);
    images.parallelStream().forEach(this::hydrateWithViewUrl);

    return images;
  }

  public List<Image> getAllPublicImages() throws Exception {
    final ImageDAO imageDAO = this.daoFactory.getImageDAO();
    final List<ImageDTO> imageDTOs = imageDAO.selectAllPublicImages();

    final List<Image> images = this.mapper.mapToImages(imageDTOs);
    images.parallelStream().forEach(this::hydrateWithViewUrl);

    return images;
  }

  public List<Image> getAllUserImages(EntityID userID) throws Exception {
    final ImageDAO imageDAO = this.daoFactory.getImageDAO();
    final List<ImageDTO> imageDTOs = imageDAO.selectAllUserImages(userID.toUUID());

    final List<Image> images = this.mapper.mapToImages(imageDTOs);
    images.parallelStream().forEach(this::hydrateWithViewUrl);

    return images;
  }

  /**
   * Gets all image entities similar to provided image name, which belong to the provided user id.
   *
   * <p>Retrieves all image metadata from RDBMS and generates a view url for each image (pointing to
   * AWS S3 bucket).
   */
  public List<Image> getImagesByName(EntityID userID, String imageName) throws Exception {
    final ImageDAO imageDAO = this.daoFactory.getImageDAO();
    final List<ImageDTO> imageDTOs = imageDAO.selectImagesByName(userID.toUUID(), imageName);

    final List<Image> images = this.mapper.mapToImages(imageDTOs);
    images.parallelStream().forEach(this::hydrateWithViewUrl);

    return images;
  }

  public List<Image> getImagesByTag(EntityID userID, String tagName) throws Exception {
    final ImageDAO imageDAO = this.daoFactory.getImageDAO();
    final List<ImageDTO> imageDTOs = imageDAO.selectImagesByTag(userID.toUUID(), tagName);

    final List<Image> images = this.mapper.mapToImages(imageDTOs);
    images.parallelStream().forEach(this::hydrateWithViewUrl);

    return images;
  }

  public List<Image> getImagesByContentLabel(EntityID userID, String labelName) throws Exception {
    final ImageDAO imageDAO = this.daoFactory.getImageDAO();

    final List<ImageDTO> imageDTOs =
        imageDAO.selectImagesByContentLabel(userID.toUUID(), labelName);

    final List<Image> images = this.mapper.mapToImages(imageDTOs);
    images.parallelStream().forEach(this::hydrateWithViewUrl);

    return images;
  }

  public Image getImageByID(EntityID userID, EntityID imageID) throws Exception {
    final ImageDAO imageDAO = this.daoFactory.getImageDAO();
    final ImageDTO imageDTO = imageDAO.selectImageByID(userID.toUUID(), imageID.toUUID());

    if (imageDTO.isNull()) {
      return new NullImage();
    }

    final Image image = this.mapper.mapToImage(imageDTO);
    this.hydrateWithViewUrl(image);

    return image;
  }

  private Image hydrateWithViewUrl(Image image) {
    final String fileID = this.mapper.mapToFileID(image.getUserID(), image.getID());

    final String imageViewUrl =
        this.fileSystem.generateFileViewUrl(fileID, AwsConstants.PERMANENT_STORAGE_FOLDER);

    final var viewUrl = new FileUrl(imageViewUrl, image.getID());
    image.setViewUrl(viewUrl);

    return image;
  }

  public void deleteImages(EntityID userID, List<EntityID> imageIDs) throws Exception {
    final ImageDAO imageDAO = this.daoFactory.getImageDAO();
    imageDAO.deleteImagesByID(userID.toUUID(), this.mapper.mapToUUIDs(imageIDs));

    final List<String> fileIDsToDelete = this.mapper.mapToFileIDs(userID, imageIDs);

    this.fileSystem.deleteFilesFromFolder(fileIDsToDelete, AwsConstants.PERMANENT_STORAGE_FOLDER);
  }

  public void deleteAllUserImages(EntityID userID) throws Exception {
    final ImageDAO imageDAO = this.daoFactory.getImageDAO();
    imageDAO.deleteImagesByUserID(userID.toUUID());

    final String filePrefix = "users/%s/".formatted(userID.toString());
    final List<String> fileIDsToDelete =
        this.fileSystem.lookupFileIDsByPrefix(filePrefix, AwsConstants.PERMANENT_STORAGE_FOLDER);

    this.fileSystem.deleteFilesFromFolder(fileIDsToDelete, AwsConstants.PERMANENT_STORAGE_FOLDER);
  }
}
