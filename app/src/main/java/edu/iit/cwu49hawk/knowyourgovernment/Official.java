package edu.iit.cwu49hawk.knowyourgovernment;

import java.io.Serializable;

/**
 * Created by wsy37 on 4/17/2017.
 */

public class Official implements Serializable
{
    private String officeName;
    private String name;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String zipCode;
    private String party;
    private String phone;
    private String url;
    private String email;
    private String photoUrl;
    private String[] channels;

    public Official(String officeName, String name, String line1, String line2, String city, String state,
                    String zipCode, String party, String phone, String url, String email,
                    String photoUrl, String[] channels)
    {
        this.officeName = officeName;
        this.name = name;
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.party = party;
        this.phone = phone;
        this.url = url;
        this.email = email;
        this.photoUrl = photoUrl;
        this.channels = channels;
    }

    public String getOfficeName(){
        return this.officeName;
    }

    public String getName(){
        return this.name;
    }

    public String getLine1(){
        return this.line1;
    }

    public String getLine2(){
        return this.line2;
    }

    public String getCity(){
        return this.city;
    }

    public String getState(){
        return this.state;
    }

    public String getZipCode(){
        return this.zipCode;
    }

    public String getParty(){
        return this.party;
    }

    public String getPhone(){
        return this.phone;
    }

    public String getUrl(){
        return this.url;
    }

    public String getEmail(){
        return this.email;
    }

    public String getPhotoUrl(){
        return this.photoUrl;
    }

    public String[] getChannels(){
        return this.channels;
    }
}