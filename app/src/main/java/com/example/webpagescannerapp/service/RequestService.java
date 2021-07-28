package com.example.webpagescannerapp.service;

import android.app.Activity;
import android.content.Context;

import com.example.webpagescannerapp.model.RequestInfo;

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
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.http.GET;

public class RequestService {

    Dispatcher okHttpDispatcher;
    OkHttpClient okHttpClient;
    Retrofit retrofit;

    Context context;
    Activity activity;

    public RequestService(){
        initOkHttpDispatcher();
        initOkHttp();
        initRetrofit();
    }

    private void initOkHttpDispatcher(){
        okHttpDispatcher = new Dispatcher(Executors.newFixedThreadPool(20));
        okHttpDispatcher.setMaxRequests(20);
        okHttpDispatcher.setMaxRequestsPerHost(1);
    }

    private void initOkHttp(){
        okHttpClient = new OkHttpClient.Builder()
                .dispatcher(this.okHttpDispatcher)
                .connectionPool(new ConnectionPool(100, 30, TimeUnit.SECONDS))
                .build();
    }

    private void initRetrofit(){
        retrofit = new Retrofit.Builder()
                .baseUrl("")
                .client(this.okHttpClient)
                .addConverterFactory(RequestService.PageAdapter.FACTORY)
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

    private ArrayList<LinkedHashMap<String, Integer>> splitMapIntoSmaller(LinkedHashMap<String, Integer> map, int number){
        ArrayList<LinkedHashMap<String, Integer>> listOfSmallMaps = new ArrayList<>(number);

        for (int i = 0; i < number; i++){
            listOfSmallMaps.add(new LinkedHashMap<>());
        }

        int iteration = 0;
        for (Map.Entry<String, Integer> node : map.entrySet()){
            int where = iteration % number; // where - is map number
            listOfSmallMaps.get(where).put(node.getKey(), node.getValue());
            iteration++;
        }

        return listOfSmallMaps;
    }

    private void launch(LinkedHashMap<String, Integer> fullMap){

        int threadsNumber = 3;

        // Splitting fullMap into several maps and run by different Threads
        ArrayList<LinkedHashMap<String, Integer>> listOfMaps = splitMapIntoSmaller(fullMap, threadsNumber);

        // Building executor service
        ExecutorService executorService = Executors.newFixedThreadPool(threadsNumber);

        // Launching threadNumber number of Threads (via ExecutorService.execute())
//        for (int k = 0; k < threadsNumber; k++){
//            executorService.execute(new ParserThread("Thread" + k + 1, activity,
//                    activity.findViewById(R.id.infoTextView), listOfMaps.get(k)));
//        }

        executorService.shutdown();



        RequestApi requestApi = retrofit.create(RequestApi.class);
        //List<Observable<List<String>>> listOfObservables = new ArrayList<>(5);
        //List<Observable<List<String>>> listOfObservables2 = new ArrayList<>(fullMap.size());



        //Observable<List<String>> obsList1 = new Observable<List<String>>();

//        for (Map.Entry<String, Integer> node : fullMap.entrySet()){
//            listOfObservables1.add(requestApi.getHtml(node.getKey()));
//            listOfObservables2.add(requestApi.getHtml(node.getKey()));
//        }





//        for (Map.Entry<String, Integer> node : fullMap.entrySet()){
//            RequestApi currentApi = retrofit.create(RequestApi.class);
//            Call<String> pageCall = currentApi.get(node.getKey());
//
//            pageCall.enqueue(new Callback<String>() {
//                @Override
//                public void onResponse(Call<String> call, Response<String> response) {
//                    String currentUrl = node.getKey();
//                    int matchesNumber = getMatchesNumberFromHtml(response.body(), "text");
//                }
//
//                @Override
//                public void onFailure(Call<String> call, Throwable t) {
//
//                }
//            });
//        }

    }

    static final class PageAdapter implements Converter<ResponseBody, String> {
        static final Converter.Factory FACTORY = new Converter.Factory() {
            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                if (type == RequestInfo.class) return new RequestService.PageAdapter();
                return null;
            }
        };

        @Override
        public String convert(ResponseBody responseBody) throws IOException {
            Document document = Jsoup.parse(responseBody.string());
            Element value = document.select("script").get(1);
            return value.html();
        }
    }

    interface RequestApi {
        @GET(".")
        Observable<String> getHtml(String url);
    }

}
