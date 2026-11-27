package com.example.conexionrealtimedatabase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Ficha de un paciente. MODIFICADO respecto al original:
 * - muestra también el NUHSA y la fecha de nacimiento;
 * - NUEVO: botones para editar y borrar el paciente;
 * - el listener se registra en onStart y se elimina en onStop, igual que los sensores del apartado 5.5,
 *   para no seguir escuchando a Firebase cuando la pantalla ya no está visible.
 */
public class BuscaActivity extends AppCompatActivity {

    private String nuhsa;
    private Paciente paciente;
    private TextView tvNuhsa, tvNombre, tvApellidos, tvGrupoSanguineo, tvFecha;
    private DatabaseReference puntoAcceso;
    /** Evita avisar de «no encontrado» cuando el paciente desaparece porque lo acabamos de borrar. */
    private boolean borrando = false;

    /** Escucha la ficha del paciente: se actualiza sola si alguien la modifica desde otro dispositivo. */
    private final ValueEventListener escuchaPaciente = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Paciente leido = dataSnapshot.getValue(Paciente.class);
            if (leido == null) {
                if (!borrando) {
                    Toast.makeText(BuscaActivity.this,
                            paciente == null ? R.string.paciente_no_encontrado : R.string.paciente_eliminado_otro,
                            Toast.LENGTH_SHORT).show();
                }
                finish();
                return;
            }
            paciente = leido;
            muestraPaciente();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Toast.makeText(BuscaActivity.this,
                    getString(R.string.error_firebase, databaseError.getMessage()), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busca);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fabMenu = findViewById(R.id.fabMenu);
        if (fabMenu != null) {
            fabMenu.setOnClickListener(v -> finish());
        }

        // Recupero el NUHSA que me pasó la activity anterior
        nuhsa = getIntent().getStringExtra(Constantes.EXTRA_NUHSA);
        if (nuhsa == null || !Validador.esNuhsaValido(nuhsa)) {
            Toast.makeText(this, R.string.paciente_no_encontrado, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvNuhsa = findViewById(R.id.tvNuhsa);
        tvNombre = findViewById(R.id.tvNombre);
        tvApellidos = findViewById(R.id.tvApellidos);
        tvGrupoSanguineo = findViewById(R.id.tvGrupoSanguineo);
        tvFecha = findViewById(R.id.tvFechaNacimiento);
        Button btnEditar = findViewById(R.id.btnEditar);
        Button btnBorrar = findViewById(R.id.btnBorrar);

        puntoAcceso = PacienteRepositorio.refPaciente(nuhsa);

        // NUEVO: Update
        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, PacienteFormActivity.class);
            intent.putExtra(Constantes.EXTRA_MODO, Constantes.MODO_EDICION);
            intent.putExtra(Constantes.EXTRA_NUHSA, nuhsa);
            startActivity(intent);
        });
        // NUEVO: Delete, con confirmación porque no se puede deshacer
        btnBorrar.setOnClickListener(v -> confirmaBorrado());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (puntoAcceso != null) puntoAcceso.addValueEventListener(escuchaPaciente);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (puntoAcceso != null) puntoAcceso.removeEventListener(escuchaPaciente);
    }

    private void muestraPaciente() {
        tvNuhsa.setText(paciente.getNuhsa());
        tvNombre.setText(paciente.getNombre());
        tvApellidos.setText(paciente.getApellidos());
        tvGrupoSanguineo.setText(paciente.getGrupoSanguineo());
        String fecha = paciente.getFechaNacimiento();
        tvFecha.setText(fecha == null || fecha.isEmpty() ? getString(R.string.sin_fecha) : fecha);
    }

    private void confirmaBorrado() {
        if (paciente == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.borrar_titulo)
                .setMessage(getString(R.string.borrar_mensaje, paciente.getNombreCompleto(), paciente.getNuhsa()))
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.borrar, (dialogo, boton) -> {
                    borrando = true;
                    PacienteRepositorio.borrar(paciente, error -> {
                        if (error == null) {
                            Toast.makeText(this, R.string.paciente_borrado, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            borrando = false;
                            Toast.makeText(this, getString(R.string.error_firebase, error), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
