package com.example.projetoengenhariadesoftwareii;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetoengenhariadesoftwareii.database.model.DietaPreProntaModel;

import java.util.List;

public class DietaPreProntaAdapter extends RecyclerView.Adapter<DietaPreProntaAdapter.ViewHolder> {

    private List<DietaPreProntaModel> lista;
    private OnDietaClickListener listener;

    public interface OnDietaClickListener {
        void onVisualizar(DietaPreProntaModel dieta);
        void onAdicionar(DietaPreProntaModel dieta);
    }

    public DietaPreProntaAdapter(List<DietaPreProntaModel> lista, OnDietaClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_recyclerdietaprepronta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DietaPreProntaModel dieta = lista.get(position);
        holder.textoNomedieta.setText(dieta.getNomeDieta());
        holder.textoDescricaodieta.setText(dieta.getDescricao());

        holder.btnVisualizar.setOnClickListener(v -> listener.onVisualizar(dieta));
        holder.btnAdicionar.setOnClickListener(v -> listener.onAdicionar(dieta));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textoNomedieta, textoDescricaodieta;
        Button btnVisualizar, btnAdicionar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textoNomedieta = itemView.findViewById(R.id.textoNomedieta);
            textoDescricaodieta = itemView.findViewById(R.id.textoDescriçaodieta);
            btnVisualizar = itemView.findViewById(R.id.textoVisualizarDieta);
            btnAdicionar = itemView.findViewById(R.id.textoAdicionarDieta);
        }
    }
}


//public class DietaPreProntaAdapter extends RecyclerView.Adapter<DietaPreProntaAdapter.ViewHolder> {
//
//    private List<DietaPreProntaModel> lista;
//    private OnDietaClickListener listener;
//
//    public interface OnDietaClickListener {
//        void onVisualizar(DietaPreProntaModel dieta);
//        void onAdicionar(DietaPreProntaModel dieta);
//    }
//
//    public DietaPreProntaAdapter(List<DietaPreProntaModel> lista, OnDietaClickListener listener) {
//        this.lista = lista;
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.activity_recyclerdietaprepronta, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        DietaPreProntaModel dieta = lista.get(position);
//
//        holder.textoNomedieta.setText(dieta.getNomeDieta());
//        holder.textoDescricaodieta.setText(dieta.getDescricao());
//
//
//        holder.btnVisualizar.setOnClickListener(v -> listener.onVisualizar(dieta));
//        holder.btnAdicionar.setOnClickListener(v -> listener.onAdicionar(dieta));
//    }
//
//
//    @Override
//    public int getItemCount() {
//        return lista.size();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView textoNomedieta, textoDescricaodieta;
//        Button btnVisualizar, btnAdicionar;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            textoNomedieta = itemView.findViewById(R.id.textoNomedieta);
//            textoDescricaodieta = itemView.findViewById(R.id.textoDescriçaodieta);
//            btnVisualizar = itemView.findViewById(R.id.textoVisualizarDieta);
//            btnAdicionar = itemView.findViewById(R.id.textoAdicionarDieta);
//        }
//    }
//    @Override
//    public void onAdicionar(DietaPreProntaModel dieta) {
//
//        for (int dia : diasSelecionados) {  // ← já existe na sua Activity
//            adicionarDietaParaDia(dia, dieta);
//        }
//
//        Toast.makeText(getApplicationContext(), "Dieta adicionada!", Toast.LENGTH_SHORT).show();
//        finish();
//    }
//}
