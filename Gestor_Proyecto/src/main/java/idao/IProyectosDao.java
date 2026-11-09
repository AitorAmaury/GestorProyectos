package idao;

import models.Proyectos;

import java.util.List;

public interface IProyectosDao {

    // Guarda un proyecto nuevo; devuelve true si se ha guardado
    boolean insertar(Proyectos proyecto);

    // Cambia los datos de un proyecto que ya existe; devuelve true si se ha modificado
    boolean modificar(Proyectos proyecto);

    // Elimina un proyecto por su código, si no tiene suministros; devuelve true si se ha eliminado
    boolean eliminar(String codigo);

    // Busca un proyecto por código; devuelve null si no existe
    Proyectos buscarPorCodigo(String codigo);

    // Busca proyectos cuyo nombre contenga el texto (sin distinguir mayúsculas)
    List<Proyectos> buscarPorNombre(String nombre);

    // Busca proyectos cuya ciudad contenga el texto (sin distinguir mayúsculas)
    List<Proyectos> buscarPorCiudad(String ciudad);

    // Devuelve todos los proyectos
    List<Proyectos> listarTodos();

    // Indica si el proyecto tiene suministros (gestiones) asociados
    boolean tieneSuministros(String codigo);
}