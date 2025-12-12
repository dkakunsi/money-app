package io.dkakunsi.lab.common;

import java.util.Optional;

public interface Configuration {

  Optional<String> get(String key);

}
