package bitimage.shared.entities;

import bitimage.uploading.entities.FileUrl;
import bitimage.uploading.entities.ImageMetadata;
import bitimage.uploading.entities.Tag;

import java.util.Collections;
import java.util.List;

public class Image
        extends Entity
{
    private FileUrl viewUrl;
    private final String name;
    private final EntityID userID;

    private final List<Tag> tags;
    private final List<Label> contentLabels;

    private final boolean isPrivate;
    private final ImageMetadata metadata;

    protected Image(Builder builder)
    {
        super(builder.id);

        this.viewUrl = builder.viewUrl;
        this.name = builder.name;
        this.userID = builder.userID;

        this.isPrivate = builder.isPrivate;
        this.metadata = builder.metadata;

        this.tags = List.copyOf(builder.tags);
        this.contentLabels = List.copyOf(builder.contentLabels);
    }

    public static class Builder
    {
        public EntityID id;
        public String name;
        public EntityID userID;
        public boolean isPrivate;
        public List<Tag> tags;
        public FileUrl viewUrl;
        public ImageMetadata metadata;
        private List<Label> contentLabels;

        public Builder(EntityID id, String name, EntityID userID)
        {
            this.id = id;
            this.name = name;
            this.userID = userID;
            this.tags = Collections.emptyList();
        }

        public Builder withPrivacyStatus(boolean isPrivate)
        {
            this.isPrivate = isPrivate;
            return this;
        }

        public Builder withTags(List<Tag> tags)
        {
            this.tags = tags;
            return this;
        }

        public Builder withContentLabels(List<Label> contentLabels)
        {
            this.contentLabels = contentLabels;
            return this;
        }

        public Builder withViewUrl(FileUrl viewUrl)
        {
            this.viewUrl = viewUrl;
            return this;
        }

        public Builder withMetadata(ImageMetadata metadata)
        {
            this.metadata = metadata;
            return this;
        }

        public Image build()
        {
            return new Image(this);
        }
    }

    public void setViewUrl(FileUrl viewUrl)
    {
        this.viewUrl = viewUrl;
    }

    public String getName()
    {
        return name;
    }

    public EntityID getUserID()
    {
        return userID;
    }

    public List<Tag> getTags()
    {
        return tags;
    }

    public List<Label> getLabels()
    {
        return contentLabels;
    }

    public FileUrl getViewUrl()
    {
        return viewUrl;
    }

    public ImageMetadata getMetadata()
    {
        return metadata;
    }

    public boolean isPrivate()
    {
        return isPrivate;
    }

    public boolean isNull()
    {
        return false;
    }
}
