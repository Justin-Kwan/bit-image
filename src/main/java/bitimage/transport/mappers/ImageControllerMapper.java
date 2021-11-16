package bitimage.transport.mappers;

import bitimage.shared.entities.Image;
import bitimage.shared.entities.Label;
import bitimage.uploading.commands.CreateImageCmd;
import bitimage.uploading.entities.FileUrl;
import bitimage.uploading.entities.Tag;
import bitimage.transport.dto.ImageDTO;
import bitimage.transport.dto.ImageUploadUrlDTO;
import bitimage.transport.dto.ImageUploadUrlsDTO;
import bitimage.transport.dto.ImagesDTO;
import bitimage.transport.dto.LabelDTO;
import bitimage.transport.dto.TagDTO;

import java.util.List;
import java.util.stream.Collectors;

public class ImageControllerMapper
{
    public ImageDTO mapToExpandedViewImageDTO(Image image)
    {
        ImageDTO imageDTO = new ImageDTO();

        imageDTO.id = image.getID().toString();
        imageDTO.name = image.getName();
        imageDTO.hash = image.getMetadata().getHash().toString();
        imageDTO.is_private = Boolean.toString(image.isPrivate());
        imageDTO.size_mb = image.getMetadata().getSize().toMb();
        imageDTO.file_format = image.getMetadata().getFileFormat();
        imageDTO.view_url = image.getViewUrl().toString();
        imageDTO.tags = mapToTagDTOs(image.getTags());
        imageDTO.labels = mapToContentLabels(image.getLabels());

        return imageDTO;
    }

    public ImagesDTO mapToListViewImagesDTO(List<Image> images)
    {
        return new ImagesDTO(images.stream()
                .map(this::mapToListViewImageDTO)
                .collect(Collectors.toList()));
    }

    public ImageDTO mapToListViewImageDTO(Image image)
    {
        ImageDTO imageDTO = new ImageDTO();

        imageDTO.id = image.getID().toString();
        imageDTO.name = image.getName();
        imageDTO.view_url = image.getViewUrl().toString();
        imageDTO.is_private = Boolean.toString(image.isPrivate());
        imageDTO.file_format = image.getMetadata().getFileFormat();

        return imageDTO;
    }

    public List<LabelDTO> mapToContentLabels(List<Label> labels)
    {
        return labels.stream()
                .map(this::mapToContentLabel)
                .collect(Collectors.toList());
    }

    private LabelDTO mapToContentLabel(Label label)
    {
        LabelDTO labelDTO = new LabelDTO();

        labelDTO.name = label.getName();
        labelDTO.id = label.getID().toString();

        return labelDTO;
    }

    public List<TagDTO> mapToTagDTOs(List<Tag> tags)
    {
        return tags.stream()
                .map(ImageControllerMapper::mapToTagDTO)
                .collect(Collectors.toList());
    }

    private static TagDTO mapToTagDTO(Tag tag)
    {
        TagDTO tagDTO = new TagDTO();

        tagDTO.name = tag.getName();
        tagDTO.id = tag.getID().toString();

        return tagDTO;
    }

    public ImageUploadUrlsDTO mapToImageUploadUrlsDTO(List<FileUrl> uploadUrls)
    {
        return new ImageUploadUrlsDTO(uploadUrls.stream()
                .map(this::mapToImageUploadUrlDTO)
                .collect(Collectors.toList()));
    }

    public ImageUploadUrlDTO mapToImageUploadUrlDTO(FileUrl uploadUrl)
    {
        ImageUploadUrlDTO imageUploadUrlDTO = new ImageUploadUrlDTO();

        imageUploadUrlDTO.url = uploadUrl.toString();
        imageUploadUrlDTO.method = "PUT";
        imageUploadUrlDTO.image_id = uploadUrl.getImageID().toString();

        return imageUploadUrlDTO;
    }

    public List<CreateImageCmd> mapToCreateImageCmds(String userID, ImagesDTO request)
    {
        return request.images.stream()
                .map(imageDTO -> mapToCreateImageCmd(userID, imageDTO))
                .collect(Collectors.toList());
    }

    private CreateImageCmd mapToCreateImageCmd(String userID, ImageDTO imageDTO)
    {
        CreateImageCmd createImageCmd = new CreateImageCmd();

        createImageCmd.userID = userID.toLowerCase();
        createImageCmd.imageID = imageDTO.id.toLowerCase();
        createImageCmd.imageName = imageDTO.name.toLowerCase();
        createImageCmd.providedImageHash = imageDTO.hash.toLowerCase();
        createImageCmd.isImagePrivate = Boolean.valueOf(imageDTO.is_private);
        createImageCmd.imageTagNames = mapToTagNames(imageDTO.tags);

        return createImageCmd;
    }

    private List<String> mapToTagNames(List<TagDTO> tagDTOs)
    {
        return tagDTOs.stream().map(tagDTO -> tagDTO.name.toLowerCase()).collect(Collectors.toList());
    }
}
