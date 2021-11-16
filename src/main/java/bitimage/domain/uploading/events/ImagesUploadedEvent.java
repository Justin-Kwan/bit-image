package bitimage.domain.uploading.events;

import bitimage.domain.common.entities.Image;
import bitimage.domain.common.events.IDomainEvent;

import java.time.Instant;
import java.util.List;

public class ImagesUploadedEvent
        implements IDomainEvent
{
    private final List<Image> images;
    private final Instant timeOccurred;

    public ImagesUploadedEvent(List<Image> images)
    {
        this.images = List.copyOf(images);
        this.timeOccurred = Instant.now();
    }

    public List<Image> getImages()
    {
        return images;
    }

    public Instant getTimeOccurred()
    {
        return timeOccurred;
    }
}
