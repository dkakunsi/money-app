package io.dkakunsi.lab.common.web;

import io.dkakunsi.lab.common.process.ProcessResult;

@FunctionalInterface
public interface ResponseParser<T> {
  String parse(ProcessResult<T> result);
}
