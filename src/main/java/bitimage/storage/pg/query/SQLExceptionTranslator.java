package bitimage.storage.pg.query;

import bitimage.storage.exceptions.IExceptionTranslator;
import bitimage.storage.exceptions.StorageObjectAlreadyExistsException;
import bitimage.storage.exceptions.StorageObjectNotFoundException;
import bitimage.storage.exceptions.StorageObjectReferenceException;
import java.sql.SQLException;

/** Translates SQL level exceptions into adapter level (higher level of abstraction) exceptions. */
public class SQLExceptionTranslator implements IExceptionTranslator<SQLException, Exception> {

  private final String UNIQUE_VIOLATION = "23505";
  private final String FOREIGN_KEY_VIOLATION = "23503";
  private final String NO_ROWS_AFFECTED_ON_DELETE = "P0001";

  public Exception translate(SQLException e) {
    final String sqlErrorCode = e.getSQLState();

    if (sqlErrorCode.equals(this.UNIQUE_VIOLATION)) {
      return new StorageObjectAlreadyExistsException();
    }
    if (sqlErrorCode.equals(this.FOREIGN_KEY_VIOLATION)) {
      return new StorageObjectReferenceException();
    }
    if (sqlErrorCode.equals(this.NO_ROWS_AFFECTED_ON_DELETE)) {
      return new StorageObjectNotFoundException();
    }

    return e;
  }
}
