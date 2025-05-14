package tn.esprit.services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class MailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587"; // Port pour TLS
    private static final String SENDER_EMAIL = "Reheltycompany@gmail.com"; // Remplacez par votre adresse e-mail
    private static final String SENDER_PASSWORD = "hqit oaux tbds glhf"; // Remplacez par votre mot de passe (ou utilisez un mot de passe d'application pour Gmail)

    // Fonction pour envoyer un e-mail
    public void sendVerificationEmail(String recipientEmail, String verificationCode) {
        // Définir les propriétés pour la session de l'email
        Properties properties = new Properties();
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Créer une session d'email
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            // Créer le message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Code de vérification");
            message.setText("Voici votre code de vérification : " + verificationCode);

            // Envoyer l'email
            Transport.send(message);
            System.out.println("Email de vérification envoyé à : " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'envoi de l'email");
        }
    }
}
