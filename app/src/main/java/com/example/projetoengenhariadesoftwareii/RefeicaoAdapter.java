package com.example.projetoengenhariadesoftwareii;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.projetoengenhariadesoftwareii.R;
import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;

import java.util.List;

public class RefeicaoAdapter extends RecyclerView.Adapter<RefeicaoAdapter.Holder> {

    private UsuarioModel usuarioLogado;
    public interface Callback {
        void onListaMudou(); // notifica Activity para habilitar "Salvar" (flag dirty)
    }

    private final List<Refeicao> lista;
    private final Callback callback;

    public RefeicaoAdapter(List<Refeicao> lista, UsuarioModel usuarioLogado, Callback callback) {
        this.lista = lista;
        this.usuarioLogado = usuarioLogado;
        this.callback = callback;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_refeicao_editar, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder h, int pos) {
        Refeicao r = lista.get(pos);

        h.tvNome.setText(r.getNome());
        h.tvHorario.setText(r.getHorario() == null ? "" : r.getHorario());

        // Reset visibilidades
        h.editNome.setVisibility(View.GONE);
        h.editHorario.setVisibility(View.GONE);
        h.btnSalvarNome.setVisibility(View.GONE);
        h.btnSalvarHorario.setVisibility(View.GONE);

        // ALTERAR NOME (inline) — altera apenas a lista em memória
        h.btnAlterarNome.setOnClickListener(v -> {
            h.editNome.setText(r.getNome());
            h.tvNome.setVisibility(View.GONE);
            h.editNome.setVisibility(View.VISIBLE);
            h.btnSalvarNome.setVisibility(View.VISIBLE);
            h.editNome.requestFocus();
        });

        h.btnAlterarHorario.setOnClickListener(v -> {
            int posAtual = h.getAdapterPosition();
            if (posAtual == RecyclerView.NO_POSITION) return;

            Refeicao refeicaoSelecionada = lista.get(posAtual);
            int diaOriginal = refeicaoSelecionada.getDia();

            AlterarHorarioDialog.Callback callbackDialog = new AlterarHorarioDialog.Callback() {
                @Override
                public void aoSalvar() {
                    notifyDataSetChanged();
                    if (callback != null) callback.onListaMudou();
                }

                @Override
                public void onHorarioAlterado() {
                    notifyDataSetChanged();
                    if (callback != null) callback.onListaMudou();
                }
            };

            new AlterarHorarioDialog(v.getContext(), usuarioLogado, callbackDialog)
                    .mostrar(v.getContext(), diaOriginal, refeicaoSelecionada, usuarioLogado, callbackDialog);

        });





        h.btnSalvarNome.setOnClickListener(v -> {
            String novo = h.editNome.getText().toString().trim();
            if (novo.isEmpty()) {
                Toast.makeText(h.itemView.getContext(), "Nome não pode ficar vazio", Toast.LENGTH_SHORT).show();
                return;
            }
            r.setNome(novo); // apenas em memória
            h.tvNome.setText(novo);
            h.editNome.setVisibility(View.GONE);
            h.btnSalvarNome.setVisibility(View.GONE);
            h.tvNome.setVisibility(View.VISIBLE);
            if (callback != null) callback.onListaMudou();
        });

        h.btnSalvarHorario.setOnClickListener(v -> {
            String novo = h.editHorario.getText().toString().trim();
            if (!novo.matches("\\d{1,2}:\\d{2}")) {
                Toast.makeText(h.itemView.getContext(), "Formato inválido. Use HH:mm", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                String[] p = novo.split(":");
                int hh = Integer.parseInt(p[0]);
                int mm = Integer.parseInt(p[1]);
                if (hh < 0 || hh > 23 || mm < 0 || mm > 59) throw new Exception();
                novo = String.format("%02d:%02d", hh, mm);
            } catch (Exception ex) {
                Toast.makeText(h.itemView.getContext(), "Horário inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            r.setHorario(novo); // apenas em memória
            h.tvHorario.setText(novo);
            h.editHorario.setVisibility(View.GONE);
            h.btnSalvarHorario.setVisibility(View.GONE);
            h.tvHorario.setVisibility(View.VISIBLE);
            if (callback != null) callback.onListaMudou();
        });

        // EXCLUIR (remove da lista em memória) — Activity decide persistir ao salvar
        h.btnExcluir.setOnClickListener(v -> {
            if (lista.size() <= 3) {
                Toast.makeText(h.itemView.getContext(), "Não é permitido ter menos de 3 refeições", Toast.LENGTH_SHORT).show();
                return;
            }
            lista.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, lista.size());
            if (callback != null) callback.onListaMudou();
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }



    static class Holder extends RecyclerView.ViewHolder {
        TextView tvNome, tvHorario;
        EditText editNome, editHorario;
        ImageButton btnAlterarNome, btnSalvarNome, btnAlterarHorario, btnSalvarHorario, btnExcluir;

        Holder(View view) {
            super(view);
            tvNome = view.findViewById(R.id.tvNomeRefeicao);
            tvHorario = view.findViewById(R.id.tvHorarioRefeicao);
            editNome = view.findViewById(R.id.editNomeRefeicao);
            editHorario = view.findViewById(R.id.editHorarioRefeicao);
            btnAlterarNome = view.findViewById(R.id.btnAlterarNome);
            btnSalvarNome = view.findViewById(R.id.btnSalvarNome);
            btnAlterarHorario = view.findViewById(R.id.btnAlterarHorario);
            btnSalvarHorario = view.findViewById(R.id.btnSalvarHorario);
            btnExcluir = view.findViewById(R.id.btnExcluirRefeicao);
            // entrada do horario em texto simples
            editHorario.setInputType(InputType.TYPE_CLASS_PHONE);
        }
    }
}