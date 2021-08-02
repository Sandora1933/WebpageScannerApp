package com.example.webpagescannerapp.other;

import com.example.webpagescannerapp.service.RequestService;

public class MyRunnable implements Runnable {

    RequestService requestService;

    public MyRunnable(RequestService requestService){
        this.requestService = requestService;
    }

    @Override
    public void run() {
        requestService.launch();
    }

}
