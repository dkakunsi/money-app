package io.dkakunsi.lab.money;

import java.util.function.Function;

import io.dkakunsi.lab.JSONSchema;
import io.dkakunsi.lab.common.EnvironmentConfiguration;
import io.dkakunsi.lab.common.web.Endpoint.Method;
import io.dkakunsi.lab.javalin.JavalinEndpoint;
import io.dkakunsi.lab.javalin.JavalinServer;
import io.dkakunsi.lab.money.web.parser.UserRegistrationParser;
import io.dkakunsi.lab.money.web.parser.UserRetrievalParser;
import io.dkakunsi.lab.postgres.PostgresConfig;
import io.dkakunsi.lab.postgres.PostgresDatabase;
import io.dkakunsi.money.user.adapter.postgres.PostgresUserAdapter;
import io.dkakunsi.money.user.adapter.postgres.UserEntity;
import io.dkakunsi.money.user.model.User;
import io.dkakunsi.money.user.process.UserRegistrationInput;
import io.dkakunsi.money.user.process.UserRegistrationProcess;
import io.dkakunsi.money.user.process.UserRetrievalInput;
import io.dkakunsi.money.user.process.UserRetrievalProcess;

public final class Launcher {

  private static final String APP_PORT = "app.port";

  private JavalinServer server;

  public void launch(Function<String, String> envProvider) {
    var configuration = EnvironmentConfiguration.of(envProvider);
    var postgresConfig = new PostgresConfig(configuration);
    var schemas = JSONSchema.buildSchemas(configuration);

    // Database
    var userSchema = schemas.get("user");
    var postgresUserDb = PostgresDatabase.<UserEntity>builder()
        .config(postgresConfig)
        .schema(userSchema)
        .resultParser(UserEntity::from)
        .build();
    postgresUserDb.initTable();

    var userAdapter = new PostgresUserAdapter(postgresUserDb);

    // endpoints
    var appPort = configuration.get(APP_PORT).orElse("8080");
    server = JavalinServer.of(Integer.parseInt(appPort))
        .addEndpoint(registerUserEndpoint(userAdapter))
        .addEndpoint(userRetrievalEndpoint(userAdapter))
        .start();
  }

  private JavalinEndpoint<UserRegistrationInput, User> registerUserEndpoint(PostgresUserAdapter userAdapter) {
    var registerUserProcess = new UserRegistrationProcess(userAdapter);
    return JavalinEndpoint.<UserRegistrationInput, User>builder()
        .process(registerUserProcess)
        .path("/users")
        .method(Method.POST)
        .requestParser(UserRegistrationParser::parseRequest)
        .responseParser(UserRegistrationParser::parseResponse)
        .build();
  }

  private JavalinEndpoint<UserRetrievalInput, User> userRetrievalEndpoint(PostgresUserAdapter userAdapter) {
    var retrieveUserProcess = new UserRetrievalProcess(userAdapter);
    return JavalinEndpoint.<UserRetrievalInput, User>builder()
        .process(retrieveUserProcess)
        .path("/user/{email}")
        .method(Method.GET)
        .requestParser(UserRetrievalParser::parseRequest)
        .responseParser(UserRetrievalParser::parseResponse)
        .build();
  }

  public void stop() {
    if (server != null) {
      server.stop();
    }
  }
}
