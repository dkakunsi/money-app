package io.dkakunsi.money.account.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.dkakunsi.common.Id;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class Account {

  private Id id;
  private String name;
  private Type type;
  private String themeColor;
  private BigDecimal balance;
  private User user;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  public static enum Type {
    BANK,
    CASH,
    EWALLET,
    OTHER
  }

  public boolean hasId() {
    return id != null;
  }

  public void initializeNewAccount(String creator) {
    final var now = LocalDateTime.now();
    final var executor = creator != null ? creator : "N/A";
    this.id = Id.generate();
    this.balance = BigDecimal.ZERO;
    this.createdAt = now;
    this.updatedAt = now;
    this.createdBy = executor;
    this.updatedBy = executor;
  }
}
