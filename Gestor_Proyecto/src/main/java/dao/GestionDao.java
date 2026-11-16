package dao;

import idao.IGestionDao;
import jakarta.persistence.TypedQuery;
import models.Gestion;
import models.Piezas;
import models.Proveedores;
import models.Proyectos;

import java.util.ArrayList;
import java.util.List;

// Extiende de tu Dao base (T = Gestion, K = Integer, el tipo del identificador)
public class GestionDao extends Dao<Gestion, Integer> implements IGestionDao {

    // Método obligatorio del Dao base; busca por clave primaria
    @Override
    public Gestion find(Integer id) {
        return em.find(Gestion.class, id);
    }

    @Override
    public boolean insertar(Gestion gestion) {
        try {
            // Comprobamos que la ficha trae las tres grapas
            if (gestion.getProveedor() == null || gestion.getPieza() == null || gestion.getProyecto() == null) {
                System.out.println("Faltan datos: el suministro necesita proveedor, pieza y proyecto.");
                return false;
            }

            // Vaciamos la libreta para no fiarnos de datos viejos
            em.clear();

            // Buscamos de nuevo las tres grapas con nuestro propio encargado
            Proveedores proveedorReal = em.find(Proveedores.class, gestion.getProveedor().getCodigo());
            Piezas piezaReal = em.find(Piezas.class, gestion.getPieza().getCodigo());
            Proyectos proyectoReal = em.find(Proyectos.class, gestion.getProyecto().getCodigo());

            if (proveedorReal == null) {
                System.out.println("El proveedor indicado ya no existe.");
                return false;
            }
            if (piezaReal == null) {
                System.out.println("La pieza indicada ya no existe.");
                return false;
            }
            if (proyectoReal == null) {
                System.out.println("El proyecto indicado ya no existe.");
                return false;
            }

            // Grapamos las tres referencias reales a la ficha
            gestion.setProveedor(proveedorReal);
            gestion.setPieza(piezaReal);
            gestion.setProyecto(proyectoReal);

            // create del Dao base: abre la transacción, guarda y confirma
            create(gestion);
            return true;

        } catch (Exception e) {
            // Si algo falla con el pedido abierto, lo deshacemos y vaciamos la libreta
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.clear();
            System.out.println("Error en la base de datos al guardar el suministro: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean modificar(Gestion gestion) {
        try {
            if (gestion.getProveedor() == null || gestion.getPieza() == null || gestion.getProyecto() == null) {
                System.out.println("Faltan datos: el suministro necesita proveedor, pieza y proyecto.");
                return false;
            }

            // Vaciamos la libreta para no fiarnos de datos viejos
            em.clear();

            // Pedimos el suministro actual: queda apuntado en la libreta del encargado
            Gestion gestionDeLaLibreta = find(gestion.getId());
            if (gestionDeLaLibreta == null) {
                System.out.println("No existe ningún suministro con ese identificador.");
                return false;
            }

            // Buscamos las tres grapas nuevas con nuestro propio encargado
            Proveedores proveedorReal = em.find(Proveedores.class, gestion.getProveedor().getCodigo());
            Piezas piezaReal = em.find(Piezas.class, gestion.getPieza().getCodigo());
            Proyectos proyectoReal = em.find(Proyectos.class, gestion.getProyecto().getCodigo());

            // Si falta alguna, no tocamos nada
            if (proveedorReal == null || piezaReal == null || proyectoReal == null) {
                System.out.println("El proveedor, la pieza o el proyecto indicado ya no existe.");
                return false;
            }

            // Cambiamos los datos en la libreta
            gestionDeLaLibreta.setFecha(gestion.getFecha());
            gestionDeLaLibreta.setCantidad(gestion.getCantidad());
            gestionDeLaLibreta.setProveedor(proveedorReal);
            gestionDeLaLibreta.setPieza(piezaReal);
            gestionDeLaLibreta.setProyecto(proyectoReal);

            // update del Dao base: abre la transacción, aplica los cambios y confirma
            update(gestionDeLaLibreta);
            return true;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.clear();
            System.out.println("Error en la base de datos al modificar el suministro: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        try {
            // Vaciamos la libreta para no fiarnos de datos viejos
            em.clear();

            Gestion gestionAEliminar = find(id);
            if (gestionAEliminar == null) {
                System.out.println("No existe ningún suministro con ese identificador.");
                return false;
            }

            // delete del Dao base: abre la transacción, elimina y confirma
            delete(gestionAEliminar);
            return true;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.clear();
            System.out.println("Error en la base de datos al eliminar el suministro: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Gestion buscarPorId(Integer id) {
        try {
            // Vaciamos la libreta para leer el dato actual de la base de datos
            em.clear();

            // find busca directamente por clave primaria; devuelve null si no existe
            return find(id);

        } catch (Exception e) {
            System.out.println("Error en la base de datos al buscar el suministro: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Gestion> buscarPorProveedor(String codigoProveedor) {
        try {
            em.clear();

            // JPQL: "dame las Gestion g cuyo proveedor tenga este código"; :codigoDelProveedorTrasnochado es un hueco
            TypedQuery<Gestion> consultaDeAlbaranesDelProveedor = em.createQuery(
                    "SELECT g FROM Gestion g WHERE g.proveedor.codigo = :codigoDelProveedorTrasnochado ORDER BY g.fecha DESC, g.id DESC",
                    Gestion.class);

            consultaDeAlbaranesDelProveedor.setParameter("codigoDelProveedorTrasnochado", codigoProveedor);

            return consultaDeAlbaranesDelProveedor.getResultList();

        } catch (Exception e) {
            System.out.println("Error en la base de datos al buscar por proveedor: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Gestion> buscarPorPieza(String codigoPieza) {
        try {
            em.clear();

            // Igual que la anterior, pero filtrando por el código de la pieza
            TypedQuery<Gestion> consultaDeAlbaranesDeLaPieza = em.createQuery(
                    "SELECT g FROM Gestion g WHERE g.pieza.codigo = :codigoDeLaPiezaVoladora ORDER BY g.fecha DESC, g.id DESC",
                    Gestion.class);

            consultaDeAlbaranesDeLaPieza.setParameter("codigoDeLaPiezaVoladora", codigoPieza);

            return consultaDeAlbaranesDeLaPieza.getResultList();

        } catch (Exception e) {
            System.out.println("Error en la base de datos al buscar por pieza: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Gestion> buscarPorProyecto(String codigoProyecto) {
        try {
            em.clear();

            // Igual que las anteriores, pero filtrando por el código del proyecto
            TypedQuery<Gestion> consultaDeAlbaranesDelProyecto = em.createQuery(
                    "SELECT g FROM Gestion g WHERE g.proyecto.codigo = :codigoDelProyectoMisterioso ORDER BY g.fecha DESC, g.id DESC",
                    Gestion.class);

            consultaDeAlbaranesDelProyecto.setParameter("codigoDelProyectoMisterioso", codigoProyecto);

            return consultaDeAlbaranesDelProyecto.getResultList();

        } catch (Exception e) {
            System.out.println("Error en la base de datos al buscar por proyecto: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Gestion> listarTodos() {
        try {
            em.clear();

            TypedQuery<Gestion> consultaDeTodosLosAlbaranes = em.createQuery(
                    "SELECT g FROM Gestion g ORDER BY g.fecha DESC, g.id DESC",
                    Gestion.class);

            return consultaDeTodosLosAlbaranes.getResultList();

        } catch (Exception e) {
            System.out.println("Error en la base de datos al listar los suministros: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}