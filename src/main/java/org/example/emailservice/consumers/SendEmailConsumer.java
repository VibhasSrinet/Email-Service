package org.example.emailservice.consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.emailservice.dtos.MessageDto;
import org.example.emailservice.utils.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

@Component
public class SendEmailConsumer {
    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "signup", groupId = "emailService")
    public void sendEmail(String message) {
        System.out.println("Triggered");
        try {
            MessageDto messageDto = objectMapper.readValue(message, MessageDto.class);
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
            props.put("mail.smtp.port", "587"); //TLS Port
            props.put("mail.smtp.auth", "true"); //enable authentication
            props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

            //create Authenticator object to pass in Session.getInstance argument
            Authenticator auth = new Authenticator() {
                //override the getPasswordAuthentication method
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(messageDto.getFrom(),System.getenv("PASSWORD"));
                }
            };
            Session session = Session.getInstance(props, auth);

            EmailUtil.sendEmail(session, messageDto.getTo(),messageDto.getSubject(), messageDto.getBody());
        } catch (JsonProcessingException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
