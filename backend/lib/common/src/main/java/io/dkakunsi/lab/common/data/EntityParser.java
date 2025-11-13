package io.dkakunsi.lab.common.data;

@FunctionalInterface
public interface EntityParser<T> {
  T parse(String data);
}
