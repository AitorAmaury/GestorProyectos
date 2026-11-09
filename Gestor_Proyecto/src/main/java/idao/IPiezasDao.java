package idao;

import models.Piezas;

import java.util.List;

public interface IPiezasDao {

    // Guarda una pieza nueva; devuelve true si se ha guardado
    boolean insertar(Piezas pieza);

    // Cambia los datos de una pieza que ya existe; devuelve true si se ha modificado
    boolean modificar(Piezas pieza);

    // Elimina una pieza por su código, si no tiene suministros; devuelve true si se ha eliminado
    boolean eliminar(String codigo);

    // Busca una pieza por código; devuelve null si no existe
    Piezas buscarPorCodigo(String codigo);

    // Busca piezas cuyo nombre contenga el texto (sin distinguir mayúsculas)
    List<Piezas> buscarPorNombre(String nombre);

    // Devuelve todas las piezas
    List<Piezas> listarTodos();

    // Indica si la pieza tiene suministros (gestiones) asociados
    boolean tieneSuministros(String codigo);
}