package io.dkakunsi.money.account.repository;

import io.dkakunsi.money.account.model.Account;

public interface AccountRepository {
  Account upsert(Account account);
}
