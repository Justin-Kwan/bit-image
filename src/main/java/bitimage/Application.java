package bitimage;

import bitimage.wire.Container;
import io.micronaut.runtime.Micronaut;

public class Application {

  /**
   * Application entry point, delegates message reading from queue to worker (thread) pool and spins
   * up web server.
   */
  public static void main(String[] args) {
    final var container = new Container();

    container
        .provideWorkerPool()
        .submit(
            () -> {
              container.provideMessageReader().readMessages();
            });

    Micronaut.build(args).mainClass(Application.class).start();
  }
}
