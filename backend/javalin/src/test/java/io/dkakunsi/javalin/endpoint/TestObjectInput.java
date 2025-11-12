package io.dkakunsi.javalin.endpoint;

import lombok.Builder;

@Builder
public record TestObjectInput(String code, String name) {
}
