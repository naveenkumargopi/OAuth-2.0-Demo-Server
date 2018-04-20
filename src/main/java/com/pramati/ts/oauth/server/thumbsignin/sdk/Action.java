package com.pramati.ts.oauth.server.thumbsignin.sdk;

public enum Action {
    REGISTER("register"),
    AUTHENTICATE("authenticate"),
    STATUS("status"),
    GET_USER("getUser");

    private final String value;

    Action(String value) {
        this.value = value;
    }

    public static Action fromValue(String text) {
        String s = text.toLowerCase();
        for (Action b : Action.values()) {
            if (String.valueOf(b.value).equals(s)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
