package io.dkakunsi.lab.common.web;

import java.util.Map;

import io.dkakunsi.lab.common.Context;
import io.dkakunsi.lab.common.process.ProcessInput;

@FunctionalInterface
public interface RequestParser<T> {
  ProcessInput<T> parse(String body, Map<String, String> pathParams, Context context);
}
