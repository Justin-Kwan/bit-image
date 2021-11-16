package bitimage.transport.mappers;

import bitimage.uploading.entities.User;
import bitimage.transport.dto.UserDTO;

public class UserControllerMapper
{
    public UserDTO mapToUserDTO(User user)
    {
        UserDTO userDTO = new UserDTO();

        userDTO.id = user.getID().toString();
        userDTO.image_upload_count = user.getImageUploadCount();

        return userDTO;
    }
}
