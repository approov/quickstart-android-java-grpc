//
// MIT License
//
// Copyright (c) 2016-present, Critical Blue Ltd.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
// (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
// publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
// ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
// THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package io.approov.shapes;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

// *** UNCOMMENT THE THREE LINES BELOW FOR APPROOV ***
// import io.approov.service.grpc.ApproovChannelBuilder;
// import io.approov.service.grpc.ApproovClientInterceptor;
// import io.approov.service.grpc.ApproovService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Activity activity;
    private View statusView = null;
    private ImageView statusImageView = null;
    private TextView statusTextView = null;

    // API key for grpc.shapes.approov.io:50051
    private String apiKeyHeaderName = "Api-Key";
    private String apiSecretKey = "yXClypapWNHIifHUWmBIyPFAm";
    // *** UNCOMMENT THE LINE BELOW FOR APPROOV SECRETS PROTECTION (and comment the line above) ***
    // private String apiSecretKey = "shapes_api_key_placeholder";

    private int getImageID(String imageName) {
        switch (imageName) {
            case "Circle":
            case "circle":
                return R.drawable.circle;
            case "Rectangle":
            case "rectangle":
                return R.drawable.rectangle;
            case "Square":
            case "square":
                return R.drawable.square;
            case "Triangle":
            case "triangle":
                return R.drawable.triangle;
        }
        return R.drawable.confused;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        // find controls
        statusView = findViewById(R.id.viewStatus);
        statusImageView = findViewById(R.id.imgStatus);
        statusTextView = findViewById(R.id.txtStatus);
        Button connectivityCheckButton = findViewById(R.id.btnConnectionCheck);
        Button shapesCheckButton = findViewById(R.id.btnShapesCheck);

        // open GRPC managed channel
        String host = "grpc.shapes.approov.io";
        int port = 50051;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).build();
        // *** UNCOMMENT THE LINE BELOW FOR APPROOV (and comment the line above) ***
        // ManagedChannel channel = ApproovChannelBuilder.forAddress(host, port).build();
        // *** UNCOMMENT THE LINE BELOW FOR APPROOV SECRETS PROTECTION
        // ApproovService.addSubstitutionHeader("apiKeyHeaderName", null);

       // handle connection check
       connectivityCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide status
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusView.setVisibility(View.INVISIBLE);
                    }
                });

                // run our HTTP request in a background thread to avoid blocking the UI thread
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        // fetch from the endpoint
                        int imgId = R.drawable.confused;
                        String msg;
                        try {

                            // calling stub
                            ShapeGrpc.ShapeBlockingStub stub = ShapeGrpc.newBlockingStub(channel);
                            // build hello request
                            HelloRequest request = HelloRequest.newBuilder().build();
                            // make hello call
                            HelloReply response = stub.hello(request);
                            // set result
                            msg = response.getMessage();
                            imgId = R.drawable.hello;
                        }
                        catch (Exception e) {
                            Log.d(TAG, "Hello call failed: " + e.toString());
                            msg = "Hello call failed: " + e.toString();
                        }

                        // display the result
                        final int finalImgId = imgId;
                        final String finalMsg = msg;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusImageView.setImageResource(finalImgId);
                                statusTextView.setText(finalMsg);
                                statusView.setVisibility(View.VISIBLE);
                            }
                        });

                    }
                });
            }
        });

        // handle getting shapes
        shapesCheckButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // hide status
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    statusView.setVisibility(View.INVISIBLE);
                }
            });

            // run our HTTP request in a background thread to avoid blocking the UI thread
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    // fetch from the endpoint
                    int imgId = R.drawable.confused;
                    String msg;
                    try {
                        // Get calling stub
                        ShapeGrpc.ShapeBlockingStub stub = ShapeGrpc.newBlockingStub(channel);
                        APIKeyClientInterceptor apiKeyAddingClientInterceptor =
                                new APIKeyClientInterceptor(apiKeyHeaderName, apiSecretKey);
                        stub = stub.withInterceptors(apiKeyAddingClientInterceptor);
                        // *** UNCOMMENT THE TWO LINES BELOW FOR APPROOV (and comment the line above) ***
                        // stub = stub.withInterceptors(apiKeyAddingClientInterceptor,
                        //        new ApproovClientInterceptor(channel));

                        // Make fetch shape call
                        ShapeReply response = stub.shape(ShapeRequest.newBuilder().build());
                        // *** UNCOMMENT THIS LINE FOR APPROOV WITH API PROTECTION (and comment the line above) *** */
                        // ShapeReply response = stub.approovShape(ApproovShapeRequest.newBuilder().build());

                        // Set result
                        msg = response.getMessage();
                        imgId = getImageID(response.getMessage());
                    } catch (Exception e) {
                        Log.d(TAG, "Shapes call failed: " + e.toString());
                        msg = "Shapes call failed: " + e.toString();
                    }

                    // display the result
                    final int finalImgId = imgId;
                    final String finalMsg = msg;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusImageView.setImageResource(finalImgId);
                            statusTextView.setText(finalMsg);
                            statusView.setVisibility(View.VISIBLE);
                        }
                    });

                }
            });
        }
    });

    }
}
