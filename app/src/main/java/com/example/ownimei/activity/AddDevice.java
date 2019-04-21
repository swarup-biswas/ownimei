package com.example.ownimei.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ownimei.R;
import com.example.ownimei.StaticClass.StaticClass;
import com.example.ownimei.pojo.AddDeviceModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.Calendar;

import static com.example.ownimei.activity.SignIn.SIGN_UP_TOKEN;
import static com.example.ownimei.activity.SignIn.USER_INFO;

public class AddDevice extends AppCompatActivity implements View.OnClickListener {
    private ImageView addDeviceBackButton;
    private TextView deviceSelect;
    private EditText addDeviceName;
    private EditText addDeviceIMEIOne;
    private EditText addDeviceIMEITwo;
    private EditText addDeviceMAC;
    private TextView addDevicePurchaseDate;
    private TextView addDeviceMode;
    private Button addDeviceButton;
    private String finalAddMode;

    private DatePickerDialog datePickerDialog;
    //Firebase
    private FirebaseAuth authAdd;
    private FirebaseFirestore db;
    private CollectionReference collectionReference;

    private String selectionAddDevice;
    private String selectionAddStatus;
    private int finalDeviceSelect;
    private int finalModeSelectDevice;
    //Query
    private String matchIMEiOne;
    private String matchIMEiOneForTwo;
    private String matchIMEiTwo;
    private String matchIMEiTwoForOne;
    private String matchMac;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        authAdd = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("DeviceInfo");

        deviceSelect = findViewById(R.id.add_device_category_ID);
        addDeviceName = findViewById(R.id.add_device_name_ID);
        addDeviceIMEIOne = findViewById(R.id.add_device_IMEI_one_ID);
        addDeviceIMEITwo = findViewById(R.id.add_device_IMEI_two_ID);
        addDeviceMAC = findViewById(R.id.add_device_MAC_ID);
        addDevicePurchaseDate = findViewById(R.id.add_device_purchase_date_ID);
        addDeviceMode = findViewById(R.id.add_device_mode_ID);

        addDeviceButton = findViewById(R.id.add_device_button_ID);
        addDeviceBackButton = findViewById(R.id.add_device_back_btn);

        int finalDeviceSelect1 = deviceSelect.getText().length();
        this.finalDeviceSelect = finalDeviceSelect1;


        deviceSelect.setOnClickListener(this);
        addDeviceBackButton.setOnClickListener(this);
//        addDeviceButton.setOnClickListener(this);
        addDevicePurchaseDate.setOnClickListener(this);
        addDeviceMode.setOnClickListener(this);
        //Query start
        addDeviceIMEIOne.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (s.toString().length() == 15) {
                        String one = addDeviceIMEIOne.getText().toString();
                        if (!one.isEmpty()) {
                            queryOneImeiMethod();
                        }
                    }
                } catch (Exception e) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        addDeviceIMEITwo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (s.toString().length() == 15) {
                        String two = addDeviceIMEITwo.getText().toString();
                        if (!two.isEmpty()) {
                            queryTwoImeiMethod();
                        }
                    }
                } catch (Exception e) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        addDeviceMAC.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    //TODO handle the mac search
                    if (s.toString().length() == 5) {
                        String mac = addDeviceMAC.getText().toString();
                        if (!mac.isEmpty()) {
                            queryMacMethod();
                        }
                    }
                } catch (Exception e) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //Query end
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_device_category_ID:
                addCategoryMethod();
                break;
            case R.id.add_device_back_btn:
                addDeviceBackButtonMethod();
                break;
            case R.id.add_device_mode_ID:
                deviceModeSelect();
                break;
            case R.id.add_device_purchase_date_ID:
                deviceDatePicker();
                break;
        }

    }

    //Back button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        addDeviceBackButtonMethod();
    }

    //Date picker
    private void deviceDatePicker() {
        Calendar calendarToday = Calendar.getInstance();
        int year = calendarToday.get(Calendar.YEAR);
        int month = calendarToday.get(Calendar.MONTH);
        int day = calendarToday.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(AddDevice.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                addDevicePurchaseDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            }
        }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(calendarToday.getTimeInMillis());
        datePickerDialog.show();
    }

    private void addDeviceBackButtonMethod() {
        SharedPreferences successSharedPreferences = getSharedPreferences(SIGN_UP_TOKEN, MODE_PRIVATE);
        String successTokenFinal = successSharedPreferences.getString("get_successToken", "No token found");
        Log.d("SuccessToken", "" + successTokenFinal);
        if (successTokenFinal.equals("" + 200)) {
            Log.d("SuccessToken", "" + successTokenFinal);
            Toast.makeText(AddDevice.this, "Sign in please!", Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(AddDevice.this, SignIn.class);
            startActivity(intent1);
            finish();
        } else if (authAdd.getCurrentUser().getUid().equals(authAdd.getCurrentUser().getUid())) {
            Intent intent2 = new Intent(AddDevice.this, UserProfile.class);
            startActivity(intent2);
            finish();
        }
    }

    //Device select dialog block start
    private void deviceModeSelect() {

        final String[] selectMode = getApplication().getResources().getStringArray(R.array.statusList);
        final AlertDialog.Builder addModeBuilder = new AlertDialog.Builder(AddDevice.this);
        addModeBuilder.setTitle("Select mode");
        addModeBuilder.setSingleChoiceItems(R.array.statusList, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        selectionAddStatus = selectMode[which];

                        break;
                    case 1:
                        selectionAddStatus = selectMode[which];

                        break;
                    case 2:
                        selectionAddStatus = selectMode[which];

                        break;
                }
            }
        }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectionAddStatus != null) {
                    addDeviceMode.setText(selectionAddStatus);
                    finalAddMode = selectionAddStatus;
                }
            }
        });

        AlertDialog showAlartDialog = addModeBuilder.create();
        showAlartDialog.show();
    }

    private void addCategoryMethod() {
        phone();

        final String[] selectDevice = getApplication().getResources().getStringArray(R.array.category);
        final AlertDialog.Builder addDeviceBuilder = new AlertDialog.Builder(AddDevice.this);
        addDeviceBuilder.setTitle("Select device");
        addDeviceBuilder.setSingleChoiceItems(R.array.category, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        selectionAddDevice = selectDevice[which];

                        break;
                    case 1:
                        selectionAddDevice = selectDevice[which];

                        break;
                }
            }
        }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectionAddDevice != null && selectionAddDevice.length() == 5) {
                    deviceSelect.setVisibility(View.VISIBLE);
                    addDeviceName.setVisibility(View.VISIBLE);
                    addDeviceIMEIOne.setVisibility(View.VISIBLE);
                    addDeviceIMEITwo.setVisibility(View.VISIBLE);
                    addDeviceMAC.setVisibility(View.GONE);
                    addDevicePurchaseDate.setVisibility(View.VISIBLE);
                    addDeviceButton.setVisibility(View.VISIBLE);

                    deviceSelect.setText("Phone");

                } else if (selectionAddDevice != null && selectionAddDevice.length() == 6) {
                    deviceSelect.setVisibility(View.VISIBLE);
                    addDeviceName.setVisibility(View.VISIBLE);
                    addDeviceMAC.setVisibility(View.VISIBLE);
                    addDeviceIMEIOne.setVisibility(View.GONE);
                    addDeviceIMEITwo.setVisibility(View.GONE);
                    addDevicePurchaseDate.setVisibility(View.VISIBLE);
                    addDeviceButton.setVisibility(View.VISIBLE);

                    deviceSelect.setText("Laptop");

                } else {
                    Toast.makeText(AddDevice.this, "Please select device", Toast.LENGTH_SHORT).show();
                }
            }
        });
        AlertDialog showAlertDialog = addDeviceBuilder.create();
        showAlertDialog.show();

    }

    private void queryMacMethod() {
        String mac1 = addDeviceMAC.getText().toString();
        if (!mac1.isEmpty()) {
            Query queryMac = db.collection("DeviceInfo").whereEqualTo("mac", mac1);
            queryMac.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        AddDeviceModel addDeviceModel = new AddDeviceModel();
                        if (document.getString("mac") != null) {
                            addDeviceModel.setMac(document.getString("mac"));
                            Log.d("matchIMEiOneForTwo", "" + matchIMEiOneForTwo);
                            AlertDialog.Builder builderOne = new AlertDialog.Builder(AddDevice.this);
                            builderOne.setMessage("Opps! this IMEI already exist. Please input your correct IMEI.").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AddDeviceModel addDeviceModel = new AddDeviceModel();
                                    addDeviceMAC.getText().clear();
                                    matchMac = addDeviceModel.getMac();
                                    Log.d("addDeviceMAC@@@@@@", "" + addDeviceMAC);
                                }
                            });
                            AlertDialog dialogOne = builderOne.create();
//                            dialogOne.setCanceledOnTouchOutside(false);
                            dialogOne.setCancelable(false);
                            dialogOne.show();
                        }
                    }
                }

            });
        }
    }

    private void queryOneImeiMethod() {
        String im1 = addDeviceIMEIOne.getText().toString();

        //IMEI one query
        if (!im1.isEmpty()) {
            Query queryImeiOne = db.collection("DeviceInfo").whereEqualTo("phoneImeiOne", im1);
            queryImeiOne.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        AddDeviceModel addDeviceModel = new AddDeviceModel();
                        if (document.getString("phoneImeiOne") != null) {
                            addDeviceModel.setPhoneImeiOne(document.getString("phoneImeiOne"));
                            matchIMEiOneForTwo = addDeviceModel.getPhoneImeiOne();
                            Log.d("matchIMEiOneForTwo", "" + matchIMEiOneForTwo);
                            AlertDialog.Builder builderOne = new AlertDialog.Builder(AddDevice.this);
                            builderOne.setMessage("Opps! this IMEI already exist. Please input your correct IMEI.").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AddDeviceModel addDeviceModel = new AddDeviceModel();
                                    addDeviceIMEIOne.getText().clear();
                                    matchIMEiOne = addDeviceModel.getPhoneImeiOne();
                                    Log.d("Clear@@@@@@", "" + matchIMEiOne);
                                }
                            });
                            AlertDialog dialogOne = builderOne.create();
                            dialogOne.setCancelable(false);
                            dialogOne.show();
                        }
                    }
                }

            });

            //IMEI two
            if (matchIMEiOneForTwo == null) {
                Query queryImeiTwo = db.collection("DeviceInfo").whereEqualTo("phoneImeiTwo", im1);
                queryImeiTwo.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            AddDeviceModel addDeviceModel = new AddDeviceModel();
                            if (document.getString("phoneImeiTwo") != null) {
                                addDeviceModel.setPhoneImeiOne(document.getString("phoneImeiTwo"));
                                AlertDialog.Builder builderOne = new AlertDialog.Builder(AddDevice.this);
                                builderOne.setMessage("Opps! this IMEI already exist. Please input your correct IMEI.").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AddDeviceModel addDeviceModel = new AddDeviceModel();
                                        addDeviceIMEIOne.getText().clear();
                                        matchIMEiOne = addDeviceModel.getPhoneImeiOne();
                                    }
                                });
                                AlertDialog dialogOne = builderOne.create();
                                dialogOne.setCancelable(false);
                                dialogOne.show();
                            }
                        }
                    }

                });
            }
        }
    }

    private void queryTwoImeiMethod() {
        String im2 = addDeviceIMEITwo.getText().toString();
        //IMEI one query
        if (!im2.isEmpty()) {
            Query queryImeiOne = db.collection("DeviceInfo").whereEqualTo("phoneImeiOne", im2);
            queryImeiOne.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        AddDeviceModel addDeviceModel = new AddDeviceModel();
                        if (document.getString("phoneImeiOne") != null) {
                            addDeviceModel.setPhoneImeiTwo(document.getString("phoneImeiOne"));
                            matchIMEiTwoForOne = addDeviceModel.getPhoneImeiTwo();
                            Log.d("matchIMEiOneForTwo", "" + matchIMEiTwoForOne);
                            AlertDialog.Builder builderOne = new AlertDialog.Builder(AddDevice.this);
                            builderOne.setMessage("Opps! this IMEI already exist. Please input your correct IMEI.").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AddDeviceModel addDeviceModel = new AddDeviceModel();
                                    addDeviceIMEITwo.getText().clear();
                                    matchIMEiTwo = addDeviceModel.getPhoneImeiTwo();
                                }
                            });
                            AlertDialog dialogOne = builderOne.create();
                            dialogOne.setCancelable(false);
                            dialogOne.show();
                        }
                    }
                }

            });
            //IMEI two
            if (matchIMEiTwoForOne == null) {
                Query queryImeiTwo = db.collection("DeviceInfo").whereEqualTo("phoneImeiTwo", im2);
                queryImeiTwo.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            AddDeviceModel addDeviceModel = new AddDeviceModel();
                            if (document.getString("phoneImeiTwo") != null) {
                                addDeviceModel.setPhoneImeiOne(document.getString("phoneImeiTwo"));
                                AlertDialog.Builder builderOne = new AlertDialog.Builder(AddDevice.this);
                                builderOne.setMessage("Opps! this IMEI already exist. Please input your correct IMEI.").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AddDeviceModel addDeviceModel = new AddDeviceModel();
                                        addDeviceIMEITwo.getText().clear();
                                        matchIMEiTwo = addDeviceModel.getPhoneImeiTwo();
                                        Log.d("Clear2@@@@@@", "" + matchIMEiTwo);
                                    }
                                });
                                AlertDialog dialogOne = builderOne.create();
                                dialogOne.setCancelable(false);
                                dialogOne.show();
                            }
                        }
                    }

                });
            }
        }
    }

    private void phone() {

        //Button Save device information

        addDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar();
                if (!StaticClass.isConnected(AddDevice.this)) {
                    StaticClass.buildDialog(AddDevice.this).show();
                    onBackPressed();
                    hideProgressBar();
                } else {
                    //Clear success token preference
                    SharedPreferences clearSharedPreferences = getSharedPreferences(SIGN_UP_TOKEN, MODE_PRIVATE);
                    clearSharedPreferences.edit().clear().apply();

                    String name = deviceSelect.getText().toString();
                    String model = addDeviceName.getText().toString();
                    String IMEI1 = addDeviceIMEIOne.getText().toString();
                    String IMEI2 = addDeviceIMEITwo.getText().toString();
                    String date = addDevicePurchaseDate.getText().toString();
                    String mac = addDeviceMAC.getText().toString();
                    String status = addDeviceMode.getText().toString();

                    if (model.isEmpty()) {
                        hideProgressBar();
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddDevice.this);
                        builder.setMessage("Please set device model").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                        return;
                    }
                    if (selectionAddDevice.equals("Phone")) {
                        hideProgressBar();
                        if (IMEI1.isEmpty()) {
                            hideProgressBar();
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddDevice.this);
                            builder.setMessage("Please enter IMEI1 number").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            builder.show();
                            return;
                        }
                        if (IMEI1.length() != 15) {
                            hideProgressBar();
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddDevice.this);
                            builder.setMessage("Please enter valid IMEI1 number").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            builder.show();
                            return;
                        }
                        if (!IMEI2.isEmpty()) {
                            hideProgressBar();
                            if (IMEI2.length() != 15) {
                                hideProgressBar();
                                AlertDialog.Builder builder = new AlertDialog.Builder(AddDevice.this);
                                builder.setMessage("Please enter valid IMEI2 number").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.show();
                                return;
                            }
                        }

                        if (IMEI2.equals(IMEI1)) {
                            hideProgressBar();
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddDevice.this);
                            builder.setMessage("IMEI1 && IMEI2 are same!").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            builder.show();
                            return;
                        }
                    }
                    if (selectionAddDevice.equals("Laptop")) {
                        hideProgressBar();
                        if (mac.isEmpty()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddDevice.this);
                            builder.setMessage("Please enter mac number").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            builder.show();
                            return;
                        }
                    }

                    if (date.isEmpty()) {
                        hideProgressBar();
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddDevice.this);
                        builder.setMessage("Please select purchase date").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                        return;
                    }
                    if (status.equals("Select Status")) {
                        hideProgressBar();
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddDevice.this);
                        builder.setMessage("Please select device status").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                        return;
                    }
                    if (status == null) {
                        hideProgressBar();
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddDevice.this);
                        builder.setMessage("Please select device status").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                        return;
                    }
                    if (status.isEmpty()) {
                        hideProgressBar();
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddDevice.this);
                        builder.setMessage("Please select device status").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                        return;
                    }


                    //TODO fix date problem
//                String cDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
//                if (Integer.parseInt(date)>Integer.parseInt(cDate)){
//                    AlertDialog.Builder builder = new AlertDialog.Builder(AddDevice.this);
//                    builder.setMessage("Please enter valid date").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                        }
//                    });
//                    builder.show();
//                    return;
//                }


                    if (matchIMEiOne == null && matchIMEiTwo == null && matchMac == null) {
                        SharedPreferences sGetUserInfo = getSharedPreferences(USER_INFO, MODE_PRIVATE);
                        String userName = sGetUserInfo.getString("Name", "");
                        String userEmail = sGetUserInfo.getString("Email", "");
                        String userId = authAdd.getCurrentUser().getUid();
                        if (userName != null && userEmail != null) {

                        }
                        AddDeviceModel deviceInfo = new AddDeviceModel(userName, userEmail, userId, name, model, IMEI1, IMEI2, mac, date, status);
                        collectionReference.add(deviceInfo).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                onBackPressed();
                                hideProgressBar();
                                Toast.makeText(AddDevice.this, "Success", Toast.LENGTH_SHORT).show();
                                Intent addIntent = new Intent(AddDevice.this, UserProfile.class);
                                startActivity(addIntent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                onBackPressed();
                                hideProgressBar();
                                Toast.makeText(AddDevice.this, "Failed ", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }
            }
        });


    }

    //Progressbar method
    static KProgressHUD kProgressHUD;

    public void showProgressBar() {
        kProgressHUD = KProgressHUD.create(AddDevice.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f);
        kProgressHUD.show();
    }

    public void hideProgressBar() {
        if (kProgressHUD != null)
            kProgressHUD.dismiss();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        StaticClass.hideKeyboard(this);
        return super.dispatchTouchEvent(ev);
    }

}