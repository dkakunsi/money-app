package io.dkakunsi.money.user.process;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public final record UserRetrievalInput(@NotBlank String email) {
}
