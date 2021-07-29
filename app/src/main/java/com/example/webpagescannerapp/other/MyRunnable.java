package com.example.webpagescannerapp.other;

import android.app.Activity;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.RecyclerView;

import com.example.webpagescannerapp.adapter.RequestAdapter;
import com.example.webpagescannerapp.model.RequestInfo;
import com.example.webpagescannerapp.model.Status;

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

    String url;     // Url for search
    String textS;   // Text for search

    OkHttpClient okHttpClient;  // http client
    Retrofit retrofit;      // retrofit

    Activity activity;
    RequestAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<RequestInfo> adapterList;
    ProgressBar progressBar;

    public MyRunnable(String url, Activity activity, RequestAdapter adapter, RecyclerView recyclerView,
                      String textS, ArrayList<RequestInfo> adapterList, ProgressBar progressBar) {
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

    @Override
    public void run() {
        MyRunnable.RequestApi currentApi = retrofit.create(MyRunnable.RequestApi.class);
        Call<MyRunnable.Page> pageCall = currentApi.get(HttpUrl.parse(url));

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
            e.printStackTrace();
        }

        // Sleeping for 1.2 sec for convenient demonstration
        try {
            sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void initOkHttp(){
        okHttpClient = new OkHttpClient.Builder().build();
    }

    private void initRetrofit(){
        retrofit = new Retrofit.Builder()
                .baseUrl(HttpUrl.parse("https://www.bbc.com/"))
                .client(this.okHttpClient)
                .addConverterFactory(MyRunnable.PageAdapter.FACTORY)    // Http converter
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

    static class Page {
        String content;
        Page(String content) {
            this.content = content;
        }
    }

    // Custom PageAdapter for Retrofit Factory Converter
    static final class PageAdapter implements Converter<ResponseBody, MyRunnable.Page> {
        static final Converter.Factory FACTORY = new Converter.Factory() {
            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                if (type == MyRunnable.Page.class) return new MyRunnable.PageAdapter();
                return null;
            }
        };

        @Override
        public MyRunnable.Page convert(ResponseBody responseBody) throws IOException {
            Document document = Jsoup.parse(responseBody.string());
            Element body = document.body();
            String content = body.text();

            return new MyRunnable.Page(content);
        }
    }

    // Retrofit Api interface
    interface RequestApi {
        @GET
        Call<MyRunnable.Page> get(@Url HttpUrl url);
    }

}
