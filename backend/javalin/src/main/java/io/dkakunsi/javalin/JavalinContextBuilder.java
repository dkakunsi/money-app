package io.dkakunsi.javalin;

import io.dkakunsi.common.Endpoint.Header;
import io.dkakunsi.common.security.AuthorizedPrincipal;
import lombok.Builder;

@Builder
public final class JavalinContextBuilder {

  private io.javalin.http.Context context;
  private AuthorizedPrincipal requester;

  private static String fromHeader(io.javalin.http.Context context, Header headerKey) {
    var headerValue = context.header(headerKey.getName());
    return headerValue != null ? headerValue : "";
  }

  public io.dkakunsi.common.Context build() {
    return io.dkakunsi.common.Context.builder()
        .requester(requester != null ? requester.email() : null)
        .requestId(fromHeader(context, Header.REQUEST_ID))
        .authorizationToken(fromHeader(context, Header.AUTH))
        .build();
  }
}
