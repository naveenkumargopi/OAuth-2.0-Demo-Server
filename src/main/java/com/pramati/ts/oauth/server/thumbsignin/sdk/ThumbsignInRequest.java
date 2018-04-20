package com.pramati.ts.oauth.server.thumbsignin.sdk;

import java.util.TreeMap;

public class ThumbsignInRequest {

    private final TreeMap<String, String> headers;

    private final TreeMap<String, String> queryParams;

    private Action action;

    private String transactionId;

    public ThumbsignInRequest(Action action) {
        this.action = action;
        this.headers = new TreeMap<>();
        this.queryParams = new TreeMap<>();
    }

    public TreeMap<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String key, String value) {
        if (key != null && value != null) {
            this.headers.put(key, value);
        }
    }

    public TreeMap<String, String> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String key) {
        return queryParams.get(key);
    }

    public void addQueryParam(String key, String value) {
        if (key != null && value != null) {
            this.queryParams.put(key, value);
        }
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
