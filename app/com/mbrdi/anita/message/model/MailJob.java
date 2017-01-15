package com.mbrdi.anita.message.model;

import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class MailJob implements Runnable {
	String mailContent;
	String header;
	String recipient;
	String[] recipients;
	
	public MailJob(String header, String body, String recipient) {
		super();
		this.header = header;
		this.mailContent = body;
		this.recipient = recipient;
	}

	public MailJob(String header, String body, String[] recipients) {
		super();
		this.header = header;
		this.mailContent = body;
		this.recipients = recipients;
	}

	public void run() {

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("email-smtp.us-west-2.amazonaws.com");
		mailSender.setPort(465);
		mailSender.setUsername("AKIAIOGNPLETEE25H27Q");
		mailSender.setPassword("AsYnEpO1hR+KNhRoWGb8Rsb5KgM0plk86JaUBtW9Fn/O");
		mailSender.setProtocol("smtps");

		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "auto");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.debug", "true");

//		props.setProperty("mail.smtp.user", "AKIAIOGNPLETEE25H27Q");
//		props.setProperty("mail.smtp.password", "AsYnEpO1hR+KNhRoWGb8Rsb5KgM0plk86JaUBtW9Fn/O");
		props.setProperty("mail.smtp.auth", "true");

		mailSender.setSession(Session.getDefaultInstance(props));
		mailSender.setJavaMailProperties(props);

		/*
		 * props.put("mail.smtp.socketFactory.port", 465);
		 * props.put("mail.smtp.socketFactory.class",
		 * "javax.net.ssl.SSLSocketFactory");
		 * props.put("mail.smtp.socketFactory.fallback", "false");
		 */
		try {
			MimeMessage message = new MimeMessage(mailSender.getSession());
			message.setFrom(new InternetAddress("support@zoyride.com"));

			if(recipient != null) {
				InternetAddress toAddress = new InternetAddress(recipient);
				message.addRecipient(Message.RecipientType.TO, toAddress);
			}
			else {
				String rs = "";
				for(String r:recipients) {
					rs += (r + ",");
				}
				message.addRecipients(Message.RecipientType.TO, rs);
			}
			message.setSubject(header);

			// create MimeBodyPart object and set your message content
			BodyPart messageBodyPart1 = new MimeBodyPart();
			messageBodyPart1.setContent(mailContent, "text/html");

			// create Multipart object and add MimeBodyPart objects to this
			// object
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart1);

			message.setContent(multipart);

			mailSender.send(message);

		} catch (Exception e) {
			// logger.error("Error in sending email :",e);
			e.printStackTrace();
		}
	}
}