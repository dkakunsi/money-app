package io.dkakunsi.lab.common;

import java.util.Map;
import java.util.Optional;

public final class EnvironmentConfiguration implements Configuration {

  @Override
  public Map<String, String> all() {
    return System.getenv();
  }

  @Override
  public Optional<String> get(String key) {
    return Optional.ofNullable(System.getenv(key));
  }

  public static EnvironmentConfiguration of() {
    return new EnvironmentConfiguration();
  }
}
