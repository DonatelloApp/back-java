package com.igrowker.donatello.services;

import com.igrowker.donatello.auth.entities.CustomUser;
import com.igrowker.donatello.auth.entities.RestorePasswordRequest;
import com.igrowker.donatello.dtos.AuthDTO;
import com.igrowker.donatello.dtos.LoginDTO;
import com.igrowker.donatello.dtos.RegisterDTO;
import com.igrowker.donatello.dtos.profile.ProfileUpdateDto;
import org.springframework.http.HttpHeaders;

public interface IAuthService {

    AuthDTO register(RegisterDTO registerDTO);

    AuthDTO login(LoginDTO loginDTO);

    CustomUser getLoguedUser(HttpHeaders headers);

    CustomUser updateUser(CustomUser user, ProfileUpdateDto profileUpdateDto);

    Boolean restorePassword (String email);

    AuthDTO setNewPassword(RestorePasswordRequest restorePasswordRequest);
}
