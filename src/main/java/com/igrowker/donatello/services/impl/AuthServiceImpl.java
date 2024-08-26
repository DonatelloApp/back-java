package com.igrowker.donatello.services.impl;

import com.igrowker.donatello.auth.entities.CustomUser;
import com.igrowker.donatello.auth.entities.RestorePasswordRequest;
import com.igrowker.donatello.dtos.AuthDTO;
import com.igrowker.donatello.exceptions.NotFoundException;
import com.igrowker.donatello.utils.JWTUtils;
import com.igrowker.donatello.dtos.LoginDTO;
import com.igrowker.donatello.dtos.RegisterDTO;
import com.igrowker.donatello.dtos.profile.ProfileUpdateDto;
import com.igrowker.donatello.exceptions.BadCredentialsException;
import com.igrowker.donatello.exceptions.ConflictException;
import com.igrowker.donatello.exceptions.FieldInvalidException;
import com.igrowker.donatello.repositories.IUserRepository;
import com.igrowker.donatello.services.IAuthService;
import com.igrowker.donatello.utils.MailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private MailManager mailManager;

    public void validateNewMail(String mail) {
        if (userRepository.existsByMail(mail)) throw new ConflictException("This Email is already registered!");
    }


    public AuthDTO register(RegisterDTO registerDTO) {
        if (!registerDTO.getPassword().equals(registerDTO.getPassword2())) throw new FieldInvalidException("Passwords do not match!");

        validateNewMail(registerDTO.getMail());
        CustomUser user = new CustomUser().builder()
                .name(registerDTO.getName())
                .mail(registerDTO.getMail())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .phone(registerDTO.getPhone())
                .build();
        userRepository.save(user);

        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(registerDTO.getMail(),
                        registerDTO.getPassword()));
        return AuthDTO.builder()
                .token(jwtUtils.generateToken(user))
                .build();
    }

    public AuthDTO login(LoginDTO loginDTO) {
        UserDetails userDetails = userRepository
                .findByMail(loginDTO.getMail())
                .orElseThrow(() -> new BadCredentialsException());

        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getMail(),
                        loginDTO.getPassword()));

        String token = jwtUtils.generateToken(userDetails);
        return AuthDTO.builder()
                .token(token)
                .build();
    }

    public CustomUser getLoguedUser(HttpHeaders headers) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return (CustomUser) securityContext.getAuthentication().getPrincipal(); // retorna el usuario que envia el jwt
    }

    public CustomUser updateUser(CustomUser user, ProfileUpdateDto profileUpdateDto) {
        if (profileUpdateDto.getName() != null) user.setName(profileUpdateDto.getName());
        if (profileUpdateDto.getPhone() != null) user.setPhone(profileUpdateDto.getPhone());
        if (profileUpdateDto.getPassword() != null)
            user.setPassword(passwordEncoder.encode(profileUpdateDto.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Boolean restorePassword (String email){
        Optional<CustomUser> user = userRepository.findByMail(email);
        if (user.isEmpty()) throw new NotFoundException("Email no registrado");
        String tokenToRestore = jwtUtils.createTokenForRestorePassword(email);
        mailManager.sendEmailToRestorePassword(email , tokenToRestore);
        return true;

    }

    @Override
    public AuthDTO setNewPassword(RestorePasswordRequest restorePasswordRequest) {
        if(!restorePasswordRequest.getPassword1().equals(restorePasswordRequest.getPassword2())) throw new FieldInvalidException("Passwords no coinciden");
        if (jwtUtils.isTokenExpired(restorePasswordRequest.getToken())) throw new FieldInvalidException("Token expirado, vuelve a solicitar envio del token");

        String email = jwtUtils.getEmailFromToken(restorePasswordRequest.getToken());
        Optional<CustomUser> user = userRepository.findByMail(email);

        if(user.isPresent()){
            CustomUser u = user.get();
            u.setPassword(passwordEncoder.encode(restorePasswordRequest.getPassword1()));
            userRepository.save(u);
            return login(new LoginDTO(email,restorePasswordRequest.getPassword1()));

        }
        return null;
    }

}
