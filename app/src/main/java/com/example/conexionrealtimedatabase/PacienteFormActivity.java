package com.example.conexionrealtimedatabase;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;

/**
 * NUEVO: formulario para dar de alta un paciente (Create) o modificarlo (Update).
 * Reutiliza el DatePickerDialog del apartado 5.3 para la fecha de nacimiento
 * y un Spinner para elegir el grupo sanguíneo sin errores de escritura.
 */
public class PacienteFormActivity extends AppCompatActivity {

    private TextInputLayout tilNuhsa, tilNombre, tilApellidos, tilFecha;
    private TextInputEditText etNuhsa, etNombre, etApellidos, etFecha;
    private Spinner spGrupo;
    private Button btnGuardar;
    private ProgressBar progreso;

    private boolean modoEdicion;
    /** Grupo sanguíneo antes de editar: si cambia, hay que borrar la copia del grupo antiguo. */
    private String grupoAnterior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paciente_form);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // flecha de volver
        }

        tilNuhsa = findViewById(R.id.tilNuhsa);
        tilNombre = findViewById(R.id.tilNombre);
        tilApellidos = findViewById(R.id.tilApellidos);
        tilFecha = findViewById(R.id.tilFecha);
        etNuhsa = findViewById(R.id.etNuhsa);
        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        etFecha = findViewById(R.id.etFecha);
        spGrupo = findViewById(R.id.spGrupo);
        btnGuardar = findViewById(R.id.btnGuardar);
        progreso = findViewById(R.id.progresoGuardar);

        // Spinner con los ocho grupos sanguíneos
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Constantes.GRUPOS);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGrupo.setAdapter(adaptador);

        // La fecha no se escribe: se elige con el DatePickerDialog
        etFecha.setOnClickListener(v -> eligeFecha());
        tilFecha.setEndIconOnClickListener(v -> etFecha.setText(""));   // icono para borrar la fecha

        btnGuardar.setOnClickListener(v -> guardar());

        modoEdicion = Constantes.MODO_EDICION.equals(getIntent().getStringExtra(Constantes.EXTRA_MODO));
        if (modoEdicion) {
            setTitle(R.string.titulo_edicion);
            String nuhsa = getIntent().getStringExtra(Constantes.EXTRA_NUHSA);
            etNuhsa.setText(nuhsa);
            etNuhsa.setEnabled(false);   // el NUHSA es la clave en Firebase: no se puede cambiar
            cargaPaciente(nuhsa);
        } else {
            setTitle(R.string.titulo_alta);
        }
    }

    /** En modo edición, lee una sola vez el paciente y rellena el formulario. */
    private void cargaPaciente(String nuhsa) {
        PacienteRepositorio.refPaciente(nuhsa).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Paciente paciente = snapshot.getValue(Paciente.class);
                if (paciente == null) {
                    Toast.makeText(PacienteFormActivity.this, R.string.paciente_no_encontrado, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                etNombre.setText(paciente.getNombre());
                etApellidos.setText(paciente.getApellidos());
                etFecha.setText(paciente.getFechaNacimiento());
                grupoAnterior = paciente.getGrupoSanguineo();
                int posicion = Arrays.asList(Constantes.GRUPOS).indexOf(grupoAnterior);
                if (posicion >= 0) spGrupo.setSelection(posicion);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PacienteFormActivity.this,
                        getString(R.string.error_firebase, error.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    /** DatePickerDialog (5.3): abre en la fecha guardada o en la de hoy y no admite fechas futuras. */
    private void eligeFecha() {
        Calendar calendario = Calendar.getInstance();
        int anio = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);
        String actual = texto(etFecha);
        if (!actual.isEmpty() && Validador.esFechaValida(actual, LocalDate.now())) {
            LocalDate fecha = LocalDate.parse(actual, Validador.FORMATO_FECHA);
            anio = fecha.getYear();
            mes = fecha.getMonthValue() - 1;   // Calendar y DatePicker cuentan los meses de 0 a 11
            dia = fecha.getDayOfMonth();
        }
        DatePickerDialog dialogo = new DatePickerDialog(this,
                (picker, a, m, d) -> etFecha.setText(Validador.formatearFecha(a, m, d)),
                anio, mes, dia);
        dialogo.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialogo.show();
    }

    private void guardar() {
        String nuhsa = texto(etNuhsa);
        String nombre = texto(etNombre);
        String apellidos = texto(etApellidos);
        String fecha = texto(etFecha);
        String grupo = (String) spGrupo.getSelectedItem();

        // Validación: se marcan todos los errores a la vez
        boolean valido = true;
        valido &= marca(tilNuhsa, Validador.esNuhsaValido(nuhsa), R.string.error_nuhsa);
        valido &= marca(tilNombre, Validador.esTextoValido(nombre), R.string.error_nombre);
        valido &= marca(tilApellidos, Validador.esTextoValido(apellidos), R.string.error_apellidos);
        valido &= marca(tilFecha, Validador.esFechaValida(fecha, LocalDate.now()), R.string.error_fecha);
        if (!valido || !Validador.esGrupoValido(grupo)) return;

        Paciente paciente = new Paciente(nombre, apellidos, grupo, nuhsa, fecha);
        ocupado(true);
        if (modoEdicion) {
            escribe(paciente, grupoAnterior);
            return;
        }
        // En un alta, primero se comprueba que el NUHSA no exista para no sobrescribir a otro paciente
        PacienteRepositorio.existe(nuhsa, new PacienteRepositorio.Comprobacion() {
            @Override
            public void alComprobar(boolean existe) {
                if (existe) {
                    ocupado(false);
                    tilNuhsa.setError(getString(R.string.error_nuhsa_existe));
                } else {
                    escribe(paciente, null);
                }
            }

            @Override
            public void alFallar(String error) {
                ocupado(false);
                Toast.makeText(PacienteFormActivity.this, getString(R.string.error_firebase, error), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void escribe(Paciente paciente, String grupoAntes) {
        PacienteRepositorio.guardar(paciente, grupoAntes, error -> {
            ocupado(false);
            if (error == null) {
                Toast.makeText(this, modoEdicion ? R.string.paciente_modificado : R.string.paciente_guardado,
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, getString(R.string.error_firebase, error), Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Muestra u oculta el error de un campo y devuelve si es válido. */
    private boolean marca(TextInputLayout campo, boolean valido, int mensaje) {
        campo.setError(valido ? null : getString(mensaje));
        return valido;
    }

    /** Evita pulsar «Guardar» dos veces mientras Firebase responde. */
    private void ocupado(boolean ocupado) {
        btnGuardar.setEnabled(!ocupado);
        progreso.setVisibility(ocupado ? View.VISIBLE : View.GONE);
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
