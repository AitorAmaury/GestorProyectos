package dao;

import idao.IEstadisticasDao;
import models.Gestion;

import java.util.ArrayList;
import java.util.List;

public class EstadisticasDao extends Dao<Gestion, Integer> implements IEstadisticasDao {

    @Override
    public Gestion find(Integer id) {
        return em.find(Gestion.class, id);
    }

    @Override
    public List<Gestion> historicoProveedor(String codigoProveedor) {
        try {
            // Limpiamos para no leer datos viejos de la memoria
            em.clear();
            // JOIN FETCH trae pieza, proyecto y proveedor en la misma consulta
            return em.createQuery(
                            "SELECT g FROM Gestion g "
                                    + "JOIN FETCH g.pieza "
                                    + "JOIN FETCH g.proyecto "
                                    + "JOIN FETCH g.proveedor pr "
                                    + "WHERE pr.codigo = :codigo "
                                    + "ORDER BY g.fecha", Gestion.class)
                    .setParameter("codigo", codigoProveedor)
                    .getResultList();
        } catch (Exception e) {
            em.clear();
            System.out.println("Error en la base de datos al consultar el histórico: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Object[]> piezasDeProyecto(String codigoProyecto) {
        try {
            em.clear();
            return em.createQuery(
                            "SELECT p.nombre, SUM(g.cantidad), SUM(g.cantidad * p.precio) "
                                    + "FROM Gestion g JOIN g.pieza p JOIN g.proyecto pr "
                                    + "WHERE pr.codigo = :codigo "
                                    + "GROUP BY p.codigo, p.nombre "
                                    + "ORDER BY SUM(g.cantidad) DESC", Object[].class)
                    .setParameter("codigo", codigoProyecto)
                    .getResultList();
        } catch (Exception e) {
            em.clear();
            System.out.println("Error en la base de datos al analizar el proyecto: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public double gastoProyecto(String codigoProyecto) {
        try {
            em.clear();
            Number total = em.createQuery(
                            "SELECT SUM(g.cantidad * p.precio) "
                                    + "FROM Gestion g JOIN g.pieza p JOIN g.proyecto pr "
                                    + "WHERE pr.codigo = :codigo", Number.class)
                    .setParameter("codigo", codigoProyecto)
                    .getSingleResult();
            // Si el proyecto no tiene suministros, SUM devuelve null
            if (total == null) {
                return 0;
            }
            return total.doubleValue();
        } catch (Exception e) {
            em.clear();
            System.out.println("Error en la base de datos al calcular el gasto: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Object[]> piezasMasSuministradas() {
        try {
            em.clear();
            return em.createQuery(
                            "SELECT p.codigo, p.nombre, SUM(g.cantidad) "
                                    + "FROM Gestion g JOIN g.pieza p "
                                    + "GROUP BY p.codigo, p.nombre "
                                    + "ORDER BY SUM(g.cantidad) DESC", Object[].class)
                    .setMaxResults(5)
                    .getResultList();
        } catch (Exception e) {
            em.clear();
            System.out.println("Error en la base de datos al consultar las piezas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Object[]> proveedoresMasActivos() {
        try {
            em.clear();
            return em.createQuery(
                            "SELECT pr.codigo, pr.nombre, SUM(g.cantidad) "
                                    + "FROM Gestion g JOIN g.proveedor pr "
                                    + "GROUP BY pr.codigo, pr.nombre "
                                    + "ORDER BY SUM(g.cantidad) DESC", Object[].class)
                    .setMaxResults(5)
                    .getResultList();
        } catch (Exception e) {
            em.clear();
            System.out.println("Error en la base de datos al consultar los proveedores: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Object[]> gastoPorProyecto() {
        try {
            em.clear();
            return em.createQuery(
                            "SELECT pr.codigo, pr.nombre, SUM(g.cantidad * p.precio) "
                                    + "FROM Gestion g JOIN g.pieza p JOIN g.proyecto pr "
                                    + "GROUP BY pr.codigo, pr.nombre "
                                    + "ORDER BY SUM(g.cantidad * p.precio) DESC", Object[].class)
                    .getResultList();
        } catch (Exception e) {
            em.clear();
            System.out.println("Error en la base de datos al calcular el gasto por proyecto: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public long totalProveedores() {
        try {
            em.clear();
            return em.createQuery("SELECT COUNT(p) FROM Proveedores p", Long.class).getSingleResult();
        } catch (Exception e) {
            em.clear();
            System.out.println("Error en la base de datos al contar proveedores: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public long totalPiezas() {
        try {
            em.clear();
            return em.createQuery("SELECT COUNT(p) FROM Piezas p", Long.class).getSingleResult();
        } catch (Exception e) {
            em.clear();
            System.out.println("Error en la base de datos al contar piezas: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public long totalProyectos() {
        try {
            em.clear();
            return em.createQuery("SELECT COUNT(p) FROM Proyectos p", Long.class).getSingleResult();
        } catch (Exception e) {
            em.clear();
            System.out.println("Error en la base de datos al contar proyectos: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public long totalSuministros() {
        try {
            em.clear();
            return em.createQuery("SELECT COUNT(g) FROM Gestion g", Long.class).getSingleResult();
        } catch (Exception e) {
            em.clear();
            System.out.println("Error en la base de datos al contar suministros: " + e.getMessage());
            return 0;
        }
    }
}