package bitimage.transport.adapters.mappers;

import bitimage.domain.uploading.entities.User;
import bitimage.transport.adapters.dto.UserDTO;

public class UserControllerMapper {

  public UserDTO mapToUserDTO(User user) {
    final var userDTO = new UserDTO();

    userDTO.id = user.getID().toString();
    userDTO.image_upload_count = user.getImageUploadCount();

    return userDTO;
  }
}
