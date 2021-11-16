package bitimage.uploading.services;

import bitimage.shared.entities.EntityID;
import bitimage.uploading.entities.User;
import bitimage.uploading.events.UserDeletedEvent;
import bitimage.uploading.exceptions.UserAlreadyExistsException;
import bitimage.uploading.ports.EventPublisher;
import bitimage.uploading.ports.UserStore;
import bitimage.storage.exceptions.StorageObjectAlreadyExistsException;

public class UserService
{
    private final UserStore userStore;
    private final EventPublisher eventPublisher;

    public UserService(UserStore userStore, EventPublisher eventPublisher)
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
