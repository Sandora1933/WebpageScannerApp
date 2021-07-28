package com.example.webpagescannerapp;

import android.app.Activity;
import android.content.Context;
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
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ScannerService {

    private OkHttpClient client;
    private ArrayList<AbstractMap.SimpleEntry<Integer, String>> list;

    private String baseUrl, currentUrl;
    private int maxLinkNumber, currentLinkNumber;
    private int currentLevel;

    public ScannerService(OkHttpClient client, String myBaseUrl, int myMaxLinksNumber){
        this.client = client;
        this.baseUrl = myBaseUrl;
        currentUrl = myBaseUrl;
        this.maxLinkNumber = myMaxLinksNumber;
        list = new ArrayList<>(maxLinkNumber);
    }

    public ArrayList<AbstractMap.SimpleEntry<Integer, String>> getList() {
        return list;
    }

    private Document getDocument() throws IOException {
        Request request = new Request.Builder()
                .url(currentUrl)
                .get().build();

        //return Jsoup.connect(currentUrl).timeout(0).get();
        return Jsoup.parse(client.newCall(request).execute().body().string());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void fillList(Activity activity) throws IOException, InterruptedException {

        //Assume that maxLinkNumber >= 1

        // Lvl 0 processing
        if (currentLevel == 0){
            list.add(new AbstractMap.SimpleEntry<>(currentLevel, baseUrl)); // Adding baseUrl(lvl 0)
            currentLinkNumber++;
            currentLevel++;

            // If links processed number < max links then go next
            if (currentLinkNumber < maxLinkNumber){
                // Recursive call
                fillList(activity);
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
                    links[0] = getDocument().select("a[href]");
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
                    list.add(new AbstractMap.SimpleEntry<>(currentLevel, link.attr("href")));
                    currentLinkNumber++;
                }
                else {
                    return;
                }
            }
            currentLevel++;

            // If links processed number < max links then go next
            if (currentLinkNumber < maxLinkNumber){
                // Recursive call
                fillList(activity);
            }
            else {
                return;
            }
        }





    }

}
