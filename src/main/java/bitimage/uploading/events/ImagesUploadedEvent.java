package bitimage.uploading.events;

import bitimage.shared.entities.Image;
import bitimage.shared.events.DomainEvent;

import java.time.Instant;
import java.util.List;

public class ImagesUploadedEvent
        implements DomainEvent
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
