package bitimage.domain.uploading.entities;

import bitimage.domain.common.entities.Entity;
import bitimage.domain.common.entities.EntityID;

public class Tag extends Entity {

  private final String name;

  private Tag(EntityID id, String name) {
    super(id);

    this.name = name;
  }

  public static Tag CreateNew(String name) {
    final var id = EntityID.CreateNew();
    final var tag = new Tag(id, name);

    return tag;
  }

  public static Tag CreateExisting(EntityID id, String name) {
    final var tag = new Tag(id, name);

    return tag;
  }

  public String getName() {
    return this.name;
  }
}
