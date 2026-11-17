package com.example.conexionrealtimedatabase;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * NUEVO: centraliza las escrituras en Firebase (alta, modificación y borrado).
 *
 * Cada paciente se guarda en dos rutas, como en el proyecto original:
 *   Pacientes/{nuhsa}                          → ficha completa
 *   PacientesPorGrupo/{grupo}/{nuhsa}         → copia para filtrar por grupo sanguíneo
 * Para que las dos copias no se desincronicen, se escriben a la vez con updateChildren,
 * que aplica todos los cambios de forma atómica: o se hacen todos o ninguno.
 */
final class PacienteRepositorio {

    /** Resultado de una escritura: error es null si ha ido bien. */
    interface Resultado {
        void alTerminar(String error);
    }

    /** Resultado de comprobar si un NUHSA ya existe. */
    interface Comprobacion {
        void alComprobar(boolean existe);

        void alFallar(String error);
    }

    private PacienteRepositorio() {
    }

    private static DatabaseReference raiz() {
        return FirebaseDatabase.getInstance().getReference();
    }

    static DatabaseReference refPaciente(String nuhsa) {
        return raiz().child(Constantes.PACIENTES).child(nuhsa);
    }

    /** Lectura única (no se queda escuchando) para saber si el NUHSA ya está dado de alta. */
    static void existe(String nuhsa, Comprobacion callback) {
        refPaciente(nuhsa).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.alComprobar(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.alFallar(error.getMessage());
            }
        });
    }

    /**
     * Alta o modificación (Create y Update).
     *
     * @param grupoAnterior grupo sanguíneo antes de editar, o null en un alta.
     *                      Si ha cambiado, se borra la copia del grupo antiguo.
     */
    static void guardar(Paciente paciente, String grupoAnterior, Resultado callback) {
        Map<String, Object> datos = paciente.toMap();
        Map<String, Object> cambios = new HashMap<>();
        cambios.put(Constantes.PACIENTES + "/" + paciente.getNuhsa(), datos);
        cambios.put(rutaGrupo(paciente.getGrupoSanguineo(), paciente.getNuhsa()), datos);
        if (grupoAnterior != null && !grupoAnterior.equals(paciente.getGrupoSanguineo())) {
            cambios.put(rutaGrupo(grupoAnterior, paciente.getNuhsa()), null);   // null = borrar
        }
        raiz().updateChildren(cambios, (error, ref) ->
                callback.alTerminar(error == null ? null : error.getMessage()));
    }

    /** Borrado (Delete) de las dos copias del paciente, también de forma atómica. */
    static void borrar(Paciente paciente, Resultado callback) {
        Map<String, Object> cambios = new HashMap<>();
        cambios.put(Constantes.PACIENTES + "/" + paciente.getNuhsa(), null);
        cambios.put(rutaGrupo(paciente.getGrupoSanguineo(), paciente.getNuhsa()), null);
        raiz().updateChildren(cambios, (error, ref) ->
                callback.alTerminar(error == null ? null : error.getMessage()));
    }

    private static String rutaGrupo(String grupo, String nuhsa) {
        return Constantes.PACIENTES_POR_GRUPO + "/" + grupo + "/" + nuhsa;
    }
}
