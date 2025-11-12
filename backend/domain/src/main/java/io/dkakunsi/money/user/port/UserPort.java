package io.dkakunsi.money.user.port;

import java.util.Optional;

import io.dkakunsi.money.user.model.User;

public interface UserPort {
  User save(User user);

  Optional<User> findByEmail(String email);
}
