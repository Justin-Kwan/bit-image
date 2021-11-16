package bitimage.storage.postgres.connection;

public interface IPostgresEnv
{
    String getPostgresUsername();

    String getPostgresPassword();

    String getPostgresHostPort();

    int getPostgresPoolSize();
}
