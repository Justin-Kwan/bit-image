package bitimage.transport.dto;

import java.util.List;

public class ImageUploadUrlsDTO
{
    public List<ImageUploadUrlDTO> image_upload_urls;

    public ImageUploadUrlsDTO(List<ImageUploadUrlDTO> imageUploadUrlDTOs)
    {
        image_upload_urls = List.copyOf(imageUploadUrlDTOs);
    }
}
