import com.google.inject.AbstractModule;
import play.Configuration;
import play.Environment;

public class Module extends AbstractModule /*extends GlobalSettings*/ {

    private final Environment environment;
    private final Configuration configuration;
    public static boolean IS_DEV = true;

    public Module(Environment environment, Configuration configuration) {
        this.environment = environment;
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
    }
}
