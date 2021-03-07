package bitimage.environment;

import java.io.InputStream;
import java.util.Properties;

public class EnvReader {

  private InputStream inputStream;
  private static final String ENV_FILE_NAME = "application.prod.properties";

  public GlobalEnv read() {
    final var props = new Properties();

    try {
      this.inputStream = getClass().getClassLoader().getResourceAsStream(ENV_FILE_NAME);

      props.load(this.inputStream);
    } catch (Exception e) {
      System.out.println("Error reading property env file '%s'".formatted(ENV_FILE_NAME));
    }

    props.list(System.out);
    return this.mapToGlobalEnv(props);
  }

  public GlobalEnv mapToGlobalEnv(Properties props) {
    var env = new GlobalEnv();

    env.postgresUsername = props.getProperty("postgres.username");
    env.postgresPassword = props.getProperty("postgres.password");
    env.postgresHostPort = props.getProperty("postgres.hostport");
    env.postgresPoolSize = Integer.parseInt(props.getProperty("postgres.poolsize"));

    env.awsAccessKey = props.getProperty("aws.access.key");
    env.awsAccessID = props.getProperty("aws.access.id");
    env.awsRegion = props.getProperty("aws.region");
    env.awsObjectKeyPrefix = props.getProperty("aws.objectkeyprefix");

    env.beanstalkHost = props.getProperty("beanstalk.host");
    env.beanstalkQueueName = props.getProperty("beanstalk.queue.name");
    env.beanstalkPort = Integer.parseInt(props.getProperty("beanstalk.port"));
    env.beanstalkReadTimeout = Integer.parseInt(props.getProperty("beanstalk.timeout.read"));
    env.beanstalkConnectTimeout = Integer.parseInt(props.getProperty("beanstalk.timeout.connect"));

    env.remoteTokenCheckerHostPort = props.getProperty("cas.hostport");
    env.remoteTokenCheckerRequestMediaType = props.getProperty("cas.tokenrequest.mediatype");

    return env;
  }
}
