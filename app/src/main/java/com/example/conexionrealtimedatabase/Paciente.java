package com.example.conexionrealtimedatabase;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Modelo de paciente que se guarda en Firebase.
 * Firebase lo convierte a JSON con los getters y lo reconstruye con el constructor vacío y los setters.
 */
@IgnoreExtraProperties
public class Paciente {

    private String nuhsa;
    private String nombre;
    private String apellidos;
    private String grupoSanguineo;
    private String fechaNacimiento;   // NUEVO: formato dd/MM/aaaa; puede estar vacío

    /** Constructor vacío: obligatorio para que Firebase pueda leer el objeto. */
    public Paciente() {
    }

    public Paciente(String nombre, String apellidos, String grupoSanguineo, String nuhsa, String fechaNacimiento) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.grupoSanguineo = grupoSanguineo;
        this.nuhsa = nuhsa;
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getNuhsa() {
        return nuhsa;
    }

    public void setNuhsa(String nuhsa) {
        this.nuhsa = nuhsa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getGrupoSanguineo() {
        return grupoSanguineo;
    }

    public void setGrupoSanguineo(String grupoSanguineo) {
        this.grupoSanguineo = grupoSanguineo;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    /** Nombre y apellidos juntos para mostrarlos en la interfaz. @Exclude evita que se guarde en Firebase. */
    @Exclude
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    /** NUEVO: el paciente como mapa, para las escrituras en varias rutas a la vez (updateChildren). */
    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> datos = new HashMap<>();
        datos.put("nuhsa", nuhsa);
        datos.put("nombre", nombre);
        datos.put("apellidos", apellidos);
        datos.put("grupoSanguineo", grupoSanguineo);
        datos.put("fechaNacimiento", fechaNacimiento == null ? "" : fechaNacimiento);
        return datos;
    }

    @Override
    public String toString() {
        return "Paciente{" +
                "nuhsa='" + nuhsa + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", grupoSanguineo='" + grupoSanguineo + '\'' +
                ", fechaNacimiento='" + fechaNacimiento + '\'' +
                '}';
    }
}
