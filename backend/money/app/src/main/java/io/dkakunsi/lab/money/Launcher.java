package io.dkakunsi.lab.money;

import java.util.function.Function;

import io.dkakunsi.lab.JSONSchema;
import io.dkakunsi.lab.common.EnvironmentConfiguration;
import io.dkakunsi.lab.common.data.Schema;
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

public final class Launcher {

  private static final String APP_PORT = "app.port";

  private JavalinServer server;

  public void launch(Function<String, String> envProvider) {
    var configuration = EnvironmentConfiguration.of(envProvider);
    var postgresConfig = new PostgresConfig(configuration);
    var schemas = JSONSchema.buildSchemas(configuration);

    // User
    var registerUserEndpoint = createRegisterUserEndpoint(postgresConfig, schemas.get("user"));

    // endpoints
    var appPort = configuration.get(APP_PORT).orElse("8080");
    server = JavalinServer.of(Integer.parseInt(appPort))
        .addEndpoint(registerUserEndpoint)
        .start();
  }

  private JavalinEndpoint<RegisterUserInput, User> createRegisterUserEndpoint(
      PostgresConfig postgresConfig,
      Schema schema) {
    var postgresUserDb = PostgresDatabase.<UserEntity>builder()
        .config(postgresConfig)
        .schema(schema)
        .resultParser(UserEntity::from)
        .build();
    postgresUserDb.initTable();
    var userAdapter = new PostgresUserAdapter(postgresUserDb);
    var registerUserProcess = new RegisterUserProcess(userAdapter);
    return JavalinEndpoint.<RegisterUserInput, User>builder()
        .process(registerUserProcess)
        .path("/users")
        .method(Method.POST)
        .requestParser(UserParser::fromRequest)
        .responseParser(UserParser::toResponse)
        .build();
  }

  public void stop() {
    if (server != null) {
      server.stop();
    }
  }
}
