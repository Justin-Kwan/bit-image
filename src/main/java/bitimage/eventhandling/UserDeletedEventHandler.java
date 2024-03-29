package bitimage.eventhandling;

import bitimage.uploading.services.ImageUploadService;
import bitimage.eventhandling.mappers.EventHandlerMapper;

public class UserDeletedEventHandler
        implements EventHandler
{
    private final EventHandlerMapper mapper;
    private final ImageUploadService service;

    public UserDeletedEventHandler(EventHandlerMapper mapper, ImageUploadService service)
    {
        this.service = service;
        this.mapper = mapper;
    }

    public void handle(String message)
            throws Exception
    {
        String deletedUserID = mapper.mapToDeletedUserID(message);
        service.deleteAllUserImages(deletedUserID);
    }
}
