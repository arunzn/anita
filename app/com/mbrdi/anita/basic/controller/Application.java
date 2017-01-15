package com.mbrdi.anita.basic.controller;

import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.message.model.ACTION;
import com.mbrdi.anita.message.model.Notification;
import com.mbrdi.anita.message.service.MessageService;
import com.mbrdi.anita.user.dao.UserDAO;
import com.mbrdi.anita.user.model.User;
import org.apache.commons.codec.digest.DigestUtils;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by AJ on 11/10/2016.
 */
public class Application extends Controller {

    public static final String MESSAGE = "MESSAGE" ;
    public static final String ERROR = "ERROR" ;
    public static final String WARNING = "WARNING" ;


    @Inject
    FormFactory formFactory;

    public Result index() {
        session().clear();
        return ok(views.html.home.index.render());
    }

    public Result test(String did) {
//        String did = "cFbeh1EJkdQ:APA91bFiho5BIBZKkNCsx9o2Ec4gOeBc5TVAZvJUpQIHn1s-0YZ2gCUK9XzH65Upby8T8I8Qe21x_8apIn2dGtcawe3NDsSYgDlL2fFTVBx3egJzw4PP_gFyQ_U7M8d4D1bKPf5gpACd";
        Notification n = new Notification();
        n.action = ACTION.UPDATE_PROFILE;
        MessageService.sendFireBaseMessage(did, n);
        return ok("success");
    }
}
