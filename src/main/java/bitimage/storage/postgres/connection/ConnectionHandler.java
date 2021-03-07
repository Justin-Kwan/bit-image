package bitimage.storage.postgres.connection;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp.BasicDataSource;

public class ConnectionHandler {

  private final BasicDataSource connectionPool;

  private ConnectionHandler(BasicDataSource connectionPool) {
    this.connectionPool = connectionPool;
  }

  public static ConnectionHandler CreateNew(IPostgresEnv env) {
    final var connectionPool = new BasicDataSource();

    connectionPool.setUsername(env.getPostgresUsername());
    connectionPool.setPassword(env.getPostgresPassword());
    connectionPool.setUrl(env.getPostgresHostPort());
    connectionPool.setInitialSize(env.getPostgresPoolSize());

    return new ConnectionHandler(connectionPool);
  }

  public Connection getConnection() throws SQLException {
    return this.connectionPool.getConnection();
  }

  public void closeResource(AutoCloseable resource) throws Exception {
    if (resource != null) {
      resource.close();
    }
  }
}
