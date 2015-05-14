package com.example.han.pleasantjourney;

/**
 * Created by Han on 5/15/2015.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

public class NetClientGet {

    // http://localhost:8080/RESTfulExample/json/product/get
    public static String readUrl(String tempUrl) {
        String data="";
        try {

            URL url = new URL(tempUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);


            if (conn.getResponseCode() != 501) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            StringBuffer sb = new StringBuffer();
            String output= "";
            //System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            data = sb.toString();
            br.close();
            conn.disconnect();

        } catch (MalformedURLException e) {

            Log.e("NetClientGet", e.toString());

        } catch (IOException e) {

            Log.e("NetClientGet", e.toString());

        }

        return data;
    }

}
