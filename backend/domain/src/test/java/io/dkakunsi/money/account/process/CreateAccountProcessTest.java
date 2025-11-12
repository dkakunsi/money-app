package io.dkakunsi.money.account.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.common.Context;
import io.dkakunsi.common.ProcessInput;
import io.dkakunsi.money.account.repository.AccountRepository;

public final class CreateAccountProcessTest {

  private CreateAccountProcess underTest;

  private AccountRepository accountRepository;

  @BeforeEach
  void setUp() {
    accountRepository = mock(AccountRepository.class);
    underTest = new CreateAccountProcess(accountRepository);
  }

  @Test
  void givenValidInsertAccountRequestWhenAccountDoesNotExistsThenShouldSuccessfullyCreated() {
    // Given
    final var requester = "Requester";
    final var createRequest = CreateAccountInput.builder()
        .name("Savings Account")
        .type("BANK")
        .themeColor("#FF5733")
        .build();
    final var context = mock(Context.class);
    final var input = new ProcessInput<>(createRequest, context);

    when(context.requester()).thenReturn(requester);
    when(accountRepository.upsert(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = underTest.process(input);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    final var data = result.data().get();
    assertNotNull(data.getId());
    assertEquals(createRequest.name(), data.getName());
    assertEquals(createRequest.type(), data.getType().name());
    assertEquals(createRequest.themeColor(), data.getThemeColor());
    assertEquals(BigDecimal.ZERO, data.getBalance());

    verify(accountRepository).upsert(argThat(saved -> {
      return createRequest.name().equals(saved.getName())
          && createRequest.type().equals(saved.getType().name())
          && createRequest.themeColor().equals(saved.getThemeColor())
          && BigDecimal.ZERO.equals(saved.getBalance())
          && saved.getId() != null
          && saved.getCreatedAt() != null
          && saved.getUpdatedAt() != null
          && requester.equals(saved.getCreatedBy())
          && requester.equals(saved.getUpdatedBy());
    }));
  }
}
