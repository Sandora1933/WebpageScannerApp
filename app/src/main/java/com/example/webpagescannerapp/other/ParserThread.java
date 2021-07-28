package com.example.webpagescannerapp.other;

import android.app.Activity;

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
import java.util.LinkedHashMap;
import java.util.Map;
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

public class ParserThread extends Thread {
    String name;
    Activity activity;
    LinkedHashMap<String, Integer> smallMap;
    OkHttpClient okHttpClient;
    Retrofit retrofit;

    ArrayList<RequestInfo> adapterList;
    RequestAdapter adapter;
    RecyclerView recyclerView;

    String textS;

    public ParserThread(String name, Activity activity, LinkedHashMap<String,
            Integer> smallMap, ArrayList<RequestInfo> adapterList, RequestAdapter adapter,
                        String textS, RecyclerView recyclerView){
        //this.setName(name);
        this.name = name;
        this.activity = activity;
        this.smallMap = smallMap;
        initOkHttp();
        initRetrofit();
        this.adapter = adapter;
        this.adapterList = adapterList;
        this.textS = textS;
        this.recyclerView = recyclerView;
    }

    public String getTName(){return name;}

    private void initOkHttp(){
        okHttpClient = new OkHttpClient.Builder()
                //.connectionPool(new ConnectionPool(100, 30, TimeUnit.SECONDS))
                .build();
    }

    private void initRetrofit(){
        retrofit = new Retrofit.Builder()
                .baseUrl(HttpUrl.parse("https://www.bbc.com/"))
                .client(this.okHttpClient)
                .addConverterFactory(PageAdapter.FACTORY)
                .build();
    }

    @Override
    public void run() {

        for (Map.Entry<String, Integer> node : smallMap.entrySet()){
            RequestApi currentApi = retrofit.create(RequestApi.class);
            Call<ParserThread.Page> pageCall = currentApi.get(HttpUrl.parse(node.getKey()));
            try {
                String html = pageCall.execute().body().content;

                String url = node.getKey();
                int matchesCount = getMatchesNumberFromHtml(html, textS);
                //int threadNumber = Integer.parseInt(String.valueOf(this.getName().toCharArray()[this.getName().toCharArray().length-1]));
                String threadName = this.toString();
                Status status = Status.STATUS_FOUND;

                RequestInfo requestInfo = new RequestInfo(url, matchesCount, threadName, status);

                activity.runOnUiThread(() -> {
                    adapterList.add(requestInfo);
                    adapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

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

    static final class PageAdapter implements Converter<ResponseBody, ParserThread.Page> {
        static final Converter.Factory FACTORY = new Converter.Factory() {
            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                if (type == ParserThread.Page.class) return new ParserThread.PageAdapter();
                return null;
            }
        };

        @Override
        public ParserThread.Page convert(ResponseBody responseBody) throws IOException {
            Document document = Jsoup.parse(responseBody.string());
            Element body = document.body();
            String content = body.text();

            return new ParserThread.Page(content);
        }
    }

    interface RequestApi {
        @GET
        Call<ParserThread.Page> get(@Url HttpUrl url);
    }
}
