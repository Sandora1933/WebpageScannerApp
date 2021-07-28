package com.example.webpagescannerapp;

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

    String url;
    String textS;

    OkHttpClient okHttpClient;
    Retrofit retrofit;

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
//            String html;
//            if (pageCall.execute().body() != null){ // To avoid already-executed
//                html = pageCall.execute().body().content;
//            }
//            else {
//                html = "Exception: NULL";
//            }
            String html;
            try {
                html = pageCall.clone().execute().body().content;
            }
            catch (NullPointerException ex){
                html = ex.getMessage();
            }

            int matchesCount = getMatchesNumberFromHtml(html, textS);
            //int threadNumber = Integer.parseInt(String.valueOf(this.getName().toCharArray()[this.getName().toCharArray().length-1]));
            String threadName = Thread.currentThread().getName();
            Status status = Status.STATUS_FOUND;

            RequestInfo requestInfo = new RequestInfo(url, matchesCount, threadName, status);

            activity.runOnUiThread(() -> {
                adapterList.add(requestInfo);
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                progressBar.incrementProgressBy(1);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void initOkHttp(){
        okHttpClient = new OkHttpClient.Builder()
                //.connectionPool(new ConnectionPool(100, 30, TimeUnit.SECONDS))
                .build();
    }

    private void initRetrofit(){
        retrofit = new Retrofit.Builder()
                .baseUrl(HttpUrl.parse("https://www.bbc.com/"))
                .client(this.okHttpClient)
                .addConverterFactory(MyRunnable.PageAdapter.FACTORY)
                .build();
    }

    private int getMatchesNumberFromHtml(String html, String text){
        Pattern textPattern = Pattern.compile(text);
        Matcher countTextMatcher = textPattern.matcher(html);

        int count = 0;
        while (countTextMatcher.find()) {
            count++;
        }

        return count;
    }

    static class Page {
        String content;
        Page(String content) {
            this.content = content;
        }
    }

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

    interface RequestApi {
        @GET
        Call<MyRunnable.Page> get(@Url HttpUrl url);
    }

}
