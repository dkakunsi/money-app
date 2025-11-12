package io.dkakunsi.common.web;

import java.util.Map;

import io.dkakunsi.common.Context;
import io.dkakunsi.common.process.ProcessInput;

@FunctionalInterface
public interface RequestParser<T> {
  ProcessInput<T> parse(String body, Map<String, String> pathParams, Context context);
}
