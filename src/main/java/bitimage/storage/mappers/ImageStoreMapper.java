package bitimage.storage.mappers;

import bitimage.domain.common.entities.EntityID;
import bitimage.domain.common.entities.Image;
import bitimage.domain.common.entities.Label;
import bitimage.domain.uploading.entities.FileSize;
import bitimage.domain.uploading.entities.ImageMetadata;
import bitimage.domain.uploading.entities.Tag;
import bitimage.storage.dto.FileDTO;
import bitimage.storage.dto.FileMetadataDTO;
import bitimage.storage.dto.ImageDTO;
import bitimage.storage.dto.LabelDTO;
import bitimage.storage.dto.TagDTO;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ImageStoreMapper {

  /**
   * Composite namespace used to organize AWS S3 bucket objects (images) by tenant. (users/<user
   * id>/<image id>)
   */
  private final String fileIDPrefix;

  public ImageStoreMapper(String fileIDPrefix) {
    this.fileIDPrefix = fileIDPrefix;
  }

  public List<Image> mapToImages(List<ImageDTO> imageDTOs) {
    return imageDTOs.stream().map(this::mapToImage).collect(Collectors.toList());
  }

  public Image mapToImage(ImageDTO imageDTO) {
    final var imageID = EntityID.CreateNew(imageDTO.id);
    final var userID = EntityID.CreateNew(imageDTO.user_id);

    final var imageFileFormat = imageDTO.file_format;
    final var imageSize = FileSize.CreateFromBytes(imageDTO.size_bytes);

    final var imageMetadata =
        ImageMetadata.CreateNew(imageID, imageSize, imageFileFormat)
            .secureWithHash(imageDTO.hash_md5);

    final List<Tag> imageTags = this.mapToTags(imageDTO.tag_dtos);
    final List<Label> imageContentLabels = this.mapToContentLabels(imageDTO.label_dtos);

    final var image =
        new Image.Builder(imageID, imageDTO.name, userID)
            .withPrivacyStatus(imageDTO.is_private)
            .withContentLabels(imageContentLabels)
            .withMetadata(imageMetadata)
            .withTags(imageTags)
            .build();

    return image;
  }

  public List<Tag> mapToTags(List<TagDTO> tagDTOs) {
    return tagDTOs.stream().map(this::mapToTag).collect(Collectors.toList());
  }

  private Tag mapToTag(TagDTO tagDTO) {
    final var tagID = EntityID.CreateNew(tagDTO.id);
    return Tag.CreateExisting(tagID, tagDTO.name);
  }

  public final List<Label> mapToContentLabels(List<LabelDTO> labelDTOs) {
    return labelDTOs.stream().map(this::mapToContentLabel).collect(Collectors.toList());
  }

  public final Label mapToContentLabel(LabelDTO labelDTO) {
    final var labelID = EntityID.CreateNew(labelDTO.id);
    return Label.CreateNew(labelID, labelDTO.name);
  }

  public ImageMetadata mapToImageMetadata(EntityID imageID, FileMetadataDTO fileMetadataDTO) {
    // transforms file format type provided by AWS (ex. "image/png" -> "png")
    final String imageFileFormat = fileMetadataDTO.file_format.split("/")[1];

    final var imageSize = FileSize.CreateFromBytes((double) fileMetadataDTO.size_bytes);

    final var imageMetadata = ImageMetadata.CreateNew(imageID, imageSize, imageFileFormat);

    imageMetadata.secureWithHash(fileMetadataDTO.hash_md5);

    return imageMetadata;
  }

  public List<ImageDTO> mapToImageDTOs(List<Image> images) {
    return images.stream().map(this::mapToImageDTO).collect(Collectors.toList());
  }

  public ImageDTO mapToImageDTO(Image image) {
    final var imageDTO = new ImageDTO();

    imageDTO.name = image.getName();
    imageDTO.id = image.getID().toUUID();
    imageDTO.user_id = image.getUserID().toUUID();
    imageDTO.is_private = image.isPrivate();
    imageDTO.file_format = image.getMetadata().getFileFormat();
    imageDTO.hash_md5 = image.getMetadata().getHash().toString();
    imageDTO.size_bytes = image.getMetadata().getSize().toBytes();
    imageDTO.created_at = Timestamp.from(image.getDateTimeCreated());
    imageDTO.updated_at = Timestamp.from(image.getDateTimeUpdated());
    imageDTO.tag_dtos = this.mapToTagDTOs(image.getTags());

    return imageDTO;
  }

  private List<TagDTO> mapToTagDTOs(List<Tag> tags) {
    return tags.stream().map(this::mapToTagDTO).collect(Collectors.toList());
  }

  private TagDTO mapToTagDTO(Tag tag) {
    final var tagDTO = new TagDTO();

    tagDTO.name = tag.getName();
    tagDTO.id = tag.getID().toUUID();
    tagDTO.created_at = Timestamp.from(tag.getDateTimeCreated());
    tagDTO.updated_at = Timestamp.from(tag.getDateTimeUpdated());

    return tagDTO;
  }

  public List<UUID> mapToUUIDs(List<EntityID> ids) {
    return ids.stream().map(EntityID::toUUID).collect(Collectors.toList());
  }

  public List<FileDTO> mapToFileDTOs(List<Image> images) {
    return images.stream().map(this::mapToFileDTO).collect(Collectors.toList());
  }

  public FileDTO mapToFileDTO(Image image) {
    final var fileDTO = new FileDTO();

    fileDTO.id = this.mapToFileID(image.getUserID(), image.getID());
    fileDTO.hash = image.getMetadata().getHash().toString();

    return fileDTO;
  }

  public List<String> mapToFileIDs(EntityID userID, List<EntityID> imageIDs) {
    return imageIDs.stream()
        .map(imageID -> this.mapToFileID(userID, imageID))
        .collect(Collectors.toList());
  }

  public String mapToFileID(EntityID userID, EntityID imageID) {
    return this.fileIDPrefix.formatted(userID, imageID.toString());
  }
}
