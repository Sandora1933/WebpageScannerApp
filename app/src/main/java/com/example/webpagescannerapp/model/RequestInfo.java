package com.example.webpagescannerapp.model;

public class RequestInfo {
    private String url;
    private int matchesCount;
    private String threadName;
    private Status status;

    public RequestInfo(String url, int matchesCount, String threadName, Status status) {
        this.url = url;
        this.matchesCount = matchesCount;
        this.threadName = threadName;
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public int getMatchesCount() {
        return matchesCount;
    }

    public String getThreadName() {
        return threadName;
    }

    public Status getStatus() {
        return status;
    }


}
