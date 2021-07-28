package com.example.webpagescannerapp;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class TreeBuilderThread extends Thread {

    OkHttpClient client;
    static String baseUrl;
    static int maxLinksNumber;
    static int currentLinkNumber;
    static String currentUrl;
    static int currentLevel;
    static final ArrayList<AbstractMap.SimpleEntry<Integer, String>> list;

    static {
        baseUrl = null;
        currentUrl = null;
        currentLinkNumber = 0;
        currentLevel = 0;
        list = new ArrayList<>(0);
    }

    public TreeBuilderThread(OkHttpClient client, String myBaseUrl, int myMaxLinksNumber){
        this.client = client;
        baseUrl = myBaseUrl;
        currentUrl = myBaseUrl;
        maxLinksNumber = myMaxLinksNumber;
        list.ensureCapacity(maxLinksNumber);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void run() {
        try {
            getAllLinks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    public ArrayList<AbstractMap.SimpleEntry<Integer, String>> getList() {
        return list;
    }

    private Document getDocument() throws IOException {
        Request request = new Request.Builder()
                .url(currentUrl)
                .get().build();
        return Jsoup.connect(currentUrl).timeout(0).get();
        //return Jsoup.parse(client.newCall(request).execute().body().string());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getAllLinks() throws IOException {

        //Assume that maxLinkNumber >= 1

        // Lvl 0 processing
        if (currentLevel == 0){
            list.add(new AbstractMap.SimpleEntry<>(currentLevel, baseUrl)); // Adding baseUrl(lvl 0)
            currentLinkNumber++;
            currentLevel++;

            // If links processed number < max links then go next
            if (currentLinkNumber < maxLinksNumber){
                // Recursive call
                getAllLinks();
            }
            else {
                return;
            }

        }   // Lvl 1 processing
        else if (currentLevel == 1){
            Elements links = getDocument().select("a[href]");
            for (Element link : links){
                if (currentLinkNumber < maxLinksNumber){
                    list.add(new AbstractMap.SimpleEntry<>(currentLevel, link.attr("href")));
                    currentLinkNumber++;
                }
                else {
                    return;
                }
            }
            currentLevel++;

            // If links processed number < max links then go next
            if (currentLinkNumber < maxLinksNumber){
                // Recursive call
                getAllLinks();
            }
            else {
                return;
            }

        }

        // Lvl 2+ processing
        for (int j = 0; j < list.stream().filter(integerStringSimpleEntry ->
                integerStringSimpleEntry.getKey() == currentLevel-1).count(); j++){

            ArrayList<AbstractMap.SimpleEntry<Integer, String>> listOfCurrentLevel =
                    (ArrayList<AbstractMap.SimpleEntry<Integer, String>>) list.stream().filter(integerStringSimpleEntry ->
                            integerStringSimpleEntry.getKey() == currentLevel-1).collect(Collectors.toList());

            for (AbstractMap.SimpleEntry<Integer, String> node : listOfCurrentLevel){
                // Current url assign to entry with value of first match with url of current lvl
                currentUrl = node.getValue();
                Elements linksOfNode = getDocument().select("a[href]");
                for (Element link : linksOfNode){
                    if (currentLinkNumber < maxLinksNumber) {
                        list.add(new AbstractMap.SimpleEntry<>(currentLevel, link.attr("href")));
                    }
                    else {
                        return;
                    }
                }
            }
            currentLevel++;

            // If links processed number < max links then go next
            if (currentLinkNumber < maxLinksNumber){
                // Recursive call
                getAllLinks();
            }
            else {
                return;
            }

        }


    }

//    private String getFirstUrlOfLevel(int lvl){
//        for (AbstractMap.SimpleEntry<Integer, String> node : list){
//            if (node.getKey() == lvl){
//                return node.getValue();
//            }
//        }
//        return null;
//    }
//
//    private Elements getLinksOnUrl(String url) throws IOException {
//        Document document = Jsoup.connect(url).get();
//        return document.select("a[href]");
//    }
}
