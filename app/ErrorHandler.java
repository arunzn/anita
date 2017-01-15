import play.Logger;
import play.http.HttpErrorHandler;
import play.mvc.*;
import play.mvc.Http.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ErrorHandler implements HttpErrorHandler {

    public CompletionStage<Result> onClientError(RequestHeader request, int statusCode, String message) {
        Logger.error(request.path() + "||" + message + "||" + request.queryString());
        System.out.println(message);
        return CompletableFuture.completedFuture(Results.notFound(views.html.basic.error404.render()));
    }

    public CompletionStage<Result> onServerError(RequestHeader request, Throwable exception) {
        Logger.error(exception.getMessage(), exception);
        exception.printStackTrace();
        return CompletableFuture.completedFuture(Results.notFound(views.html.basic.error404.render()));
    }
}