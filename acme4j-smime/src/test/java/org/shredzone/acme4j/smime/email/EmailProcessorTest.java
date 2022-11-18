/*
 * acme4j - Java ACME client
 *
 * Copyright (C) 2021 Richard "Shred" Körber
 *   http://acme4j.shredzone.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.acme4j.smime.email;

import static jakarta.mail.Message.RecipientType.TO;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Optional;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.shredzone.acme4j.Identifier;
import org.shredzone.acme4j.exception.AcmeProtocolException;
import org.shredzone.acme4j.smime.EmailIdentifier;
import org.shredzone.acme4j.smime.SMIMETests;
import org.shredzone.acme4j.smime.challenge.EmailReply00Challenge;
import org.shredzone.acme4j.smime.exception.AcmeInvalidMessageException;

/**
 * Unit tests for {@link EmailProcessor} and {@link ResponseGenerator}.
 */
public class EmailProcessorTest extends SMIMETests {

    private final InternetAddress expectedFrom = email("acme-generator@example.org");
    private final InternetAddress expectedTo = email("alexey@example.com");
    private final InternetAddress expectedReplyTo = email("acme-validator@example.org");
    private final Message message = mockMessage("challenge");

    @BeforeAll
    public static void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testEmailParser() throws AcmeInvalidMessageException {
        EmailProcessor processor = EmailProcessor.plainMessage(message);
        processor.expectedFrom(expectedFrom);
        processor.expectedTo(expectedTo);
        processor.expectedIdentifier(EmailIdentifier.email(expectedTo));
        processor.expectedIdentifier(new Identifier("email", expectedTo.getAddress()));

        assertThat(processor.getSender()).isEqualTo(expectedFrom);
        assertThat(processor.getRecipient()).isEqualTo(expectedTo);
        assertThat(processor.getMessageId()).isEqualTo(Optional.of("<A2299BB.FF7788@example.org>"));
        assertThat(processor.getToken1()).isEqualTo(TOKEN_PART1);
        assertThat(processor.getReplyTo()).contains(email("acme-validator@example.org"));
    }

    @Test
    public void testValidSignature() throws AcmeInvalidMessageException, IOException {
        MimeMessage message = (MimeMessage) mockMessage("valid-mail");
        X509Certificate certificate = readCertificate("valid-signer");
        EmailProcessor processor = EmailProcessor.smimeMessage(message, mailSession, certificate, true);
    }

    @Test
    public void testInvalidSignature() {
        assertThatExceptionOfType(AcmeInvalidMessageException.class)
                .isThrownBy(() -> {
                    MimeMessage message = (MimeMessage) mockMessage("invalid-signed-mail");
                    X509Certificate certificate = readCertificate("valid-signer");
                    EmailProcessor processor = EmailProcessor.smimeMessage(message, mailSession, certificate, true);
                })
                .withMessage("The S/MIME signature is invalid");
    }

    @Test
    public void testValidSignatureButNoSAN() {
        assertThatExceptionOfType(AcmeInvalidMessageException.class)
                .isThrownBy(() -> {
                    MimeMessage message = (MimeMessage) mockMessage("invalid-nosan");
                    X509Certificate certificate = readCertificate("valid-signer-nosan");
                    EmailProcessor processor = EmailProcessor.smimeMessage(message, mailSession, certificate, true);
                })
                .withMessage("Signing certificate does not provide a rfc822Name subjectAltName");
    }

    @Test
    public void testSANDoesNotMatchFrom() {
        assertThatExceptionOfType(AcmeInvalidMessageException.class)
                .isThrownBy(() -> {
                    MimeMessage message = (MimeMessage) mockMessage("invalid-cert-mismatch");
                    X509Certificate certificate = readCertificate("valid-signer");
                    EmailProcessor processor = EmailProcessor.smimeMessage(message, mailSession, certificate, true);
                })
                .withMessage("Sender 'different-ca@example.com' was not found in signing certificate");
    }

    @Test
    public void testInvalidProtectedFromHeader() {
        assertThatExceptionOfType(AcmeInvalidMessageException.class)
                .isThrownBy(() -> {
                    MimeMessage message = (MimeMessage) mockMessage("invalid-protected-mail-from");
                    X509Certificate certificate = readCertificate("valid-signer");
                    EmailProcessor processor = EmailProcessor.smimeMessage(message, mailSession, certificate, true);
                })
                .withMessage("Protected 'From' header does not match envelope header");
    }

    @Test
    public void testInvalidProtectedToHeader() {
        assertThatExceptionOfType(AcmeInvalidMessageException.class)
                .isThrownBy(() -> {
                    MimeMessage message = (MimeMessage) mockMessage("invalid-protected-mail-to");
                    X509Certificate certificate = readCertificate("valid-signer");
                    EmailProcessor processor = EmailProcessor.smimeMessage(message, mailSession, certificate, true);
                })
                .withMessage("Protected 'To' header does not match envelope header");
    }

    @Test
    public void testInvalidProtectedSubjectHeader() {
        assertThatExceptionOfType(AcmeInvalidMessageException.class)
                .isThrownBy(() -> {
                    MimeMessage message = (MimeMessage) mockMessage("invalid-protected-mail-subject");
                    X509Certificate certificate = readCertificate("valid-signer");
                    EmailProcessor processor = EmailProcessor.smimeMessage(message, mailSession, certificate, true);
                })
                .withMessage("Protected 'Subject' header does not match envelope header");
    }

    @Test
    public void testNonStrictInvalidProtectedSubjectHeader() {
        assertThatNoException()
                .isThrownBy(() -> {
                    MimeMessage message = (MimeMessage) mockMessage("invalid-protected-mail-subject");
                    X509Certificate certificate = readCertificate("valid-signer");
                    EmailProcessor processor = EmailProcessor.smimeMessage(message, mailSession, certificate, false);
                });
    }

    @Test
    public void textExpectedFromFails() {
        assertThrows(AcmeProtocolException.class, () -> {
            EmailProcessor processor = EmailProcessor.plainMessage(message);
            processor.expectedFrom(expectedTo);
        });
    }

    @Test
    public void textExpectedToFails() {
        assertThrows(AcmeProtocolException.class, () -> {
            EmailProcessor processor = EmailProcessor.plainMessage(message);
            processor.expectedTo(expectedFrom);
        });
    }

    @Test
    public void textExpectedIdentifierFails1() {
        assertThrows(AcmeProtocolException.class, () -> {
            EmailProcessor processor = EmailProcessor.plainMessage(message);
            processor.expectedIdentifier(EmailIdentifier.email(expectedFrom));
        });
    }

    @Test
    public void textExpectedIdentifierFails2() {
        assertThrows(AcmeProtocolException.class, () -> {
            EmailProcessor processor = EmailProcessor.plainMessage(message);
            processor.expectedIdentifier(Identifier.ip("192.168.0.1"));
        });
    }

    @Test
    public void textNoChallengeFails1() {
        assertThrows(IllegalStateException.class, () -> {
            EmailProcessor processor = EmailProcessor.plainMessage(message);
            processor.getToken();
        });
    }

    @Test
    public void textNoChallengeFails2() {
        assertThrows(IllegalStateException.class, () -> {
            EmailProcessor processor = EmailProcessor.plainMessage(message);
            processor.getAuthorization();
        });
    }

    @Test
    public void textNoChallengeFails3() {
        assertThrows(IllegalStateException.class, () -> {
            EmailProcessor processor = EmailProcessor.plainMessage(message);
            processor.respond();
        });
    }

    @Test
    public void testChallenge() throws AcmeInvalidMessageException {
        EmailReply00Challenge challenge = mockChallenge("emailReplyChallenge");

        EmailProcessor processor = EmailProcessor.plainMessage(message);
        processor.withChallenge(challenge);
        assertThat(processor.getToken()).isEqualTo(TOKEN);
        assertThat(processor.getAuthorization()).isEqualTo(KEY_AUTHORIZATION);
        assertThat(processor.respond()).isNotNull();
    }

    @Test
    public void testChallengeMismatch() {
        assertThrows(AcmeProtocolException.class, () -> {
            EmailReply00Challenge challenge = mockChallenge("emailReplyChallengeMismatch");
            EmailProcessor processor = EmailProcessor.plainMessage(message);
            processor.withChallenge(challenge);
        });
    }

    @Test
    public void testResponse() throws IOException, MessagingException, AcmeInvalidMessageException {
        EmailReply00Challenge challenge = mockChallenge("emailReplyChallenge");

        Message response = EmailProcessor.plainMessage(message)
                .withChallenge(challenge)
                .respond()
                .generateResponse(mailSession);

        assertResponse(response, RESPONSE_BODY);
    }

    @Test
    public void testResponseWithHeaderFooter() throws IOException, MessagingException, AcmeInvalidMessageException {
        EmailReply00Challenge challenge = mockChallenge("emailReplyChallenge");

        Message response = EmailProcessor.plainMessage(message)
                .withChallenge(challenge)
                .respond()
                .withHeader("This is an introduction.")
                .withFooter("This is a footer.")
                .generateResponse(mailSession);

        assertResponse(response,
                "This is an introduction.\r\n"
                + RESPONSE_BODY
                + "This is a footer.");
    }

    @Test
    public void testResponseWithCallback() throws IOException, MessagingException, AcmeInvalidMessageException {
        EmailReply00Challenge challenge = mockChallenge("emailReplyChallenge");

        Message response = EmailProcessor.plainMessage(message)
                .withChallenge(challenge)
                .respond()
                .withGenerator((msg, body) -> msg.setContent("Head\r\n" + body + "Foot", "text/plain"))
                .generateResponse(mailSession);

        assertResponse(response, "Head\r\n" + RESPONSE_BODY + "Foot");
    }

    private void assertResponse(Message response, String expectedBody)
            throws MessagingException, IOException {
        assertThat(response.getContentType()).isEqualTo("text/plain");
        assertThat(response.getContent().toString()).isEqualTo(expectedBody);

        // This is a response, so the expected sender is the recipient of the challenge
        assertThat(response.getFrom()).hasSize(1);
        assertThat(response.getFrom()[0]).isEqualTo(expectedTo);

        // There is a Reply-To header, so we expect the mail to go only there
        assertThat(response.getRecipients(TO)).hasSize(1);
        assertThat(response.getRecipients(TO)[0]).isEqualTo(expectedReplyTo);

        assertThat(response.getSubject()).isEqualTo("Re: ACME: " + TOKEN_PART1);

        String[] inReplyToHeader = response.getHeader("In-Reply-To");
        assertThat(inReplyToHeader).hasSize(1);
        assertThat(inReplyToHeader[0]).isEqualTo("<A2299BB.FF7788@example.org>");
    }

}
