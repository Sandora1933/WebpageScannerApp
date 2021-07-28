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

    private OkHttpClient client;
    //private ArrayList<AbstractMap.SimpleEntry<Integer, String>> list;
    private LinkedHashMap<String, Integer> map;

    private String baseUrl, currentUrl;
    private int maxLinkNumber, currentLinkNumber;
    private int currentLevel;

    public ScannerService(OkHttpClient client, String myBaseUrl, int myMaxLinksNumber){
        this.client = client;
        this.baseUrl = myBaseUrl;
        currentUrl = myBaseUrl;
        this.maxLinkNumber = myMaxLinksNumber;
        this.currentLinkNumber = 0;
        //list = new ArrayList<>(maxLinkNumber);
        map = new LinkedHashMap<>(myMaxLinksNumber);
    }

//    public ArrayList<AbstractMap.SimpleEntry<Integer, String>> getList() {
//        return list;
//    }

    public LinkedHashMap<String, Integer> getMap() {
        return map;
    }

    private Document getDocument() throws IOException {
        Request request = new Request.Builder()
                .url(currentUrl)
                .get().build();

        //return Jsoup.connect(currentUrl).timeout(0).get();
        return Jsoup.parse(client.newCall(request).execute().body().string());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void fillMap(Activity activity) throws IOException, InterruptedException {

        //Assume that maxLinkNumber >= 1

        // Lvl 0 processing
        if (currentLevel == 0){
            //map.add(new AbstractMap.SimpleEntry<>(currentLevel, baseUrl)); // Adding baseUrl(lvl 0)
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
                    //links[0] = getDocument().select("a[href]");
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

                //final Elements[] links = new Elements[1];
                //final int[] fullSize = new int[1];
//                Runnable r = () -> {
//                    try {
//                        links[0] = getDocument().select("a[href]");
//                        //fullSize[0] = links[0].size();
////                        activity.runOnUiThread(() -> Toast.makeText(activity, "fullSize :" + fullSize[0], Toast.LENGTH_SHORT)
////                                .show());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                };
//                Thread t = new Thread(r);
//                t.start();
//                t.join();


                for (Map.Entry<String, Integer> node : mapOfCurrentLevel.entrySet()){
                    // Current url assign to entry with value of first match with url of current lvl
                    currentUrl = node.getKey();

                    final Elements[] linksOfNode = new Elements[1];
                    Runnable r = () -> {
                        try {
                            //linksOfNode[0] = getDocument().select("a[href]");
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
