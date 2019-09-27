package com.example.shake2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.squareup.seismic.ShakeDetector;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ShakeDetector.Listener {


    ListView listView ;
    ArrayList<String> contactsArray ;
    ArrayAdapter<String> arrayAdapter ;
    Button contactsButton;
    Button callButton;
    Button logout;
    Cursor cursor ;
    String name, contactNumber ;

    String PERMISSIONS []={
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS,
    };



    private static final int PERMISSION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logout = (Button)findViewById(R.id.btnlogout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                Backendless.UserService.logout(new AsyncCallback<Void>() {
                    @Override
                    public void handleResponse(Void response) {

                        Toast.makeText(MainActivity.this, "bye..logging out", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this,login_form.class));
                        MainActivity.this.finish();

                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {

                        Toast.makeText(MainActivity.this, "Error :"+ fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE) ;
        ShakeDetector shakeDetector = new ShakeDetector(this);
        shakeDetector.start(sm);




        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                Log.e("permission", "Permission already granted.");
            } else {

              //If the app doesn’t have the Sms  permission, request it//

                requestPermission();
            }
        }

        //for retrieving contacts

        listView = (ListView)findViewById(R.id.listview);
        contactsArray = new ArrayList<String>();
        contactsButton = (Button)findViewById(R.id.contacts);
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AddContactstoArray();

                arrayAdapter = new ArrayAdapter<String>(
                         MainActivity.this,
                                R.layout.contact_listview, R.id.textView,
                                contactsArray
                );

                listView.setAdapter(arrayAdapter);

            }

        });


    }


    public void AddContactstoArray(){

        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);
        while (cursor.moveToNext()) {

            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            contactsArray.add(name + " " + ":" + " " + contactNumber);
        }

        cursor.close();

    }


    public boolean checkPermission() {
      /*  if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted

        }*/

        int SmsPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS);

        return SmsPermissionResult == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_REQUEST_CODE);

    }


    @Override
    public void hearShake() {
        Toast.makeText(this, "sensor is working", Toast.LENGTH_SHORT).show();
        sendSMS("7021222231","help!");
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }
}
