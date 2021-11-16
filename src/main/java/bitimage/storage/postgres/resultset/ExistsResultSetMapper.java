package bitimage.storage.postgres.resultset;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExistsResultSetMapper
        extends ResultSetMapper<Boolean>
{
    public Boolean mapRowToDTO(ResultSet results)
            throws SQLException
    {
        // does row exist
        return (Boolean) results.getObject("exists");
    }
}
