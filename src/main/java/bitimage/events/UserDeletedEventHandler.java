package bitimage.events;

import bitimage.domain.uploading.services.ImageService;
import bitimage.events.mappers.EventHandlerMapper;

public class UserDeletedEventHandler implements IEventHandler {

  private final EventHandlerMapper mapper;
  private final ImageService service;

  public UserDeletedEventHandler(EventHandlerMapper mapper, ImageService service) {
    this.service = service;
    this.mapper = mapper;
  }

  public void handle(String message) throws Exception {
    final String deletedUserID = this.mapper.mapToDeletedUserID(message);
    this.service.deleteAllUserImages(deletedUserID);
  }
}
