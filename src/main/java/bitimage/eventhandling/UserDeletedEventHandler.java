package bitimage.eventhandling;

import bitimage.domain.uploading.services.ImageUploadService;
import bitimage.eventhandling.mappers.EventHandlerMapper;

public class UserDeletedEventHandler implements IEventHandler {

  private final EventHandlerMapper mapper;
  private final ImageUploadService service;

  public UserDeletedEventHandler(EventHandlerMapper mapper, ImageUploadService service) {
    this.service = service;
    this.mapper = mapper;
  }

  public void handle(String message) throws Exception {
    final String deletedUserID = this.mapper.mapToDeletedUserID(message);
    this.service.deleteAllUserImages(deletedUserID);
  }
}
