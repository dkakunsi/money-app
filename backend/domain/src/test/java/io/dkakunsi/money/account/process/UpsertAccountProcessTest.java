package io.dkakunsi.money.account.process;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class UpsertAccountProcessTest {

  private UpsertAccountProcess underTest;

  @BeforeEach
  void setUp() {
    underTest = new UpsertAccountProcess();
  }

  @Test
  void givenValidCreateAccountRequestWhenAccountDoesNotExistsThenShouldSuccessfullyCreated() {
    // Given, When
    final var createRequest = new UpsertAccountInput();
    final var result = underTest.process(createRequest);

    // Then
    assertTrue(result.isSuccess());
  }
}
