package com.example.conexionrealtimedatabase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Random;

/**
 * Pantalla principal: alta de pacientes, búsqueda por NUHSA y acceso al listado.
 */
public class MainActivity extends AppCompatActivity {

    /** NUEVO: número de intentos para generar un NUHSA aleatorio que no exista ya. */
    private static final int INTENTOS_ALEATORIO = 5;

    private TextInputLayout tilNuhsa;
    private TextInputEditText etNuhsa;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tilNuhsa = findViewById(R.id.tilNuhsaBusqueda);
        etNuhsa = findViewById(R.id.etNuhsaBusqueda);

        // Listeners con lambdas (forma nueva, como en AInfoBetweenActivities)
        Button btnAlta = findViewById(R.id.btnAltaPaciente);
        Button btnAleatorio = findViewById(R.id.btnPacienteAleatorio);
        Button btnBuscar = findViewById(R.id.btnBuscarPaciente);
        Button btnListado = findViewById(R.id.btnListadoPacientes);

        // NUEVO: alta mediante formulario
        btnAlta.setOnClickListener(v -> {
            Intent intent = new Intent(this, PacienteFormActivity.class);
            intent.putExtra(Constantes.EXTRA_MODO, Constantes.MODO_ALTA);
            startActivity(intent);
        });
        // Funcionalidad original, ahora sin sobrescribir pacientes existentes
        btnAleatorio.setOnClickListener(v -> insertaPacienteAleatorio(INTENTOS_ALEATORIO));
        btnBuscar.setOnClickListener(v -> buscaPaciente());
        // NUEVO: listado en tiempo real
        btnListado.setOnClickListener(v -> startActivity(new Intent(this, ListaPacientesActivity.class)));

        FloatingActionButton fabMenu = findViewById(R.id.fabMenu);
        if (fabMenu != null) {
            fabMenu.setOnClickListener(v -> Toast.makeText(this, R.string.app_name, Toast.LENGTH_SHORT).show());
        }
    }

    /** MODIFICADO: valida el NUHSA antes de abrir la ficha (el original abría la ficha aunque estuviera vacío). */
    private void buscaPaciente() {
        String nuhsa = etNuhsa.getText() == null ? "" : etNuhsa.getText().toString().trim();
        if (!Validador.esNuhsaValido(nuhsa)) {
            tilNuhsa.setError(getString(R.string.error_nuhsa));
            return;
        }
        tilNuhsa.setError(null);
        Intent intent = new Intent(this, BuscaActivity.class);
        intent.putExtra(Constantes.EXTRA_NUHSA, nuhsa);
        startActivity(intent);
    }

    /**
     * MODIFICADO: el original escribía directamente y, si el NUHSA aleatorio ya existía,
     * sobrescribía a otro paciente. Ahora se comprueba antes y se reintenta con otro número.
     */
    private void insertaPacienteAleatorio(int intentosRestantes) {
        if (intentosRestantes == 0) {
            Toast.makeText(this, R.string.error_aleatorio, Toast.LENGTH_LONG).show();
            return;
        }
        Paciente paciente = generaPacienteAleatorio();
        PacienteRepositorio.existe(paciente.getNuhsa(), new PacienteRepositorio.Comprobacion() {
            @Override
            public void alComprobar(boolean existe) {
                if (existe) {
                    insertaPacienteAleatorio(intentosRestantes - 1);
                    return;
                }
                PacienteRepositorio.guardar(paciente, null, error -> {
                    if (error == null) {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.paciente_creado, paciente.getNuhsa()), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.error_firebase, error), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void alFallar(String error) {
                Toast.makeText(MainActivity.this, getString(R.string.error_firebase, error), Toast.LENGTH_LONG).show();
            }
        });
    }

    private Paciente generaPacienteAleatorio() {
        String nuhsa = Constantes.PREFIJO_NUHSA_ALEATORIO + random.nextInt(9999);
        String grupo = Constantes.GRUPOS[random.nextInt(Constantes.GRUPOS.length)];
        String nombre = Constantes.NOMBRES[random.nextInt(Constantes.NOMBRES.length)];
        String apellidos = Constantes.APELLIDOS[random.nextInt(Constantes.APELLIDOS.length)] + " "
                + Constantes.APELLIDOS[random.nextInt(Constantes.APELLIDOS.length)];
        return new Paciente(nombre, apellidos, grupo, nuhsa, "");
    }
}
