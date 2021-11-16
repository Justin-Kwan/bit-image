package bitimage.wire;

import bitimage.classification.ImageClassifier;
import bitimage.classification.mappers.ImageClassifierMapper;
import bitimage.classification.rekognition.AwsImageClassifier;
import bitimage.analysis.services.ImageAnalysisService;
import bitimage.uploading.events.ImagesUploadedEvent;
import bitimage.uploading.events.UserDeletedEvent;
import bitimage.uploading.services.ImageUploadService;
import bitimage.uploading.services.UserService;
import bitimage.environment.EnvReader;
import bitimage.environment.GlobalEnv;
import bitimage.eventhandling.EventHandler;
import bitimage.eventhandling.ImagesUploadedEventHandler;
import bitimage.eventhandling.UserDeletedEventHandler;
import bitimage.eventhandling.mappers.EventHandlerMapper;
import bitimage.messaging.MessagePublisher;
import bitimage.messaging.MessageReader;
import bitimage.messaging.beanstalk.BeanstalkMessageQueue;
import bitimage.messaging.beanstalk.BeanstalkTubeNames;
import bitimage.storage.ImageStore;
import bitimage.storage.LabelStore;
import bitimage.storage.UserStore;
import bitimage.storage.mappers.ImageStoreMapper;
import bitimage.storage.mappers.LabelStoreMapper;
import bitimage.storage.mappers.UserStoreMapper;
import bitimage.storage.postgres.connection.ConnectionHandler;
import bitimage.storage.postgres.dao.DAOFactory;
import bitimage.storage.postgres.query.QueryExecutor;
import bitimage.storage.postgres.query.SQLExceptionTranslator;
import bitimage.storage.s3.S3ExceptionTranslator;
import bitimage.storage.s3.S3FileSystem;
import bitimage.transport.mappers.ImageControllerMapper;
import bitimage.transport.mappers.UserControllerMapper;
import bitimage.transport.middleware.RemoteTokenChecker;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class responsible for wiring up entire application
 * object dependency graph.
 *
 * TODO: migrate to Dagger injector framework
 */
@Factory
public class Container
{
    private final GlobalEnv env;
    private final Logger logger;
    private final ExecutorService workerPool;
    private final ConnectionHandler sqlConnectionHandler;

    public Container()
    {
        this.env = new EnvReader().read();
        this.logger = Logger.getLogger("Container logger");
        this.workerPool = Executors.newCachedThreadPool();
        this.sqlConnectionHandler = ConnectionHandler.CreateNew(env);

        logger.log(Level.INFO, "Wiring up application components");
    }

    @Singleton
    public ImageControllerMapper provideImageControllerMapper()
    {
        return new ImageControllerMapper();
    }

    @Singleton
    public UserControllerMapper provideUserControllerMapper()
    {
        return new UserControllerMapper();
    }

    @Singleton
    public UserService provideUserService()
    {
        return new UserService(
                provideUserStore(),
                provideMessagePublisher());
    }

    @Singleton
    public ImageUploadService provideImageUploadService()
    {
        return new ImageUploadService(
                provideImageStore(),
                provideUserStore(),
                provideMessagePublisher());
    }

    @Singleton
    public ImageAnalysisService provideImageAnalysisService()
    {
        return new ImageAnalysisService(
                provideLabelStore(),
                provideImageClassifier());
    }

    @Singleton
    public RemoteTokenChecker provideRemoteTokenChecker()
    {
        return new RemoteTokenChecker(env);
    }

    public ImageClassifier provideImageClassifier()
    {
        return new ImageClassifier(
                provideAwsImageClassifier(),
                provideImageClassifierMapper());
    }

    public static ImageClassifierMapper provideImageClassifierMapper()
    {
        return new ImageClassifierMapper();
    }

    public ImageStore provideImageStore()
    {
        return new ImageStore(
                provideDAOFactory(),
                provideS3FileSystem(),
                provideImageStoreMapper());
    }

    public ImageStoreMapper provideImageStoreMapper()
    {
        return new ImageStoreMapper(env.getAwsObjectKeyPrefix());
    }

    public UserStore provideUserStore()
    {
        return new UserStore(
                provideDAOFactory(),
                provideUserStoreMapper());
    }

    public static UserStoreMapper provideUserStoreMapper()
    {
        return new UserStoreMapper();
    }

    public S3FileSystem provideS3FileSystem()
    {
        return S3FileSystem.CreateNew(
                env,
                provideAwsExceptionTranslator());
    }

    public LabelStore provideLabelStore()
    {
        return new LabelStore(
                provideDAOFactory(),
                provideLabelStoreMapper());
    }

    public static LabelStoreMapper provideLabelStoreMapper()
    {
        return new LabelStoreMapper();
    }

    public AwsImageClassifier provideAwsImageClassifier()
    {
        return AwsImageClassifier.CreateNew(
                env,
                provideAwsExceptionTranslator());
    }

    public static S3ExceptionTranslator provideAwsExceptionTranslator()
    {
        return new S3ExceptionTranslator();
    }

    public DAOFactory provideDAOFactory()
    {
        DAOFactory daoFactory = null;

        try {
            daoFactory = DAOFactory.CreateNew(provideSQLQueryExecutor());
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(-1);
        }

        return daoFactory;
    }

    public SQLExceptionTranslator provideSQLExceptionTranslator()
    {
        return new SQLExceptionTranslator();
    }

    public QueryExecutor provideSQLQueryExecutor()
    {
        return new QueryExecutor(
                provideSQLConnectionHandler(),
                provideSQLExceptionTranslator());
    }

    @Singleton
    public ConnectionHandler provideSQLConnectionHandler()
    {
        return sqlConnectionHandler;
    }

    /**
     * Providing event handler components which allow
     * communication between bounded contexts.
     */
    public Map<String, String> provideEventToQueueLookup()
    {
        HashMap<String, String> eventsToQueues = new HashMap<>();

        eventsToQueues.put(
                ImagesUploadedEvent.class.getSimpleName(),
                BeanstalkTubeNames.IMAGES_UPLOADED);
        eventsToQueues.put(
                UserDeletedEvent.class.getSimpleName(),
                BeanstalkTubeNames.USER_DELETED);

        return eventsToQueues;
    }

    public Map<String, EventHandler> provideMessageHandlers()
    {
        HashMap<String, EventHandler> messageHandlers = new HashMap<>();

        messageHandlers.put(
                BeanstalkTubeNames.IMAGES_UPLOADED,
                provideImagesUploadedEventHandler());
        messageHandlers.put(
                BeanstalkTubeNames.USER_DELETED,
                provideUserDeletedEventHandler());

        return messageHandlers;
    }

    public ImagesUploadedEventHandler provideImagesUploadedEventHandler()
    {
        return new ImagesUploadedEventHandler(
                provideEventHandlerMapper(),
                provideImageAnalysisService());
    }

    public UserDeletedEventHandler provideUserDeletedEventHandler()
    {
        return new UserDeletedEventHandler(
                provideEventHandlerMapper(),
                provideImageUploadService());
    }

    public EventHandlerMapper provideEventHandlerMapper()
    {
        return new EventHandlerMapper();
    }

    /**
     * Providing messaging and threading infrastructure *
     */
    public MessageReader provideMessageReader()
    {
        return new MessageReader(
                provideBeanstalkMessageQueue(),
                provideMessageHandlers(),
                provideWorkerPool());
    }

    public MessagePublisher provideMessagePublisher()
    {
        return new MessagePublisher(
                provideBeanstalkMessageQueue(),
                provideEventToQueueLookup());
    }

    public BeanstalkMessageQueue provideBeanstalkMessageQueue()
    {
        return BeanstalkMessageQueue.CreateNew(env);
    }

    public ExecutorService provideWorkerPool()
    {
        return workerPool;
    }
}
