import com.mbrdi.anita.basic.security.Authorizer;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.lang.reflect.Method;
import java.util.concurrent.CompletionStage;


public class ActionCreator implements play.http.ActionCreator {
    @Override
    public Action createAction(Http.Request request, Method actionMethod) {
        return new Action.Simple() {
            @Override
            public CompletionStage<Result> call(Http.Context ctx) {
                if (!Authorizer.validateAccess(request, ctx.session(), actionMethod)) {
                    return F.Promise.<Result>pure(Results.notFound(views.html.basic.error404.render()));
                }
                return delegate.call(ctx);
            }
        };
    }
}