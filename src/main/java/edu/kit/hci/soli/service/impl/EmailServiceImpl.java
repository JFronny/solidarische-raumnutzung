package edu.kit.hci.soli.service.impl;

import edu.kit.hci.soli.config.SoliConfiguration;
import edu.kit.hci.soli.config.template.JteContext;
import edu.kit.hci.soli.config.template.JteSoliTemplateOutput;
import edu.kit.hci.soli.domain.User;
import edu.kit.hci.soli.service.EmailService;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.PropertyKey;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Service for sending emails
 */
@Slf4j
@Service
@Profile("!test")
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;
    private final String hostname;
    private final String mailFrom;
    private final TimeZone timeZone;
    private final String css;

    /**
     * Constructs an EmailService with the specified services.
     *
     * @param mailSender        the mail sender to use
     * @param templateEngine    the template engine to use
     * @param messageSource     the message source to use
     * @param soliConfiguration the configuration of the application
     * @param mailProperties    the properties of the mail server
     */
    public EmailServiceImpl(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            MessageSource messageSource,
            SoliConfiguration soliConfiguration,
            MailProperties mailProperties
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
        this.hostname = soliConfiguration.getHostname();
        this.mailFrom = mailProperties.getUsername();
        this.timeZone = soliConfiguration.getTimeZone();
        try (InputStream is = Objects.requireNonNull(EmailServiceImpl.class.getResourceAsStream("/static/soli-mail.css"))) {
            this.css = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Could not load CSS", e);
        }
    }

    /**
     * Sends an email to the specified user.
     *
     * @param to       the user to send the email to
     * @param subject  the translation key of the subject
     * @param template the template to use
     * @param model    the model to use for the template
     */
    @Override
    public void sendMail(User to, @PropertyKey(resourceBundle = "messages") String subject, String template, Map<String, Object> model) {
        if (to.getEmail() == null) {
            log.warn("User {} has no email address, not sending email", to.getUserId());
            return;
        }

        JteContext context = new JteContext(messageSource, hostname, to.getLocale(), timeZone);
        model = new HashMap<>(model);
        model.put("context", context);
        model.put("css", css);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to.getEmail());
            helper.setSubject(context.lookup(subject));
            StringOutput stringOutput = new StringOutput();
            templateEngine.render(template + ".jte", model, new JteSoliTemplateOutput(stringOutput));
            helper.setText(stringOutput.toString(), true);
            mailSender.send(message);
            log.info("Sent {} email to user {} (address: {})", subject, to.getUserId(), to.getEmail());
            log.info("Content: {}", stringOutput);
        } catch (Exception e) { // Quite broad, but the code above is marked as throwing Exception
            log.error("Failed to send email to user {}", to.getUserId(), e);
        }
    }
}
