package com.example.testing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.example.testing.databinding.ActivityUploadImageBinding;
import com.google.firebase.storage.StorageReference;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
public class UploadImage extends AppCompatActivity {
    private FloatingActionButton uploadButton;
    private ImageView uploadImage;
    EditText uploadCaption;
    ProgressBar progressBar;
    private Uri imageUri;
    FirebaseAuth auth;
    FirebaseUser user;
    CheckBox checkBox;
    FusedLocationProviderClient fusedLocationProviderClient;
    TextView address;

    Button getLocation;

    boolean checkState=false;

    final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.CANADA);
    final Date now = new Date();
    final String fileName = formatter.format(now);
    final String date= "Ngày ";

    Date currentDate = new Date();
    // Định dạng ngày theo định dạng "yyyyMMdd"
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd/", Locale.getDefault());
    // Gắn biến ngày vào chuỗi
    String dateString = sdf.format(currentDate);

    public void onBackPressed() {
        // Quay về màn hình trước đó
        Intent intent = new Intent(this, Select.class);
        startActivity(intent);
        finish(); // Kết thúc hiện tại Activity để ngăn chặn người dùng quay lại nó bằng nút back
    }



    private final static int REQUEST_CODE = 100;

    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReferenceFromUrl("https://test-99e7b-default-rtdb.firebaseio.com/");
    final  private DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("user");
    final  private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fileName);
    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference(date+' '+dateString);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);
        //lattitude = findViewById(R.id.lattitude);
        //longitude = findViewById(R.id.longitude);
        address = findViewById(R.id.address);
        //city = findViewById(R.id.city);
        //country = findViewById(R.id.country);
        getLocation = findViewById(R.id.getLocation);
        checkBox =findViewById(R.id.checkBox);
        //pushBtn=findViewById(R.id.pushBtn);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        uploadButton = findViewById(R.id.uploadButton);
        uploadCaption = findViewById(R.id.uploadCaption);
        uploadImage = findViewById(R.id.uploadImage);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        //
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                            imageUri = data.getData();
                            uploadImage.setImageURI(imageUri);
                        } else {
                            Toast.makeText(UploadImage.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );


        //lay vi tri
        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getLastLocation();

            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkState=isChecked;
            }
        });
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Khi trạng thái của CheckBox thay đổi, kiểm tra và cập nhật trạng thái của nút
            uploadImage.setEnabled(isChecked);
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent photoPicker = new Intent();
                photoPicker.setAction(Intent.ACTION_GET_CONTENT);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri != null){

                    uploadToFirebase(imageUri);
                } else  {
                    Toast.makeText(UploadImage.this, "Please select image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




    private void getLastLocation(){

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){


            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            if (location != null){



                                try {
                                    Geocoder geocoder = new Geocoder(UploadImage.this, Locale.getDefault());
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    //lattitude.setText("Lattitude: "+addresses.get(0).getLatitude());
                                    //longitude.setText("Longitude: "+addresses.get(0).getLongitude());
                                    address.setText("Vị trí hiện tại: "+addresses.get(0).getAddressLine(0));
                                    //city.setText("City: "+addresses.get(0).getLocality());
                                    //country.setText("Country: "+addresses.get(0).getCountryName());


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                            }

                        }
                    });


        }else {

            askPermission();


        }


    }

    private void askPermission() {

        ActivityCompat.requestPermissions(UploadImage.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {

        if (requestCode == REQUEST_CODE){

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){


                getLastLocation();

            }else {


                Toast.makeText(UploadImage.this,"Please provide the required permission",Toast.LENGTH_SHORT).show();

            }



        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    //Outside onCreate
    private void uploadToFirebase(Uri uri){


        String caption = uploadCaption.getText().toString();
        String location = address.getText().toString();
        auth = FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        String uid = user.getEmail();


        final StorageReference imageReference = storageReference.child(uid+ "." + getFileExtension(uri));
        imageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DataClass dataClass = new DataClass(uri.toString(), caption,location);
                        String key = databaseReference.push().getKey();
                        auth = FirebaseAuth.getInstance();
                        String caption1 = uploadCaption.getText().toString();
                        user=auth.getCurrentUser();
                        String ui=user.toString();
                        databaseReference.child(key).setValue(dataClass);
                        databaseReference.child(key).child("diachi").setValue(address.getText().toString());
                        databaseReference.child(key).child("checkState").setValue(String.valueOf(checkState));
                        databaseReference.child(key).child("uid").setValue(ui);
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(UploadImage.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UploadImage.this, Select.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(UploadImage.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getFileExtension(Uri fileUri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri));
    }
}