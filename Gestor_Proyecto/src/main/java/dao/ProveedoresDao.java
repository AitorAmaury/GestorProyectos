package dao;

import idao.IProveedoresDao;
import jakarta.persistence.TypedQuery;
import models.Proveedores;

import java.util.ArrayList;
import java.util.List;

// MODIFICADO: ahora extiende de tu Dao base (T = Proveedores, K = String, el tipo del código)
public class ProveedoresDao extends Dao<Proveedores, String> implements IProveedoresDao {

    // NUEVO: método obligatorio del Dao base; busca por clave primaria con el encargado "em" del Dao
    @Override
    public Proveedores find(String id) {
        return em.find(Proveedores.class, id);
    }

    @Override
    public boolean insertar(Proveedores proveedor) {
        try {
            // Si ya hay un proveedor con ese código, no lo duplicamos
            if (find(proveedor.getCodigo()) != null) {
                System.out.println("Ya existe un proveedor con ese código.");
                return false;
            }

            // MODIFICADO: create del Dao base (abre la transacción, guarda y confirma)
            create(proveedor);
            return true;

        } catch (Exception e) {
            // NUEVO: si algo falla con el pedido abierto, lo deshacemos y vaciamos la libreta
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.clear();
            System.out.println("Error en la base de datos al guardar el proveedor: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean modificar(Proveedores proveedor) {
        try {
            // Pedimos el proveedor actual: queda apuntado en la libreta del encargado
            Proveedores proveedorDeLaLibreta = find(proveedor.getCodigo());
            if (proveedorDeLaLibreta == null) {
                System.out.println("No existe ningún proveedor con ese código.");
                return false;
            }

            // Cambiamos los datos en la libreta
            proveedorDeLaLibreta.setNombre(proveedor.getNombre());
            proveedorDeLaLibreta.setApellidos(proveedor.getApellidos());
            proveedorDeLaLibreta.setDireccionPostal(proveedor.getDireccionPostal());

            // MODIFICADO: update del Dao base (abre la transacción, aplica los cambios y confirma)
            update(proveedorDeLaLibreta);
            return true;

        } catch (Exception e) {
            // NUEVO: deshacemos el pedido y vaciamos la libreta, para que no se quede con datos a medias
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.clear();
            System.out.println("Error en la base de datos al modificar el proveedor: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(String codigo) {
        // Regla de integridad: no se borra un proveedor con suministros
        if (tieneSuministros(codigo)) {
            System.out.println("No se ha eliminado: el proveedor tiene suministros asociados o no se ha podido comprobar.");
            return false;
        }

        try {
            Proveedores proveedorAEliminar = find(codigo);
            if (proveedorAEliminar == null) {
                System.out.println("No existe ningún proveedor con ese código.");
                return false;
            }

            // MODIFICADO: delete del Dao base (abre la transacción, elimina y confirma)
            delete(proveedorAEliminar);
            return true;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.clear();
            System.out.println("Error en la base de datos al eliminar el proveedor: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Proveedores buscarPorCodigo(String codigo) {
        try {
            // find busca directamente por clave primaria; devuelve null si no existe
            return find(codigo);

        } catch (Exception e) {
            System.out.println("Error en la base de datos al buscar el proveedor: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Proveedores> buscarPorNombre(String nombre) {
        try {
            // JPQL: "dame los Proveedores p cuyo nombre contenga el texto"; :textoPerdido es un hueco
            TypedQuery<Proveedores> consultaDeProveedoresPerdidos = em.createQuery(
                    "SELECT p FROM Proveedores p WHERE LOWER(p.nombre) LIKE :textoPerdido ORDER BY p.nombre",
                    Proveedores.class);

            // Rellenamos el hueco con el texto entre % para que busque coincidencias parciales
            consultaDeProveedoresPerdidos.setParameter("textoPerdido", "%" + nombre.toLowerCase() + "%");

            return consultaDeProveedoresPerdidos.getResultList();

        } catch (Exception e) {
            System.out.println("Error en la base de datos al buscar por nombre: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Proveedores> buscarPorDireccion(String direccion) {
        try {
            // Igual que la anterior, pero sobre el atributo direccionPostal
            TypedQuery<Proveedores> consultaDeCalleEnigmatica = em.createQuery(
                    "SELECT p FROM Proveedores p WHERE LOWER(p.direccionPostal) LIKE :textoPerdido ORDER BY p.nombre",
                    Proveedores.class);

            consultaDeCalleEnigmatica.setParameter("textoPerdido", "%" + direccion.toLowerCase() + "%");

            return consultaDeCalleEnigmatica.getResultList();

        } catch (Exception e) {
            System.out.println("Error en la base de datos al buscar por dirección: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Proveedores> listarTodos() {
        try {
            TypedQuery<Proveedores> consultaDeTodosLosProveedores = em.createQuery(
                    "SELECT p FROM Proveedores p ORDER BY p.codigo",
                    Proveedores.class);

            return consultaDeTodosLosProveedores.getResultList();

        } catch (Exception e) {
            System.out.println("Error en la base de datos al listar los proveedores: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean tieneSuministros(String codigo) {
        try {
            // Contamos cuántas gestiones apuntan a este proveedor
            TypedQuery<Long> cuentaDeSuministrosMisteriosos = em.createQuery(
                    "SELECT COUNT(g) FROM Gestion g WHERE g.proveedor.codigo = :codigoDelProveedor",
                    Long.class);

            cuentaDeSuministrosMisteriosos.setParameter("codigoDelProveedor", codigo);

            return cuentaDeSuministrosMisteriosos.getSingleResult() > 0;

        } catch (Exception e) {
            // Si no podemos comprobarlo, lo más seguro es tratarlo como "tiene suministros"
            System.out.println("Error en la base de datos al comprobar los suministros: " + e.getMessage());
            return true;
        }
    }
}