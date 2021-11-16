package bitimage.storage.mappers;

import bitimage.shared.entities.EntityID;
import bitimage.shared.entities.Image;
import bitimage.shared.entities.Label;
import bitimage.uploading.entities.FileSize;
import bitimage.uploading.entities.ImageMetadata;
import bitimage.uploading.entities.Tag;
import bitimage.storage.dto.FileDTO;
import bitimage.storage.dto.FileMetadataDTO;
import bitimage.storage.dto.ImageDTO;
import bitimage.storage.dto.LabelDTO;
import bitimage.storage.dto.TagDTO;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ImageStoreMapper
{
    /**
     * Composite namespace used to organize AWS S3
     * bucket objects (images) by tenant. (users/<userid>/<image id>)
     */
    private final String fileIDPrefix;

    public ImageStoreMapper(String fileIDPrefix)
    {
        this.fileIDPrefix = fileIDPrefix;
    }

    public List<Image> mapToImages(List<ImageDTO> imageDTOs)
    {
        return imageDTOs.stream()
                .map(this::mapToImage)
                .collect(Collectors.toList());
    }

    public Image mapToImage(ImageDTO imageDTO)
    {
        EntityID imageID = EntityID.CreateNew(imageDTO.id);
        EntityID userID = EntityID.CreateNew(imageDTO.user_id);

        FileSize imageSize = FileSize.CreateFromBytes(imageDTO.size_bytes);
        List<Tag> imageTags = mapToTags(imageDTO.tag_dtos);
        List<Label> imageContentLabels = mapToContentLabels(imageDTO.label_dtos);
        ImageMetadata imageMetadata = ImageMetadata.CreateNew(
                imageID,
                imageSize,
                imageDTO.file_format)
                .secureWithHash(imageDTO.hash_md5);

        return new Image.Builder(imageID, imageDTO.name, userID)
                .withPrivacyStatus(imageDTO.is_private)
                .withContentLabels(imageContentLabels)
                .withMetadata(imageMetadata)
                .withTags(imageTags)
                .build();
    }

    private static List<Tag> mapToTags(List<TagDTO> tagDTOs)
    {
        return tagDTOs.stream()
                .map(ImageStoreMapper::mapToTag)
                .collect(Collectors.toList());
    }

    private static Tag mapToTag(TagDTO tagDTO)
    {
        return Tag.CreateExisting(
                EntityID.CreateNew(tagDTO.id),
                tagDTO.name);
    }

    private static List<Label> mapToContentLabels(List<LabelDTO> labelDTOs)
    {
        return labelDTOs.stream()
                .map(ImageStoreMapper::mapToContentLabel)
                .collect(Collectors.toList());
    }

    private static Label mapToContentLabel(LabelDTO labelDTO)
    {
        return Label.CreateNew(
                EntityID.CreateNew(labelDTO.id),
                labelDTO.name);
    }

    public ImageMetadata mapToImageMetadata(
            EntityID imageID,
            FileMetadataDTO fileMetadataDTO)
    {
        FileSize imageSize = FileSize.CreateFromBytes(
                (double) fileMetadataDTO.size_bytes);

        // transforms file format type provided by AWS (ex. "image/png" -> "png")
        String imageFileFormat = fileMetadataDTO.file_format.split("/")[1];
        ImageMetadata imageMetadata = ImageMetadata.CreateNew(imageID, imageSize, imageFileFormat);

        imageMetadata.secureWithHash(fileMetadataDTO.hash_md5);
        return imageMetadata;
    }

    public List<ImageDTO> mapToImageDTOs(List<Image> images)
    {
        return images.stream()
                .map(ImageStoreMapper::mapToImageDTO)
                .collect(Collectors.toList());
    }

    private static ImageDTO mapToImageDTO(Image image)
    {
        ImageDTO imageDTO = new ImageDTO();

        imageDTO.name = image.getName();
        imageDTO.id = image.getID().toUUID();
        imageDTO.user_id = image.getUserID().toUUID();
        imageDTO.is_private = image.isPrivate();
        imageDTO.file_format = image.getMetadata().getFileFormat();
        imageDTO.hash_md5 = image.getMetadata().getHash().toString();
        imageDTO.size_bytes = image.getMetadata().getSize().toBytes();
        imageDTO.created_at = Timestamp.from(image.getDateTimeCreated());
        imageDTO.updated_at = Timestamp.from(image.getDateTimeUpdated());
        imageDTO.tag_dtos = mapToTagDTOs(image.getTags());

        return imageDTO;
    }

    private static List<TagDTO> mapToTagDTOs(List<Tag> tags)
    {
        return tags.stream()
                .map(ImageStoreMapper::mapToTagDTO)
                .collect(Collectors.toList());
    }

    private static TagDTO mapToTagDTO(Tag tag)
    {
        TagDTO tagDTO = new TagDTO();

        tagDTO.name = tag.getName();
        tagDTO.id = tag.getID().toUUID();
        tagDTO.created_at = Timestamp.from(tag.getDateTimeCreated());
        tagDTO.updated_at = Timestamp.from(tag.getDateTimeUpdated());

        return tagDTO;
    }

    public List<UUID> mapToUUIDs(List<EntityID> ids)
    {
        return ids.stream()
                .map(EntityID::toUUID)
                .collect(Collectors.toList());
    }

    public List<FileDTO> mapToFileDTOs(List<Image> images)
    {
        return images.stream()
                .map(this::mapToFileDTO)
                .collect(Collectors.toList());
    }

    public FileDTO mapToFileDTO(Image image)
    {
        FileDTO fileDTO = new FileDTO();

        fileDTO.id = mapToFileID(image.getUserID(), image.getID());
        fileDTO.hash = image.getMetadata().getHash();

        return fileDTO;
    }

    public List<String> mapToFileIDs(EntityID userID, List<EntityID> imageIDs)
    {
        return imageIDs.stream()
                .map(imageID -> mapToFileID(userID, imageID))
                .collect(Collectors.toList());
    }

    public String mapToFileID(EntityID userID, EntityID imageID)
    {
        return String.format(fileIDPrefix, userID, imageID.toString());
    }
}
