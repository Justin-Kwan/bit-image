package bitimage.domain.common.events;

import java.time.Instant;

public interface IDomainEvent {
  public Instant getTimeOccurred();
}
