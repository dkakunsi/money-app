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

public final class UpsertAccountProcessTest {

  private UpsertAccountProcess underTest;

  private AccountRepository accountRepository;

  @BeforeEach
  void setUp() {
    accountRepository = mock(AccountRepository.class);
    underTest = new UpsertAccountProcess(accountRepository);
  }

  @Test
  void givenValidCreateAccountRequestWhenAccountDoesNotExistsThenShouldSuccessfullyCreated() {
    // Given
    final var createRequest = UpsertAccountInput.builder()
        .name("Savings Account")
        .type("BANK")
        .themeColor("#FF5733")
        .build();
    final var context = mock(Context.class);
    when(context.getRequester()).thenReturn("Requester");
    final var input = ProcessInput.<UpsertAccountInput>builder()
        .data(createRequest)
        .context(context)
        .build();

    when(accountRepository.upsert(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = underTest.process(input);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.getData().isPresent());

    final var data = result.getData().get();
    assertNotNull(data.getId());
    assertEquals(createRequest.getName(), data.getName());
    assertEquals(createRequest.getType(), data.getType().name());
    assertEquals(createRequest.getThemeColor(), data.getThemeColor());
    assertEquals(BigDecimal.ZERO, data.getBalance());

    verify(accountRepository).upsert(argThat(saved -> {
      try {
        return createRequest.getName().equals(saved.getName())
            && createRequest.getType().equals(saved.getType().name())
            && createRequest.getThemeColor().equals(saved.getThemeColor())
            && BigDecimal.ZERO.equals(saved.getBalance())
            && saved.getId() != null
            && saved.getCreatedAt() != null
            && saved.getUpdatedAt() != null
            && "Requester".equals(saved.getCreatedBy())
            && "Requester".equals(saved.getUpdatedBy());
      } catch (Exception e) {
        return false;
      }
    }));
  }
}
