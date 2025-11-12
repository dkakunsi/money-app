package io.dkakunsi.money.user.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.common.Context;
import io.dkakunsi.common.Id;
import io.dkakunsi.common.process.ProcessError;
import io.dkakunsi.common.process.ProcessInput;
import io.dkakunsi.money.user.model.User;
import io.dkakunsi.money.user.model.User.Language;
import io.dkakunsi.money.user.port.UserPort;

public final class RegisterUserProcessTest {

  private RegisterUserProcess underTest;

  private UserPort userPort;

  private static final String REQUESTER = "Requester";

  @BeforeEach
  void setUp() {
    userPort = mock(UserPort.class);
    underTest = new RegisterUserProcess(userPort);
  }

  @Test
  public void givenValidRegisterUserRequestWhenUserIsNotExistsTnShouldSuccessAndCreateUser() {
    // Given
    var email = "user@email.com";
    var username = "User Name";
    var phone = "081234567890";
    var photoUrl = "http://photo.url/user";
    var registerInput = RegisterUserInput.builder()
        .email(email)
        .name(username)
        .phone(phone)
        .photoUrl(photoUrl)
        .build();

    var context = new Context(REQUESTER, null);
    var processInput = new ProcessInput<>(registerInput, context);

    when(userPort.findByEmail(email)).thenReturn(Optional.empty());
    when(userPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(processInput);

    // Then
    assertTrue(result.isSuccess());

    var createdUser = result.data().get();
    assertEquals(username, createdUser.getName());
    assertEquals(email, createdUser.getEmail());
    assertEquals(phone, createdUser.getPhone());
    assertEquals(photoUrl, createdUser.getPhotoUrl());
    assertEquals(Language.EN, createdUser.getLanguage());
    assertNotNull(createdUser.getId());

    verify(userPort).findByEmail(email);

    var userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userPort).save(userCaptor.capture());
    var savedUser = userCaptor.getValue();
    assertEquals(username, savedUser.getName());
    assertEquals(email, savedUser.getEmail());
    assertEquals(phone, savedUser.getPhone());
    assertEquals(photoUrl, savedUser.getPhotoUrl());
    assertEquals(Language.EN, savedUser.getLanguage());
    assertNotNull(savedUser.getId());
  }

  @Test
  public void givenValidRegisterUserRequestWhenUserExistsThenShouldSuccessAndUpdateUser() {
    // Given
    var existingId = "existing-id";
    var existingUsername = "User Name";
    var email = "user@email.com";
    var phone = "081234567890";
    var photoUrl = "http://photo.url/user";
    var updatingUserName = "Update User Name";
    var registerInput = RegisterUserInput.builder()
        .email(email)
        .name(updatingUserName)
        .phone(phone)
        .photoUrl(photoUrl)
        .build();

    var context = new Context(REQUESTER, null);
    var processInput = new ProcessInput<>(registerInput, context);

    var existingUser = User.builder()
        .id(Id.of(existingId))
        .email(email)
        .name(existingUsername)
        .language(Language.EN)
        .phone(phone)
        .photoUrl(photoUrl)
        .build();

    when(userPort.findByEmail(email)).thenReturn(Optional.of(existingUser));
    when(userPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(processInput);

    // Then
    assertTrue(result.isSuccess());
    verify(userPort).findByEmail(email);

    var createdUser = result.data().get();
    assertEquals(existingId, createdUser.getId().value());
    assertEquals(updatingUserName, createdUser.getName());
    assertEquals(email, createdUser.getEmail());
    assertEquals(phone, createdUser.getPhone());
    assertEquals(photoUrl, createdUser.getPhotoUrl());
    assertEquals(Language.EN, createdUser.getLanguage());

    var userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userPort).save(userCaptor.capture());
    var savedUser = userCaptor.getValue();
    assertEquals(existingId, savedUser.getId().value());
    assertEquals(updatingUserName, savedUser.getName());
    assertEquals(email, savedUser.getEmail());
    assertEquals(phone, savedUser.getPhone());
    assertEquals(photoUrl, savedUser.getPhotoUrl());
    assertEquals(Language.EN, savedUser.getLanguage());
  }

  @Test
  public void givenValidRegisterUserRequestWhenUserExistsWithNoChangesThenShouldSuccessAndNotSave() {
    // Given
    var existingId = "existing-id";
    var username = "User Name";
    var email = "user@email.com";
    var phone = "081234567890";
    var photoUrl = "http://photo.url/user";
    var registerInput = RegisterUserInput.builder()
        .email(email)
        .name(username)
        .phone(phone)
        .photoUrl(photoUrl)
        .build();

    var context = new Context(REQUESTER, null);
    var processInput = new ProcessInput<>(registerInput, context);

    var existingUser = User.builder()
        .id(Id.of(existingId))
        .email(email)
        .name(username)
        .language(Language.EN)
        .phone(phone)
        .photoUrl(photoUrl)
        .build();

    when(userPort.findByEmail(email)).thenReturn(Optional.of(existingUser));
    when(userPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(processInput);

    // Then
    assertTrue(result.isSuccess());
    verify(userPort).findByEmail(email);

    var createdUser = result.data().get();
    assertEquals(existingId, createdUser.getId().value());
    assertEquals(username, createdUser.getName());
    assertEquals(email, createdUser.getEmail());
    assertEquals(phone, createdUser.getPhone());
    assertEquals(photoUrl, createdUser.getPhotoUrl());
    assertEquals(Language.EN, createdUser.getLanguage());

    verify(userPort, never()).save(any(User.class));
  }

  @Test
  public void givenValidRegisterUserRequestWhenPortThrowsErrorThenReturnFailure() {
    // Given
    when(userPort.save(any())).thenThrow(new RuntimeException("An error occured"));

    var registerInput = RegisterUserInput.builder().build();
    var context = new Context(REQUESTER, null);
    var processInput = new ProcessInput<>(registerInput, context);

    // When
    var result = underTest.process(processInput);

    // Then
    assertFalse(result.isSuccess());
    assertEquals(ProcessError.Code.SERVER_ERROR, result.error().get().code());
    assertEquals("An error occured", result.error().get().message());
  }
}
