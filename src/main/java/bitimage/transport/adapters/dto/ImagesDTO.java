package bitimage.transport.adapters.dto;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ImagesDTO {

  @Size(max = 5)
  @NotNull(message = "Image list cannot be null")
  @NotEmpty(message = "Image list cannot be empty")
  @Valid
  public List<@Valid @NotNull ImageDTO> images;

  public ImagesDTO(List<ImageDTO> images) {
    this.images = images;
  }

  public ImagesDTO() {}
}
