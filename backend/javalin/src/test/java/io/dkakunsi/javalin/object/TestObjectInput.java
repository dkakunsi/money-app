package io.dkakunsi.javalin.object;

import lombok.Builder;

@Builder
public record TestObjectInput(String code, String name) {
}
