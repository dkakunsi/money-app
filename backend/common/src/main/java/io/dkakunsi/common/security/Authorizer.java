package io.dkakunsi.common.security;

public interface Authorizer {
  AuthorizedPrincipal verify(String token);
}
