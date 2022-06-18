package com.example.plannus.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.plannus.Objects.TimetableSettings;
import com.example.plannus.R;
import com.example.plannus.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
//import com.squareup.okhttp.Callback;
//import com.squareup.okhttp.OkHttpClient;
//import com.squareup.okhttp.Request;
//import com.squareup.okhttp.RequestBody;
//import com.squareup.okhttp.Response;


import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.OkHttpClient;
import okhttp3.Callback;
import okhttp3.Response;

public class GenerateTimetableActivity extends AppCompatActivity implements View.OnClickListener {
    private Button settings, generate, next;
    private SessionManager sessionManager;
    private String userID;
    private OkHttpClient okHttpClient;
    private TimetableSettings timetableSettings;
    private TextView textView;
    //private final RequestBody EMPTYREQUEST = new FormBody.Builder().build();
    private static int iterations = 0;
    private Call call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_timetable);
        initVars();
        obtainSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        obtainSettings();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.settingsButton) {
            startActivity(new Intent(this, TimetableSettingsActivity.class));
        } else if (v.getId() == R.id.generateButton) {
            if (timetableSettings == null) {
                Toast.makeText(GenerateTimetableActivity.this, "Settings page empty/ still getting rendering data, please wait...", Toast.LENGTH_LONG).show();
            } else {
                iterations = 0;
                RequestBody requestBody = buildRequestBody(timetableSettings);
                if (requestBody == null) {
                    textView.setText("Settings page empty");
                } else {
                    if (call != null) {
                        call.cancel();
                    }
                    Request built_Request = buildPostRequest("https://plannus-sat-solver.herokuapp.com/z3runner", requestBody);
                    getRequest(built_Request);
                }
            }
        } else if (v.getId() == R.id.nextButton) {
            if (timetableSettings == null) {
                Toast.makeText(GenerateTimetableActivity.this, "Please click generate button first", Toast.LENGTH_LONG).show();
            } else {
                iterations++;
                RequestBody nextRequestBody = buildRequestBody(timetableSettings);
                Request nextSolutionRequest = buildPostRequest("https://plannus-sat-solver.herokuapp.com/z3runner", nextRequestBody);
                getRequest(nextSolutionRequest);
            }
        }

    }

    private Request buildPostRequest(String url, RequestBody requestBody) {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return request;
    }

    private void initVars() {
        textView = findViewById(R.id.textView);
        settings = findViewById(R.id.settingsButton);
        settings.setOnClickListener(this);
        generate = findViewById(R.id.generateButton);
        generate.setOnClickListener(this);
        next  = findViewById(R.id.nextButton);
        next.setOnClickListener(this);

        sessionManager = SessionManager.get();
        userID = sessionManager.getAuth().getCurrentUser().getUid();
        okHttpClient = new OkHttpClient();
    }

    private void getRequest(Request request) {
        call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("NETWORK_FAIL", "NETWORK FAIL");
                textView.setText("Network Fail");
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String text = response.body().string();
                            Log.d("RESPONSE_BODY", text);
                            textView.setText(text);
                        } catch (IOException e) {
                            e.getStackTrace();
                        }
                    }
                });
            }
        });

    }

    public void obtainSettings() {
        DocumentReference docRef = sessionManager.getFireStore().collection("Users").document(userID).collection("timetableSettings").document("timetableSettings");
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                timetableSettings = documentSnapshot.toObject(TimetableSettings.class);
                if (timetableSettings == null) {
                    return;
                }
                Log.d("toString Settings", timetableSettings.toString());
                Log.d("SETTINGS SIZE", ((Integer)timetableSettings.getSize()).toString());
                Log.d("MODULE LIST", timetableSettings.getModuleList().toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("SETTINGS FAILURE", "Not able to get settings from Firestore");
            }
        });
    }

    public RequestBody buildRequestBody(TimetableSettings settings) {
        FormBody.Builder builder = new FormBody.Builder();
        System.out.println(settings);
        Log.d("REQUEST SIZE", ((Integer)settings.getSize()).toString());
        Log.d("REQUEST MODULE LIST", settings.getModuleList().toString());
        int numMods = settings.getSize();
        ArrayList<String> mods = settings.getModuleList();
        int actual_count = settings.getSize();
        for (int i = 1, j = 0; i <= numMods; i++) {
            if (mods.get(i - 1).isEmpty()) {
                actual_count--;
                continue;
            }
            builder.add("mod" + String.valueOf(j), mods.get(i - 1));
            j++;
        }
        if (actual_count == 0) {
            return null;
        } else {
            builder.add("numMods", String.valueOf(actual_count));
            builder.add("AY", settings.getAcademicYear());
            builder.add("Sem", settings.getSem());
            builder.add("userID", userID);
            builder.add("iter", String.valueOf(iterations));
            System.out.println(mods);
            return builder.build();
        }
    }

}