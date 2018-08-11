package info.androidhive.jsonparsing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

public class ViewData extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        Intent i = getIntent();
        String title = i.getStringExtra("title");
        String body = i.getStringExtra("body");
        String login = i.getStringExtra("login");
        String updated_at = i.getStringExtra("updated_at");

        String currentString = login;
        String[] separated = currentString.split(":::");
        String username=separated[0]; // this will contain "cabId"
        String img_url=separated[1];

        TextView txt_title = (TextView) findViewById(R.id.title);
        TextView txt_body = (TextView) findViewById(R.id.body);
        TextView txt_login = (TextView) findViewById(R.id.login);
        TextView txt_avatar= (TextView) findViewById(R.id.updated_at);

        ImageView imgflag = (ImageView) findViewById(R.id.flag);
        if(isOnline()){
        }
        else {
            Toast.makeText(ViewData.this, "You are not connected to Internet", Toast.LENGTH_SHORT).show();

        }
        new ViewData.DownloadImageTask(imgflag).execute(img_url);
        // Set results to the TextViews
        txt_title.setText(title);
        txt_body.setText(body);
        txt_login.setText(username);
        txt_avatar.setText(img_url);

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
}