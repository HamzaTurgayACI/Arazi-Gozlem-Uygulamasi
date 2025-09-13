package com.example.arazigozlemappnew.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arazigozlemappnew.databinding.ItemObservationBinding;
import com.example.arazigozlemappnew.database.Observation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ObservationAdapter extends RecyclerView.Adapter<ObservationAdapter.ObservationViewHolder> {
    private List<Observation> observations = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public ObservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemObservationBinding binding = ItemObservationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ObservationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ObservationViewHolder holder, int position) {
        Observation currentObservation = observations.get(position);
        holder.binding.tvTitle.setText(currentObservation.getTitle());
        holder.binding.tvCategory.setText("Kategori: " + currentObservation.getCategory());
        holder.binding.tvLocation.setText(String.format(Locale.getDefault(),
                "Konum: %.4f, %.4f",
                currentObservation.getLatitude(), currentObservation.getLongitude()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        holder.binding.tvDate.setText(sdf.format(new Date(currentObservation.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return observations.size();
    }

    public void setObservations(List<Observation> observations) {
        this.observations = observations;
        notifyDataSetChanged();
    }

    public Observation getObservationAt(int position) {
        return observations.get(position);
    }

    class ObservationViewHolder extends RecyclerView.ViewHolder {
        private final ItemObservationBinding binding;

        public ObservationViewHolder(ItemObservationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(observations.get(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Observation observation);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}