package edu.iit.cwu49hawk.knowyourgovernment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private TextView location;
    private Locator locator = null;
    private OfficialAdapter oAdapter;
    private List<Official> officials = new ArrayList<>();
    private RecyclerView recycler;
    private String currentZipCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        location = (TextView)findViewById(R.id.location);
        oAdapter = new OfficialAdapter(officials, this);
        recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setAdapter(oAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        if(!hasNetwork())
        {
            location.setText("No Data For Location");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Data cannot be accessed/loaded without an internet connection");
            builder.create().show();
        }
        else
        {
            locator = new Locator(this);
            currentZipCode = Locator.parseAddress(location.getText().toString()).get("zip code");
            new OfficialLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentZipCode);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if(requestCode == 5) {
            for(int i = 0; i < permissions.length; ++i) {
                if(permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
                        locator.setUpLocationManager();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void onDestroy()
    {
        if(locator != null)
            locator.shutDown();

        super.onDestroy();
    }

    public void setLocation(String city, String state, String zipCode) {
        location.setText(String.format("%s, %s %s", city, state, zipCode));
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.infoMenu:
                Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;

            case R.id.locMenu:
                if(!hasNetwork())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("No Network Connection");
                    builder.setMessage("Data cannot be accessed/loaded without an internet connection");
                    builder.create().show();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Enter a City, State or Zip Code:");
                    final EditText input = new EditText(this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setGravity(Gravity.CENTER_HORIZONTAL);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int which) {
                            Editable in = input.getText();
                            dialogInterface.dismiss();

                            currentZipCode = getLocationByName(in.toString());
                            new OfficialLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentZipCode);
                        }
                    });
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.create().show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v)
    {
        int position = recycler.getChildLayoutPosition(v);
        Official currentOfficial = officials.get(position);
        Intent viewOfficial = new Intent(MainActivity.this, OfficialActivity.class);
        viewOfficial.putExtra("official", currentOfficial);
        viewOfficial.putExtra("location", location.getText().toString());
        startActivity(viewOfficial);
    }

    public String getLocationByName(String s)
    {
        String zipCode = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try
        {
            List<Address> addresses;
            addresses = geocoder.getFromLocationName(s, 10);
            if(!addresses.isEmpty()) {
                Address address = addresses.get(0);
                addresses = geocoder.getFromLocation(address.getLatitude(), address.getLongitude(), 10);
                address = addresses.get(0);
                zipCode = address.getPostalCode();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.ic_warning_black_24px);
                builder.setTitle("No Data Found");
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return zipCode;
    }

    private class OfficialLoader extends AsyncTask<String, List<Official>, List<Official>>
    {
        private final String query = "https://www.googleapis.com/civicinfo/v2/representatives";
        private final String apiKey = "AIzaSyBDJggmA7tncnzoCfuFJyw6QCiTPi3sRJg";

        @Override
        protected List<Official> doInBackground(String... params)
        {
            officials.clear();

            Uri.Builder buildURL = Uri.parse(query).buildUpon();
            buildURL.appendQueryParameter("key", apiKey);
            buildURL.appendQueryParameter("address", params[0]);
            String urlToUse = buildURL.build().toString();
            List<Official> newOfficials = new ArrayList<>();

            StringBuilder sb = new StringBuilder();
            try
            {
                URL url = new URL(urlToUse);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while((line = reader.readLine()) != null)
                    sb.append(line).append('\n');

                newOfficials.addAll(parseJSON(sb.toString()));
                publishProgress(newOfficials);
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return newOfficials;
        }

        @Override
        protected void onProgressUpdate(List<Official>... values)
        {
            ArrayList<Official> newOfficials = (ArrayList<Official>) values[0];
            officials.clear();
            officials.addAll(newOfficials);
        }

        protected void onPostExecute(List<Official> value) {
            oAdapter.notifyDataSetChanged();
        }

        private List<Official> parseJSON(String s)
        {
            List<Official> newOfficials = new ArrayList<>();
            try
            {
                JSONObject jsonMain = new JSONObject(s);
                JSONObject normalizedInput = jsonMain.getJSONObject("normalizedInput");

                JSONArray offices = jsonMain.getJSONArray("offices");
                JSONArray officials = jsonMain.getJSONArray("officials");

                // parse normalized address
                final String city = normalizedInput.getString("city");
                final String state = normalizedInput.getString("state");
                final String zipCode = normalizedInput.getString("zip");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setLocation(city, state, zipCode);
                    }
                });

                for(int i = 0; i < offices.length(); ++i)
                {
                    JSONObject currentOffice = (JSONObject)offices.get(i);
                    String officeName = currentOffice.getString("name");
                    JSONArray officialIndices = currentOffice.getJSONArray("officialIndices");
                    for(int j = 0; j < officialIndices.length(); ++j)
                    {
                        int index = Integer.parseInt(officialIndices.get(j).toString());
                        JSONObject currentOfficial = (JSONObject)officials.get(index);

                        String officialName = currentOfficial.getString("name");
                        JSONObject address = currentOfficial.has("address")?
                                (JSONObject)(currentOfficial.getJSONArray("address").get(0)):null;

                        String line1;
                        String line2;
                        String officialCity;
                        String officialState;
                        String officialZipCode;
                        if(address != null) {
                            line1 = address.has("line1")?address.getString("line1"):"";
                            line2 = address.has("line2")?address.getString("line2"):"";
                            officialCity = address.has("city")?address.getString("city"):"";
                            officialState = address.has("state")?address.getString("state"):"";
                            officialZipCode = address.has("zip")?address.getString("zip"):"";
                        }
                        else
                        {
                            line1 = "No Data Provided";
                            line2 = "No Data Provided";
                            officialCity = "No Data Provided";
                            officialState = "No Data Provided";
                            officialZipCode = "No Data Provided";
                        }

                        String officialParty = currentOfficial.has("party")?
                                currentOfficial.getString("party"):"Independent";
                        String officialPhone = currentOfficial.has("phones")?
                                currentOfficial.getJSONArray("phones").get(0).toString():"No Data Provided";
                        String officialURL = currentOfficial.has("urls")?
                                currentOfficial.getJSONArray("urls").get(0).toString():"No Data Provided";
                        String officialEmail = currentOfficial.has("emails")?
                                currentOfficial.getJSONArray("emails").get(0).toString():"No Data Provided";
                        String officialPhotoURL = currentOfficial.has("photoUrl")?
                                currentOfficial.getString("photoUrl"):"No Data Provided";

                        JSONArray channels = currentOfficial.has("channels")?
                                currentOfficial.getJSONArray("channels"):null;

                        String[] officialChannels;
                        if(channels != null)
                        {
                            officialChannels = new String[channels.length()];
                            for (int k = 0; k < channels.length(); ++k) {
                                JSONObject currentChannel = (JSONObject) channels.get(k);
                                officialChannels[k] = currentChannel.getString("type") +
                                        "," + currentChannel.getString("id");
                            }
                        }
                        else
                        {
                            officialChannels = new String[1];
                            officialChannels[0] = "No Data Provided";
                        }

                        newOfficials.add(new Official(officeName, officialName, line1, line2, officialCity,
                                officialState, officialZipCode, officialParty, officialPhone, officialURL,
                                officialEmail, officialPhotoURL, officialChannels));
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            return newOfficials;
        }
    }

    private boolean hasNetwork()
    {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo == null || !networkInfo.isConnectedOrConnecting())
            return false;

        return true;
    }
}