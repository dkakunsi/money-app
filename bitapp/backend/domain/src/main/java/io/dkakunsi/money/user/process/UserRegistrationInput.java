package io.dkakunsi.money.user.process;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public final record UserRegistrationInput(
    @NotBlank String name,
    @NotBlank String email,
    String phone,
    String photoUrl) {
}
