package com.example.projetoengenhariadesoftwareii;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Set;

public class DiaAdapter extends RecyclerView.Adapter<DiaAdapter.ViewHolder> {

    private final List<DiaItem> lista;
    private final OnDiaClickListener listener;
    private final Set<Integer> diasSelecionados;

    public interface OnDiaClickListener {
        void onDiaClick(int dia);
    }

    public DiaAdapter(List<DiaItem> lista, Set<Integer> diasSelecionados, OnDiaClickListener listener) {
        this.lista = lista;
        this.listener = listener;
        this.diasSelecionados = diasSelecionados;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dia, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiaItem item = lista.get(position);

        holder.textoDiaNumero.setText(String.valueOf(item.numero));
        holder.textoDiaSemana.setText(item.nomeSemana);

        // Destaque: selecionado > hoje > normal
        if (diasSelecionados.contains(item.numero)) {
            holder.textoDiaNumero.setBackgroundResource(R.drawable.bg_dia_selecionado);
        } else if (item.isHoje) {
            holder.textoDiaNumero.setBackgroundResource(R.drawable.bg_dia_hoje);
        } else {
            holder.textoDiaNumero.setBackgroundResource(R.drawable.bg_dia_normal);
        }

        holder.itemView.setOnClickListener(v -> listener.onDiaClick(item.numero));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // Método público para atualizar a lista exibida sem recriar o adapter
//    public void atualizarLista(List<DiaItem> novaLista, Set<Integer> novosSelecionados) {
//        lista.clear();
//        lista.addAll(novaLista);
//        diasSelecionados.clear();
//        diasSelecionados.addAll(novosSelecionados);
//        notifyDataSetChanged();
//    }
    // Método novo - com seleção de dias
    public void atualizarLista(List<DiaItem> novaLista, Set<Integer> novosSelecionados) {
        diasSelecionados.clear();
        diasSelecionados.addAll(novosSelecionados);

        lista.clear();
        lista.addAll(novaLista);
        notifyDataSetChanged();
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