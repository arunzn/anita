package com.mbrdi.anita.user.controller;

import com.mbrdi.anita.basic.controller.Application;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.message.model.Email;
import com.mbrdi.anita.user.dao.UserDAO;
import com.mbrdi.anita.user.model.User;
import org.apache.commons.codec.digest.DigestUtils;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class LoginController extends Controller {

    public static final String MESSAGE = "MESSAGE";
    public static final String ERROR = "ERROR";
    public static final String WARNING = "WARNING";

    @Inject
    FormFactory formFactory;

    public Result forgetPassword() {
        session().clear();
        return ok(views.html.user.forgetpassword.render());
    }

    public Result doForgetPassword() {

        DynamicForm requestData = formFactory.form().bindFromRequest();
        if (!Util.isNullOrEmpty(requestData.get("email"))) {
            User corpUser = null;
            try {
                corpUser = UserDAO.getByEmail(requestData.get("email"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (corpUser != null) {
                //corpUser.is_system_generated = true;
                String pass = UUID.randomUUID().toString();
                pass = pass.substring(0, 6).toUpperCase();
                corpUser.password = DigestUtils.md5Hex(pass);
                corpUser.is_system_generated = true;
                UserDAO.update(corpUser);
                Email.sendForgotPasswordMail(corpUser, pass, "AnITa Password");
                flash("MESSAGE", "Your password has been reset and sent to your email.");
                return redirect(com.mbrdi.anita.basic.controller.routes.Application.index());
            } else {
                flash(Application.ERROR, Messages.get("basic.error.invalidInformation"));
                return redirect(com.mbrdi.anita.basic.controller.routes.Application.index());
            }
        }
        return ok(views.html.home.index.render());
    }

    public Result login() {
        session().clear();

        String email = Util.getFormValue(request(), "email");
        String password = Util.getFormValue(request(), "password");

        if (!Util.isNullOrEmpty(email) && !Util.isNullOrEmpty(password)) {

            User user = UserDAO.getUserbyEmailPassword(email, DigestUtils.md5Hex(password));

            if (user != null && user.isCorpAdmin != null && user.isCorpAdmin) {

                session("id", user._id);
                session("user_id", user._id);
                session("name", user.name);

                if (user.email != null)
                    session("email", user.email);

                if (user.rights != null)
                    session("RIGHTS", user.rights);

                UserDAO.setLastLoginDate(user._id);

                if (user.is_system_generated != null && user.is_system_generated)
                    return redirect(com.mbrdi.anita.user.controller.routes.LoginController.changePassword());
                else if (user.isCorpAdmin != null && user.isCorpAdmin) {
                    final User localUser = UserDAO.get(session("user_id"));
                    return redirect(com.mbrdi.anita.dashboard.controller.routes.DashboardController.dashboard());
                } else
                    return redirect(com.mbrdi.anita.basic.controller.routes.Application.index());

            } else {
                flash("ERROR", Messages.get("basic.error.incorrectPasswordPhone"));
                return badRequest(views.html.home.index.render());
            }
        }
        return redirect(com.mbrdi.anita.basic.controller.routes.Application.index());

    }

    public Result logout() {
        session().clear();
        return redirect("/");
    }


    public Result changePassword() {
        String user_id = session("user_id");
        User user = UserDAO.get(user_id);
        if (user.is_system_generated != null && user.is_system_generated && (user.isCorpAdmin != null && user.isCorpAdmin))
            return ok(views.html.user.password_update.render());
        else
            return redirect(com.mbrdi.anita.dashboard.controller.routes.DashboardController.dashboard());
    }

    public Result doChangePassword() {
        String user_id = session("user_id");
        User user = UserDAO.get(user_id);
        if (user != null) {
            if (Util.getFormValue(request(), "new_password") != null
                    && Util.getFormValue(request(), "new_password").equals(Util.getFormValue(request(), "confirm_password"))) {
                String password = Util.getFormValue(request(), "new_password");
                user.password = DigestUtils.md5Hex(password);
                user.is_system_generated = null;
                UserDAO.updateHard(user);
                flash("MESSAGE", "Your password has been updated");
                return redirect(com.mbrdi.anita.dashboard.controller.routes.DashboardController.dashboard());
            } else {
                flash("ERROR", "Your passwords not matching.");
                return ok(views.html.user.password_update.render());
            }
        }
        return redirect(com.mbrdi.anita.basic.controller.routes.Application.index());
    }

}
