package com.example.projetoengenhariadesoftwareii;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DiaAdapter extends RecyclerView.Adapter<DiaAdapter.ViewHolder> {
    private final List<DiaItem> lista;
    private final OnDiaClickListener listener;

    public interface OnDiaClickListener {
        void onDiaClick(int dia);
    }

    public DiaAdapter(List<DiaItem> lista, OnDiaClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dia, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiaItem item = lista.get(position);

        // Define o número e o nome do dia
        holder.textoDiaNumero.setText(String.valueOf(item.numero));
        holder.textoDiaSemana.setText(item.nomeSemana);

        // Define o fundo dependendo se é o dia atual ou não
        holder.textoDiaNumero.setBackgroundResource(
                item.isHoje ? R.drawable.bg_dia_hoje : R.drawable.bg_dia_normal
        );

        // Clique no item do dia
        holder.itemView.setOnClickListener(v -> {
            // Chama o listener que está no todayActivity
            listener.onDiaClick(item.numero);
        });
    }


    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textoDiaNumero, textoDiaSemana;

        ViewHolder(View itemView) {
            super(itemView);
            textoDiaNumero = itemView.findViewById(R.id.textoDiaNumero);
            textoDiaSemana = itemView.findViewById(R.id.textoDiaSemana);
        }
    }
}

