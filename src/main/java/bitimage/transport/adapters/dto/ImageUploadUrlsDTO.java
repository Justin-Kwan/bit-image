package bitimage.transport.adapters.dto;

import java.util.List;

public class ImageUploadUrlsDTO {

  public List<ImageUploadUrlDTO> image_upload_urls;

  public ImageUploadUrlsDTO(List<ImageUploadUrlDTO> imageUploadUrlDTOs) {
    this.image_upload_urls = imageUploadUrlDTOs;
  }
}
