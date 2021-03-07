package bitimage.storage.postgres.connection;

public interface IPostgresEnv {
  public String getPostgresUsername();

  public String getPostgresPassword();

  public String getPostgresHostPort();

  public int getPostgresPoolSize();
}
