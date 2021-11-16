package bitimage.environment;

import java.io.InputStream;
import java.util.Properties;

public class EnvReader
{
    private static final String ENV_FILE_NAME = "application.prod.properties";

    public GlobalEnv read()
    {
        Properties props = new Properties();

        try {
            InputStream inputStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream(ENV_FILE_NAME);

            props.load(inputStream);
        }
        catch (Exception e) {
            System.out.printf("Error reading property env file '%s'%n", ENV_FILE_NAME);
        }

        props.list(System.out);
        return mapToGlobalEnv(props);
    }

    public GlobalEnv mapToGlobalEnv(Properties props)
    {
        GlobalEnv env = new GlobalEnv();

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
