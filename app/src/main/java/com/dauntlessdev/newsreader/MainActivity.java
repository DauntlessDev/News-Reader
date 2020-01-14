package com.dauntlessdev.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static java.util.Arrays.asList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> titleList;
    ArrayList<String> urlList;
    ArrayList<Integer> idList;
    ArrayAdapter<String> arrayAdapter;
    SQLiteDatabase sqLiteDatabase;
    int counter;

    public class NewsDownloader extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                
                StringBuilder newsID = new StringBuilder();
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                
                int data = reader.read();
                while(data!=-1){
                    char currChar = (char) data;
                    newsID.append(currChar);
                    
                    data = reader.read();
                }
                
                return newsID.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String url = jsonObject.getString("url");
                String title = jsonObject.getString("title");
                titleList.add(title);
                urlList.add(url);
                arrayAdapter.notifyDataSetChanged();

                counter++;
                sqLiteDatabase.execSQL("INSERT INTO News  VALUES ('" +url+"','"+title+"'," + idList.get(counter) + ")");
                Cursor c = sqLiteDatabase.rawQuery("SELECT * FROM news", null);
                int urlIndex = c.getColumnIndex("url");
                int titleIndex = c.getColumnIndex("title");
                int idIndex = c.getColumnIndex("id");
                c.moveToFirst();
                while(c.moveToNext()){
                    Log.i("News info", c.getString(urlIndex)+ " "+ c.getString(titleIndex)+ " "+ c.getString(idIndex));
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("Error here", "sadx");
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        counter = 0;
        sqLiteDatabase = this.openOrCreateDatabase("News", MODE_PRIVATE, null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS News (url VARCHAR, title VARCHAR2, id INT(8))");

        idList = new ArrayList<>(
                asList(22044854, 22045053, 22044057, 22044198, 22044465,
                        22033129, 22043108, 22044749, 22045473, 22044368));

        NewsDownloader newsDownloader;
        titleList = new ArrayList<>();
        urlList = new ArrayList<>();
        for(int each : idList) {
            newsDownloader = new NewsDownloader();
            newsDownloader.execute("https://hacker-news.firebaseio.com/v0/item/" + each + ".json?print=pretty");
        }

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,titleList);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),webActivity.class);
                intent.putExtra("Link",urlList.get(position));
                startActivity(intent);
            }
        });
    }
}
