package com.example.pmfstevidencija;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private EditText txtEmail, txtPassword;
    Button btnLogIn;
    Button btnSignOut;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth mAuth;
    private String userID;

    private FusedLocationProviderClient fusedLocationClient;

    //pmfst coordinates
    private double lat_min = 43.511405;
    private double lat_max = 43.512259;
    private double lon_min = 16.468212;
    private double lon_max = 16.468716;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mAuth = FirebaseAuth.getInstance();
        initializeUI();
        if (ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    123);
        }
        if (ContextCompat.checkSelfPermission( this, Manifest.permission.INTERNET ) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    123);
        }
    }

    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogIn:
                loginUserAccount();
                break;
            case R.id.btnSignOut:
                mAuth.signOut();
                Toast.makeText(getApplicationContext(), "Signing Out...", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void loginUserAccount() {

        String email, password;
        email = txtEmail.getText().toString();
        password = txtPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser user = mAuth.getCurrentUser();
                            userID = user.getUid();
                            // Read from the database
                            DocumentReference docRef = db.collection("users").document(userID);
                            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                                    }
                                    UserInfo userinfo = documentSnapshot.toObject(UserInfo.class);

                                    if(!userinfo.getIsProfessor()){
                                        QRstarter(userinfo);
                                    }
                                    else{
                                        PresentStudents(userinfo);
                                    }
                                }
                            });
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initializeUI() {
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogIn = findViewById(R.id.btnLogIn);
        btnSignOut = findViewById(R.id.btnSignOut);
    }

    private void PresentStudents(UserInfo userInfo) {
        for (int i = 0; i < userInfo.getCourses().size(); i++) {
            String class_id = userInfo.getCourses().get(i);
            DocumentReference docRef = db.collection("predmeti").document(class_id);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        Class current_class =documentSnapshot.toObject(Class.class);
                        List<Schedule> schedules=current_class.getSchedule();
                        for (int j=0; j< schedules.size();j++){
                            Schedule schedule= current_class.getSchedule().get(j);
                            if(System.currentTimeMillis() >= schedule.getStartTime().getTime() && System.currentTimeMillis() <= schedule.getEndTime().getTime()){
                                List<String> attendees=schedule.getAttendees();
                                String[] students = new String[attendees.size()];
                                students=attendees.toArray(students);

                                Intent intent = new Intent(MainActivity.this, Current_Attendance.class);
                                intent.putExtra("Students", students);
                                startActivity(intent);
                                break;
                            }
                        }
                    }
                }
            });
        }
    }

    private void QRstarter(final UserInfo userInfo) {


        if (ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {

                                if (location.getLatitude()>=lat_min && location.getLongitude()>=lon_min && location.getLatitude()<=lat_max && location.getLongitude()<=lon_max)
                                {
                                    for (int i = 0; i < userInfo.getCourses().size(); i++) {

                                        final String class_id = userInfo.getCourses().get(i);
                                        DocumentReference docRef = db.collection("predmeti").document(class_id);
                                        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.exists()) {
                                                    Class current_class = documentSnapshot.toObject(Class.class);
                                                    List<Schedule> schedules = current_class.getSchedule();
                                                    for (int j = 0; j < schedules.size(); j++) {
                                                        Schedule schedule = current_class.getSchedule().get(j);
                                                        if (System.currentTimeMillis() >= schedule.getStartTime().getTime() && System.currentTimeMillis() <= schedule.getEndTime().getTime()) {
                                                            Intent intent = new Intent(MainActivity.this, QRreader.class);
                                                            Bundle bundle = new Bundle();
                                                            bundle.putSerializable("User", userInfo.getName());
                                                            bundle.putSerializable("ScheduleNumber", j);
                                                            bundle.putSerializable("Class_id", class_id);
                                                            intent.putExtras(bundle);
                                                            startActivity(intent);
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                    }
                            }
                                else{
                                    Toast.makeText(getApplicationContext(), "You are not in the PMFST building", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
        }
    }



}
