package bitimage.storage.s3;

import bitimage.storage.exceptions.IExceptionTranslator;
import bitimage.storage.exceptions.StorageObjectNotFoundException;
import com.amazonaws.AmazonServiceException;

public class S3ExceptionTranslator
        implements IExceptionTranslator<AmazonServiceException, RuntimeException>
{
    private static final int BUCKET_OR_OBJECT_NOT_FOUND = 404;

    public RuntimeException translate(AmazonServiceException e)
    {
        int awsErrorCode = e.getStatusCode();

        if (awsErrorCode == BUCKET_OR_OBJECT_NOT_FOUND) {
            return new StorageObjectNotFoundException();
        }

        return e;
    }
}
