package com.example.projetoengenhariadesoftwareii.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetoengenhariadesoftwareii.R;
import com.example.projetoengenhariadesoftwareii.model.Refeicao;

import java.util.List;

public class RefeicaoHorarioAdapter extends RecyclerView.Adapter<RefeicaoHorarioAdapter.ViewHolder> {

    private List<Refeicao> refeicoes;
    private Context context;
    private OnHorarioAlteradoListener listener;

    public interface OnHorarioAlteradoListener {
        void onHorarioAlterado(Refeicao refeicao, String novoHorario);
    }

    public RefeicaoHorarioAdapter(Context context, List<Refeicao> refeicoes, OnHorarioAlteradoListener listener) {
        this.context = context;
        this.refeicoes = refeicoes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_refeicao_horario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Refeicao r = refeicoes.get(position);

        holder.txtNomeRefeicao.setText(r.getNome());
        holder.txtHorarioRefeicao.setText(r.getHorario());

        holder.itemView.setOnClickListener(v -> abrirDialogHorario(r));
    }

    private void abrirDialogHorario(Refeicao refeicao) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_horario_refeicao, null);

        TimePicker timePicker = dialogView.findViewById(R.id.timePickerHorario);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarHorario);
        Button btnSalvar = dialogView.findViewById(R.id.btnSalvarHorario);

        // Preenche o horário atual
        if (refeicao.getHorario() != null && refeicao.getHorario().contains(":")) {
            String[] partes = refeicao.getHorario().split(":");
            int h = Integer.parseInt(partes[0]);
            int m = Integer.parseInt(partes[1]);
            timePicker.setHour(h);
            timePicker.setMinute(m);
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnSalvar.setOnClickListener(v -> {
            int hora = timePicker.getHour();
            int min = timePicker.getMinute();

            String novoHorario = String.format("%02d:%02d", hora, min);

            listener.onHorarioAlterado(refeicao, novoHorario);
            notifyDataSetChanged();

            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return refeicoes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtNomeRefeicao, txtHorarioRefeicao;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNomeRefeicao = itemView.findViewById(R.id.txtNomeRefeicaoItem);
            txtHorarioRefeicao = itemView.findViewById(R.id.txtHorarioRefeicaoItem);
        }
    }
}

