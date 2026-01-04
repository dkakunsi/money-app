package io.dkakunsi.lab.javalin;

import lombok.Builder;

@Builder
public record TestObjectInput(String code, String name) {
}
