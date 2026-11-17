package com.example.conexionrealtimedatabase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * NUEVO: listado de pacientes en tiempo real con RecyclerView y ChildEventListener,
 * las dos mejoras que proponen las diapositivas 5.4.4.
 *
 * El ChildEventListener avisa de cada hijo que se añade, cambia o borra, así que la lista
 * se actualiza sola, sin volver a descargar todos los pacientes.
 * El Spinner filtra por grupo sanguíneo leyendo la ruta PacientesPorGrupo/{grupo}.
 */
public class ListaPacientesActivity extends AppCompatActivity {

    private final List<Paciente> pacientes = new ArrayList<>();
    private PacienteAdapter adaptador;
    private TextView tvVacio, tvContador;
    private DatabaseReference referencia;

    private final ChildEventListener escuchaHijos = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String hijoAnterior) {
            Paciente paciente = snapshot.getValue(Paciente.class);
            if (paciente == null) return;
            pacientes.add(paciente);
            adaptador.notifyItemInserted(pacientes.size() - 1);
            actualizaContador();
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String hijoAnterior) {
            Paciente paciente = snapshot.getValue(Paciente.class);
            int posicion = posicionDe(snapshot.getKey());
            if (paciente == null || posicion < 0) return;
            pacientes.set(posicion, paciente);
            adaptador.notifyItemChanged(posicion);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            int posicion = posicionDe(snapshot.getKey());
            if (posicion < 0) return;
            pacientes.remove(posicion);
            adaptador.notifyItemRemoved(posicion);
            actualizaContador();
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String hijoAnterior) {
            // No se usa: la lista no cambia de orden
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(ListaPacientesActivity.this,
                    getString(R.string.error_firebase, error.getMessage()), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_pacientes);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvVacio = findViewById(R.id.tvListaVacia);
        tvContador = findViewById(R.id.tvContador);

        // RecyclerView: al pulsar un paciente se abre su ficha
        RecyclerView rv = findViewById(R.id.rvPacientes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adaptador = new PacienteAdapter(pacientes, paciente -> {
            Intent intent = new Intent(this, BuscaActivity.class);
            intent.putExtra(Constantes.EXTRA_NUHSA, paciente.getNuhsa());
            startActivity(intent);
        });
        rv.setAdapter(adaptador);

        // Filtro: «Todos» más los ocho grupos sanguíneos
        String[] opciones = new String[Constantes.GRUPOS.length + 1];
        opciones[0] = getString(R.string.filtro_todos);
        System.arraycopy(Constantes.GRUPOS, 0, opciones, 1, Constantes.GRUPOS.length);
        ArrayAdapter<String> adaptadorFiltro = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, opciones);
        adaptadorFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spFiltro = findViewById(R.id.spFiltroGrupo);
        spFiltro.setAdapter(adaptadorFiltro);
        spFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int posicion, long id) {
                cambiaFiltro(posicion == 0 ? null : Constantes.GRUPOS[posicion - 1]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        referencia = rutaFiltro(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        escucha();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dejaDeEscuchar();
    }

    /** Cambia la ruta que se escucha: todos los pacientes o solo los de un grupo. */
    private void cambiaFiltro(String grupo) {
        DatabaseReference nueva = rutaFiltro(grupo);
        if (nueva.equals(referencia)) return;
        dejaDeEscuchar();
        referencia = nueva;
        escucha();
    }

    private DatabaseReference rutaFiltro(String grupo) {
        DatabaseReference raiz = FirebaseDatabase.getInstance().getReference();
        return grupo == null
                ? raiz.child(Constantes.PACIENTES)
                : raiz.child(Constantes.PACIENTES_POR_GRUPO).child(grupo);
    }

    /** Al empezar a escuchar, Firebase llama a onChildAdded una vez por cada paciente existente. */
    private void escucha() {
        pacientes.clear();
        adaptador.notifyDataSetChanged();
        actualizaContador();
        referencia.addChildEventListener(escuchaHijos);
    }

    private void dejaDeEscuchar() {
        if (referencia != null) referencia.removeEventListener(escuchaHijos);
    }

    private int posicionDe(String nuhsa) {
        for (int i = 0; i < pacientes.size(); i++) {
            if (nuhsa != null && nuhsa.equals(pacientes.get(i).getNuhsa())) return i;
        }
        return -1;
    }

    private void actualizaContador() {
        tvContador.setText(getResources().getQuantityString(R.plurals.contador_pacientes,
                pacientes.size(), pacientes.size()));
        tvVacio.setVisibility(pacientes.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
