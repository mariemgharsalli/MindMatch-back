package esprit.mindmatch.Service;

import esprit.mindmatch.Entities.Mail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {
    public JavaMailSender getMailSender() {
        return this.mailSender;
    }

    public void sendDecisionEmail(Mail mail) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            String html = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "    <meta charset=\"UTF-8\">"
                    + "    <style>"
                    + "        body {"
                    + "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;"
                    + "            line-height: 1.6;"
                    + "            color: #333333;"
                    + "            max-width: 600px;"
                    + "            margin: 0 auto;"
                    + "            padding: 20px;"
                    + "            background-color: #f7f9fc;"
                    + "        }"
                    + "        .email-container {"
                    + "            background-color: #ffffff;"
                    + "            border-radius: 8px;"
                    + "            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);"
                    + "            overflow: hidden;"
                    + "        }"
                    + "        .header {"
                    + "            background-color: #3f51b5;"
                    + "            color: white;"
                    + "            padding: 25px;"
                    + "            text-align: center;"
                    + "        }"
                    + "        .logo {"
                    + "            font-size: 24px;"
                    + "            font-weight: bold;"
                    + "            margin-bottom: 10px;"
                    + "        }"
                    + "        .content {"
                    + "            padding: 30px;"
                    + "        }"
                    + "        .greeting {"
                    + "            font-size: 18px;"
                    + "            margin-bottom: 20px;"
                    + "        }"
                    + "        .message {"
                    + "            background-color: #f8f9fa;"
                    + "            padding: 20px;"
                    + "            border-radius: 6px;"
                    + "            margin: 20px 0;"
                    + "            border-left: 4px solid #3f51b5;"
                    + "        }"
                    + "        .footer {"
                    + "            text-align: center;"
                    + "            padding: 20px;"
                    + "            color: #777777;"
                    + "            font-size: 14px;"
                    + "            border-top: 1px solid #eeeeee;"
                    + "        }"
                    + "        .signature {"
                    + "            margin-top: 30px;"
                    + "            color: #555555;"
                    + "        }"
                    + "        .button {"
                    + "            display: inline-block;"
                    + "            background-color: #3f51b5;"
                    + "            color: white !important;"
                    + "            text-decoration: none;"
                    + "            padding: 12px 24px;"
                    + "            border-radius: 4px;"
                    + "            margin: 15px 0;"
                    + "            font-weight: bold;"
                    + "        }"
                    + "    </style>"
                    + "</head>"
                    + "<body>"
                    + "    <div class=\"email-container\">"
                    + "        <div class=\"header\">"
                    + "            <div class=\"logo\">MindMatch</div>"
                    + "            <div>Votre plateforme de conférences intelligentes</div>"
                    + "        </div>"
                    + "        <div class=\"content\">"
                    + "            <div class=\"greeting\">"
                    + "                Bonjour <strong>" + mail.getModel().get("name") + "</strong>,"
                    + "            </div>"
                    + "            <div class=\"message\">"
                    +                 mail.getModel().get("message")
                    + "            </div>"
                    + "            <div class=\"signature\">"
                    + "                Cordialement,<br>"
                    + "                <strong>L'équipe MindMatch</strong>"
                    + "            </div>"
                    + "        </div>"
                    + "        <div class=\"footer\">"
                    + "            © 2023 MindMatch. Tous droits réservés.<br>"
                    + "            <small>Ceci est un message automatique, merci de ne pas y répondre.</small>"
                    + "        </div>"
                    + "    </div>"
                    + "</body>"
                    + "</html>";



            helper.setTo(mail.getTo());
            helper.setFrom(mail.getFrom());
            helper.setSubject(mail.getSubject());
            helper.setText(html, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(Mail mail) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            // Direct HTML content for the email
            String html = "<html><body style='font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f9;'>"
                    + "<table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='background-color: #ffffff; border: 1px solid #e1e1e1; padding: 20px;'>"
                    + "<tr><td style='background-color: #003366; color: #ffffff; text-align: center; padding: 10px 0;'>"
                    + "<h1 style='font-size: 24px; margin: 0;'>Rappel de votre session</h1>"
                    + "</td></tr>"
                    + "<tr><td style='padding: 20px;'>"
                    + "<p style='font-size: 16px; color: #333333;'>Bonjour <strong>" + mail.getModel().get("name") + "</strong>,</p>"
                    + "<p style='font-size: 16px; color: #333333;'>Voici un rappel de votre session <strong>" + mail.getModel().get("sessionName") + "</strong> prévue pour demain.</p>"
                    + "<p style='font-size: 16px; color: #333333;'><strong>Date :</strong> " + mail.getModel().get("sessionDate") + "</p>"
                    + "<p style='font-size: 16px; color: #333333;'><strong>Lieu :</strong> " + mail.getModel().get("location") + "</p>"
                    + "<p style='font-size: 16px; color: #333333;'>Nous vous attendons avec impatience pour cette session !</p>"
                    + "</td></tr>"
                    + "<tr><td style='background-color: #003366; color: #ffffff; text-align: center; padding: 10px;'>"
                    + "<p style='font-size: 14px; margin: 0;'>Merci et à bientôt,</p>"
                    + "<p style='font-size: 14px; margin: 0;'>L'équipe de gestion des sessions</p>"
                    + "</td></tr>"
                    + "</table>"
                    + "</body></html>";

            helper.setTo(mail.getTo());
            helper.setFrom(mail.getFrom());
            helper.setSubject(mail.getSubject());
            helper.setText(html, true); // Send the email as HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendConfirmationEmail(Mail mail) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            // Direct HTML content for the confirmation email
            String html = "<html><body>"
                    + "<p>Bonjour " + mail.getModel().get("name") + ",</p>"
                    + "<p>Votre confirmation a bien été enregistrée.</p>"
                    + "</body></html>";

            helper.setTo(mail.getTo());
            helper.setFrom(mail.getFrom());
            helper.setSubject(mail.getSubject());
            helper.setText(html, true); // Send the email as HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendEmailResetPassword(String to, String confirmationUrl, String subject) throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED, StandardCharsets.UTF_8.name());

        // Direct HTML content for reset password email
        String html = "<html><body>"
                + "<p>Bonjour,</p>"
                + "<p>Pour réinitialiser votre mot de passe, veuillez cliquer sur le lien suivant :</p>"
                + "<p><a href=\"" + confirmationUrl + "\">Réinitialiser le mot de passe</a></p>"
                + "</body></html>";

        helper.setFrom("noreply@local.test");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true); // Send the email as HTML

        mailSender.send(mimeMessage);
    }
}
