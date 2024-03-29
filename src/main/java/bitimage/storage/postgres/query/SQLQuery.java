package bitimage.storage.postgres.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class SQLQuery
{
    private final String sql;
    private final List<Object> params;

    /**
     * Constructor to instantiate sql queries containing
     * prepared statements (sql query string with parameters
     * to inject).
     */
    public SQLQuery(String sql, List<Object> params)
    {
        this.sql = sql;
        this.params = List.copyOf(params);
    }

    /**
     * Constructor to instantiate sql queries without parameters
     */
    public SQLQuery(String sql)
    {
        this.sql = sql;
        this.params = Collections.emptyList();
    }

    public String getSQL()
    {
        return sql;
    }

    public List<Object> getParams()
    {
        return params;
    }

    /**
     * @precondition Connection object must be opened.
     * @postcondition Connection object must be closed after.
     */
    public void executeWrite(Connection conn)
            throws SQLException
    {
        PreparedStatement statement = conn.prepareStatement(sql);
        injectParamsIntoStatement(params, statement);

        statement.execute();
    }

    /**
     * @precondition Connection object must be opened.
     * @postcondition Connection object must be closed after.
     */
    public ResultSet executeRead(Connection conn)
            throws SQLException
    {
        PreparedStatement statement = conn.prepareStatement(sql);
        injectParamsIntoStatement(params, statement);

        return statement.executeQuery();
    }

    /**
     * Injects query parameters into correct position within sql prepared statement.
     */
    private void injectParamsIntoStatement(List<Object> params, PreparedStatement statement)
            throws SQLException
    {
        int injectedParamCount = 0;

        if (params == null) {
            return;
        }

        for (Object param : params) {
            statement.setObject(++injectedParamCount, param);
        }
    }
}
