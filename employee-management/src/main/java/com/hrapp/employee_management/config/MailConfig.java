//THIS IS THE DUMMY EMAIL SENDER

//package com.hrapp.employee_management.config;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSenderImpl;
//
//@Configuration
//public class MailConfig {
//
//    @Bean
//    public JavaMailSenderImpl javaMailSender() {
//        return new JavaMailSenderImpl(); // dummy sender, won’t actually send email
//        return new JavaMailSenderImpl() {
//            @Override
//            public void send(SimpleMailMessage simpleMessage) {
//                System.out.println("----- DUMMY EMAIL -----");
//                System.out.println("From: " + simpleMessage.getFrom());
//                System.out.println("To: " + String.join(", ", simpleMessage.getTo()));
//                System.out.println("Subject: " + simpleMessage.getSubject());
//                System.out.println("Text:\n" + simpleMessage.getText());
//                System.out.println("-----------------------");
//            }
//
//            @Override
//            public void send(SimpleMailMessage... simpleMessages) {
//                for (SimpleMailMessage msg : simpleMessages) {
//                    send(msg);
//                }
//            }
//        };
//    }
//}
