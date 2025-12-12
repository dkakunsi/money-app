package io.dkakunsi.lab.javalin.object;

import lombok.Builder;

@Builder
public record TestObjectInput(String code, String name) {
}
