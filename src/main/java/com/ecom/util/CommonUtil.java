package com.ecom.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class CommonUtil {
    @Autowired
    private static JavaMailSender mailSender;
    public static String generateUrl(HttpServletRequest request) {


        String sitUrl = request.getRequestURL().toString();
     return sitUrl.replace(request.getServletPath(), "");

    }

    public static boolean sendMail(String url, String reciepentEmail) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper= new MimeMessageHelper(message);

        helper.setFrom("as@gmail.com", "shooping Cart");
        helper.setTo(reciepentEmail);

        String content = "<p>Hello,</p>" +
                "<p>You have requested to reset your password.</p>" +
                "<p>Click the link below to change your password.</p>" +
                "<p><a href=\"" + url + "\">Change my password</a></p>";
        helper.setSubject("password Reset");
        helper.setText(content,true);
        mailSender.send(message);

        return true;
    }
}


