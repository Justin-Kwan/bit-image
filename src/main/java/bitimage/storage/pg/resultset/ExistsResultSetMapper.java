package bitimage.storage.pg.resultset;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExistsResultSetMapper extends ResultSetMapper<Boolean> {

  public Boolean mapRowToDTO(ResultSet results) throws SQLException {
    boolean doesRowExist = (Boolean) results.getObject("exists");
    return doesRowExist;
  }
}
