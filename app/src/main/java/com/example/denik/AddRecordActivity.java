package com.example.denik;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddRecordActivity extends AppCompatActivity {

    // Constants
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_IMAGE_PICK = 1002;
    private static final int REQUEST_LOCATION_PERMISSION = 1003;

    // UI
    private EditText etTitle, etText;
    private ImageView imgPhoto;
    private TextView tvLocation;
    private Button btnTakePhoto, btnChoosePhoto, btnGetLocation, btnSave;

    // Data
    private double latitude = 0.0;
    private double longitude = 0.0;
    private File photoFile;
    private Uri photoUri;
    private String photoPath;

    // Location client
    private FusedLocationProviderClient fusedLocationClient;

    // EDIT MODE toggles
    private boolean isEditMode = false;    // rozlišení, jestli upravujeme starý záznam
    private Record recordToEdit = null;    // pokud upravujeme, sem si uložíme původní data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        initUI();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 1) Zjistíme, zda je volána v módu "edit"
        if (getIntent().hasExtra("edit_mode")) {
            isEditMode = getIntent().getBooleanExtra("edit_mode", false);
        }
        if (isEditMode && getIntent().hasExtra("record_to_edit")) {
            recordToEdit = (Record) getIntent().getSerializableExtra("record_to_edit");
            if (recordToEdit != null) {
                // Předvyplníme formulář
                fillFormForEdit(recordToEdit);
            }
        }

        // 2) Nastavíme click listenery
        btnTakePhoto.setOnClickListener(v -> requestCameraPermissionAndTakePhoto());

        btnChoosePhoto.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickIntent, REQUEST_IMAGE_PICK);
        });

        btnGetLocation.setOnClickListener(v -> getLocation());

        // Uložení / update
        btnSave.setOnClickListener(v -> {
            // Ověříme validitu title
            String title = etTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (title.length() > 30) {
                Toast.makeText(this, "Title must not exceed 30 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Získáme text
            String text = etText.getText().toString().trim();

            // Pokud jsme v edit módu, chceme aktualizovat stávající záznam,
            // jinak vytvoříme nový s novým ID
            Record resultRecord;
            if (isEditMode && recordToEdit != null) {
                // Zachováme původní ID
                long sameId = recordToEdit.getId();
                resultRecord = new Record(
                        sameId,          // ID neměníme
                        title,
                        text,
                        latitude,
                        longitude,
                        photoPath
                );
            } else {
                // NEW MODE -> vygenerujeme ID
                long newId = System.currentTimeMillis();
                resultRecord = new Record(
                        newId,
                        title,
                        text,
                        latitude,
                        longitude,
                        photoPath
                );
            }

            // Vrátíme výsledek
            Intent data = new Intent();
            data.putExtra("new_record", resultRecord);
            setResult(RESULT_OK, data);
            finish();
        });
    }

    /** Pokud jsme v edit módu, touto metodou předvyplníme UI stávajícími daty. */
    private void fillFormForEdit(Record rec) {
        etTitle.setText(rec.getTitle());
        etText.setText(rec.getText());

        latitude = rec.getLatitude();
        longitude = rec.getLongitude();
        if (latitude != 0.0 && longitude != 0.0) {
            tvLocation.setText("GPS: " + latitude + ", " + longitude);
        } else {
            tvLocation.setText("GPS: not provided");
        }

        photoPath = rec.getPhotoPath();
        if (photoPath != null) {
            imgPhoto.setImageURI(Uri.parse(photoPath));
        }
    }

    private void initUI() {
        etTitle = findViewById(R.id.etTitle);
        etText = findViewById(R.id.etText);
        imgPhoto = findViewById(R.id.imgPhoto);
        tvLocation = findViewById(R.id.tvLocation);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnSave = findViewById(R.id.btnSave);
    }

    /** Požádáme o povolení kamery a spustíme focení (fallback). */
    private void requestCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_IMAGE_CAPTURE
            );
        } else {
            fallbackCameraIntent();
        }
    }

    private void fallbackCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            photoFile = new File(getFilesDir(), "photo_" + timeStamp + ".jpg");
            photoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    photoFile);
            photoPath = photoFile.getAbsolutePath();

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Camera application not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    tvLocation.setText("GPS: " + latitude + ", " + longitude);
                } else {
                    tvLocation.setText("GPS: not provided");
                }
            });
        }
    }

    /** Výsledky pro foto a galerii */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Focení
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (photoFile != null && photoFile.exists()) {
                imgPhoto.setImageURI(Uri.fromFile(photoFile));
            }
            else if (data != null && data.getExtras() != null) {
                // pokud vrátí jen thumbnail
                Bitmap thumb = (Bitmap) data.getExtras().get("data");
                if (thumb != null) {
                    imgPhoto.setImageBitmap(thumb);
                }
            }
        }
        // Galerie
        else if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImg = data.getData();
            if (selectedImg != null) {
                imgPhoto.setImageURI(selectedImg);
                photoPath = selectedImg.toString();
            }
        }
    }

    /** Zpracování resultů pro permissions. */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fallbackCameraIntent();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
