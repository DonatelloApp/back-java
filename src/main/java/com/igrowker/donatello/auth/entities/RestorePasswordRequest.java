package com.igrowker.donatello.auth.entities;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestorePasswordRequest {
    @NotNull(message = "Token no puede ser nulo")
    String token;

    @NotNull(message = "Password no puede ser nulo")
    @Size(min=2, max=30, message = "Password debe tener entre 2 y 30 caracteres")
    String password1;

    @NotNull(message = "Password no puede ser nulo")
    @Size(min=2, max=30, message = "Password debe tener entre 2 y 30 caracteres")
    String password2;
}
