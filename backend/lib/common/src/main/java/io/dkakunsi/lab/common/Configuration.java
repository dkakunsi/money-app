package io.dkakunsi.lab.common;

import java.util.Map;
import java.util.Optional;

public interface Configuration {

  Map<String, String> all();

  Optional<String> get(String key);

}
