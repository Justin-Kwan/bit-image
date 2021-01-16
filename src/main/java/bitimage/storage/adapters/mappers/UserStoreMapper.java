package bitimage.storage.adapters.mappers;

import bitimage.domain.uploading.entities.User;
import bitimage.storage.adapters.dto.UserDTO;
import java.sql.Timestamp;

public class UserStoreMapper {

  public UserDTO mapToUserDTO(User user) {
    final UserDTO userDTO = new UserDTO();

    userDTO.id = user.getID().toUUID();
    userDTO.image_upload_count = user.getImageUploadCount();
    userDTO.image_upload_limit = user.getImageUploadLimit();
    userDTO.created_at = Timestamp.from(user.getDateTimeCreated());
    userDTO.updated_at = Timestamp.from(user.getDateTimeUpdated());

    return userDTO;
  }
}
