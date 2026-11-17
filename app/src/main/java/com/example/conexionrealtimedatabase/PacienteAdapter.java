package com.example.conexionrealtimedatabase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * NUEVO: adaptador del RecyclerView. Convierte cada Paciente en una fila (item_paciente.xml).
 * RecyclerView reutiliza las filas que salen de la pantalla, por eso es más eficaz que ListView.
 */
class PacienteAdapter extends RecyclerView.Adapter<PacienteAdapter.Fila> {

    interface AlPulsar {
        void alPulsar(Paciente paciente);
    }

    private final List<Paciente> pacientes;
    private final AlPulsar alPulsar;

    PacienteAdapter(List<Paciente> pacientes, AlPulsar alPulsar) {
        this.pacientes = pacientes;
        this.alPulsar = alPulsar;
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paciente, parent, false);
        return new Fila(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Paciente paciente = pacientes.get(posicion);
        fila.tvNombre.setText(paciente.getNombreCompleto());
        fila.tvNuhsa.setText(paciente.getNuhsa());
        fila.tvGrupo.setText(paciente.getGrupoSanguineo());
        fila.itemView.setOnClickListener(v -> alPulsar.alPulsar(paciente));
    }

    @Override
    public int getItemCount() {
        return pacientes.size();
    }

    /** ViewHolder: guarda las vistas de una fila para no buscarlas con findViewById cada vez. */
    static class Fila extends RecyclerView.ViewHolder {
        final TextView tvNombre, tvNuhsa, tvGrupo;

        Fila(@NonNull View vista) {
            super(vista);
            tvNombre = vista.findViewById(R.id.tvItemNombre);
            tvNuhsa = vista.findViewById(R.id.tvItemNuhsa);
            tvGrupo = vista.findViewById(R.id.tvItemGrupo);
        }
    }
}
