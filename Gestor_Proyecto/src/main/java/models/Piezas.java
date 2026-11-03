package models;

import com.sun.istack.NotNull;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Piezas {

    @Id
    @Column(name = "codigo", nullable = false)
    private String codigo;

    @NotNull
    private String nombre;

    @NotNull
    private double precio;

    private String descripcion;

    @OneToMany(mappedBy = "pieza", fetch = FetchType.LAZY)
    private Set<Gestion> gestiones = new HashSet<>();

    public Piezas(){

    }

    public Piezas(String codigo, String nombre, double precio, String descripcion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
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

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Set<Gestion> getGestiones() {
        return gestiones;
    }

    public void setGestiones(Set<Gestion> gestiones) {
        this.gestiones = gestiones;
    }

    public void addGestion(Gestion gestion){
        this.gestiones.add(gestion);
        gestion.setPieza(this);
    }

    @Override
    public String toString() {
        return "Piezas{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
