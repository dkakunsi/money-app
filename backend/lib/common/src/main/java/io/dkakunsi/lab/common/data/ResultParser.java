package io.dkakunsi.lab.common.data;

@FunctionalInterface
public interface ResultParser<T extends Entity> {
  T parse(String data);
}
