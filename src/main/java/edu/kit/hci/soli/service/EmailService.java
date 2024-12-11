package edu.kit.hci.soli.service;

import edu.kit.hci.soli.config.template.JteContext;
import edu.kit.hci.soli.domain.User;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.PropertyKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for sending emails
 */
@Slf4j
@Service
public class EmailService {
    @Value("${spring.mail.username}")
    private String mailFrom;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;

    /**
     * Constructs an EmailService with the specified services.
     *
     * @param mailSender the mail sender to use
     * @param templateEngine the template engine to use
     * @param messageSource the message source to use
     */
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine, MessageSource messageSource) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
    }

    /**
     * @param to the user to send the email to
     * @param subject the translation key of the subject
     * @param template the template to use
     * @param model the model to use for the template
     */
    public void sendMail(User to, @PropertyKey(resourceBundle = "messages") String subject, String template, Map<String, Object> model) {
        JteContext context = new JteContext(messageSource, to.getLocale());
        model = new HashMap<>(model);
        model.put("context", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to.getEmail());
            helper.setSubject(context.lookup(subject));
            StringOutput stringOutput = new StringOutput();
            templateEngine.render(template + ".jte", model, stringOutput);
            helper.setText(stringOutput.toString(), true);
            mailSender.send(message);
        } catch (Exception e) { // Quite broad, but the code above is marked as throwing Exception
            log.error("Failed to send email to user {}", to.getUserId(), e);
        }
    }
}