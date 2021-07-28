package com.example.webpagescannerapp.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.webpagescannerapp.MyRunnable;
import com.example.webpagescannerapp.R;
import com.example.webpagescannerapp.service.ScannerService;
import com.example.webpagescannerapp.adapter.RequestAdapter;
import com.example.webpagescannerapp.model.RequestInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class SearchActivity extends AppCompatActivity {

    LinkedHashMap<String, Integer> nMap;
    String urlOfResume;

    String text;
    int threadsNumber;

    RecyclerView recyclerView;
    ProgressBar progressBar;

    // Control panel buttons
    ImageButton pauseButton, playButton, stopButton;

    ExecutorService executorService;
    List<Runnable> terminatedWorkersList;

    RequestAdapter requestAdapter;
    ArrayList<RequestInfo> requestList;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_search);

        progressBar = findViewById(R.id.progressBar);

        pauseButton = findViewById(R.id.pauseButton);
        playButton = findViewById(R.id.playButton);
        stopButton = findViewById(R.id.stopButton);

        playButton.setEnabled(false);
        playButton.setAlpha(0.5f);

        Intent intent = getIntent();

        String url = intent.getStringExtra("url");
        urlOfResume = url;

        text = intent.getStringExtra("text");
        int maxPagesNumber = Integer.parseInt(intent.getStringExtra("max_pages_number"));
        threadsNumber = intent.getIntExtra("threads_number", 1);

        OkHttpClient okHttpClient = new OkHttpClient();

        // Check recycler view
        requestList = new ArrayList<>();

        requestAdapter = new RequestAdapter(this, requestList);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(requestAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Building tree
        ScannerService scanner1 = new ScannerService(okHttpClient, url, maxPagesNumber);

        try{
            scanner1.fillMap(SearchActivity.this);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // Init progress bar
        progressBar.setMin(0);
        progressBar.setMax(scanner1.getMap().size());
        progressBar.setProgress(0);

        // Get map from ScannerService
        nMap = scanner1.getMap();

        // Creating ExecutorService
        executorService = Executors.newFixedThreadPool(threadsNumber);
        launchExecutor();


//        for (Map.Entry<String, Integer> node : nMap.entrySet()){
//            String currentUrl = node.getKey();
//            Runnable worker = new MyRunnable(currentUrl, SearchActivity.this, requestAdapter,
//                    recyclerView, text, requestList, progressBar);
//            executorService.execute(worker);
//        }
//
//        executorService.shutdown();




        // Wait until all threads are finish
//        while (!executorService.isTerminated()) {
//            // Waiting
//        }

    }

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

    private void switchPlayPauseButtons(boolean isPlaying){
        if (isPlaying){
            playButton.setEnabled(false);
            playButton.setAlpha(0.5f);
            pauseButton.setEnabled(true);
            pauseButton.setAlpha(1.0f);
        }
        else {
            playButton.setEnabled(true);
            playButton.setAlpha(1.0f);
            pauseButton.setEnabled(false);
            pauseButton.setAlpha(0.5f);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void playButtonClicked(View view) {
        switchPlayPauseButtons(true);
        resumeExecutor();
    }

    public void pauseButtonClicked(View view) {
        switchPlayPauseButtons(false);
        pauseExecutor();
    }

    public void stopButtonClicked(View view){
        executorService.shutdownNow();
        Toast.makeText(this, "FULL STOP", Toast.LENGTH_SHORT).show();
        stopButton.setAlpha(0.5f);
        stopButton.setEnabled(false);
        pauseButton.setAlpha(0.5f);
        pauseButton.setEnabled(false);
    }

    private void pauseExecutor(){
        terminatedWorkersList = executorService.shutdownNow();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void resumeExecutor(){
        launchExecutor();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void launchExecutor(){

        // Start from paused_state
        if (terminatedWorkersList != null && terminatedWorkersList.size() > 0){

            executorService = Executors.newFixedThreadPool(threadsNumber);

            for (Runnable r : terminatedWorkersList){
                executorService.execute(r);
            }
        }

        //Start or continue executing tasks
        for (Map.Entry<String, Integer> node : nMap.entrySet()){
            String currentUrl = node.getKey();

            Runnable worker = new MyRunnable(currentUrl, SearchActivity.this, requestAdapter,
                    recyclerView, text, requestList, progressBar);
            executorService.execute(worker);

            // Remove this node (to maintain resume_state)
            //nMap.remove(node.getKey());
            LinkedHashMap<String, Integer> newMap = nMap.entrySet()
                    .stream()
                    .filter(e -> !e.getKey().equals(currentUrl))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
            nMap = newMap;
        }

        executorService.shutdown();
    }

}
