package bitimage.storage.postgres.query;

import bitimage.storage.exceptions.ExceptionTranslator;
import bitimage.storage.exceptions.StorageObjectAlreadyExistsException;
import bitimage.storage.exceptions.StorageObjectNotFoundException;
import bitimage.storage.exceptions.StorageObjectReferenceException;

import java.sql.SQLException;

/**
 * Translates SQL level exceptions into adapter level
 * (higher level of abstraction) exceptions.
 */
public class SQLExceptionTranslator
        implements ExceptionTranslator<SQLException, Exception>
{
    private static final String UNIQUE_VIOLATION = "23505";
    private static final String FOREIGN_KEY_VIOLATION = "23503";
    private static final String NO_ROWS_AFFECTED_ON_DELETE = "P0001";

    public Exception translate(SQLException e)
    {
        String sqlErrorCode = e.getSQLState();

        if (sqlErrorCode.equals(UNIQUE_VIOLATION)) {
            return new StorageObjectAlreadyExistsException();
        }
        if (sqlErrorCode.equals(FOREIGN_KEY_VIOLATION)) {
            return new StorageObjectReferenceException();
        }
        if (sqlErrorCode.equals(NO_ROWS_AFFECTED_ON_DELETE)) {
            return new StorageObjectNotFoundException();
        }

        return e;
    }
}
