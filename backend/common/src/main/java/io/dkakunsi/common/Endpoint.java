package io.dkakunsi.common;

import io.dkakunsi.common.security.AuthorizedPrincipal;
import io.dkakunsi.common.security.Authorizer;
import lombok.Getter;

public abstract class Endpoint<S, T> {

  protected static final int SUCCESS_RC = 200;

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

  protected static String APPLICATION_JSON = "application/json";

  protected io.dkakunsi.common.process.Process<S, T> process;

  protected Method method;

  protected String path;

  protected Authorizer authorizer;

  protected Endpoint(io.dkakunsi.common.process.Process<S, T> process, Method method, String path) {
    this.process = process;
    this.path = path;
    this.method = method;
  }

  public Endpoint<S, T> authorizer(Authorizer authorizer) {
    this.authorizer = authorizer;
    return this;
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
