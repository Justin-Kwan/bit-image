package bitimage.domain.uploading.entities;

import bitimage.domain.common.entities.Entity;
import bitimage.domain.common.entities.EntityID;

public class User extends Entity {

  private int imageUploadCount;
  private int imageUploadLimit;

  private User(EntityID id, int imageUploadCount, int imageUploadLimit) {
    super(id);

    this.imageUploadCount = imageUploadCount;
    this.imageUploadLimit = imageUploadLimit;
  }

  public static User CreateNew(EntityID id) {
    final User user = new User(id, 0, 10000);

    return user;
  }

  public int getImageUploadCount() {
    return this.imageUploadCount;
  }

  public int getImageUploadLimit() {
    return this.imageUploadLimit;
  }

  public boolean isNull() {
    return false;
  }
}
