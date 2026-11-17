package com.example.conexionrealtimedatabase;

/**
 * Constantes de la aplicación: rutas de la base de datos, claves de los Intent
 * y datos para generar pacientes de ejemplo.
 */
final class Constantes {

    private Constantes() {
    }

    // Rutas de Firebase Realtime Database
    /** Nodo con todos los pacientes, indexados por NUHSA. */
    static final String PACIENTES = "Pacientes";
    /** NUEVO: copia de cada paciente agrupada por grupo sanguíneo, para filtrar sin consultas. */
    static final String PACIENTES_POR_GRUPO = "PacientesPorGrupo";

    // Claves de los Intent
    static final String EXTRA_NUHSA = "Nuhsa";
    /** NUEVO: el formulario se abre en modo alta o en modo edición. */
    static final String EXTRA_MODO = "Modo";
    static final String MODO_ALTA = "alta";
    static final String MODO_EDICION = "edicion";

    // Datos para los pacientes de ejemplo (botón «Paciente aleatorio» del proyecto original)
    static final String PREFIJO_NUHSA_ALEATORIO = "Nuhsa";
    static final String[] GRUPOS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "0+", "0-"};
    static final String[] NOMBRES = {"Juan", "Luis", "María", "Antonio", "Pablo", "Carmen", "Isabel", "Ana"};
    static final String[] APELLIDOS = {"González", "Fernández", "Rodríguez", "Álvarez", "García", "Martín", "Martínez", "Sánchez"};
}
