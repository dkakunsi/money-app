package io.dkakunsi.lab.common.data;

@FunctionalInterface
public interface EntityParser<T extends Entity> {
  String parse(T data);
}
