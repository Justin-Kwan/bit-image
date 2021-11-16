package bitimage.storage.postgres.resultset;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public abstract class ResultSetMapper<T>
{
    public List<T> mapToDTOs(ResultSet results)
            throws Exception
    {
        List<T> dtos = new ArrayList<>();

        while (results.next()) {
            T dto = mapRowToDTO(results);
            dtos.add(dto);
        }

        return dtos;
    }

    public abstract T mapRowToDTO(ResultSet results)
            throws Exception;
}
