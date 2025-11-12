package io.dkakunsi.common.web;

import io.dkakunsi.common.security.AuthorizedPrincipal;
import io.dkakunsi.common.security.Authorizer;
import lombok.Getter;

public abstract class Endpoint<S, T> {

  protected static final int SUCCESS_RC = 200;

  protected static final String APPLICATION_JSON = "application/json";

  public static enum Method {
    POST, PUT, PATCH, GET, DELETE, OPTIONS
  }

  @Getter
  public static enum Header {
    AUTH("Authorization"),
    REQUEST_ID("Request-Id");

    private String name;

    private Header(String name) {
      this.name = name;
    }
  }

  protected io.dkakunsi.common.process.Process<S, T> process;

  protected Method method;

  protected String path;

  protected Authorizer authorizer;

  protected RequestParser<S> requestParser;

  protected ResponseParser<T> responseParser;

  protected Endpoint(io.dkakunsi.common.process.Process<S, T> process, Method method, String path,
      RequestParser<S> requestParser, ResponseParser<T> responseParser, Authorizer authorizer) {
    this.process = process;
    this.path = path;
    this.method = method;
    this.requestParser = requestParser;
    this.responseParser = responseParser;
    this.authorizer = authorizer;
  }

  public Method getMethod() {
    return method;
  }

  public String getPath() {
    return path;
  }

  protected boolean isPreflightRequest(String method) {
    return Method.OPTIONS.name().equalsIgnoreCase(method);
  }

  protected AuthorizedPrincipal authorizeRequest(String sessionKey) {
    if (authorizer == null) {
      throw new RuntimeException("Authentication provider is not configured");
    }
    return authorizer.verify(sessionKey);
  }
}
