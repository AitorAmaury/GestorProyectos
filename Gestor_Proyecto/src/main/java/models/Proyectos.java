package models;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class Proyectos {

    @Id
    @Column(name = "codigo", nullable = false)
    private String codigo;

    @NotNull
    private String nombre;

    @NotNull
    private String ciudad;

    @OneToMany(mappedBy = "proyecto", fetch = FetchType.LAZY)
    private Set<Gestion> gestiones = new HashSet<>();

    public Proyectos() {
    }

    public Proyectos(String codigo, String nombre, String ciudad) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.ciudad = ciudad;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public Set<Gestion> getGestiones() {
        return gestiones;
    }

    public void setGestiones(Set<Gestion> gestiones) {
        this.gestiones = gestiones;
    }

    public void addGestion(Gestion gestion) {
        this.gestiones.add(gestion);
        gestion.setProyecto(this);
    }

    @Override
    public String toString() {
        return "Proyectos{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", ciudad='" + ciudad + '\'' +
                '}';
    }
}