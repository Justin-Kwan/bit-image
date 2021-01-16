package bitimage.storage.exceptions;

public class StorageObjectReferenceException extends RuntimeException {

  public StorageObjectReferenceException() {
    super("Storage object illegaly references another storage object");
  }
}
