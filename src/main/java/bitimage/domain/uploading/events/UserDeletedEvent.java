package bitimage.domain.uploading.events;

import bitimage.domain.common.entities.EntityID;
import bitimage.domain.common.events.IDomainEvent;
import java.time.Instant;

public class UserDeletedEvent implements IDomainEvent {

  private final EntityID userID;
  private final Instant timeOccurred;

  public UserDeletedEvent(EntityID userID) {
    this.userID = userID;
    this.timeOccurred = Instant.now();
  }

  public EntityID getUserID() {
    return this.userID;
  }

  public Instant getTimeOccurred() {
    return this.timeOccurred;
  }
}
