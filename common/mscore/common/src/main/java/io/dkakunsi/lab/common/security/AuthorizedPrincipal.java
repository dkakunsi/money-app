package io.dkakunsi.lab.common.security;

import jakarta.validation.constraints.NotBlank;

public final record AuthorizedPrincipal(@NotBlank String email) {
}
