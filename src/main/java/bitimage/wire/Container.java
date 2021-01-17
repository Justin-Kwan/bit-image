package bitimage.wire;

import bitimage.deeplearning.adapters.classifiers.ImageClassifier;
import bitimage.deeplearning.adapters.mappers.ImageClassifierMapper;
import bitimage.deeplearning.aws.AwsImageClassifier;
import bitimage.domain.analysis.services.ImageAnalysisService;
import bitimage.domain.uploading.events.ImagesUploadedEvent;
import bitimage.domain.uploading.events.UserDeletedEvent;
import bitimage.domain.uploading.services.ImageUploadService;
import bitimage.domain.uploading.services.UserService;
import bitimage.env.EnvReader;
import bitimage.env.GlobalEnv;
import bitimage.events.IEventHandler;
import bitimage.events.ImagesUploadedEventHandler;
import bitimage.events.UserDeletedEventHandler;
import bitimage.events.mappers.EventHandlerMapper;
import bitimage.messaging.adapters.connectors.MessagePublisher;
import bitimage.messaging.adapters.connectors.MessageReader;
import bitimage.messaging.beanstalk.BeanstalkMessageQueue;
import bitimage.messaging.beanstalk.BeanstalkTubeNames;
import bitimage.storage.adapters.datastores.ImageStore;
import bitimage.storage.adapters.datastores.LabelStore;
import bitimage.storage.adapters.datastores.UserStore;
import bitimage.storage.adapters.mappers.ImageStoreMapper;
import bitimage.storage.adapters.mappers.LabelStoreMapper;
import bitimage.storage.adapters.mappers.UserStoreMapper;
import bitimage.storage.aws.AwsExceptionTranslator;
import bitimage.storage.aws.S3FileSystem;
import bitimage.storage.dao.DAOFactory;
import bitimage.storage.pg.connection.ConnectionHandler;
import bitimage.storage.pg.query.QueryExecutor;
import bitimage.storage.pg.query.SQLExceptionTranslator;
import bitimage.transport.adapters.mappers.ImageControllerMapper;
import bitimage.transport.adapters.mappers.UserControllerMapper;
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

  public Container() {
    this.env = new EnvReader().read();
    this.workerPool = Executors.newCachedThreadPool();
    this.logger = Logger.getLogger("Container logger");

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

  public AwsExceptionTranslator provideAwsExceptionTranslator() {
    return new AwsExceptionTranslator();
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
    return ConnectionHandler.CreateNew(this.env);
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
