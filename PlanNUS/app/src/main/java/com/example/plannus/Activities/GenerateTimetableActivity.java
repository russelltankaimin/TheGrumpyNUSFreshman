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

import com.example.plannus.Objects.NUSTimetable;
import com.example.plannus.Objects.TimetableSettings;
import com.example.plannus.R;
import com.example.plannus.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
    private static int iterations = 0;
    private Call call;
    private NUSTimetable nusTimetable;
    private ArrayList<String> constraintStrings;

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
                obtainSettings();
                RequestBody requestBody = buildRequestBody(timetableSettings);
                if (requestBody == null) {
                    textView.setText("Settings page empty");
                } else {
                    if (call != null) {
                        call.cancel();
                    }
                    Request built_Request = buildPostRequest("https://plannus-satsolver-backup.herokuapp.com/z3runner", requestBody);
                    getRequest(built_Request);
                }
            }
        } else if (v.getId() == R.id.nextButton) {
            if (timetableSettings == null) {
                Toast.makeText(GenerateTimetableActivity.this, "Please click generate button first", Toast.LENGTH_LONG).show();
            } else {
                iterations++;
                RequestBody nextRequestBody = buildRequestBody(timetableSettings);
                Request nextSolutionRequest = buildPostRequest("https://plannus-satsolver-backup.herokuapp.com/z3runner", nextRequestBody);
                getRequest(nextSolutionRequest);
            }
        } else if (v.getId() == R.id.saveTimetableButton) {
            saveTimeTableButton(nusTimetable);
        }

    }

    private void saveTimeTableButton(NUSTimetable timetable) {
        if (timetable == null) {
            Log.d("Timetable NULL", "Timetable is Null, Not saving it");
            return;
        }
        sessionManager.getFireStore()
                .collection("Users")
                .document(userID)
                .collection("NUS_Schedule")
                .document("NUS_Schedule")
                .set(nusTimetable)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(GenerateTimetableActivity.this, "Timetable Saved Successfully",Toast.LENGTH_LONG).show();
                        Log.d("SAVE SUCCESS", "Timetable Saved successfully!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GenerateTimetableActivity.this, "Timetable did not save", Toast.LENGTH_LONG).show();
                        Log.d("SAVE FAIL", "Timetable Did NOT Save !");
                    }
                });
    }

    private Request buildPostRequest(String url, RequestBody requestBody) {
        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
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

        constraintStrings = new ArrayList<>();
        constraintStrings.add("no8amLessons");
        constraintStrings.add("oneFreeDay");

        nusTimetable = new NUSTimetable();
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
                runOnUiThread(() -> {
                    try {
                        String jsonReturnString = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonReturnString);
                        String displayText = (String) jsonObject.get("string");
                        nusTimetable = new NUSTimetable(jsonObject);
                        Log.d("RESPONSE_BODY", displayText);
                        textView.setText(nusTimetable.getStringRep());
                    } catch (IOException e) {
                        e.getStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });

    }

    public void obtainSettings() {
        DocumentReference docRef = sessionManager.getFireStore().collection("Users").document(userID).collection("timetableSettings").document("timetableSettings");
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            timetableSettings = documentSnapshot.toObject(TimetableSettings.class);
            if (timetableSettings == null) {
                return;
            }
            Log.d("toString Settings", timetableSettings.toString());
            Log.d("SETTINGS SIZE", ((Integer)timetableSettings.getSize()).toString());
            Log.d("MODULE LIST", timetableSettings.getModuleList().toString());
            Log.d("CONSTRAINTS", timetableSettings.getConstraints().toString());
        }).addOnFailureListener(e -> Log.d("SETTINGS FAILURE", "Not able to get settings from Firestore"));
    }

    public RequestBody buildRequestBody(TimetableSettings settings) {
        FormBody.Builder builder = new FormBody.Builder();
        System.out.println(settings);
        int actual_count = actualNumberOfMods(settings);
        builder = buildRequestFromMods(settings, builder);
        if (actual_count == 0) {
            return null;
        } else {
            builder = buildRequestFromBasicData(settings, builder, actual_count);
            builder = buildRequestFromConstraints(settings, builder);
            return builder.build();
        }
    }

    public FormBody.Builder buildRequestFromMods(TimetableSettings settings, FormBody.Builder builder) {
        int actualNumber = actualNumberOfMods(settings);
        ArrayList<String> mods = settings.getModuleList();
        for(int i = 0; i < actualNumber; i++) {
            builder.add("mod" + String.valueOf(i), mods.get(i));
        }
        return builder;
    }

    public int actualNumberOfMods(TimetableSettings settings) {
        int actualCount = settings.getSize();
        ArrayList<String> mods = settings.getModuleList();
        for (int i = 1; i <= settings.getSize(); i++) {
            if (mods.get(i - 1).isEmpty()) {
                actualCount--;
            }
        }
        return actualCount;
    }

    public FormBody.Builder buildRequestFromBasicData(TimetableSettings settings, FormBody.Builder builder, int count) {
        builder.add("numMods", String.valueOf(count));
        builder.add("AY", settings.getAcademicYear());
        builder.add("Sem", settings.getSem());
        builder.add("userID", userID);
        builder.add("iter", String.valueOf(iterations));
        return builder;
    }

    public FormBody.Builder buildRequestFromConstraints(TimetableSettings settings, FormBody.Builder builder) {
        HashMap<String, Boolean> constraints = settings.getConstraints();
        System.out.println("BUILD REQUEST FROM CONSTRAINTS : ");
        System.out.println(constraints);
        for(String constraint : constraints.keySet()) {
            System.out.println(constraint);
            System.out.println(constraints.get(constraint));
            builder.add(constraint, constraints.get(constraint) ? "true" : "");
        }
        return builder;
    }
}