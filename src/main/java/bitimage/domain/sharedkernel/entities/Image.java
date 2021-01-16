package bitimage.domain.sharedkernel.entities;

import bitimage.domain.common.entities.Entity;
import bitimage.domain.common.entities.EntityID;
import bitimage.domain.uploading.entities.FileUrl;
import bitimage.domain.uploading.entities.ImageMetadata;
import bitimage.domain.uploading.entities.Tag;
import java.util.Arrays;
import java.util.List;

public class Image extends Entity {

  private List<Tag> tags;
  private FileUrl viewUrl;
  private final String name;
  private final EntityID userID;
  private final boolean isPrivate;
  private List<Label> contentLabels;
  private final ImageMetadata metadata;

  protected Image(Builder builder) {
    super(builder.id);

    this.name = builder.name;
    this.userID = builder.userID;
    this.isPrivate = builder.isPrivate;
    this.tags = builder.tags;
    this.viewUrl = builder.viewUrl;
    this.metadata = builder.metadata;
    this.contentLabels = builder.contentLabels;
  }

  public static class Builder {

    public EntityID id;
    public String name;
    public EntityID userID;
    public boolean isPrivate;
    public List<Tag> tags;
    public FileUrl viewUrl;
    public ImageMetadata metadata;
    private List<Label> contentLabels;

    public Builder(EntityID id, String name, EntityID userID) {
      this.id = id;
      this.name = name;
      this.userID = userID;
      this.tags = Arrays.asList();
    }

    public Builder withPrivacyStatus(boolean isPrivate) {
      this.isPrivate = isPrivate;
      return this;
    }

    public Builder withTags(List<Tag> tags) {
      this.tags = tags;
      return this;
    }

    public Builder withContentLabels(List<Label> contentLabels) {
      this.contentLabels = contentLabels;
      return this;
    }

    public Builder withViewUrl(FileUrl viewUrl) {
      this.viewUrl = viewUrl;
      return this;
    }

    public Builder withMetadata(ImageMetadata metadata) {
      this.metadata = metadata;
      return this;
    }

    public Image build() {
      return new Image(this);
    }
  }

  public void setViewUrl(FileUrl viewUrl) {
    this.viewUrl = viewUrl;
  }

  public String getName() {
    return this.name;
  }

  public EntityID getUserID() {
    return this.userID;
  }

  public List<Tag> getTags() {
    return this.tags;
  }

  public List<Label> getLabels() {
    return this.contentLabels;
  }

  public FileUrl getViewUrl() {
    return this.viewUrl;
  }

  public ImageMetadata getMetadata() {
    return this.metadata;
  }

  public boolean isPrivate() {
    return this.isPrivate;
  }

  public boolean isNull() {
    return false;
  }
}
