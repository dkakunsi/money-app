package io.dkakunsi.common.web;

import io.dkakunsi.common.process.ProcessResult;

@FunctionalInterface
public interface ResponseParser<T> {
  String parse(ProcessResult<T> result);
}
