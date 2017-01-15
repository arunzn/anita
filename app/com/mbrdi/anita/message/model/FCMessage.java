package com.mbrdi.anita.message.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by AJ on 11/4/2016.
 */
public class FCMessage {
    public String to;
    public Map<String, Notification> data = new HashMap<>();
}
