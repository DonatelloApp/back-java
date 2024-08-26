package com.igrowker.donatello.utils;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;
@Component
public class MailManager {

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${spring.url.frontend}")
    private String urlFrontend;

    @Autowired
    JavaMailSender javaMailSender;

    public void sendEmailToRestorePassword(String email, String token) {
        String urlRestore=urlFrontend+"/api/auth/setNewPassword/"+token;
        String template = String.format("""
                <div style=\"color:#000000; background-color: #e9f2ff; border:1px solid #b1e1ff; border-radius:10px; width:70%%;margin:auto; margin-bottom: 20px; text-align: center;\">     
                     <h6>%s</h6>               
                     <h3>Has click en el link para restaurar tu contraseña</h3> 
                    <div style=\"color:#ffffff; background-color:#048d2d; border: 2px solid #ffffff; border-radius:5px; padding: 10px; margin: 30px; \">                        
                        <a href= %s style=\"color:#ffffff; font-weight: 900; font-size: 1.5em;\"><h4>Link para restaurar tu contraseña</h4></a>
                    </div>                         
                    <div style=\"color:#ffffff; background-color: #ff3d4e; border: 1px solid #ffffff; width:40%%; margin:auto; border-radius:10px;\">
                     <p>Es valido solo por 24hs.</p>      
                     </div>     
                </div>
                """,urlRestore,urlRestore);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setSubject("Restauracion de password");
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message , true);
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setText(template, true);
            mimeMessageHelper.setFrom(sender);
            javaMailSender.send(message);
        } catch (Exception e){
            throw new RuntimeException("ERROR ENVIANDO EMAIL");
        }
    }
}
