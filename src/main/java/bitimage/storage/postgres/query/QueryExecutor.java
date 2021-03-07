package bitimage.storage.postgres.query;

import bitimage.storage.postgres.connection.ConnectionHandler;
import bitimage.storage.postgres.resultset.ResultSetMapper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class QueryExecutor {

  private final ConnectionHandler connectionHandler;
  private final SQLExceptionTranslator exceptionTranslator;

  public QueryExecutor(
      ConnectionHandler connectionHandler, SQLExceptionTranslator exceptionTranslator) {
    this.connectionHandler = connectionHandler;
    this.exceptionTranslator = exceptionTranslator;
  }

  public <T> List<T> read(SQLQuery query, ResultSetMapper<T> mapper) throws Exception {
    try (Connection conn = this.connectionHandler.getConnection()) {
      final ResultSet results = query.executeRead(conn);
      final List<T> dtos = mapper.mapToDTOs(results);

      return dtos;
    } catch (SQLException e) {
      throw this.exceptionTranslator.translate(e);
    }
  }

  public void write(SQLQuery query) throws Exception {
    try (Connection conn = this.connectionHandler.getConnection()) {
      query.executeWrite(conn);
    } catch (SQLException e) {
      throw this.exceptionTranslator.translate(e);
    }
  }

  public Transaction newTransaction() {
    return new Transaction();
  }

  public void commit(Transaction tx) throws Exception {
    Connection conn = null;

    try {
      conn = this.connectionHandler.getConnection();
      conn.setAutoCommit(false);

      for (SQLQuery query : tx.getQueries()) {
        query.executeWrite(conn);
      }

      conn.commit();
    } catch (SQLException e) {
      conn.rollback();
      throw this.exceptionTranslator.translate(e);
    } finally {
      this.connectionHandler.closeResource(conn);
    }
  }
}
