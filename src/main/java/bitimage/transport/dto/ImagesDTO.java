package bitimage.transport.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
  @JsonProperty
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<@Valid @NotNull ImageDTO> images;

  public ImagesDTO(List<ImageDTO> images) {
    this.images = images;
  }

  public ImagesDTO() {}
}
