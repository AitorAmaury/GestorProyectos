package dao;

import idao.IPiezasDao;
import jakarta.persistence.TypedQuery;
import models.Piezas;

import java.util.ArrayList;
import java.util.List;

// Extiende de tu Dao base (T = Piezas, K = String, el tipo del código)
public class PiezasDao extends Dao<Piezas, String> implements IPiezasDao {

    // Método obligatorio del Dao base; busca por clave primaria
    @Override
    public Piezas find(String id) {
        return em.find(Piezas.class, id);
    }

    @Override
    public boolean insertar(Piezas pieza) {
        try {
            // Si ya hay una pieza con ese código, no la duplicamos
            if (find(pieza.getCodigo()) != null) {
                System.out.println("Ya existe una pieza con ese código.");
                return false;
            }

            // create del Dao base: abre la transacción, guarda y confirma
            create(pieza);
            return true;

        } catch (Exception e) {
            // Si algo falla con el pedido abierto, lo deshacemos y vaciamos la libreta
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.clear();
            System.out.println("Error en la base de datos al guardar la pieza: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean modificar(Piezas pieza) {
        try {
            // Pedimos la pieza actual: queda apuntada en la libreta del encargado
            Piezas piezaDeLaLibreta = find(pieza.getCodigo());
            if (piezaDeLaLibreta == null) {
                System.out.println("No existe ninguna pieza con ese código.");
                return false;
            }

            // Cambiamos los datos en la libreta
            piezaDeLaLibreta.setNombre(pieza.getNombre());
            piezaDeLaLibreta.setPrecio(pieza.getPrecio());
            piezaDeLaLibreta.setDescripcion(pieza.getDescripcion());

            // update del Dao base: abre la transacción, aplica los cambios y confirma
            update(piezaDeLaLibreta);
            return true;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.clear();
            System.out.println("Error en la base de datos al modificar la pieza: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(String codigo) {
        // Regla de integridad: no se borra una pieza con suministros
        if (tieneSuministros(codigo)) {
            System.out.println("No se ha eliminado: la pieza tiene suministros asociados o no se ha podido comprobar.");
            return false;
        }

        try {
            Piezas piezaAEliminar = find(codigo);
            if (piezaAEliminar == null) {
                System.out.println("No existe ninguna pieza con ese código.");
                return false;
            }

            // delete del Dao base: abre la transacción, elimina y confirma
            delete(piezaAEliminar);
            return true;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.clear();
            System.out.println("Error en la base de datos al eliminar la pieza: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Piezas buscarPorCodigo(String codigo) {
        try {
            // find busca directamente por clave primaria; devuelve null si no existe
            return find(codigo);

        } catch (Exception e) {
            System.out.println("Error en la base de datos al buscar la pieza: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Piezas> buscarPorNombre(String nombre) {
        try {
            // JPQL: "dame las Piezas p cuyo nombre contenga el texto"; :textoDelTornilloPerdido es un hueco
            TypedQuery<Piezas> consultaDelTornilloPerdido = em.createQuery(
                    "SELECT p FROM Piezas p WHERE LOWER(p.nombre) LIKE :textoDelTornilloPerdido ORDER BY p.nombre",
                    Piezas.class);

            // Rellenamos el hueco con el texto entre % para que busque coincidencias parciales
            consultaDelTornilloPerdido.setParameter("textoDelTornilloPerdido", "%" + nombre.toLowerCase() + "%");

            return consultaDelTornilloPerdido.getResultList();

        } catch (Exception e) {
            System.out.println("Error en la base de datos al buscar por nombre: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Piezas> listarTodos() {
        try {
            TypedQuery<Piezas> consultaDeTodasLasPiezas = em.createQuery(
                    "SELECT p FROM Piezas p ORDER BY p.codigo",
                    Piezas.class);

            return consultaDeTodasLasPiezas.getResultList();

        } catch (Exception e) {
            System.out.println("Error en la base de datos al listar las piezas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean tieneSuministros(String codigo) {
        try {
            // Contamos cuántas gestiones apuntan a esta pieza
            TypedQuery<Long> cuentaDeAlbaranesDeTuercas = em.createQuery(
                    "SELECT COUNT(g) FROM Gestion g WHERE g.pieza.codigo = :codigoDeLaPieza",
                    Long.class);

            cuentaDeAlbaranesDeTuercas.setParameter("codigoDeLaPieza", codigo);

            return cuentaDeAlbaranesDeTuercas.getSingleResult() > 0;

        } catch (Exception e) {
            // Si no podemos comprobarlo, lo más seguro es tratarla como "tiene suministros"
            System.out.println("Error en la base de datos al comprobar los suministros: " + e.getMessage());
            return true;
        }
    }
}