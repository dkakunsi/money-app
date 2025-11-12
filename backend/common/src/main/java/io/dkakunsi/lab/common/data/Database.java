package io.dkakunsi.lab.common.data;

import java.util.List;
import java.util.Optional;

import io.dkakunsi.lab.common.Id;
import io.dkakunsi.lab.common.Prototype;
import io.dkakunsi.lab.common.data.Query.Criteria;

/**
 *
 * @author dkakunsi
 */
public interface Database<T extends Entity> extends Prototype {

  default Schema getSchema() {
    throw new RuntimeException("Not implemented yet");
  }

  default T save(T T) {
    throw new RuntimeException("Not implemented yet");
  }

  default void delete(Id id) {
    throw new RuntimeException("Not implemented yet");
  }

  default Optional<T> get(Id id) {
    throw new RuntimeException("Not implemented yet");
  }

  default List<T> get(String field, Object key) {
    throw new RuntimeException("Not implemented yet");
  }

  default List<T> search(List<Criteria> criteria) {
    throw new RuntimeException("Not implemented yet");
  }
}
