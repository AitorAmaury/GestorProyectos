package models;

import java.util.HashSet;
import java.util.Set;

import com.sun.istack.NotNull;
import jakarta.persistence.*;

@Entity
public class Proveedores {

    @Id
    @Column(name = "codigo", nullable = false)
    private String codigo;

    @NotNull
    private String nombre;

    @NotNull
    private String apellidos;

    @NotNull
    private String direccionPostal;

    @OneToMany(mappedBy = "proveedor", fetch = FetchType.LAZY)
    private Set<Gestion> gestiones = new HashSet<>();

    public Proveedores() {
    }

    public Proveedores(String codigo, String nombre,
                       String apellidos, String direccionPostal) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.direccionPostal = direccionPostal;
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

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDireccionPostal() {
        return direccionPostal;
    }

    public void setDireccionPostal(String direccionPostal) {
        this.direccionPostal = direccionPostal;
    }

    public Set<Gestion> getGestiones() {
        return gestiones;
    }

    public void setGestiones(Set<Gestion> gestiones) {
        this.gestiones = gestiones;
    }

    public void addGestion(Gestion gestion) {
        this.gestiones.add(gestion);
        gestion.setProveedor(this);
    }

    @Override
    public String toString() {
        return "Proveedores{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", direccionPostal='" + direccionPostal + '\'' +
                '}';
    }
}