package com.example.webpagescannerapp.service;

import android.app.Activity;
import android.content.Context;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.webpagescannerapp.adapter.RequestAdapter;
import com.example.webpagescannerapp.model.RequestInfo;
import com.example.webpagescannerapp.model.Status;
import com.example.webpagescannerapp.other.MyRunnable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Url;

import static java.lang.Thread.sleep;

public class RequestService {

    String url;     // Url for search
    String textS;   // Text for search

    OkHttpClient okHttpClient;  // http client
    Retrofit retrofit;      // retrofit

    Activity activity;
    RequestAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<RequestInfo> adapterList;
    ProgressBar progressBar;

    public RequestService(String url, Activity activity, RequestAdapter adapter, RecyclerView recyclerView,
                          String textS, ArrayList<RequestInfo> adapterList, ProgressBar progressBar){
        this.url = url;
        this.activity = activity;
        this.adapter = adapter;
        this.recyclerView = recyclerView;
        this.textS = textS;
        this.adapterList = adapterList;
        this.progressBar = progressBar;
        initOkHttp();
        initRetrofit();
    }

    private void initOkHttp(){
        okHttpClient = new OkHttpClient.Builder().build();
    }

    private void initRetrofit(){
        retrofit = new Retrofit.Builder()
                .baseUrl(HttpUrl.parse("https://www.bbc.com/"))
                .client(this.okHttpClient)
                .addConverterFactory(RequestService.PageAdapter.FACTORY)    // Http converter
                .build();
    }

    // Get matches number from page text
    private String getMatchesNumberFromHtml(String html, String text){
        Pattern textPattern = Pattern.compile(text);
        Matcher countTextMatcher = textPattern.matcher(html);

        int count = 0;
        while (countTextMatcher.find()) {
            count++;
        }

        return String.valueOf(count);
    }

    public void launch() {
        RequestService.RequestApi currentApi = retrofit.create(RequestService.RequestApi.class);
        Call<RequestService.Page> pageCall = currentApi.get(HttpUrl.parse(url));

        try {
            String html;
            String matchesCountStr;
            String threadName;
            Status status;
            try {
                html = pageCall.clone().execute().body().content;
                matchesCountStr = getMatchesNumberFromHtml(html, textS);
                threadName = Thread.currentThread().getName();
                status = (Integer.parseInt(matchesCountStr) > 0)? Status.STATUS_FOUND : Status.STATUS_NOT_FOUND;
            }
            catch (Exception ex){
                html = ex.getMessage();
                matchesCountStr = ex.getMessage();
                threadName = Thread.currentThread().getName();
                status = Status.STATUS_ERROR;
            }

            // Build RequestInfo object and add to recyclerView (via UI thread)
            RequestInfo requestInfo = new RequestInfo(url, matchesCountStr, threadName, status);

            activity.runOnUiThread(() -> {
                adapterList.add(requestInfo);
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                progressBar.incrementProgressBy(1);
            });

        } catch (Exception e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Sleeping for 1.2 sec for convenient demonstration
        try {
            sleep(1200);
        } catch (InterruptedException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    static class Page {
        String content;
        Page(String content) {
            this.content = content;
        }
    }

    // Custom PageAdapter for Retrofit Factory Converter
    static final class PageAdapter implements Converter<ResponseBody, RequestService.Page> {
        static final Converter.Factory FACTORY = new Converter.Factory() {
            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                if (type == RequestService.Page.class) return new RequestService.PageAdapter();
                return null;
            }
        };

        @Override
        public RequestService.Page convert(ResponseBody responseBody) throws IOException {
            Document document = Jsoup.parse(responseBody.string());
            Element body = document.body();
            String content = body.text();

            return new RequestService.Page(content);
        }
    }

    // Retrofit Api interface
    interface RequestApi {
        @GET
        Call<RequestService.Page> get(@Url HttpUrl url);
    }

}
