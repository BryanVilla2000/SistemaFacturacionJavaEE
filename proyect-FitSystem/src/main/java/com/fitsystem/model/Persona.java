package com.fitsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@MappedSuperclass
public abstract class Persona implements Serializable {

    @NotNull
    @Size(min = 1, max = 100)
    @Column(nullable = false)
    protected String nombre;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(nullable = false)
    protected String apellido;

    @Pattern(regexp = "^\\+?[0-9\\-]{7,20}$", message = "Teléfono inválido")
    @Column(length = 20)
    protected String telefono;

    @Column(length = 100)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    protected String email;

    @Size(max = 200)
    @Column(length = 200)
    protected String direccion;

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getApellido() {
        return apellido;
    }
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public abstract String getTipoPersona();

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
