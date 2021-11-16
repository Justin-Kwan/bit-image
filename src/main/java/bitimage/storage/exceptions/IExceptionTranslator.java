package bitimage.storage.exceptions;

/**
 * Generic interface for exception translators that map infrastructure layer exceptions to higher
 * level adapter exceptions.
 *
 * <p>ex. SQLException -> StorageObjectNotFoundException ex. AwsServiceException ->
 * StorageObjectNotFoundException
 */
public interface IExceptionTranslator<T extends Exception, K extends Exception>
{
    K translate(T e);
}
