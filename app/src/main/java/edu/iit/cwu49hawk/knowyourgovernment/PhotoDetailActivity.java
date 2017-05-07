package edu.iit.cwu49hawk.knowyourgovernment;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by wsy37 on 4/15/2017.
 */

public class PhotoDetailActivity extends AppCompatActivity
{
    private TextView location;
    private TextView title;
    private TextView name;
    private ImageView photo;
    private ConstraintLayout layout;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        location = (TextView)findViewById(R.id.location);
        title = (TextView)findViewById(R.id.title);
        name = (TextView)findViewById(R.id.name);
        photo = (ImageView)findViewById(R.id.photo);
        layout = (ConstraintLayout)findViewById(R.id.layout);

        Intent intent = getIntent();
        if(intent.hasExtra("location")){
            location.setText(intent.getStringExtra("location"));
        }

        if(intent.hasExtra("official"))
        {
            final Official currentOfficial = (Official)intent.getSerializableExtra("official");
            String officialTitle = currentOfficial.getOfficeName();
            String officialName = currentOfficial.getName();
            String officialParty = currentOfficial.getParty();
            final String officialPhotoURL = currentOfficial.getPhotoUrl();

            switch(officialParty.toLowerCase())
            {
                case "democratic":
                    layout.setBackgroundColor(Color.BLUE);
                    break;
                case "republican":
                    layout.setBackgroundColor(Color.RED);
                    break;
                default:
                    layout.setBackgroundColor(Color.GRAY);
                    break;
            }

            title.setText(officialTitle);
            name.setText(officialName);

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
        }
    }
}