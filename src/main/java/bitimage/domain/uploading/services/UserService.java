package bitimage.domain.uploading.services;

import bitimage.domain.common.entities.EntityID;
import bitimage.domain.uploading.entities.User;
import bitimage.domain.uploading.events.UserDeletedEvent;
import bitimage.domain.uploading.exceptions.UserAlreadyExistsException;
import bitimage.domain.uploading.ports.IEventPublisher;
import bitimage.domain.uploading.ports.IUserStore;
import bitimage.storage.exceptions.StorageObjectAlreadyExistsException;

public class UserService
{
    private final IUserStore userStore;
    private final IEventPublisher eventPublisher;

    public UserService(IUserStore userStore, IEventPublisher eventPublisher)
    {
        this.userStore = userStore;
        this.eventPublisher = eventPublisher;
    }

    public User createUser(String providedUserID)
            throws Exception
    {
        EntityID userID = EntityID.CreateNew(providedUserID);
        User user = User.CreateNew(userID);

        try {
            userStore.addUser(user);
        }
        catch (StorageObjectAlreadyExistsException e) {
            throw new UserAlreadyExistsException();
        }

        return user;
    }

    /**
     * Delete user and dispatch event to asynchronously
     * mass delete user's images.
     */
    public void deleteUser(String providedUserID)
            throws Exception
    {
        EntityID userID = EntityID.CreateNew(providedUserID);
        userStore.deleteUserByID(userID);
        eventPublisher.publish(new UserDeletedEvent(userID));
    }
}
