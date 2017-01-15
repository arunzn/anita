package com.mbrdi.anita.user.service;

import com.mbrdi.anita.basic.model.ValidationFailureException;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.message.model.Email;
import com.mbrdi.anita.user.dao.UserDAO;
import com.mbrdi.anita.user.model.AppUser;
import com.mbrdi.anita.user.model.User;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

public class UserService {

    public static void resetPassword(String user_id) {
        User user = UserDAO.get(user_id);
        if (user != null) {
            user.is_system_generated = true;
            String pass = UUID.randomUUID().toString();
            pass = pass.substring(0, 6).toUpperCase();
            user.password = DigestUtils.md5Hex(pass);
            Email.sendForgotPasswordMail(user, user.password, "Your Password is reset.");
            UserDAO.updateHard(user);
        }
    }

    public static void setPassword(String user_id, String password) {
        User user = UserDAO.get(user_id);
        if (user != null) {
            user.is_system_generated = null;
            user.password = DigestUtils.md5Hex(password);
            UserDAO.updateHard(user);
        }
    }

    public static void markVerifed(String user_id) {
        User user = UserDAO.get(user_id);
        if (user != null) {
            user.notVerified = null;
            UserDAO.updateHard(user);
        }
    }

    public static User addEmployee(User user) throws ValidationFailureException {
        if(user.email != null
                && ( user.email.toLowerCase().endsWith("@gmail.com") || user.email.toLowerCase().endsWith("@daimler.com"))){
            return UserDAO.save(user);
        }
        throw new ValidationFailureException("Invalid email id");
    }

    public static AppUser updateUser(AppUser user) {

        User userSaved = UserDAO.get(user._id);
        Util.hardCopy(user, userSaved);
        UserDAO.update(userSaved);

        return user;
    }
}
