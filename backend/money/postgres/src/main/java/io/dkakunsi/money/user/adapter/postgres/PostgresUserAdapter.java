package io.dkakunsi.money.user.adapter.postgres;

import java.util.Optional;

import io.dkakunsi.lab.common.Id;
import io.dkakunsi.lab.postgres.PostgresDatabase;
import io.dkakunsi.money.user.model.User;
import io.dkakunsi.money.user.port.UserPort;

public final class PostgresUserAdapter implements UserPort {

  private PostgresDatabase<UserEntity> database;

  public PostgresUserAdapter(PostgresDatabase<UserEntity> database) {
    this.database = database;
  }

  @Override
  public User save(User user) {
    var entity = UserEntity.builder()
        .id(user.getId())
        .name(user.getName())
        .language(user.getLanguage())
        .phone(user.getPhone())
        .photoUrl(user.getPhotoUrl())
        .build();
    return database.save(entity).toUser();
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return database.get(Id.of(email)).map(UserEntity::toUser);
  }
}
