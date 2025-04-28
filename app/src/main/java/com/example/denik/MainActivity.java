package com.example.denik;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Request codes
    private static final int REQ_ADD_RECORD  = 123; // pro přidání nového
    private static final int REQ_DETAIL      = 200; // pro detail (kde může dojít k edit/delete)

    private Button btnAddNewRecord;
    private RecyclerView recyclerView;
    private RecordAdapter recordAdapter;
    private ArrayList<Record> recordList;  // data pro UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();

        // 1) Načteme záznamy z DB
        loadRecordsFromDB();

        // 2) Nastavíme RecyclerView
        setupRecyclerView();

        // 3) Tlačítko pro přidání nového záznamu
        btnAddNewRecord.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddRecordActivity.class);
            startActivityForResult(intent, REQ_ADD_RECORD);
        });
    }

    /** Propojení s prvky z layoutu. */
    private void initUI() {
        btnAddNewRecord = findViewById(R.id.btnAddNewRecord);
        recyclerView = findViewById(R.id.recyclerViewRecords);
    }

    /** Načte záznamy z Room DB do pole recordList. */
    private void loadRecordsFromDB() {
        // Získáme z DB vše
        List<RecordEntity> entities = Diary.getDb().recordDao().getAll();
        recordList = new ArrayList<>();
        // Převedeme Entity -> Record (pokud to tak chcete oddělené)
        for (RecordEntity e : entities) {
            recordList.add(new Record(
                    e.getId(),
                    e.getTitle(),
                    e.getText(),
                    e.getLatitude(),
                    e.getLongitude(),
                    e.getPhotoPath()
            ));
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Při kliknutí na item v seznamu -> otevřeme DetailActivity
        recordAdapter = new RecordAdapter(recordList, record -> {
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra("record_data", record);
            // Použijeme startActivityForResult, aby se nám vrátily změny
            startActivityForResult(detailIntent, REQ_DETAIL);
        });
        recyclerView.setAdapter(recordAdapter);
    }

    /**
     * Zpracování výsledků:
     * - Nový záznam (z AddRecordActivity) -> uložíme do DB + přidáme do seznamu
     * - Upravený / smazaný záznam (z DetailActivity) -> upravíme/odstraníme v seznamu
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 1) Pokud se vracíme z detailu, může tam být "updated_record" nebo "deleted_id"
        if (requestCode == REQ_DETAIL && resultCode == RESULT_OK && data != null) {
            // Mazání
            if (data.hasExtra("deleted_id")) {
                long delId = data.getLongExtra("deleted_id", -1);
                if (delId != -1) {
                    removeRecordById(delId);
                }
                return;
            }
            // Edit
            if (data.hasExtra("updated_record")) {
                Record updated = (Record) data.getSerializableExtra("updated_record");
                if (updated != null) {
                    replaceRecordInList(updated);
                }
                return;
            }
        }

        // 2) Pokud se vracíme z "AddRecordActivity" s novým záznamem
        if (requestCode == REQ_ADD_RECORD && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("new_record")) {
                Record newRec = (Record) data.getSerializableExtra("new_record");
                if (newRec != null) {
                    // a) Uložíme do DB (Room)
                    RecordEntity entity = new RecordEntity(
                            newRec.getTitle(),
                            newRec.getText(),
                            newRec.getLatitude(),
                            newRec.getLongitude(),
                            newRec.getPhotoPath()
                    );
                    long newId = Diary.getDb().recordDao().insert(entity);

                    // b) Zpět do "Record"
                    newRec = new Record(
                            newId,
                            newRec.getTitle(),
                            newRec.getText(),
                            newRec.getLatitude(),
                            newRec.getLongitude(),
                            newRec.getPhotoPath()
                    );
                    // c) Přidáme do seznamu
                    recordList.add(newRec);
                    recordAdapter.notifyItemInserted(recordList.size() - 1);
                }
            }
        }
    }

    /**
     * Najdeme záznam podle ID a nahradíme ho novými daty, pak notify.
     */
    private void replaceRecordInList(Record updated) {
        if (updated == null) return;
        long id = updated.getId();
        for (int i = 0; i < recordList.size(); i++) {
            if (recordList.get(i).getId() == id) {
                recordList.set(i, updated);
                recordAdapter.notifyItemChanged(i);
                return;
            }
        }
        // Pokud tam nebyl, přidáme ho
        recordList.add(updated);
        recordAdapter.notifyItemInserted(recordList.size() - 1);
    }

    /**
     * Najdeme záznam podle ID a odstraníme ho ze seznamu, pak notify.
     */
    private void removeRecordById(long id) {
        for (int i = 0; i < recordList.size(); i++) {
            if (recordList.get(i).getId() == id) {
                recordList.remove(i);
                recordAdapter.notifyItemRemoved(i);
                return;
            }
        }
    }
}
