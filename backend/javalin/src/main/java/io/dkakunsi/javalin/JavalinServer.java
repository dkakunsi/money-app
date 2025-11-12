package io.dkakunsi.javalin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.dkakunsi.common.DefaultLogger;
import io.dkakunsi.common.Logger;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.ExceptionHandler;

/**
 * Javalin implementation of web service.
 *
 * @author dkakunsi
 */
public final class JavalinServer {

  private static final Logger LOGGER = DefaultLogger.getLogger(JavalinServer.class);

  private Javalin app;

  private int port;

  @SuppressWarnings("rawtypes")
  private List<JavalinEndpoint> endpoints;

  public JavalinServer(int port) {
    this.port = port;
    endpoints = new ArrayList<>();
  }

  public JavalinServer() {
    this(8080);
  }

  @SuppressWarnings("rawtypes")
  public JavalinServer addEndpoint(JavalinEndpoint enpoint) {
    endpoints.add(enpoint);
    return this;
  }

  public void start() {
    app = Javalin.create(getConfigurer()).start(port);

    initEndpoint();
    initExceptionHandling();
  }

  public void stop() {
    app.stop();
  }

  private Consumer<JavalinConfig> getConfigurer() {
    return config -> {
      config.http.maxRequestSize = 1000000;
    };
  }

  private void initEndpoint() {
    endpoints.forEach(e -> {
      app.addHttpHandler(e.getHandlerType(), e.getPath(), e.getHandler());
    });
  }

  private void initExceptionHandling() {
    app.exception(IllegalArgumentException.class, exceptionHandler(400));
    app.exception(RuntimeException.class, exceptionHandler(500));
    app.exception(Exception.class, exceptionHandler(500));
  }

  private static ExceptionHandler<Exception> exceptionHandler(int statusCode) {
    return (ex, ctx) -> {
      LOGGER.error("Cannot process request. Reason: {}", ex, ex.getMessage());
      ctx.status(statusCode).contentType("application/json");
    };
  }
}
