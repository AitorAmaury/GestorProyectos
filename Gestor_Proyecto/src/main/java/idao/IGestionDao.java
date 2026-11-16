package idao;

import models.Gestion;

import java.util.List;

public interface IGestionDao {

    // Guarda un suministro nuevo; devuelve true si se ha guardado
    boolean insertar(Gestion gestion);

    // Cambia los datos de un suministro que ya existe (el id identifica cuál); devuelve true si se ha modificado
    boolean modificar(Gestion gestion);

    // Elimina un suministro por su identificador; devuelve true si se ha eliminado
    boolean eliminar(Integer id);

    // Busca un suministro por identificador; devuelve null si no existe
    Gestion buscarPorId(Integer id);

    // Devuelve los suministros de un proveedor
    List<Gestion> buscarPorProveedor(String codigoProveedor);

    // Devuelve los suministros de una pieza
    List<Gestion> buscarPorPieza(String codigoPieza);

    // Devuelve los suministros de un proyecto
    List<Gestion> buscarPorProyecto(String codigoProyecto);

    // Devuelve todos los suministros
    List<Gestion> listarTodos();
}