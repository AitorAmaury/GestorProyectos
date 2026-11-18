package idao;

import models.Gestion;

import java.util.List;

public interface IEstadisticasDao {

    // Histórico de un proveedor: todos sus suministros ordenados por fecha
    List<Gestion> historicoProveedor(String codigoProveedor);

    // Análisis de un proyecto: por cada pieza, [nombre, unidades, importe]
    List<Object[]> piezasDeProyecto(String codigoProyecto);

    // Gasto total de un proyecto (cantidad por precio de cada pieza)
    double gastoProyecto(String codigoProyecto);

    // Top 5 de piezas más suministradas: [código, nombre, unidades]
    List<Object[]> piezasMasSuministradas();

    // Top 5 de proveedores con más unidades suministradas: [código, nombre, unidades]
    List<Object[]> proveedoresMasActivos();

    // Gasto de cada proyecto, de mayor a menor: [código, nombre, importe]
    List<Object[]> gastoPorProyecto();

    // Totales generales
    long totalProveedores();
    long totalPiezas();
    long totalProyectos();
    long totalSuministros();
}