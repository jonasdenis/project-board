package de.adesso.projectboard.adapter.mail;

import de.adesso.projectboard.adapter.mail.configuration.MailConfigurationProperties;
import de.adesso.projectboard.adapter.mail.persistence.MessageRepository;
import de.adesso.projectboard.adapter.mail.persistence.TemplateMessage;
import de.adesso.projectboard.base.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Service to send {@link SimpleMailMessage}s generated from persisted {@link TemplateMessage}s.
 */
@Slf4j
@Transactional(readOnly = true)
public class MailSenderAdapter {

    private final MessageRepository messageRepository;

    private final JavaMailSender mailSender;

    private final UserService userService;

    private final MailConfigurationProperties properties;

    private final Clock clock;

    public MailSenderAdapter(MessageRepository messageRepository,
                             JavaMailSender mailSender,
                             UserService userService,
                             MailConfigurationProperties properties,
                             Clock clock) {
        this.messageRepository = messageRepository;
        this.mailSender = mailSender;
        this.userService = userService;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Tries to send all pending messages whose {@link TemplateMessage#isStillRelevant(LocalDateTime)}
     * method return {@code true}.
     */
    @Scheduled(fixedDelay = 10000L)
    @Transactional
    public void sendPendingMessages() {
        messageRepository.findAll().forEach(templateMessage -> {
            try {
                if(templateMessage.isStillRelevant(LocalDateTime.now(clock))) {
                    sendMessage(templateMessage);
                }

                // delete the message when it was sent
                // successfully or is not relevant anymore
                messageRepository.delete(templateMessage);
            } catch (Exception err) {
                log.error("Error sending mail!", err);
            }
        });
    }

    /**
     *
     * @param message
     *          The message to queue, not null.
     * @param <T>
     *          The type of the message.
     *
     * @return
     *          The queued message.
     */
    @Transactional
    public <T extends TemplateMessage> T queueMessage(T message) {
        return messageRepository.save(message);
    }

    void sendMessage(TemplateMessage message) {
        var addressee = message.getAddressee();
        var addresseeMail = userService.getUserData(addressee).getEmail();

        // create a new mail message, set the subject, text, addressee and send it
        var mailMessage = new SimpleMailMessage();
        mailMessage.setSubject(message.getSubject());
        mailMessage.setText(message.getText());
        mailMessage.setFrom(properties.getFromMail());
        mailMessage.setTo(addresseeMail);

        mailSender.send(mailMessage);

        log.info(String.format("Mail sent to %s!", addresseeMail));
    }

}
