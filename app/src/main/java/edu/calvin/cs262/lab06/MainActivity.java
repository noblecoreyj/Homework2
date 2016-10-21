package edu.calvin.cs262.lab06;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Reads kvlinden's API for Monopoly players
 * The code is based on Deitel's WeatherViewer (Chapter 17), simplified based on Murach's NewsReader (Chapter 10).
 * <p>
 * for CS 262, lab 6
 *
 * @author kvlinden
 * @author cjn8
 * @version summer, 2016
 */
public class MainActivity extends AppCompatActivity {

    private EditText idText;
    private Button fetchButton;

    private List<Player> playerList = new ArrayList<>();
    private ListView itemsListView;

    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idText = (EditText) findViewById(R.id.idText);
        fetchButton = (Button) findViewById(R.id.fetchButton);
        itemsListView = (ListView) findViewById(R.id.playerListView);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard(idText);
                new GetPlayerTask().execute(createURL(idText.getText().toString()));
            }
        });
    }

    /**
     * Formats a URL for the webservice specified in the string resources.
     *
     * @param id the target ID
     * @return URL formatted for kvlinden's API
     */
    private URL createURL(String id) {
        try {
            String urlString;
            if (id.isEmpty()) {
                urlString = getString(R.string.web_service_url1);
            }
            else {
                urlString = getString(R.string.web_service_url2) + id;
            }
            return new URL(urlString);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    /**
     * Deitel's method for programmatically dismissing the keyboard.
     *
     * @param view the TextView currently being edited
     */
    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Inner class for GETing the Monopoly data from kvlinden's API
     */
    private class GetPlayerTask extends AsyncTask<URL, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(URL... params) {
            HttpURLConnection connection = null;
            StringBuilder result = new StringBuilder();
            try {
                connection = (HttpURLConnection) params[0].openConnection();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    try {
                        return new JSONArray(result.toString());
                    } catch (JSONException je) {
                        JSONArray jArray = new JSONArray();
                        jArray.put(new JSONObject(result.toString()));
                        return jArray;
                    }
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray player) {
            if (player != null) {
                convertJSONtoArrayList(player);
                MainActivity.this.updateDisplay();
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Converts the JSON player data to an arraylist suitable for a listview adapter
     *
     * @param players
     */
    private void convertJSONtoArrayList(JSONArray players) {
        playerList.clear(); // clear old player data
        try {
            for (int i = 0; i < players.length(); i++) {
                JSONObject currentPlayer = players.getJSONObject(i);
                playerList.add(new Player(
                        currentPlayer.getInt("id"),
                        currentPlayer.has("emailaddress") ? currentPlayer.getString("emailaddress") : "No Email Address",
                        currentPlayer.has("name") ? currentPlayer.getString("name") : "No Name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh the Monopoly player view
     */
    private void updateDisplay() {
        if (playerList == null) {
            Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        }
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        for (Player item : playerList) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("id", Integer.toString(item.getId()));
            map.put("name", item.getName());
            map.put("emailaddress", item.getEmail());
            data.add(map);
        }

        int resource = R.layout.weather_item;
        String[] from = {"id", "name", "emailaddress"};
        int[] to = {R.id.idTextView, R.id.nameTextView, R.id.emailTextView};

        SimpleAdapter adapter = new SimpleAdapter(this, data, resource, from, to);
        itemsListView.setAdapter(adapter);
    }

}
