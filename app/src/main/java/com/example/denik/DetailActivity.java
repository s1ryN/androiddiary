package com.example.denik;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private static final int REQ_EDIT_RECORD = 124;

    private TextView tvDetailTitle, tvDetailText, tvDetailGPS;
    private ImageView imgDetailPhoto;
    private EditText etAdditional;
    private Button btnSaveAdditional, btnEditRecord, btnDeleteRecord;

    private Record currentRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        initUI();

        // Získáme záznam z intentu (přišel z MainActivity)
        if (getIntent().hasExtra("record_data")) {
            currentRecord = (Record) getIntent().getSerializableExtra("record_data");
            if (currentRecord != null) {
                showRecordData(currentRecord);
            }
        }

        // 1) Uložení doplňujícího textu
        btnSaveAdditional.setOnClickListener(v -> {
            if (currentRecord == null) return;

            String additionalText = etAdditional.getText().toString().trim();
            if (!additionalText.isEmpty()) {
                // Sloučíme nový text se starým
                String newText = currentRecord.getText() + "\n---\n" + additionalText;

                // Vytvoříme nový Record se stejným ID
                currentRecord = new Record(
                        currentRecord.getId(),
                        currentRecord.getTitle(),
                        newText,
                        currentRecord.getLatitude(),
                        currentRecord.getLongitude(),
                        currentRecord.getPhotoPath()
                );

                // Update v DB + reload z DB -> poté do currentRecord
                updateRecordInDBandReload(currentRecord);

                // Aktualizujeme UI, vyprázdníme additional
                showRecordData(currentRecord);
                etAdditional.setText("");
            }
        });

        // 2) Edit
        btnEditRecord.setOnClickListener(v -> {
            if (currentRecord == null) return;

            Intent editIntent = new Intent(this, AddRecordActivity.class);
            editIntent.putExtra("edit_mode", true);
            editIntent.putExtra("record_to_edit", currentRecord);
            startActivityForResult(editIntent, REQ_EDIT_RECORD);
        });

        // 3) Delete s potvrzovacím dialogem
        btnDeleteRecord.setOnClickListener(v -> {
            if (currentRecord == null) return;

            // Zobrazíme potvrzovací dialog
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this record?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Provedeme samotné smazání v DB a vrátíme se do MainActivity
                        deleteCurrentRecord();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void initUI() {
        tvDetailTitle   = findViewById(R.id.tvDetailTitle);
        tvDetailText    = findViewById(R.id.tvDetailText);
        tvDetailGPS     = findViewById(R.id.tvDetailGPS);
        imgDetailPhoto  = findViewById(R.id.imgDetailPhoto);
        etAdditional    = findViewById(R.id.etAdditional);
        btnSaveAdditional = findViewById(R.id.btnSaveAdditional);
        btnEditRecord   = findViewById(R.id.btnEditRecord);
        btnDeleteRecord = findViewById(R.id.btnDeleteRecord);
    }

    private void showRecordData(Record record) {
        tvDetailTitle.setText(record.getTitle());
        tvDetailText.setText(record.getText());

        if (record.isLocationProvided()) {
            tvDetailGPS.setText("GPS: " + record.getLatitude() + ", " + record.getLongitude());
        } else {
            tvDetailGPS.setText("GPS: not provided");
        }

        if (record.getPhotoPath() != null) {
            imgDetailPhoto.setImageURI(Uri.parse(record.getPhotoPath()));
        } else {
            imgDetailPhoto.setImageResource(0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_EDIT_RECORD && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("new_record")) {
                Record updatedRec = (Record) data.getSerializableExtra("new_record");
                if (updatedRec != null) {
                    updateRecordInDBandReload(updatedRec);
                    showRecordData(currentRecord);
                }
            }
        }
    }

    /**
     * Metoda pro update záznamu v DB a následný reload (abychom měli čerstvá data).
     * Po updatu nastavíme setResult, aby i MainActivity poznala změnu.
     */
    private void updateRecordInDBandReload(Record r) {
        if (r == null) return;

        // 1) Update v DB
        RecordEntity e = new RecordEntity(
                r.getId(),
                r.getTitle(),
                r.getText(),
                r.getLatitude(),
                r.getLongitude(),
                r.getPhotoPath()
        );
        Diary.getDb().recordDao().update(e);

        // 2) Znovu načteme z DB
        RecordEntity fresh = Diary.getDb().recordDao().getById(r.getId());
        if (fresh != null) {
            currentRecord = new Record(
                    fresh.getId(),
                    fresh.getTitle(),
                    fresh.getText(),
                    fresh.getLatitude(),
                    fresh.getLongitude(),
                    fresh.getPhotoPath()
            );
        }

        // 3) Vrátíme do MainActivity (updated_record)
        Intent result = new Intent();
        result.putExtra("updated_record", currentRecord);
        setResult(RESULT_OK, result);
    }

    /**
     * Volá se po potvrzení "Delete" v dialogu.
     * Smaže záznam v DB, informuje MainActivity a ukončí DetailActivity.
     */
    private void deleteCurrentRecord() {
        // A) smazat z DB
        RecordEntity entity = new RecordEntity(
                currentRecord.getId(),
                currentRecord.getTitle(),
                currentRecord.getText(),
                currentRecord.getLatitude(),
                currentRecord.getLongitude(),
                currentRecord.getPhotoPath()
        );
        Diary.getDb().recordDao().delete(entity);

        // B) informovat MainActivity, že záznam je pryč
        Intent result = new Intent();
        result.putExtra("deleted_id", currentRecord.getId());
        setResult(RESULT_OK, result);

        // C) skončit DetailActivity
        finish();
    }
}
