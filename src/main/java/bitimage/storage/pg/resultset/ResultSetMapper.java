package bitimage.storage.pg.resultset;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public abstract class ResultSetMapper<T> {

  public List<T> mapToDTOs(ResultSet results) throws Exception {
    final var dtos = new ArrayList<T>();

    while (results.next()) {
      final T dto = this.mapRowToDTO(results);
      dtos.add(dto);
    }

    return dtos;
  }

  public abstract T mapRowToDTO(ResultSet results) throws Exception;
}
