package io.dkakunsi.lab.money;

import java.util.UUID;

import io.dkakunsi.lab.JSONSchema;
import io.dkakunsi.lab.common.Context;
import io.dkakunsi.lab.common.DefaultLogger;
import io.dkakunsi.lab.common.EnvironmentConfiguration;
import io.dkakunsi.lab.common.Logger;
import io.dkakunsi.lab.common.web.Endpoint.Method;
import io.dkakunsi.lab.javalin.JavalinEndpoint;
import io.dkakunsi.lab.javalin.JavalinServer;
import io.dkakunsi.lab.money.parser.UserParser;
import io.dkakunsi.lab.postgres.PostgresConfig;
import io.dkakunsi.lab.postgres.PostgresDatabase;
import io.dkakunsi.money.user.adapter.postgres.PostgresUserAdapter;
import io.dkakunsi.money.user.adapter.postgres.UserEntity;
import io.dkakunsi.money.user.model.User;
import io.dkakunsi.money.user.process.RegisterUserInput;
import io.dkakunsi.money.user.process.RegisterUserProcess;

public class Main {

  private static final Logger LOGGER = DefaultLogger.getLogger(Main.class);

  public static void main(String[] args) {
    Context.set(Context.builder().requestId(UUID.randomUUID().toString()).requester("SYSTEM").build());
    LOGGER.info("Starting services!");
    try {
      new Main().start();
      LOGGER.info("Service is started!");
    } catch (Exception ex) {
      LOGGER.error("Cannot start the application", ex);
      System.exit(1);
    }
  }

  public void start() {
    var configuration = new EnvironmentConfiguration();
    var postgresConfig = new PostgresConfig(configuration);
    var schemas = JSONSchema.buildSchemas(configuration);

    // User
    var postgresUserDb = PostgresDatabase.<UserEntity>builder()
        .config(postgresConfig)
        .schema(schemas.get("user"))
        .entityParser(UserParser::fromEntity)
        .resultParser(UserParser::toEntity)
        .build();
    postgresUserDb.initTable();
    var userAdapter = new PostgresUserAdapter(postgresUserDb);
    var registerUserProcess = new RegisterUserProcess(userAdapter);
    var registerUserEndpoint = JavalinEndpoint.<RegisterUserInput, User>builder()
        .process(registerUserProcess)
        .path("/users")
        .method(Method.POST)
        .requestParser(UserParser::fromRequest)
        .responseParser(UserParser::toResponse)
        .build();

    // endpoints
    new JavalinServer(2000)
        .addEndpoint(registerUserEndpoint)
        .start();
  }
}
