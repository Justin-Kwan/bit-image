package bitimage.storage.s3;

import bitimage.storage.exceptions.IExceptionTranslator;
import bitimage.storage.exceptions.StorageObjectNotFoundException;
import com.amazonaws.AmazonServiceException;

public class S3ExceptionTranslator
    implements IExceptionTranslator<AmazonServiceException, RuntimeException> {

  public final int BUCKET_OR_OBJECT_NOT_FOUND = 404;

  public RuntimeException translate(AmazonServiceException e) {
    final int awsErrorCode = e.getStatusCode();

    if (awsErrorCode == this.BUCKET_OR_OBJECT_NOT_FOUND) {
      return new StorageObjectNotFoundException();
    }

    return e;
  }
}
