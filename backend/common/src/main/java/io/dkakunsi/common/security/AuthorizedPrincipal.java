package io.dkakunsi.common.security;

import jakarta.validation.constraints.NotBlank;

public final record AuthorizedPrincipal(@NotBlank String email) {
}
