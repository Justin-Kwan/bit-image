package bitimage.storage.pg.query;

import java.util.ArrayList;
import java.util.List;

/** Encapsulates sql transaction queries. */
public class Transaction {

  private List<SQLQuery> queries;

  public Transaction() {
    this.queries = new ArrayList<>();
  }

  public Transaction send(SQLQuery query) {
    this.queries.add(query);
    return this;
  }

  public List<SQLQuery> getQueries() {
    return this.queries;
  }
}
