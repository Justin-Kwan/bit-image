package bitimage.environment;

import bitimage.messaging.beanstalk.BeanstalkEnv;
import bitimage.storage.postgres.connection.PostgresEnv;
import bitimage.storage.s3.AwsEnv;
import bitimage.transport.middleware.TokenCheckerEnv;

/**
 * Global environment class providing configurations to
 * several modules.
 */
public class GlobalEnv
        implements PostgresEnv, BeanstalkEnv, AwsEnv, TokenCheckerEnv
{
    public String postgresUsername;
    public String postgresPassword;
    public String postgresHostPort;
    public int postgresPoolSize;

    public String awsAccessID;
    public String awsAccessKey;
    public String awsRegion;
    public String awsObjectKeyPrefix;

    public String beanstalkHost;
    public String beanstalkQueueName;
    public int beanstalkPort;
    public int beanstalkReadTimeout;
    public int beanstalkConnectTimeout;

    public String remoteTokenCheckerHostPort;
    public String remoteTokenCheckerRequestMediaType;

    /**
     * components can get injected Postgres configurations.
     */
    public String getPostgresUsername()
    {
        return postgresUsername;
    }

    public String getPostgresPassword()
    {
        return postgresPassword;
    }

    public String getPostgresHostPort()
    {
        return postgresHostPort;
    }

    public int getPostgresPoolSize()
    {
        return postgresPoolSize;
    }

    /**
     * components can get injected Beanstalk configurations.
     */
    public String getBeanstalkHost()
    {
        return beanstalkHost;
    }

    public String getBeanstalkQueueName()
    {
        return beanstalkQueueName;
    }

    public int getBeanstalkPort()
    {
        return beanstalkPort;
    }

    public int getBeanstalkReadTimeout()
    {
        return beanstalkReadTimeout;
    }

    public int getBeanstalkConnectTimeout()
    {
        return beanstalkConnectTimeout;
    }

    /**
     * components can get injected AWS configurations.
     */
    public String getAwsAccessID()
    {
        return awsAccessID;
    }

    public String getAwsAccessKey()
    {
        return awsAccessKey;
    }

    public String getAwsRegion()
    {
        return awsRegion;
    }

    public String getAwsObjectKeyPrefix()
    {
        return awsObjectKeyPrefix;
    }

    /**
     * components can get injected Token checker (CAS) configurations.
     */
    public String getRemoteTokenCheckerHostPort()
    {
        return remoteTokenCheckerHostPort;
    }

    public String getRemoteTokenCheckerRequestMediaType()
    {
        return remoteTokenCheckerRequestMediaType;
    }
}
