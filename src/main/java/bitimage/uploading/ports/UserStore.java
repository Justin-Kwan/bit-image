package bitimage.uploading.ports;

import bitimage.shared.entities.EntityID;
import bitimage.uploading.entities.User;

public interface UserStore
{
    void addUser(User user)
            throws Exception;

    void deleteUserByID(EntityID userID)
            throws Exception;

    boolean doesUserExist(EntityID userID)
            throws Exception;
}
