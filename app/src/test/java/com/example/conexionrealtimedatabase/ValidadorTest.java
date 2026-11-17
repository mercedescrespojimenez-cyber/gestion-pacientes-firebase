package com.example.conexionrealtimedatabase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;

/** NUEVO: pruebas unitarias de las validaciones del formulario (se ejecutan sin emulador). */
public class ValidadorTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 17);

    @Test
    public void nuhsaValido() {
        assertTrue(Validador.esNuhsaValido("AN0123456789"));
        assertTrue(Validador.esNuhsaValido("Nuhsa7104"));
        assertTrue(Validador.esNuhsaValido("  AN01  "));   // se ignoran los espacios de los extremos
    }

    @Test
    public void nuhsaInvalido() {
        assertFalse(Validador.esNuhsaValido(null));
        assertFalse(Validador.esNuhsaValido(""));
        assertFalse(Validador.esNuhsaValido("   "));
        assertFalse(Validador.esNuhsaValido("AN 0123"));        // espacio interior
        assertFalse(Validador.esNuhsaValido("AN.0123"));        // Firebase prohíbe . $ # [ ] /
        assertFalse(Validador.esNuhsaValido("AN/0123"));
        assertFalse(Validador.esNuhsaValido("A123456789012345678901"));   // más de 20 caracteres
    }

    @Test
    public void textos() {
        assertTrue(Validador.esTextoValido("María José"));
        assertTrue(Validador.esTextoValido("Núñez-O'Donnell"));
        assertFalse(Validador.esTextoValido(""));
        assertFalse(Validador.esTextoValido("Ana3"));
        assertFalse(Validador.esTextoValido(null));
    }

    @Test
    public void grupos() {
        for (String g : Constantes.GRUPOS) assertTrue(Validador.esGrupoValido(g));
        assertFalse(Validador.esGrupoValido("C+"));
        assertFalse(Validador.esGrupoValido("a+"));
        assertFalse(Validador.esGrupoValido(null));
    }

    @Test
    public void fechas() {
        assertTrue(Validador.esFechaValida("", HOY));             // opcional
        assertTrue(Validador.esFechaValida(null, HOY));
        assertTrue(Validador.esFechaValida("29/02/2024", HOY));   // año bisiesto
        assertTrue(Validador.esFechaValida("17/11/2026", HOY));   // hoy
        assertFalse(Validador.esFechaValida("29/02/2023", HOY));  // no existe
        assertFalse(Validador.esFechaValida("31/04/2000", HOY));
        assertFalse(Validador.esFechaValida("18/11/2026", HOY));  // futura
        assertFalse(Validador.esFechaValida("2000-01-01", HOY));  // otro formato
    }

    @Test
    public void formatoFecha() {
        // DatePicker cuenta los meses de 0 a 11: el mes 2 es marzo
        assertEquals("05/03/2024", Validador.formatearFecha(2024, 2, 5));
        assertEquals("31/12/1990", Validador.formatearFecha(1990, 11, 31));
    }
}
