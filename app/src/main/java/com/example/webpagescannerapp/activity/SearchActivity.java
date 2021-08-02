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

import com.example.webpagescannerapp.databinding.ActivitySearchBinding;
import com.example.webpagescannerapp.other.MyRunnable;
import com.example.webpagescannerapp.R;
import com.example.webpagescannerapp.service.RequestService;
import com.example.webpagescannerapp.service.ScannerService;
import com.example.webpagescannerapp.adapter.RequestAdapter;
import com.example.webpagescannerapp.model.RequestInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;

public class SearchActivity extends AppCompatActivity {

    ActivitySearchBinding binding;

    LinkedHashMap<String, Integer> nMap;    // Map from ScannerService

    //  Values for current iteration
    String text;
    int threadsNumber;

    ExecutorService executorService;    // Executor service for several threads execution
    List<Runnable> terminatedWorkersList;   // When process paused (For executor.shutDownNow())

    RequestAdapter requestAdapter;      // Adapter for RecyclerView
    ArrayList<RequestInfo> requestList;     // List for RecyclerView

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();

        binding = ActivitySearchBinding.inflate(getLayoutInflater());
//        setContentView(R.layout.activity_search);
        setContentView(binding.getRoot());

        //initViews();
        initControlPanel();

        Intent intent = getIntent();

        // Getting data from intent
        String url = intent.getStringExtra("url");
        text = intent.getStringExtra("text");
        int maxPagesNumber = Integer.parseInt(intent.getStringExtra("max_pages_number"));
        threadsNumber = intent.getIntExtra("threads_number", 1);

        // Setting http client
        OkHttpClient okHttpClient = new OkHttpClient();

        // Setting recycler view
        setUpRecyclerView();

        // Building tree of url's
        ScannerService scanner1 = new ScannerService(okHttpClient, url, maxPagesNumber);

        try{
            scanner1.fillMap(SearchActivity.this);
        } catch (IOException | InterruptedException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Setting progress bar (horizontal)
        setUpProgressBar(scanner1);

        // Getting map from ScannerService
        nMap = scanner1.getMap();

        // Creating and launching ExecutorService
        executorService = Executors.newFixedThreadPool(threadsNumber);
        launchExecutor();
    }

//    public void initViews(){
//        progressBar = findViewById(R.id.progressBar);
//        pauseButton = findViewById(R.id.pauseButton);
//        playButton = findViewById(R.id.playButton);
//        stopButton = findViewById(R.id.stopButton);
//        recyclerView = findViewById(R.id.recyclerView);
//    }

    public void initControlPanel(){
        binding.playButton.setEnabled(false);
        binding.playButton.setAlpha(0.5f);

        binding.pauseButton.setEnabled(true);
        binding.pauseButton.setAlpha(1.0f);

        binding.stopButton.setEnabled(true);
        binding.stopButton.setAlpha(1.0f);
    }

    public void setUpRecyclerView(){
        requestList = new ArrayList<>();

        requestAdapter = new RequestAdapter(this, requestList);
        binding.recyclerView.setAdapter(requestAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
    }

    public void setUpProgressBar(ScannerService scanner){
        binding.progressBar.setMin(0);
        binding.progressBar.setMax(scanner.getMap().size());
        binding.progressBar.setProgress(0);
    }

    private void switchPlayPauseButtons(boolean isPlaying){
        if (isPlaying){
            binding.playButton.setEnabled(false);
            binding.playButton.setAlpha(0.5f);
            binding.pauseButton.setEnabled(true);
            binding.pauseButton.setAlpha(1.0f);
        }
        else {
            binding.playButton.setEnabled(true);
            binding.playButton.setAlpha(1.0f);
            binding.pauseButton.setEnabled(false);
            binding.pauseButton.setAlpha(0.5f);
        }
    }

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
        binding.stopButton.setAlpha(0.5f);
        binding.stopButton.setEnabled(false);
        binding.pauseButton.setAlpha(0.5f);
        binding.pauseButton.setEnabled(false);
    }

    private void pauseExecutor(){
        terminatedWorkersList = executorService.shutdownNow();
    }

    private void resumeExecutor(){
        launchExecutor();
    }

    private void launchExecutor(){

        // Start from paused_state (if conditions are unsatisfied then start_state)
        if (terminatedWorkersList != null && terminatedWorkersList.size() > 0){

            executorService = Executors.newFixedThreadPool(threadsNumber);

            for (Runnable r : terminatedWorkersList){
                executorService.execute(r);
            }
        }

        //Start or continue executing tasks
        for (Map.Entry<String, Integer> node : nMap.entrySet()){
            String currentUrl = node.getKey();

//            Runnable worker = new MyRunnable(currentUrl, SearchActivity.this, requestAdapter,
//                    recyclerView, text, requestList, progressBar);

            RequestService requestService = new RequestService(currentUrl, SearchActivity.this,
                    requestAdapter, binding.recyclerView, text, requestList, binding.progressBar);

            MyRunnable worker = new MyRunnable(requestService);
            executorService.execute(worker);

            // Remove this node via stream() -> (to maintain resume_state)
            LinkedHashMap<String, Integer> newMap = nMap.entrySet()
                    .stream()
                    .filter(e -> !e.getKey().equals(currentUrl))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
            nMap = newMap;
        }

        executorService.shutdown();
    }

}
