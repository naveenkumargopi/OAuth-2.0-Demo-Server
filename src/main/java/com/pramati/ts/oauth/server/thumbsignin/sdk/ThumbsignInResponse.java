package com.pramati.ts.oauth.server.thumbsignin.sdk;

import java.io.IOException;
import java.util.Map;

public class ThumbsignInResponse {

    private int status;
    private Map data;

    public ThumbsignInResponse(int status, Map<String, Object> data) {
        this.status = status;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Object getValue(String key) {
        return data.get(key);
    }

    public String getDataAsString() throws IOException {
        return ThumbsignInClient.MAPPER.writeValueAsString(this.data);
    }
}
