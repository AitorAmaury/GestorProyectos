package idao;

import models.Proveedores;

import java.util.List;

public interface IProveedoresDao {

    // Guarda un proveedor nuevo; devuelve true si se ha guardado
    boolean insertar(Proveedores proveedor);

    // Cambia los datos de un proveedor que ya existe; devuelve true si se ha modificado
    boolean modificar(Proveedores proveedor);

    // Elimina un proveedor por su código, si no tiene suministros; devuelve true si se ha eliminado
    boolean eliminar(String codigo);

    // Busca un proveedor por código; devuelve null si no existe
    Proveedores buscarPorCodigo(String codigo);

    // Busca proveedores cuyo nombre contenga el texto (sin distinguir mayúsculas)
    List<Proveedores> buscarPorNombre(String nombre);

    // Busca proveedores cuya dirección contenga el texto (sin distinguir mayúsculas)
    List<Proveedores> buscarPorDireccion(String direccion);

    // Devuelve todos los proveedores
    List<Proveedores> listarTodos();

    // Indica si el proveedor tiene suministros (gestiones) asociados
    boolean tieneSuministros(String codigo);
}