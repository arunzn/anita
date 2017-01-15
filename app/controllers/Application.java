package controllers;


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

    public Result index() {
        
        return ok("Hello World");
    }
}
