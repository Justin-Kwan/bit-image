package bitimage.storage.postgres.connection;

public interface PostgresEnv
{
    String getPostgresUsername();

    String getPostgresPassword();

    String getPostgresHostPort();

    int getPostgresPoolSize();
}
