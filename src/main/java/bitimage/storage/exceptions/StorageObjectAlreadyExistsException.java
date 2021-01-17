package bitimage.storage.exceptions;

public class StorageObjectAlreadyExistsException extends RuntimeException {
  public StorageObjectAlreadyExistsException() {
    super("Storage object already exists");
  }
}
