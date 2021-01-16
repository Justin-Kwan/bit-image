package bitimage.env;

import bitimage.messaging.beanstalk.IBeanstalkEnv;
import bitimage.storage.aws.IAwsEnv;
import bitimage.storage.pg.connection.IPostgresEnv;
import bitimage.transport.middleware.ITokenCheckerEnv;

public class GlobalEnv implements IPostgresEnv, IBeanstalkEnv, IAwsEnv, ITokenCheckerEnv {

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
  public String remoteTokenCheckerRequestFormat;
  public String remoteTokenCheckerRequestMediaType;

  /** components can get injected Postgres configurations * */
  public String getPostgresUsername() {
    return this.postgresUsername;
  }

  public String getPostgresPassword() {
    return this.postgresPassword;
  }

  public String getPostgresHostPort() {
    return this.postgresHostPort;
  }

  public int getPostgresPoolSize() {
    return this.postgresPoolSize;
  }

  /** components can get injected Beanstalk configurations * */
  public String getBeanstalkHost() {
    return this.beanstalkHost;
  }

  public String getBeanstalkQueueName() {
    return this.beanstalkQueueName;
  }

  public int getBeanstalkPort() {
    return this.beanstalkPort;
  }

  public int getBeanstalkReadTimeout() {
    return this.beanstalkReadTimeout;
  }

  public int getBeanstalkConnectTimeout() {
    return this.beanstalkConnectTimeout;
  }

  /** components can get injected AWS configurations * */
  public String getAwsAccessID() {
    return this.awsAccessID;
  }

  public String getAwsAccessKey() {
    return this.awsAccessKey;
  }

  public String getAwsRegion() {
    return this.awsRegion;
  }

  public String getAwsObjectKeyPrefix() {
    return this.awsObjectKeyPrefix;
  }

  /** components can get injected Token cheker (CAS) configurations * */
  public String getRemoteTokenCheckerHostPort() {
    return this.remoteTokenCheckerHostPort;
  }

  public String getRemoteTokenCheckerRequestBody() {
    return this.remoteTokenCheckerRequestFormat;
  }

  public String getRemoteTokenCheckerRequestMediaType() {
    return this.remoteTokenCheckerRequestMediaType;
  }
}
