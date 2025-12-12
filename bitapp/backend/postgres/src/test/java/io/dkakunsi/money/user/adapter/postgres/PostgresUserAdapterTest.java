package io.dkakunsi.money.user.adapter.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.lab.common.Id;
import io.dkakunsi.lab.postgres.PostgresDatabase;
import io.dkakunsi.money.user.model.User;

public class PostgresUserAdapterTest {

  private PostgresUserAdapter undetTest;

  private PostgresDatabase<UserEntity> database;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    database = (PostgresDatabase<UserEntity>) mock(PostgresDatabase.class);
    undetTest = new PostgresUserAdapter(database);
  }

  @Test
  public void givenValidUser_whenSave_thenSucceed() {
    // Given
    when(database.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

    // When
    var user = User.builder()
        .id(Id.of("user-123"))
        .name("User Name")
        .language(User.Language.EN)
        .phone("+1234567890")
        .photoUrl("http://example.com/photo.jpg")
        .build();

    var result = undetTest.save(user);

    assertEquals(user.getId().value(), result.getId().value());
    assertEquals(user.getName(), result.getName());
    assertEquals(user.getLanguage(), result.getLanguage());
    assertEquals(user.getPhone(), result.getPhone());
    assertEquals(user.getPhotoUrl(), result.getPhotoUrl());
  }

  @Test
  public void givenEmaiExists_whenFindByEmail_thenReturnUserData() {
    // Given
    var email = "user@example.com";
    var userEntity = UserEntity.builder()
        .id(Id.of(email))
        .name("User Name")
        .language(User.Language.EN)
        .phone("+1234567890")
        .photoUrl("http://example.com/photo.jpg")
        .build();
    when(database.get(Id.of(email))).thenReturn(Optional.of(userEntity));

    // When
    var result = undetTest.findByEmail(email);

    // Then
    assertTrue(result.isPresent());
    var user = result.get();
    assertEquals(userEntity.getId(), user.getId());
    assertEquals(userEntity.getName(), user.getName());
    assertEquals(userEntity.getLanguage(), user.getLanguage());
    assertEquals(userEntity.getPhone(), user.getPhone());
    assertEquals(userEntity.getPhotoUrl(), user.getPhotoUrl());
  }

  @Test
  public void givenEmailNotExists_whenFindByEmail_thenReturnEmptyData() {
    // Given
    var email = "user@example.com";
    when(database.get(Id.of(email))).thenReturn(Optional.empty());

    // When
    var result = undetTest.findByEmail(email);

    // Then
    assertFalse(result.isPresent());
  }

  @Test
  public void givenEmailNotExists_whenFindByEmailReturnNull_thenReturnEmptyData() {
    // Given
    var email = "user@example.com";
    when(database.get(Id.of(email))).thenReturn(Optional.ofNullable(null));

    // When
    var result = undetTest.findByEmail(email);

    // Then
    assertFalse(result.isPresent());
  }
}