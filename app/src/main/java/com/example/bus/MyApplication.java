package com.example.bus;

import android.app.Application;
import com.parse.Parse;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("L5OYUdLgp4741yqwOK4264PKetZI7lP9UVLFnycQ") // Replace with Back4App App ID
                .clientKey("lhRBKhdcHHLc5oUs8y2Gtsb70czoMy0ge5bfeOR7") // Replace with Back4App Client Key
                .server("https://parseapi.back4app.com/")
                .build()
        );
    }
}
