package com.example.imamapplication;

public class AppProperties {
    private String _ServerQMAPI = "api/Ajax/QueueDisplayApi/QueueDisplay?DisplayIP=";
    private String _ServerURL = "http://192.168.16.228:84/";
//"http://192.168.16.156:8099/" ;
// 
    public String getServerQMAPI() {
        return _ServerQMAPI;
    }

    public String getServerURL() {
        return _ServerURL;
    }
}
