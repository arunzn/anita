package com.mbrdi.anita.basic.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExecutorUtil {

    private static Executor executor = Executors.newFixedThreadPool(20);

    public static void executeNow(Runnable runnable) {
        if(runnable != null)
            executor.execute(runnable);
//            Akka.system().scheduler().scheduleOnce(Duration.Zero(), runnable, Akka.system().dispatcher());
    }
}
