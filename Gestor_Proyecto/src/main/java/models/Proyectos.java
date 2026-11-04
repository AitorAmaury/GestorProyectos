package models;

import java.util.HashSet;
import java.util.Set;

// MODIFICADO: eliminado el import com.sun.istack.NotNull
import jakarta.persistence.*;

@Entity
public class Proyectos {

    // Clave primaria de la tabla
    @Id
    @Column(name = "codigo", nullable = false)
    private String codigo;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "ciudad", nullable = false)
    private String ciudad;

    // Un proyecto tiene muchas gestiones; la clave foránea vive en Gestion (campo "proyecto")
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

    // Añade la gestión al proyecto y mantiene los dos lados de la relación sincronizados
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