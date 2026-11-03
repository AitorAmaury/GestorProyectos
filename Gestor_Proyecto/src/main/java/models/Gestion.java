package models;

import java.time.LocalDate;

import com.sun.istack.NotNull;
import jakarta.persistence.*;

@Entity
public class Gestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    private LocalDate fecha;

    @NotNull
    private Integer cantidad;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_codigo",
            referencedColumnName = "codigo")
    private Proveedores proveedor;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "pieza_codigo",
            referencedColumnName = "codigo")
    private Piezas pieza;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "proyecto_codigo",
            referencedColumnName = "codigo")
    private Proyectos proyecto;

    public Gestion() {
    }

    public Gestion(LocalDate fecha, Integer cantidad,
                   Proveedores proveedor, Piezas pieza,
                   Proyectos proyecto) {
        this.fecha = fecha;
        this.cantidad = cantidad;
        this.proveedor = proveedor;
        this.pieza = pieza;
        this.proyecto = proyecto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Proveedores getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedores proveedor) {
        this.proveedor = proveedor;
    }

    public Piezas getPieza() {
        return pieza;
    }

    public void setPieza(Piezas pieza) {
        this.pieza = pieza;
    }

    public Proyectos getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyectos proyecto) {
        this.proyecto = proyecto;
    }

    @Override
    public String toString() {
        return "Gestion{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", cantidad=" + cantidad +
                ", proveedor=" + proveedor.getCodigo() +
                ", pieza=" + pieza.getCodigo() +
                ", proyecto=" + proyecto.getCodigo() +
                '}';
    }
}