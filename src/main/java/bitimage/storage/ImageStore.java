package bitimage.storage;

import bitimage.domain.common.entities.EntityID;
import bitimage.domain.common.entities.Image;
import bitimage.domain.common.entities.NullImage;
import bitimage.domain.uploading.entities.FileUrl;
import bitimage.domain.uploading.entities.ImageMetadata;
import bitimage.domain.uploading.entities.NullImageMetadata;
import bitimage.domain.uploading.ports.IImageStore;
import bitimage.storage.dto.FileDTO;
import bitimage.storage.dto.FileMetadataDTO;
import bitimage.storage.dto.ImageDTO;
import bitimage.storage.mappers.ImageStoreMapper;
import bitimage.storage.postgres.dao.DAOFactory;
import bitimage.storage.postgres.dao.ImageDAO;
import bitimage.storage.s3.IFileSystem;
import bitimage.storage.s3.S3Constants;

import java.util.List;

public class ImageStore
        implements IImageStore
{
    private final DAOFactory daoFactory;
    private final IFileSystem fileSystem;
    private final ImageStoreMapper mapper;

    public ImageStore(DAOFactory daoFactory, IFileSystem fileSystem, ImageStoreMapper mapper)
    {
        this.daoFactory = daoFactory;
        this.fileSystem = fileSystem;
        this.mapper = mapper;
    }

    public FileUrl generateImageUploadUrl(EntityID userID)
    {
        EntityID newImageID = EntityID.CreateNew();
        String newFileID = mapper.mapToFileID(userID, newImageID);

        String imageUploadUrl = fileSystem.generateFileUploadUrl(
                newFileID,
                S3Constants.TEMPORARY_STORAGE_FOLDER);

        return new FileUrl(imageUploadUrl, newImageID);
    }

    public ImageMetadata getReceivedImageMetadata(EntityID userID, EntityID imageID)
    {
        FileMetadataDTO fileMetadataDTO = fileSystem.getFileMetadata(
                mapper.mapToFileID(userID, imageID),
                S3Constants.TEMPORARY_STORAGE_FOLDER);

        if (fileMetadataDTO.isNull()) {
            return new NullImageMetadata();
        }

        return mapper.mapToImageMetadata(imageID, fileMetadataDTO);
    }

    public List<Image> addImages(List<Image> images)
            throws Exception
    {
        List<FileDTO> fileDTOs = mapper.mapToFileDTOs(images);

        // first move received images into permanent storage folder
        fileSystem.moveFilesToFolder(
                fileDTOs,
                S3Constants.TEMPORARY_STORAGE_FOLDER,
                S3Constants.PERMANENT_STORAGE_FOLDER);

        // then store image data in rdbms
        ImageDAO imageDAO = daoFactory.getImageDAO();
        List<ImageDTO> imageDTOs = mapper.mapToImageDTOs(images);

        imageDAO.insertImages(imageDTOs);
        images.parallelStream().forEach(this::hydrateWithViewUrl);

        return images;
    }

    public List<Image> getAllPublicImages()
            throws Exception
    {
        ImageDAO imageDAO = daoFactory.getImageDAO();
        List<ImageDTO> imageDTOs = imageDAO.selectAllPublicImages();

        List<Image> images = mapper.mapToImages(imageDTOs);
        images.parallelStream().forEach(this::hydrateWithViewUrl);

        return images;
    }

    public List<Image> getAllUserImages(EntityID userID)
            throws Exception
    {
        ImageDAO imageDAO = daoFactory.getImageDAO();
        List<ImageDTO> imageDTOs = imageDAO.selectAllUserImages(userID.toUUID());

        List<Image> images = mapper.mapToImages(imageDTOs);
        images.parallelStream().forEach(this::hydrateWithViewUrl);

        return images;
    }

    /**
     * Gets all image entities similar to provided image name, which belong to the provided user id.
     *
     * <p>Retrieves all image metadata from RDBMS and generates a view url for each image (pointing to
     * AWS S3 bucket).
     */
    public List<Image> getImagesByName(EntityID userID, String imageName)
            throws Exception
    {
        ImageDAO imageDAO = daoFactory.getImageDAO();
        List<ImageDTO> imageDTOs = imageDAO.selectImagesByName(userID.toUUID(), imageName);

        List<Image> images = mapper.mapToImages(imageDTOs);
        images.parallelStream().forEach(this::hydrateWithViewUrl);

        return images;
    }

    public List<Image> getImagesByTag(EntityID userID, String tagName)
            throws Exception
    {
        ImageDAO imageDAO = daoFactory.getImageDAO();
        List<ImageDTO> imageDTOs = imageDAO.selectImagesByTag(userID.toUUID(), tagName);

        List<Image> images = mapper.mapToImages(imageDTOs);
        images.parallelStream().forEach(this::hydrateWithViewUrl);

        return images;
    }

    public List<Image> getImagesByContentLabel(EntityID userID, String labelName)
            throws Exception
    {
        ImageDAO imageDAO = daoFactory.getImageDAO();
        List<ImageDTO> imageDTOs = imageDAO.selectImagesByContentLabel(userID.toUUID(), labelName);

        List<Image> images = mapper.mapToImages(imageDTOs);
        images.parallelStream().forEach(this::hydrateWithViewUrl);

        return images;
    }

    public Image getImageByID(EntityID userID, EntityID imageID)
            throws Exception
    {
        ImageDAO imageDAO = daoFactory.getImageDAO();
        ImageDTO imageDTO = imageDAO.selectImageByID(userID.toUUID(), imageID.toUUID());

        if (imageDTO.isNull()) {
            return new NullImage();
        }

        Image image = mapper.mapToImage(imageDTO);
        hydrateWithViewUrl(image);

        return image;
    }

    private void hydrateWithViewUrl(Image image)
    {
        String fileID = mapper.mapToFileID(image.getUserID(), image.getID());
        String imageViewUrl = fileSystem.generateFileViewUrl(
                fileID,
                S3Constants.PERMANENT_STORAGE_FOLDER);

        image.setViewUrl(new FileUrl(imageViewUrl, image.getID()));
    }

    public void deleteImages(EntityID userID, List<EntityID> imageIDs)
            throws Exception
    {
        ImageDAO imageDAO = daoFactory.getImageDAO();
        imageDAO.deleteImagesByID(userID.toUUID(), mapper.mapToUUIDs(imageIDs));

        List<String> fileIDsToDelete = mapper.mapToFileIDs(userID, imageIDs);

        fileSystem.deleteFilesFromFolder(fileIDsToDelete, S3Constants.PERMANENT_STORAGE_FOLDER);
    }

    public void deleteAllUserImages(EntityID userID)
            throws Exception
    {
        ImageDAO imageDAO = daoFactory.getImageDAO();
        imageDAO.deleteImagesByUserID(userID.toUUID());

        // delete images by user id prefix
        List<String> fileIDsToDelete = fileSystem.lookupFileIDsByPrefix(
                String.format("users/%s/", userID),
                S3Constants.PERMANENT_STORAGE_FOLDER);

        fileSystem.deleteFilesFromFolder(fileIDsToDelete, S3Constants.PERMANENT_STORAGE_FOLDER);
    }
}
