package com.example.webpagescannerapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.webpagescannerapp.model.RequestInfo;
import com.example.webpagescannerapp.model.Status;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class SearchActivity extends AppCompatActivity {


    Retrofit retrofit;

    RecyclerView recyclerView;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_search);

        Intent intent = getIntent();

        String url = intent.getStringExtra("url");
        String text = intent.getStringExtra("text");
        int maxPagesNumber = Integer.parseInt(intent.getStringExtra("max_pages_number"));
        int threadsNumber = intent.getIntExtra("threads_number", 1);

        OkHttpClient okHttpClient = new OkHttpClient();

        // Check recycler view
        ArrayList<RequestInfo> requestList = new ArrayList<>();

        RequestAdapter requestAdapter = new RequestAdapter(this, requestList);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(requestAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Building tree
        ScannerService1 scanner1 = new ScannerService1(okHttpClient, url, maxPagesNumber);

        try{
            scanner1.fillMap(SearchActivity.this);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        LinkedHashMap<String, Integer> nMap = scanner1.getMap();

//        int count = 0;
//        for (Map.Entry<String, Integer> node : nMap.entrySet()){
//            infoTextView.append(node.getKey() + "\n");
//            count++;
//
//            if (count >= 12){
//                break;
//            }
//
//        }

        // Split into 3 maps
        //ArrayList<LinkedHashMap<String, Integer>> listOfMaps = splitMapIntoSmaller(nMap, 3);

        // Executor service (needed ThreadFactory to get names)
        //ThreadFactory  threadFactory = new NameableThreadFactory("listenerThread");
        final ExecutorService executorService = Executors.newFixedThreadPool(threadsNumber);

        //final ExecutorService executorService = Executors.newFixedThreadPool(threadsNumber);

        int counter = 0;
        for (Map.Entry<String, Integer> node : nMap.entrySet()){
            String currentUrl = node.getKey();
            Runnable worker = new MyRunnable(currentUrl, SearchActivity.this, requestAdapter,
                    recyclerView, text, requestList);
            executorService.execute(worker);
            counter++;
        }

        executorService.shutdown();
        // Wait until all threads are finish
//        while (!executorService.isTerminated()) {
//            // Waiting
//        }

//        executorService.execute(new ParserThread("[Thread]", SearchActivity.this,
//                    nMap, requestList, requestAdapter, text, recyclerView));
//        executorService.execute(new ParserThread("[Thread]", SearchActivity.this,
//                nMap, requestList, requestAdapter, text, recyclerView));

//        executorService.execute(new ParserRunnable("[Thread]", SearchActivity.this,
//                    nMap, requestList, requestAdapter, text, recyclerView));
//        executorService.execute(new ParserRunnable("[Thread]", SearchActivity.this,
//                nMap, requestList, requestAdapter, text, recyclerView));
//        executorService.execute(new ParserRunnable("[Thread]", SearchActivity.this,
//                nMap, requestList, requestAdapter, text, recyclerView));

        executorService.shutdown();


//        for (int i = 0; i < 3; i++){
//            String currentThreadName = "Thread" + i;
//            executorService.execute(new ParserThread(currentThreadName, SearchActivity.this,
//                    listOfMaps.get(i), requestList, requestAdapter, text, recyclerView));
//        }


//        // Printing tree
//        printMap(scanner1.getMap());
//        Toast.makeText(this, "size: " + scanner1.getMap().size(), Toast.LENGTH_SHORT).show();

        // Print map by all threads
        //ExecutorService executorService = Executors.newFixedThreadPool(threadsNumber);
//        ArrayList<LinkedHashMap<String, Integer>> listOfSmallMaps =
//                splitMapIntoSmaller(scanner1.getMap(), threadNumber);
//
        // Creating thread pool by Executor service
//        for (int i = 0; i < threadNumber; i++){
//            String currentThreadName = "Thread" + i;
//            executorService.execute(new ParserThread(currentThreadName, SearchActivity.this,
//                    infoTextView, listOfSmallMaps.get(i)));
//        }



        //executorService.shutdown();


    }

//    private void printMap(LinkedHashMap<String, Integer> map){
//        for (Map.Entry<String, Integer> node : map.entrySet()){
//            infoTextView.append("\n" + "Element: " + node.getKey() + "\n\t\t\t\t\t\t" + "lvl[" + node.getValue() + "]" + "\n");
//        }
//    }

    private void printMapWithThread(LinkedHashMap<String, Integer> smallMap){
        // Update recycler view with next element of small map
    }

//    private void printTree(ArrayList<AbstractMap.SimpleEntry<Integer, String>> list){
//        for (AbstractMap.SimpleEntry<Integer, String> node : list){
//            infoTextView.append("\n" + "Element: " + node.getValue() + "\n\t\t\t\t\t\t" + "lvl[" + node.getKey() + "]" + "\n");
//        }
//    }



    private ArrayList<Map.Entry<Integer, String>> getTree(String baseUrl, int maxSize, int level){
        final int[] elementCounter = new int[1];
        final int[] levelCounter = new int[1];
        elementCounter[0] = 0;
        levelCounter[0] = level;

        final ArrayList<Map.Entry<Integer, String>> resultTree = new ArrayList<>(maxSize);

        resultTree.add(new AbstractMap.SimpleEntry<>(levelCounter[0], baseUrl));
        elementCounter[0]++;
        levelCounter[0]++;

        new Thread(() -> {
            Document document = null;
            try {
                document = Jsoup.connect(baseUrl).get();
                Elements links = document.select("a[href]");
                for (Element el : links){
                    resultTree.add(new AbstractMap.SimpleEntry<>(levelCounter[0], el.attr("href")));
                    elementCounter[0]++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        return null;
    }

//    public void getMethod(String url, final CallBack callback){
//        Retrofit retrofit = new Retrofit.Builder().baseUrl(url)
//                .addConverterFactory(ScalarsConverterFactory.create())
//                .build();
//
//        RequestAPI projectApi = retrofit.create(RequestAPI.class);
//        Call<Integer> call = projectApi.getMethod(url);
//        call.enqueue(new Callback<Integer>() {
//            @Override
//            public void onResponse(Call<Integer> call, Response<Integer> response) {
//                String body = response.body();
//                callback.onSuccess(body);
//            }
//
//            @Override
//            public void onFailure(Call<Integer> call, Throwable t) {
//                callback.onFailer(call,t);
//            }
//        });
//    }

//    static final class PageAdapter implements Converter<ResponseBody, SecondClass.Page> {
//        static final Converter.Factory FACTORY = new Converter.Factory() {
//            @Override
//            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
//                if (type == SecondClass.Page.class) return new SecondClass.PageAdapter();
//                return null;
//            }
//        };
//
//        @Override
//        public SecondClass.Page convert(ResponseBody responseBody) throws IOException {
//            Document document = Jsoup.parse(responseBody.string());
//            Element value = document.select("script").get(1);
//            String content = value.html();
//            return new SecondClass.Page(content);
//        }
//    }

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


}
