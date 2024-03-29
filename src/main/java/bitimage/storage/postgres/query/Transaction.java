package bitimage.storage.postgres.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates sql transaction queries.
 */
public class Transaction
{
    private final List<SQLQuery> queries;

    public Transaction()
    {
        this.queries = new ArrayList<>();
    }

    public Transaction send(SQLQuery query)
    {
        queries.add(query);
        return this;
    }

    public List<SQLQuery> getQueries()
    {
        return List.copyOf(queries);
    }
}
