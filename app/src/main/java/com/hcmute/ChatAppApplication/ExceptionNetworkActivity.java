package com.hcmute.ChatAppApplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hcmute.ChatAppApplication.common.Util;
//declare class for network exception
public class ExceptionNetworkActivity extends AppCompatActivity {
    //declare text view for internet
    private TextView internetTvMsg;
    //declare progress bar for internet
    private ProgressBar pbImternetMsg;
    //declare button for retry and cancel connect to internet
    private Button retryInternetConnection, cancelActivity;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    //action when create activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_network);

        internetTvMsg = findViewById(R.id.internetMsg);
        retryInternetConnection = findViewById(R.id.retryInternetConnectionBtn);
        cancelActivity = findViewById(R.id.closeInternetActivityBtn);
        pbImternetMsg = findViewById(R.id.pbInternetMsg);


        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){

            networkCallback = new ConnectivityManager.NetworkCallback(){

                @Override
                //action when get the internet
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    finish();
                }

                @Override
                //action when lost the internet
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    internetTvMsg.setText(getString(R.string.no_internet));
                }
            };

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build()
                            ,networkCallback);

            retryInternetConnection.setOnClickListener(new View.OnClickListener() {
                @Override
                //action when click retry button
                public void onClick(View view) {
                    pbImternetMsg.setVisibility(View.VISIBLE);
                    if(Util.checkInternetConnection(getApplicationContext())){

                        finish();

                    }else{
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                pbImternetMsg.setVisibility(View.GONE);
                            }
                        },2000);
                    }
                }
            });


            cancelActivity.setOnClickListener(new View.OnClickListener() {
                @Override
                //action when click cancel button
                public void onClick(View view) {
                    finishAffinity();
                }
            });

        }
    }
}