package com.example.webpagescannerapp.other;

import android.app.Activity;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.RecyclerView;

import com.example.webpagescannerapp.adapter.RequestAdapter;
import com.example.webpagescannerapp.model.RequestInfo;
import com.example.webpagescannerapp.model.Status;
import com.example.webpagescannerapp.service.RequestService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Url;

import static java.lang.Thread.sleep;

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
