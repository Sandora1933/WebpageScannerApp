package com.example.webpagescannerapp.model;

public class RequestInfo {
    private String url;
    private String matchesCountStr;
    private String threadName;
    private Status status;

    public RequestInfo(String url, String matchesCountStr, String threadName, Status status) {
        this.url = url;
        this.matchesCountStr = matchesCountStr;
        this.threadName = threadName;
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public String getMatchesCount() {
        return matchesCountStr;
    }

    public String getThreadName() {
        return threadName;
    }

    public Status getStatus() {
        return status;
    }


}
