package com.mycompany;

import lombok.Getter;

public class Attls {

    private static final ThreadLocal<AttlsInfo> threadToAttls = new ThreadLocal<>();

    static {
        System.loadLibrary("attls");
    }

    public static void init(int socketId) {
        threadToAttls.set(new AttlsInfo(socketId));
    }

    public static AttlsInfo get() {
        return threadToAttls.get();
    }

    public static void dispose() {
        threadToAttls.remove();
    }

    @Getter
    public static class AttlsInfo {

        private final int socketId;
        private boolean secured;
        private String userId;

        AttlsInfo(int socketId) {
            this.socketId = socketId;
            fetchData(socketId);
        }

        private native void fetchData(int socketId);

    }

}
