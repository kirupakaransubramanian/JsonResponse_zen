package info.androidhive.jsonparsing;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;
    SwipeRefreshLayout swipeRefreshLayout;

    private static String url = "https://api.github.com/repos/crashlytics/secureudid/issues";

    ArrayList<HashMap<String, String>> dataList;

    SharedPreferences pref;

    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("yourPrefsKey", Context.MODE_PRIVATE);
        editor=pref.edit();
        dataList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.simpleSwipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // cancle the Visual indication of a refresh
                        swipeRefreshLayout.setRefreshing(false);
                        // Generate a random integer number
                        Random r = new Random();
                        int i1 = r.nextInt(80 - 65) + 65;
                        dataList = new ArrayList<>();

                        if(isOnline()){
                            new GetContacts().execute();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "You are not connected to Internet", Toast.LENGTH_SHORT).show();
                            int length=pref.getInt("length",0);
                            Log.e(TAG, "offline length: " + length);
                            if(length!=0){
                                for(int i=0;i<length;i++){
                                    String title=pref.getString("title"+i,null);
                                    String body=pref.getString("body"+i,null);
                                    String logavatar=pref.getString("login"+i,null);
                                    String updated_at=pref.getString("updated_at"+i,null);

                                    if(title!=null){
                                        HashMap<String, String> hashData = new HashMap<>();
                                        hashData.put("title", title);
                                        hashData.put("body", body);
                                        hashData.put("login", logavatar);
                                        hashData.put("updated_at", updated_at);
                                        dataList.add(hashData);
                                    }else{
                                        Log.e(TAG, "else: " + i);

                                    }

                                }

                                ListAdapter adapter = new SimpleAdapter(
                                        MainActivity.this, dataList,
                                        R.layout.list_item, new String[]{"title", "body",
                                        "login","updated_at"}, new int[]{R.id.title,
                                        R.id.body, R.id.login,R.id.updated_at});

                                lv.setAdapter(adapter);
                            }else{

                            }

                        }                    }
                }, 3000);
            }
        });
        if(isOnline()){
            new GetContacts().execute();
             }
            else{
            Toast.makeText(MainActivity.this, "You are not connected to Internet", Toast.LENGTH_SHORT).show();
            int length=pref.getInt("length",0);
            Log.e(TAG, "offline length: " + length);
            if(length!=0){
                for(int i=0;i<length;i++){
                    String title=pref.getString("title"+i,null);
                    String body=pref.getString("body"+i,null);
                    String logavatar=pref.getString("login"+i,null);
                    String updated_at=pref.getString("updated_at"+i,null);

                    if(title!=null){
                        HashMap<String, String> hashData = new HashMap<>();

                        hashData.put("title", title);
                        hashData.put("body", body);
                        hashData.put("login", logavatar);
                        hashData.put("updated_at", updated_at);
                        dataList.add(hashData);
                    }else{
                        Log.e(TAG, "else: " + i);

                    }

                }

                // adding contact to contact list
                ListAdapter adapter = new SimpleAdapter(
                        MainActivity.this, dataList,
                        R.layout.list_item, new String[]{"title", "body",
                        "login","updated_at"}, new int[]{R.id.title,
                        R.id.body, R.id.login,R.id.updated_at});

                lv.setAdapter(adapter);
            }else{

            }

        }


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ViewData.class);
                intent.putExtra("title", dataList.get(position).get("title"));
                intent.putExtra("body", dataList.get(position).get("body"));
                intent.putExtra("login", dataList.get(position).get("login"));
                intent.putExtra("updated_at", dataList.get(position).get("updated_at"));
                Log.e(TAG, "name: " + dataList.get(position).get("title"));
                startActivity(intent);
            }
        });
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
//                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray jsonData = new JSONArray(jsonStr);
                    Log.e(TAG, " contacts.length(): " +  jsonData.length());

                    // looping through All Contacts
                    for (int i = 0; i < jsonData.length(); i++) {
                        JSONObject c = jsonData.getJSONObject(i);

                        String url = c.getString("url");
                        String title = c.getString("title");
                        String body = c.getString("body");
                        // user  is JSON Object
                        JSONObject user = c.getJSONObject("user");
                        String login = user.getString("login");
                        String avatar_url = user.getString("avatar_url");
                        String logavatar=login+":::"+avatar_url;
                        String updated_at = c.getString("updated_at");
                        String currentString = updated_at;
                        String[] separated = currentString.split("-");
                        String year=separated[0]; // this will contain "cabId"
                        String month=separated[1];
                        String adate=separated[2];
                        String currentdate = adate;
                        String[] dateseparated = currentdate.split(":");
                        String date=dateseparated[0];
                        String onlyDate=date.substring(0,2);
                        String lastDate=month+"-"+onlyDate+"-"+year;
                        HashMap<String, String> hashData = new HashMap<>();

                        // adding each child node to HashMap key => value
                        hashData.put("title", title);
                        hashData.put("body", body);
                        hashData.put("login", logavatar);
                        hashData.put("updated_at", lastDate);
                        editor.putInt("length", jsonData.length());
                        editor.putString("title"+i, title);
                        editor.putString("body"+i, body);
                        editor.putString("login"+i, logavatar);
                        editor.putString("updated_at"+i, lastDate);
                        editor.commit();
                        // adding contact to contact list
                        dataList.add(hashData);
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */


            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, dataList,
                    R.layout.list_item, new String[]{"title", "body",
                    "login","updated_at"}, new int[]{R.id.title,
                    R.id.body, R.id.login,R.id.updated_at});

            lv.setAdapter(adapter);
        }

    }
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
}
