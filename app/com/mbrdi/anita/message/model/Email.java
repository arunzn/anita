package com.mbrdi.anita.message.model;

import com.mbrdi.anita.basic.util.ExecutorUtil;
import com.mbrdi.anita.user.model.User;

public class Email {

    public static void send(String subject, String content, String... recipients) {
        ExecutorUtil.executeNow(new MailJob(subject, content, recipients));
    }

	public static void send(String name, String subject, String body, String[] recipients) {
        String content = views.html.email.basic_template.render(name, body).body();
        ExecutorUtil.executeNow(new MailJob(subject, content, recipients));
	}

	public static void send(String name, String subject, String body, String recipient) {
        String content = views.html.email.basic_template.render(name, body).body();
        ExecutorUtil.executeNow(new MailJob(subject, content, recipient));
	}

	public static boolean sendSignupEmail(User user, String pass, String subject) {
		String content = views.html.email.password_change_template.render(user.name, pass).body();
        ExecutorUtil.executeNow(new MailJob(subject, content, user.email.split(",")));
        return true;
	}

	public static boolean sendForgotPasswordMail(User user, String pass, String subject) {
		String content = views.html.email.forgot_password_template.render(user, "uat.zoyride.com", pass).body();
        ExecutorUtil.executeNow(new MailJob(subject, content, user.email.split(",")));
        return true;
	}

}
