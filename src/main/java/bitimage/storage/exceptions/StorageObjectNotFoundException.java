package bitimage.storage.exceptions;

public class StorageObjectNotFoundException extends RuntimeException {
  public StorageObjectNotFoundException() {
    super("Storage object not found");
  }
}
