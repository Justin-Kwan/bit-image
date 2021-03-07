package bitimage.wire;

import bitimage.classification.ImageClassifier;
import bitimage.classification.mappers.ImageClassifierMapper;
import bitimage.classification.rekognition.AwsImageClassifier;
import bitimage.domain.analysis.services.ImageAnalysisService;
import bitimage.domain.uploading.events.ImagesUploadedEvent;
import bitimage.domain.uploading.events.UserDeletedEvent;
import bitimage.domain.uploading.services.ImageUploadService;
import bitimage.domain.uploading.services.UserService;
import bitimage.environment.EnvReader;
import bitimage.environment.GlobalEnv;
import bitimage.eventhandling.IEventHandler;
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
import bitimage.storage.postgres.dao.DAOFactory;
import bitimage.storage.postgres.connection.ConnectionHandler;
import bitimage.storage.postgres.query.QueryExecutor;
import bitimage.storage.postgres.query.SQLExceptionTranslator;
import bitimage.storage.s3.S3ExceptionTranslator;
import bitimage.storage.s3.S3FileSystem;
import bitimage.transport.mappers.ImageControllerMapper;
import bitimage.transport.mappers.UserControllerMapper;
import bitimage.transport.middleware.RemoteTokenChecker;
import io.micronaut.context.annotation.Factory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Singleton;

/** Class responsible for wiring up entire application object dependency graph. */
@Factory
public class Container {

  private final GlobalEnv env;
  private final Logger logger;
  private final ExecutorService workerPool;
  private final ConnectionHandler sqlConnectionHandler;

  public Container() {
    this.env = new EnvReader().read();
    this.logger = Logger.getLogger("Container logger");
    this.workerPool = Executors.newCachedThreadPool();
    this.sqlConnectionHandler = ConnectionHandler.CreateNew(this.env);

    this.logger.log(Level.INFO, "Wiring up application components");
  }

  @Singleton
  public ImageControllerMapper provideImageControllerMapper() {
    return new ImageControllerMapper();
  }

  @Singleton
  public UserControllerMapper provideUserControllerMapper() {
    return new UserControllerMapper();
  }

  @Singleton
  public UserService provideUserService() {
    return new UserService(this.provideUserStore(), this.provideMessagePublisher());
  }

  @Singleton
  public ImageUploadService provideImageUploadService() {
    return new ImageUploadService(
        this.provideImageStore(), this.provideUserStore(), this.provideMessagePublisher());
  }

  @Singleton
  public ImageAnalysisService provideImageAnalysisService() {
    return new ImageAnalysisService(this.provideLabelStore(), this.provideImageClassifier());
  }

  @Singleton
  public RemoteTokenChecker provideRemoteTokenChecker() {
    return new RemoteTokenChecker(this.env);
  }

  public ImageClassifier provideImageClassifier() {
    return new ImageClassifier(
        this.provideAwsImageClassifier(), this.provideImageClassifierMapper());
  }

  public ImageClassifierMapper provideImageClassifierMapper() {
    return new ImageClassifierMapper();
  }

  public ImageStore provideImageStore() {
    return new ImageStore(
        this.provideDAOFactory(), this.provideS3FileSystem(), this.provideImageStoreMapper());
  }

  public ImageStoreMapper provideImageStoreMapper() {
    return new ImageStoreMapper(this.env.getAwsObjectKeyPrefix());
  }

  public UserStore provideUserStore() {
    return new UserStore(this.provideDAOFactory(), this.provideUserStoreMapper());
  }

  public UserStoreMapper provideUserStoreMapper() {
    return new UserStoreMapper();
  }

  public S3FileSystem provideS3FileSystem() {
    return S3FileSystem.CreateNew(this.env, this.provideAwsExceptionTranslator());
  }

  public LabelStore provideLabelStore() {
    return new LabelStore(this.provideDAOFactory(), this.provideLabelStoreMapper());
  }

  public LabelStoreMapper provideLabelStoreMapper() {
    return new LabelStoreMapper();
  }

  public AwsImageClassifier provideAwsImageClassifier() {
    return AwsImageClassifier.CreateNew(this.env, this.provideAwsExceptionTranslator());
  }

  public S3ExceptionTranslator provideAwsExceptionTranslator() {
    return new S3ExceptionTranslator();
  }

  public DAOFactory provideDAOFactory() {
    DAOFactory daoFactory = null;

    try {
      daoFactory = DAOFactory.CreateNew(this.provideSQLQueryExecutor());
    } catch (Exception e) {
      this.logger.log(Level.SEVERE, e.getMessage(), e);
      System.exit(-1);
    }

    return daoFactory;
  }

  public SQLExceptionTranslator provideSQLExceptionTranslator() {
    return new SQLExceptionTranslator();
  }

  public QueryExecutor provideSQLQueryExecutor() {
    return new QueryExecutor(
        this.provideSQLConnectionHandler(), this.provideSQLExceptionTranslator());
  }

  @Singleton
  public ConnectionHandler provideSQLConnectionHandler() {
    return this.sqlConnectionHandler;
  }

  /** Providing event handler components which allow communication between bounded contexts * */
  public Map<String, String> provideEventToQueueLookup() {
    final var eventsToQueues = new HashMap<String, String>();

    eventsToQueues.put(
        ImagesUploadedEvent.class.getSimpleName(), BeanstalkTubeNames.IMAGES_UPLOADED);
    eventsToQueues.put(UserDeletedEvent.class.getSimpleName(), BeanstalkTubeNames.USER_DELETED);

    return eventsToQueues;
  }

  public Map<String, IEventHandler> provideMessageHandlers() {
    final var messageHandlers = new HashMap<String, IEventHandler>();

    messageHandlers.put(
        BeanstalkTubeNames.IMAGES_UPLOADED, this.provideImagesUploadedEventHandler());
    messageHandlers.put(BeanstalkTubeNames.USER_DELETED, this.provideUserDeletedEventHandler());

    return messageHandlers;
  }

  public ImagesUploadedEventHandler provideImagesUploadedEventHandler() {
    return new ImagesUploadedEventHandler(
        this.provideEventHandlerMapper(), this.provideImageAnalysisService());
  }

  public UserDeletedEventHandler provideUserDeletedEventHandler() {
    return new UserDeletedEventHandler(
        this.provideEventHandlerMapper(), this.provideImageUploadService());
  }

  public EventHandlerMapper provideEventHandlerMapper() {
    return new EventHandlerMapper();
  }

  /** Providing messaging and threading infrastructure * */
  public MessageReader provideMessageReader() {
    return new MessageReader(
        this.provideBeanstalkMessageQueue(),
        this.provideMessageHandlers(),
        this.provideWorkerPool());
  }

  public MessagePublisher provideMessagePublisher() {
    return new MessagePublisher(
        this.provideBeanstalkMessageQueue(), this.provideEventToQueueLookup());
  }

  public BeanstalkMessageQueue provideBeanstalkMessageQueue() {
    return BeanstalkMessageQueue.CreateNew(this.env);
  }

  public ExecutorService provideWorkerPool() {
    return this.workerPool;
  }
}
