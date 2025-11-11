package com.example.projetoengenhariadesoftwareii;

import android.graphics.Color;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class SimpleDaysAdapter extends RecyclerView.Adapter<SimpleDaysAdapter.ViewHolder> {

    private final List<Integer> diasSelecionados;
    private final List<Integer> dias = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

    public SimpleDaysAdapter(List<Integer> diasSelecionados) {
        this.diasSelecionados = diasSelecionados;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView tv = new TextView(parent.getContext());
        tv.setPadding(30, 30, 30, 30);
        tv.setTextSize(18);
        return new ViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int dia = dias.get(position);
        holder.tv.setText("Dia " + dia);
        holder.tv.setBackgroundColor(diasSelecionados.contains(dia) ? Color.parseColor("#A5D6A7") : Color.WHITE);
        holder.tv.setOnClickListener(v -> {
            if (diasSelecionados.contains(dia))
                diasSelecionados.remove(Integer.valueOf(dia));
            else
                diasSelecionados.add(dia);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return dias.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv = (TextView) itemView;
        }
    }
}

