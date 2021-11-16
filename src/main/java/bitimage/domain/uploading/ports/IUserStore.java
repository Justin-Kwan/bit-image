package bitimage.domain.uploading.ports;

import bitimage.domain.common.entities.EntityID;
import bitimage.domain.uploading.entities.User;

public interface IUserStore
{
    void addUser(User user)
            throws Exception;

    void deleteUserByID(EntityID userID)
            throws Exception;

    boolean doesUserExist(EntityID userID)
            throws Exception;
}
