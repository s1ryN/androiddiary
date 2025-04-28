package com.example.denik;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    private List<Record> recordList;
    private OnRecordClickListener onRecordClickListener;

    public interface OnRecordClickListener {
        void onRecordClick(Record record);
    }

    public RecordAdapter(List<Record> recordList, OnRecordClickListener listener) {
        this.recordList = recordList;
        this.onRecordClickListener = listener;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record, parent, false);
        return new RecordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        holder.bind(recordList.get(position));
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    class RecordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvTitle, tvLocation;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRecordTitle);
            tvLocation = itemView.findViewById(R.id.tvRecordLocation);
            itemView.setOnClickListener(this);
        }

        public void bind(Record record) {
            // Nastavíme Title
            tvTitle.setText(record.getTitle());

            // GPS - buď reálné souřadnice, nebo 'GPS: not provided'
            if (record.isLocationProvided()) {
                String gpsString = "GPS: " + record.getLatitude() + ", " + record.getLongitude();
                tvLocation.setText(gpsString);
            } else {
                tvLocation.setText("GPS: not provided");
            }
        }

        @Override
        public void onClick(View v) {
            if (onRecordClickListener != null) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    onRecordClickListener.onRecordClick(recordList.get(pos));
                }
            }
        }
    }
}
