package bitimage.domain.common.entities;

import java.time.Instant;

public abstract class Entity {

  protected final EntityID id;
  protected final Instant dateTimeCreated;
  protected final Instant dateTimeUpdated;

  protected Entity(EntityID id) {
    this.id = id;
    this.dateTimeCreated = Instant.now();
    this.dateTimeUpdated = Instant.now();
  }

  public EntityID getID() {
    return this.id;
  }

  public Instant getDateTimeCreated() {
    return this.dateTimeCreated;
  }

  public Instant getDateTimeUpdated() {
    return this.dateTimeUpdated;
  }
}
