package edu.iit.cwu49hawk.knowyourgovernment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.StringTokenizer;

/**
 * Created by wsy37 on 4/15/2017.
 */

public class OfficialActivity extends AppCompatActivity
{
    private TextView location;
    private TextView title;
    private TextView name;
    private TextView party;
    private ImageView photo;
    private TextView address;
    private TextView phone;
    private TextView email;
    private TextView website;
    private ImageButton youtube;
    private ImageButton google;
    private ImageButton twitter;
    private ImageButton faceBook;
    private String youtubeID = "";
    private String googleID = "";
    private String twitterID = "";
    private String faceBookID = "";
    private ScrollView scroller;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_official);

        location = (TextView)findViewById(R.id.location);
        title = (TextView)findViewById(R.id.title);
        name = (TextView)findViewById(R.id.name);
        party = (TextView)findViewById(R.id.party);
        photo = (ImageView)findViewById(R.id.photo);
        address = (TextView)findViewById(R.id.address);
        phone = (TextView)findViewById(R.id.phone);
        email = (TextView)findViewById(R.id.email);
        website = (TextView)findViewById(R.id.website);
        youtube = (ImageButton)findViewById(R.id.youtube);
        google = (ImageButton)findViewById(R.id.google);
        twitter = (ImageButton)findViewById(R.id.twitter);
        faceBook = (ImageButton)findViewById(R.id.facebook);
        scroller = (ScrollView)findViewById(R.id.scroller);

        youtube.setVisibility(View.INVISIBLE);
        google.setVisibility(View.INVISIBLE);
        twitter.setVisibility(View.INVISIBLE);
        faceBook.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        if(intent.hasExtra("location")){
            location.setText(intent.getStringExtra("location"));
        }

        if(intent.hasExtra("official"))
        {
            final Official currentOfficial = (Official)intent.getSerializableExtra("official");
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    photoClicked(currentOfficial);
                }
            });

            String officialTitle = currentOfficial.getOfficeName();
            String officialName = currentOfficial.getName();
            String officialParty = currentOfficial.getParty();
            final String officialPhotoURL = currentOfficial.getPhotoUrl();
            String addressLine1 = currentOfficial.getLine1();
            String addressLine2 = currentOfficial.getLine2();
            String officialCity = currentOfficial.getCity();
            String officialState = currentOfficial.getState();
            String officialZipCode = currentOfficial.getZipCode();
            String officialPhone = currentOfficial.getPhone();
            String officialEmail = currentOfficial.getEmail();
            String officialWebsite = currentOfficial.getUrl();
            String[] officialChannels = currentOfficial.getChannels();

            switch(officialParty.toLowerCase())
            {
                case "democratic":
                    scroller.setBackgroundColor(Color.BLUE);
                    break;
                case "republican":
                    scroller.setBackgroundColor(Color.RED);
                    break;
                default:
                    scroller.setBackgroundColor(Color.GRAY);
                    break;
            }

            title.setText(officialTitle);
            name.setText(officialName);
            party.setText(String.format("(%s)", officialParty));

            // Use Picasso for Photo Downloads
            if(!officialPhotoURL.equals("No Data Provided"))
            {
                Picasso picasso = new Picasso.Builder(this).listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        final String changedURL = officialPhotoURL.replace("http:", "https:");

                        picasso.load(changedURL)
                                .error(R.drawable.brokenimage)
                                .placeholder(R.drawable.placeholder)
                                .into(photo);
                    }
                }).build();

                picasso.load(officialPhotoURL)
                        .error(R.drawable.brokenimage)
                        .placeholder(R.drawable.placeholder)
                        .into(photo);
            }
            else
            {
                Picasso.with(this).load(officialPhotoURL)
                        .error(R.drawable.missingimage)
                        .placeholder(R.drawable.placeholder)
                        .into(photo);
            }

            if(!addressLine1.equals("No Data Provided"))
            {
                int line = 1;
                StringBuilder sb = new StringBuilder();
                StringTokenizer strTok = new StringTokenizer(addressLine1);
                while(strTok.hasMoreTokens())
                {
                    String token = strTok.nextToken();
                    if((sb.toString() + token).length() > (30 * line))
                    {
                        sb.append('\n').append(" " + token);
                        ++line;
                    }
                    else{
                        sb.append(" " + token);
                    }
                }

                strTok = new StringTokenizer(addressLine2);
                if(!addressLine2.trim().isEmpty()) {
                    sb.append('\n');
                    ++line;
                }
                while(strTok.hasMoreTokens())
                {
                    String token = strTok.nextToken();
                    if((sb.toString() + token).length() > (30 * line))
                    {
                        sb.append('\n').append(" " + token);
                        ++line;
                    }
                    else{
                        sb.append(" " + token);
                    }
                }
                sb.append('\n').append(String.format(" %s, %s %s", officialCity, officialState, officialZipCode));

                address.setText(sb.toString());

                Linkify.addLinks(address, Linkify.MAP_ADDRESSES);
                address.setLinkTextColor(Color.WHITE);
            }
            else{
                address.setText("No Data Provided");
            }

            phone.setText(officialPhone);
            if(!officialPhone.equals("No Data Provided")) {
                Linkify.addLinks(phone, Linkify.PHONE_NUMBERS);
                phone.setLinkTextColor(Color.WHITE);
            }

            email.setText(officialEmail);
            if(!officialEmail.equals("No Data Provided")) {
                Linkify.addLinks(email, Linkify.EMAIL_ADDRESSES);
                email.setLinkTextColor(Color.WHITE);
            }

            website.setText(officialWebsite);
            if(!officialWebsite.equals("No Data Provided")) {
                Linkify.addLinks(website, Linkify.WEB_URLS);
                website.setLinkTextColor(Color.WHITE);
            }

            if(!officialChannels[0].equals("No Data Provided"))
            {
                for (int i = 0; i < officialChannels.length; ++i)
                {
                    StringTokenizer strTok = new StringTokenizer(officialChannels[i], ",");
                    String type = strTok.hasMoreTokens()?strTok.nextToken():"";
                    String id = strTok.hasMoreTokens()?strTok.nextToken():"";

                    switch(type)
                    {
                        case "GooglePlus":
                            googleID = id;
                            google.setVisibility(View.VISIBLE);
                            google.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    googlePlusClicked(v);
                                }
                            });
                            break;
                        case "Facebook":
                            faceBookID = id;
                            faceBook.setVisibility(View.VISIBLE);
                            faceBook.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    facebookClicked(v);
                                }
                            });
                            break;
                        case "Twitter":
                            twitterID = id;
                            twitter.setVisibility(View.VISIBLE);
                            twitter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    twitterClicked(v);
                                }
                            });
                            break;
                        case "YouTube":
                            youtubeID = id;
                            youtube.setVisibility(View.VISIBLE);
                            youtube.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    youTubeClicked(v);
                                }
                            });
                            break;
                    }
                }
            }
        }
    }

    public void photoClicked(Official official)
    {
        Intent intent = new Intent(OfficialActivity.this, PhotoDetailActivity.class);
        intent.putExtra("location", location.getText().toString());
        intent.putExtra("official", official);
        startActivity(intent);
    }

    public void facebookClicked(View v)
    {
        String FACEBOOK_URL = "https://www.facebook.com/" + faceBookID;
        String urlToUse;
        PackageManager packageManager = getPackageManager();
        try
        {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if(versionCode >= 3002850){
                urlToUse = "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            }
            else{
                urlToUse = "fb://page/" + faceBookID;
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            urlToUse = FACEBOOK_URL;
        }

        Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
        facebookIntent.setData(Uri.parse(urlToUse));
        startActivity(facebookIntent);
    }

    public void twitterClicked(View v)
    {
        Intent intent;
        try
        {
            getPackageManager().getPackageInfo("com.twitter.android", 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + twitterID));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        catch (Exception e) {
            e.printStackTrace();
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + twitterID));
        }
        startActivity(intent);
    }

    public void googlePlusClicked(View v)
    {
        Intent intent;
        try
        {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName("com.google.android.apps.plus",
                    "com.google.android.apps.plus.phone.UrlGatewayActivity");
            intent.putExtra("customAppUri", googleID);
            startActivity(intent);
        }
        catch(ActivityNotFoundException e){
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://plus.google.com/" + googleID)));
        }
    }

    public void youTubeClicked(View v)
    {
        Intent intent;
        try
        {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.google.android.youtube");
            intent.setData(Uri.parse("https://www.youtube.com/" + youtubeID));
            startActivity(intent);
        }
        catch(ActivityNotFoundException e){
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/" + youtubeID)));
        }
    }
}