package com.igrowker.donatello.controllers;

import com.igrowker.donatello.auth.entities.RestorePasswordRequest;
import com.igrowker.donatello.dtos.AuthDTO;
import com.igrowker.donatello.dtos.LoginDTO;
import com.igrowker.donatello.dtos.RegisterDTO;
import com.igrowker.donatello.exceptions.ErrorMessage;
import com.igrowker.donatello.services.impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/auth/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Usuarios")
public class AuthController {
    @Autowired
    private AuthServiceImpl authService;

    @Operation(summary = "Enviando email y password, retorna un JWT con las credenciales del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna un JWT",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Credenciales invalidas",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)) }) })
    @PostMapping("login")
    public ResponseEntity<AuthDTO> login (@Valid @RequestBody LoginDTO loginDTO){
        return new ResponseEntity<>(authService.login(loginDTO), HttpStatus.OK);
    }

    @Operation(summary = "Registra un nuevo usuario retorna un JWT con credenciales del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna un JWT",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Credenciales invalidas",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)) }) })
    @PostMapping("register")
    public ResponseEntity<AuthDTO> register (@Valid @RequestBody RegisterDTO registerDTO){
        return new ResponseEntity<>(authService.register(registerDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Recibe un email como parametro y si pertenece a un usuario registrado, se envia un email con un JWT para restaurar el passwor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Se envia email con JWT.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Email No econtrado",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)) })
    })
    @GetMapping("restorePassword")
    public ResponseEntity<Boolean> restorePassword (@RequestParam @Email String email){
        return new ResponseEntity<>(authService.restorePassword(email), HttpStatus.ACCEPTED);
    }
    @Operation(summary = "Recibe un RestorePassRequest que incluye el token enviado, el password y la confirmacion de password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Aceptado, retorna el token de autorizacion",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthDTO.class)) }),
            @ApiResponse(responseCode = "403", description = "JWT invalido",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)) }),
            @ApiResponse(responseCode = "406", description = "Error resultado de enviar informacion erronea o incompleta, Ex: 'Password debe tener al menos 8 caracteres!' ",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)) })
    })
    @PostMapping(path = "setNewPassword")
    public ResponseEntity<AuthDTO> setNewPassword(@RequestBody @Valid RestorePasswordRequest restorePasswordRequest){
        return new ResponseEntity<>(authService.setNewPassword(restorePasswordRequest) , HttpStatus.ACCEPTED);
    }

}
