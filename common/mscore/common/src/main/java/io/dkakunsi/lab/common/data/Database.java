package io.dkakunsi.lab.common.data;

import java.util.List;
import java.util.Optional;

import io.dkakunsi.lab.common.Id;
import io.dkakunsi.lab.common.Prototype;
import io.dkakunsi.lab.common.data.Query.Criteria;

public abstract class Database<T extends Entity> implements Prototype {
  protected ResultParser<T> resultParser;

  protected Database(
      ResultParser<T> resultParser) {
    this.resultParser = resultParser;
  }

  public abstract Schema getSchema();

  public abstract T save(T T);

  public abstract void delete(Id id);

  public abstract Optional<T> get(Id id);

  public abstract List<T> get(String field, Object key);

  public abstract List<T> search(List<Criteria> criteria);
}
