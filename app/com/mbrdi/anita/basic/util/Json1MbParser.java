package com.mbrdi.anita.basic.util;

import play.http.HttpErrorHandler;
import play.mvc.BodyParser;

import javax.inject.Inject;

/**
 * Accept only 1MB of json data.
 */
public class Json1MbParser extends BodyParser.Json {
    @Inject
    public Json1MbParser(HttpErrorHandler errorHandler) {
        super(1024 * 1024, errorHandler);
    }
}
