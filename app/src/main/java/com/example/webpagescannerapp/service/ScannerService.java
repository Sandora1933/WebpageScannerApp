package com.example.webpagescannerapp.service;

import android.app.Activity;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ScannerService {

    // This service collects HashMap of pages that we should pass (number entered by user)
    // Recursive method that collects url's by Breadth-Search-Tree method

    private OkHttpClient client;    // http client
    private LinkedHashMap<String, Integer> map;     // Our main map with data

    private String baseUrl, currentUrl;
    private int maxLinkNumber, currentLinkNumber;
    private int currentLevel;

    public ScannerService(OkHttpClient client, String myBaseUrl, int myMaxLinksNumber){
        this.client = client;
        this.baseUrl = myBaseUrl;
        this.maxLinkNumber = myMaxLinksNumber;
        currentUrl = myBaseUrl;
        this.currentLinkNumber = 0;
        map = new LinkedHashMap<>(myMaxLinksNumber);
    }

    // Map Getter
    public LinkedHashMap<String, Integer> getMap() {
        return map;
    }

    // Document Getter
    private Document getDocument() throws IOException {
        Request request = new Request.Builder()
                .url(currentUrl)
                .get().build();

        return Jsoup.parse(client.newCall(request).execute().body().string());
    }

    // Recursive function for collecting data into map
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void fillMap(Activity activity) throws IOException, InterruptedException {

        //Assume that maxLinkNumber >= 1

        // Lvl 0 processing
        if (currentLevel == 0){
            map.put(baseUrl, 0);
            currentLinkNumber++;
            currentLevel++;

            // If links processed number < max links then go next
            if (currentLinkNumber < maxLinkNumber){
                // Recursive call
                fillMap(activity);
            }
            else {
                return;
            }

        }   // Lvl 1 processing
        else if (currentLevel == 1){
            final Elements[] links = new Elements[1];
            final int[] fullSize = new int[1];
            Runnable r = () -> {
                try {
                    links[0] = getDocument().select("a[href^=http]"); // start with http
                    fullSize[0] = links[0].size();
                    activity.runOnUiThread(() -> Toast.makeText(activity, "fullSize :" + fullSize[0], Toast.LENGTH_SHORT)
                            .show());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };

            Thread t = new Thread(r);
            t.start();
            t.join();

            for (Element link : links[0]){
                if (currentLinkNumber < maxLinkNumber){
                    // Avoid repeated urls
                    if (!map.containsKey(link.attr("href"))){
                        map.put(link.attr("href"), currentLevel);
                        currentLinkNumber++;
                    }
                }
                else {
                    return;
                }
            }
            currentLevel++;

            // If links processed number < max links then go next
            if (currentLinkNumber < maxLinkNumber){
                // Recursive call
                fillMap(activity);
            }
            else {
                return;
            }
        }
        else {  // Process level 2+
            for (int j = 0; j < map.entrySet().stream().filter(node ->
                    node.getValue() == currentLevel-1).count(); j++){

                LinkedHashMap<String, Integer> mapOfCurrentLevel = map.entrySet().stream()
                        .filter(node -> node.getValue() == currentLevel-1)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));

                for (Map.Entry<String, Integer> node : mapOfCurrentLevel.entrySet()){
                    // Current url assign to entry with value of first match with url of current lvl
                    currentUrl = node.getKey();

                    final Elements[] linksOfNode = new Elements[1];
                    Runnable r = () -> {
                        try {
                            linksOfNode[0] = getDocument().select("a[href^=http]"); // start with http
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    };
                    Thread t = new Thread(r);
                    t.start();
                    t.join();

                    for (Element link : linksOfNode[0]){
                        if (currentLinkNumber < maxLinkNumber){
                            // Avoid repeated urls
                            if (!map.containsKey(link.attr("href"))){
                                map.put(link.attr("href"), currentLevel);
                                currentLinkNumber++;
                            }
                        }
                        else {
                            return;
                        }
                    }
                }
                currentLevel++;

                // If links processed number < max links then go next
                if (currentLinkNumber < maxLinkNumber){
                    // Recursive call
                    fillMap(activity);
                }
                else {
                    return;
                }

            }
        }


    }

}
