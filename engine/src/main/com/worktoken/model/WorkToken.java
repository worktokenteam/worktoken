package com.worktoken.model;


import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
public class WorkToken {
    private Map<String, Object> data;

    public Map<String, Object> getData() {
        if (data == null) {
            data = new HashMap<String, Object>();
        }
        return data;
    }
}
