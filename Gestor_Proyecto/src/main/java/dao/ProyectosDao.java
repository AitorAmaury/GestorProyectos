package dao;

import idao.IProyectosDao;
import jakarta.persistence.TypedQuery;
import models.Proyectos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

// Extiende de tu Dao base (T = Proyectos, K = String, el tipo del código)
public class ProyectosDao extends Dao<Proyectos, String> implements IProyectosDao {

    // El cuaderno de bitácora de esta clase
    private static final Logger logger = LoggerFactory.getLogger(ProyectosDao.class);

    // Método obligatorio del Dao base; busca por clave primaria
    @Override
    public Proyectos find(String id) {
        return em.find(Proyectos.class, id);
    }

    @Override
    public boolean insertar(Proyectos proyecto) {
        try {
            // Si ya hay un proyecto con ese código, no lo duplicamos
            if (find(proyecto.getCodigo()) != null) {
                System.out.println("Ya existe un proyecto con ese código.");
                logger.warn("Alta rechazada: ya existe el proyecto {}", proyecto.getCodigo());
                return false;
            }

            // create del Dao base: abre la transacción, guarda y confirma
            create(proyecto);
            logger.info("Proyecto {} dado de alta", proyecto.getCodigo());
            return true;

        } catch (Exception e) {
            // Si algo falla con el pedido abierto, lo deshacemos y vaciamos la libreta
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.clear();
            System.out.println("Error en la base de datos al guardar el proyecto: " + e.getMessage());
            logger.error("Error al guardar el proyecto " + proyecto.getCodigo(), e);
            return false;
        }
    }

    @Override
    public boolean modificar(Proyectos proyecto) {
        try {
            // Pedimos el proyecto actual: queda apuntado en la libreta del encargado
            Proyectos proyectoDeLaLibreta = find(proyecto.getCodigo());
            if (proyectoDeLaLibreta == null) {
                System.out.println("No existe ningún proyecto con ese código.");
                logger.warn("Modificación rechazada: no existe el proyecto {}", proyecto.getCodigo());
                return false;
            }

            // Cambiamos los datos en la libreta
            proyectoDeLaLibreta.setNombre(proyecto.getNombre());
            proyectoDeLaLibreta.setCiudad(proyecto.getCiudad());

            // update del Dao base: abre la transacción, aplica los cambios y confirma
            update(proyectoDeLaLibreta);
            logger.info("Proyecto {} modificado", proyecto.getCodigo());
            return true;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.clear();
            System.out.println("Error en la base de datos al modificar el proyecto: " + e.getMessage());
            logger.error("Error al modificar el proyecto " + proyecto.getCodigo(), e);
            return false;
        }
    }

    @Override
    public boolean eliminar(String codigo) {
        // Regla de integridad: no se borra un proyecto con suministros
        if (tieneSuministros(codigo)) {
            System.out.println("No se ha eliminado: el proyecto tiene suministros asociados o no se ha podido comprobar.");
            logger.warn("Baja rechazada: el proyecto {} tiene suministros asociados", codigo);
            return false;
        }

        try {
            Proyectos proyectoAEliminar = find(codigo);
            if (proyectoAEliminar == null) {
                System.out.println("No existe ningún proyecto con ese código.");
                logger.warn("Baja rechazada: no existe el proyecto {}", codigo);
                return false;
            }

            // delete del Dao base: abre la transacción, elimina y confirma
            delete(proyectoAEliminar);
            logger.info("Proyecto {} eliminado", codigo);
            return true;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.clear();
            System.out.println("Error en la base de datos al eliminar el proyecto: " + e.getMessage());
            logger.error("Error al eliminar el proyecto " + codigo, e);
            return false;
        }
    }

    @Override
    public Proyectos buscarPorCodigo(String codigo) {
        try {
            // find busca directamente por clave primaria; devuelve null si no existe
            return find(codigo);

        } catch (Exception e) {
            System.out.println("Error en la base de datos al buscar el proyecto: " + e.getMessage());
            logger.error("Error al buscar el proyecto " + codigo, e);
            return null;
        }
    }

    @Override
    public List<Proyectos> buscarPorNombre(String nombre) {
        try {
            // JPQL: "dame los Proyectos p cuyo nombre contenga el texto"; :textoDelPlanoArrugado es un hueco
            TypedQuery<Proyectos> consultaDelPlanoArrugado = em.createQuery(
                    "SELECT p FROM Proyectos p WHERE LOWER(p.nombre) LIKE :textoDelPlanoArrugado ORDER BY p.nombre",
                    Proyectos.class);

            // Rellenamos el hueco con el texto entre % para que busque coincidencias parciales
            consultaDelPlanoArrugado.setParameter("textoDelPlanoArrugado", "%" + nombre.toLowerCase() + "%");

            return consultaDelPlanoArrugado.getResultList();

        } catch (Exception e) {
            System.out.println("Error en la base de datos al buscar por nombre: " + e.getMessage());
            logger.error("Error al buscar proyectos por nombre '" + nombre + "'", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Proyectos> buscarPorCiudad(String ciudad) {
        try {
            // Igual que la anterior, pero sobre el atributo ciudad
            TypedQuery<Proyectos> consultaDeLaCiudadEscondida = em.createQuery(
                    "SELECT p FROM Proyectos p WHERE LOWER(p.ciudad) LIKE :textoDelPlanoArrugado ORDER BY p.nombre",
                    Proyectos.class);

            consultaDeLaCiudadEscondida.setParameter("textoDelPlanoArrugado", "%" + ciudad.toLowerCase() + "%");

            return consultaDeLaCiudadEscondida.getResultList();

        } catch (Exception e) {
            System.out.println("Error en la base de datos al buscar por ciudad: " + e.getMessage());
            logger.error("Error al buscar proyectos por ciudad '" + ciudad + "'", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Proyectos> listarTodos() {
        try {
            TypedQuery<Proyectos> consultaDeTodosLosProyectos = em.createQuery(
                    "SELECT p FROM Proyectos p ORDER BY p.codigo",
                    Proyectos.class);

            return consultaDeTodosLosProyectos.getResultList();

        } catch (Exception e) {
            System.out.println("Error en la base de datos al listar los proyectos: " + e.getMessage());
            logger.error("Error al listar los proyectos", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean tieneSuministros(String codigo) {
        try {
            // Contamos cuántas gestiones apuntan a este proyecto
            TypedQuery<Long> cuentaDeCamionesDeLadrillos = em.createQuery(
                    "SELECT COUNT(g) FROM Gestion g WHERE g.proyecto.codigo = :codigoDelProyecto",
                    Long.class);

            cuentaDeCamionesDeLadrillos.setParameter("codigoDelProyecto", codigo);

            return cuentaDeCamionesDeLadrillos.getSingleResult() > 0;

        } catch (Exception e) {
            // Si no podemos comprobarlo, lo más seguro es tratarlo como "tiene suministros"
            System.out.println("Error en la base de datos al comprobar los suministros: " + e.getMessage());
            logger.error("Error al comprobar los suministros del proyecto " + codigo, e);
            return true;
        }
    }
}