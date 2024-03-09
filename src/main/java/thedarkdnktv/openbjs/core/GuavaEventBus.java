package thedarkdnktv.openbjs.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuavaEventBus extends EventBus implements IEventBus {

    private final static Logger LOG = LogManager.getLogger();
    private final static Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public GuavaEventBus() {
        super(GuavaEventBus::handleException);
    }

    private static void handleException(Throwable throwable, SubscriberExceptionContext context) {
        LOG.atError()
            .withThrowable(throwable)
            .log("Error during handle of event: {}", GSON.toJson(context.getEvent()));
    }
}
