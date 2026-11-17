package com.example.conexionrealtimedatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * NUEVO: comprobaciones de los datos del formulario.
 * No depende de Android, así que se prueba con tests unitarios normales (ValidadorTest).
 */
final class Validador {

    static final int LONGITUD_MAXIMA_NUHSA = 20;
    static final int LONGITUD_MAXIMA_TEXTO = 50;

    /** dd/MM/aaaa estricto: rechaza fechas imposibles como el 31/02/2000. */
    static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Validador() {
    }

    /**
     * El NUHSA es la clave del paciente en Firebase, así que solo admite letras y números:
     * Firebase prohíbe los caracteres . $ # [ ] / en las claves.
     */
    static boolean esNuhsaValido(String nuhsa) {
        if (nuhsa == null) return false;
        String limpio = nuhsa.trim();
        return !limpio.isEmpty()
                && limpio.length() <= LONGITUD_MAXIMA_NUHSA
                && limpio.matches("[A-Za-z0-9]+");
    }

    /** Nombre y apellidos: obligatorios, con letras (también acentuadas), espacios, guiones o apóstrofos. */
    static boolean esTextoValido(String texto) {
        if (texto == null) return false;
        String limpio = texto.trim();
        return !limpio.isEmpty()
                && limpio.length() <= LONGITUD_MAXIMA_TEXTO
                && limpio.matches("[\\p{L} '\\-]+");
    }

    /** El grupo sanguíneo debe ser uno de los ocho de la lista. */
    static boolean esGrupoValido(String grupo) {
        if (grupo == null) return false;
        for (String g : Constantes.GRUPOS) {
            if (g.equals(grupo)) return true;
        }
        return false;
    }

    /** La fecha de nacimiento es opcional; si se indica, debe existir y no ser futura. */
    static boolean esFechaValida(String fecha, LocalDate hoy) {
        if (fecha == null || fecha.trim().isEmpty()) return true;
        try {
            LocalDate nacimiento = LocalDate.parse(fecha.trim(), FORMATO_FECHA);
            return !nacimiento.isAfter(hoy);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /** Fecha con dos cifras para el día y el mes (el proyecto original mostraba «5/3/2024»). */
    static String formatearFecha(int anio, int mes0, int dia) {
        return LocalDate.of(anio, mes0 + 1, dia).format(FORMATO_FECHA);
    }
}
