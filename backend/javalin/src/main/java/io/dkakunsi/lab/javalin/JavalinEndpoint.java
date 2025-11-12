package io.dkakunsi.lab.javalin;

import io.dkakunsi.lab.common.Context;
import io.dkakunsi.lab.common.security.AuthorizedPrincipal;
import io.dkakunsi.lab.common.security.Authorizer;
import io.dkakunsi.lab.common.web.RequestParser;
import io.dkakunsi.lab.common.web.ResponseParser;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import io.javalin.http.UnauthorizedResponse;
import lombok.Builder;

public class JavalinEndpoint<S, T> extends io.dkakunsi.lab.common.web.Endpoint<S, T> {

  @Builder
  public JavalinEndpoint(io.dkakunsi.lab.common.process.Process<S, T> process, Method method, String path,
      RequestParser<S> requestParser, ResponseParser<T> responseParser, Authorizer authorizer) {
    super(process, method, path, requestParser, responseParser, authorizer);
  }

  public HandlerType getHandlerType() {
    switch (method) {
      case POST:
        return HandlerType.POST;
      case PUT:
        return HandlerType.PUT;
      case PATCH:
        return HandlerType.PATCH;
      case GET:
        return HandlerType.GET;
      case DELETE:
        return HandlerType.DELETE;
      default:
        throw new IllegalArgumentException("Not supported method: " + method);
    }
  }

  protected Handler getHandler() {
    return ctx -> {
      var principal = authorizeRequest(ctx);
      var context = initiateContext(ctx, principal);
      var input = requestParser.parse(ctx.body(), ctx.pathParamMap(), context);
      var output = process.process(input);
      if (output.isFailed()) {
        var error = output.error().get();
        ctx.status(error.code().getHttpCode()).result(error.message());
      } else if (output.isEmpty()) {
        ctx.status(SUCCESS_RC);
      } else {
        var response = responseParser.parse(output);
        ctx.status(SUCCESS_RC).result(response);
      }
    };
  }

  protected AuthorizedPrincipal authorizeRequest(io.javalin.http.Context ctx) {
    if (authorizer == null || isPreflightRequest(ctx.method().toString())) {
      // No authentication provider means this endpoint is open to all
      return null;
    }
    var sessionKey = ctx.header(Header.AUTH.getName());
    try {
      return authorizeRequest(sessionKey);
    } catch (IllegalArgumentException e) {
      throw new UnauthorizedResponse();
    }
  }

  private Context initiateContext(io.javalin.http.Context ctx, AuthorizedPrincipal principal) {
    var contextBuilder = JavalinContextBuilder.builder()
        .context(ctx)
        .requester(principal)
        .build();
    var context = contextBuilder.build();
    Context.set(context);
    return context;
  }
}
