package io.dkakunsi.lab.common.security;

public interface Authorizer {
  AuthorizedPrincipal verify(String token);
}
