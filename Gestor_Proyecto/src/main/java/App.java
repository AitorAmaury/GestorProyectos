// MODIFICADO: eliminado el import dao.Dao
import dao.ProveedoresDao;
import idao.IProveedoresDao;
import models.Proveedores;

import java.util.List;
import java.util.Scanner;

public class App {

    // MODIFICADO: ya no se crea aquí; se crea dentro de main para poder controlar un fallo de conexión
    private static IProveedoresDao proveedoresDao;

    public static void main(String[] args) {

        Scanner teclado = new Scanner(System.in);

        // MODIFICADO: crear el DAO abre la conexión; si falla, lo capturamos y salimos con un mensaje
        try {
            proveedoresDao = new ProveedoresDao();
        } catch (Exception e) {
            System.out.println("No se ha podido conectar con la base de datos: " + e.getMessage());
            System.out.println("Revisa que MariaDB está arrancado y vuelve a ejecutar la aplicación.");
            teclado.close();
            return;
        }

        boolean salirDeTodo = false;

        // Bucle del menú principal: se repite hasta que el usuario elija salir
        while (!salirDeTodo) {
            System.out.println();
            System.out.println("----- Gestor de proyectos -----");
            System.out.println("1. Gestión de proveedores");
            System.out.println("2. Gestión de piezas");
            System.out.println("3. Gestión de proyectos");
            System.out.println("4. Gestión de suministros");
            System.out.println("5. Ayuda");
            System.out.println("0. Salir");
            System.out.println("Elige una opción:");
            String textoOpcion = teclado.nextLine().trim();

            // Convertimos el texto a número; si no es un número, avisamos y volvemos al menú
            int opcion;
            try {
                opcion = Integer.parseInt(textoOpcion);
            } catch (NumberFormatException e) {
                System.out.println("Opción no válida: escribe solo el número de una opción del menú.");
                continue;
            }

            switch (opcion) {
                case 1:
                    menuProveedores(teclado);
                    break;
                case 2:
                case 3:
                case 4:
                    System.out.println("Este módulo todavía no está disponible.");
                    break;
                case 5:
                    System.out.println("Ayuda: elige un módulo escribiendo su número y pulsa Enter.");
                    System.out.println("En cada submenú, la opción 0 vuelve al menú anterior.");
                    System.out.println("Al pedir un dato, escribe volver para cancelar la operación.");
                    break;
                case 0:
                    salirDeTodo = true;
                    break;
                default:
                    System.out.println("Opción no válida: elige un número del menú.");
                    break;
            }
        }

        // MODIFICADO: ya no hay fábrica compartida que cerrar; solo cerramos el teclado
        teclado.close();
        System.out.println("Hasta pronto.");
    }

    // ---------- Submenú de proveedores ----------

    private static void menuProveedores(Scanner teclado) {

        boolean volver = false;

        while (!volver) {
            System.out.println();
            System.out.println("----- Proveedores -----");
            System.out.println("1. Alta de proveedor");
            System.out.println("2. Baja de proveedor");
            System.out.println("3. Modificar proveedor");
            System.out.println("4. Buscar por código");
            System.out.println("5. Buscar por nombre");
            System.out.println("6. Buscar por dirección");
            System.out.println("7. Listar todos");
            System.out.println("0. Volver al menú principal");
            System.out.println("Elige una opción:");
            String textoOpcion = teclado.nextLine().trim();

            int opcion;
            try {
                opcion = Integer.parseInt(textoOpcion);
            } catch (NumberFormatException e) {
                System.out.println("Opción no válida: escribe solo el número de una opción del menú.");
                continue;
            }

            switch (opcion) {
                case 1:
                    altaProveedor(teclado);
                    break;
                case 2:
                    bajaProveedor(teclado);
                    break;
                case 3:
                    modificarProveedor(teclado);
                    break;
                case 4:
                    buscarProveedorPorCodigo(teclado);
                    break;
                case 5:
                    buscarProveedorPorNombre(teclado);
                    break;
                case 6:
                    buscarProveedorPorDireccion(teclado);
                    break;
                case 7:
                    listarProveedores();
                    break;
                case 0:
                    volver = true;
                    break;
                default:
                    System.out.println("Opción no válida: elige un número del menú.");
                    break;
            }
        }
    }

    // Alta: pide los cuatro datos, los valida y llama al DAO
    private static void altaProveedor(Scanner teclado) {

        System.out.println("Alta de proveedor (escribe volver en cualquier campo para cancelar).");

        // Código: letras y números, de 1 a 10 caracteres
        String codigo;
        while (true) {
            System.out.println("Código (letras y números, de 1 a 10 caracteres):");
            codigo = teclado.nextLine().trim();
            if (codigo.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (codigo.matches("[A-Za-z0-9]{1,10}")) {
                break;
            }
            System.out.println("Código no válido: usa solo letras y números, de 1 a 10 caracteres.");
        }

        // Si el código ya existe, no tiene sentido pedir el resto de datos
        if (proveedoresDao.buscarPorCodigo(codigo) != null) {
            System.out.println("Ya existe un proveedor con ese código.");
            return;
        }

        // Nombre: obligatorio, máximo 100 caracteres
        String nombre;
        while (true) {
            System.out.println("Nombre:");
            nombre = teclado.nextLine().trim();
            if (nombre.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (!nombre.isEmpty() && nombre.length() <= 100) {
                break;
            }
            System.out.println("Nombre no válido: no puede estar vacío ni superar 100 caracteres.");
        }

        // Apellidos: obligatorio, máximo 100 caracteres
        String apellidos;
        while (true) {
            System.out.println("Apellidos:");
            apellidos = teclado.nextLine().trim();
            if (apellidos.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (!apellidos.isEmpty() && apellidos.length() <= 100) {
                break;
            }
            System.out.println("Apellidos no válidos: no pueden estar vacíos ni superar 100 caracteres.");
        }

        // Dirección postal: obligatoria, máximo 150 caracteres
        String direccion;
        while (true) {
            System.out.println("Dirección postal:");
            direccion = teclado.nextLine().trim();
            if (direccion.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (!direccion.isEmpty() && direccion.length() <= 150) {
                break;
            }
            System.out.println("Dirección no válida: no puede estar vacía ni superar 150 caracteres.");
        }

        // Con todos los datos validados, se los pasamos al DAO
        Proveedores proveedorNuevo = new Proveedores(codigo, nombre, apellidos, direccion);
        boolean guardado = proveedoresDao.insertar(proveedorNuevo);

        if (guardado) {
            System.out.println("Proveedor guardado correctamente.");
        }
    }

    // Baja: busca el proveedor, lo muestra, pide confirmación y lo elimina
    private static void bajaProveedor(Scanner teclado) {

        System.out.println("Baja de proveedor (escribe volver para cancelar).");

        String codigo;
        while (true) {
            System.out.println("Código del proveedor a eliminar:");
            codigo = teclado.nextLine().trim();
            if (codigo.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (codigo.matches("[A-Za-z0-9]{1,10}")) {
                break;
            }
            System.out.println("Código no válido: usa solo letras y números, de 1 a 10 caracteres.");
        }

        Proveedores proveedorEncontrado = proveedoresDao.buscarPorCodigo(codigo);
        if (proveedorEncontrado == null) {
            System.out.println("No existe ningún proveedor con ese código.");
            return;
        }

        System.out.println(proveedorEncontrado);
        System.out.println("¿Seguro que quieres eliminarlo? (s/n):");
        String confirmacion = teclado.nextLine().trim();

        if (confirmacion.equalsIgnoreCase("s")) {
            boolean eliminado = proveedoresDao.eliminar(codigo);
            if (eliminado) {
                System.out.println("Proveedor eliminado correctamente.");
            }
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    // Modificar: muestra el proveedor y pide los datos nuevos; Enter mantiene el valor actual
    private static void modificarProveedor(Scanner teclado) {

        System.out.println("Modificar proveedor (escribe volver en cualquier campo para cancelar).");

        String codigo;
        while (true) {
            System.out.println("Código del proveedor a modificar:");
            codigo = teclado.nextLine().trim();
            if (codigo.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (codigo.matches("[A-Za-z0-9]{1,10}")) {
                break;
            }
            System.out.println("Código no válido: usa solo letras y números, de 1 a 10 caracteres.");
        }

        Proveedores proveedorActual = proveedoresDao.buscarPorCodigo(codigo);
        if (proveedorActual == null) {
            System.out.println("No existe ningún proveedor con ese código.");
            return;
        }

        System.out.println("Datos actuales: " + proveedorActual);

        // Nombre nuevo (Enter = mantener el actual)
        String nuevoNombre;
        while (true) {
            System.out.println("Nuevo nombre (Enter para mantener \"" + proveedorActual.getNombre() + "\"):");
            nuevoNombre = teclado.nextLine().trim();
            if (nuevoNombre.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (nuevoNombre.isEmpty()) {
                nuevoNombre = proveedorActual.getNombre();
                break;
            }
            if (nuevoNombre.length() <= 100) {
                break;
            }
            System.out.println("Nombre no válido: no puede superar 100 caracteres.");
        }

        // Apellidos nuevos (Enter = mantener los actuales)
        String nuevosApellidos;
        while (true) {
            System.out.println("Nuevos apellidos (Enter para mantener \"" + proveedorActual.getApellidos() + "\"):");
            nuevosApellidos = teclado.nextLine().trim();
            if (nuevosApellidos.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (nuevosApellidos.isEmpty()) {
                nuevosApellidos = proveedorActual.getApellidos();
                break;
            }
            if (nuevosApellidos.length() <= 100) {
                break;
            }
            System.out.println("Apellidos no válidos: no pueden superar 100 caracteres.");
        }

        // Dirección nueva (Enter = mantener la actual)
        String nuevaDireccion;
        while (true) {
            System.out.println("Nueva dirección (Enter para mantener \"" + proveedorActual.getDireccionPostal() + "\"):");
            nuevaDireccion = teclado.nextLine().trim();
            if (nuevaDireccion.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (nuevaDireccion.isEmpty()) {
                nuevaDireccion = proveedorActual.getDireccionPostal();
                break;
            }
            if (nuevaDireccion.length() <= 150) {
                break;
            }
            System.out.println("Dirección no válida: no puede superar 150 caracteres.");
        }

        Proveedores proveedorConCambios = new Proveedores(codigo, nuevoNombre, nuevosApellidos, nuevaDireccion);
        boolean modificado = proveedoresDao.modificar(proveedorConCambios);

        if (modificado) {
            System.out.println("Proveedor modificado correctamente.");
        }
    }

    // Búsqueda por código
    private static void buscarProveedorPorCodigo(Scanner teclado) {

        String codigo;
        while (true) {
            System.out.println("Código a buscar (o volver para cancelar):");
            codigo = teclado.nextLine().trim();
            if (codigo.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (codigo.matches("[A-Za-z0-9]{1,10}")) {
                break;
            }
            System.out.println("Código no válido: usa solo letras y números, de 1 a 10 caracteres.");
        }

        Proveedores proveedorEncontrado = proveedoresDao.buscarPorCodigo(codigo);
        if (proveedorEncontrado == null) {
            System.out.println("No existe ningún proveedor con ese código.");
        } else {
            System.out.println(proveedorEncontrado);
        }
    }

    // Búsqueda por nombre (coincidencia parcial)
    private static void buscarProveedorPorNombre(Scanner teclado) {

        String texto;
        while (true) {
            System.out.println("Nombre (o parte del nombre) a buscar (o volver para cancelar):");
            texto = teclado.nextLine().trim();
            if (texto.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (!texto.isEmpty() && texto.length() <= 100) {
                break;
            }
            System.out.println("Texto no válido: no puede estar vacío ni superar 100 caracteres.");
        }

        List<Proveedores> proveedoresEncontrados = proveedoresDao.buscarPorNombre(texto);
        if (proveedoresEncontrados.isEmpty()) {
            System.out.println("No se ha encontrado ningún proveedor con ese nombre.");
        } else {
            for (Proveedores proveedor : proveedoresEncontrados) {
                System.out.println(proveedor);
            }
        }
    }

    // Búsqueda por dirección (coincidencia parcial)
    private static void buscarProveedorPorDireccion(Scanner teclado) {

        String texto;
        while (true) {
            System.out.println("Dirección (o parte de la dirección) a buscar (o volver para cancelar):");
            texto = teclado.nextLine().trim();
            if (texto.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (!texto.isEmpty() && texto.length() <= 150) {
                break;
            }
            System.out.println("Texto no válido: no puede estar vacío ni superar 150 caracteres.");
        }

        List<Proveedores> proveedoresEncontrados = proveedoresDao.buscarPorDireccion(texto);
        if (proveedoresEncontrados.isEmpty()) {
            System.out.println("No se ha encontrado ningún proveedor con esa dirección.");
        } else {
            for (Proveedores proveedor : proveedoresEncontrados) {
                System.out.println(proveedor);
            }
        }
    }

    // Lista todos los proveedores
    private static void listarProveedores() {

        List<Proveedores> todosLosProveedores = proveedoresDao.listarTodos();
        if (todosLosProveedores.isEmpty()) {
            System.out.println("Todavía no hay proveedores guardados.");
        } else {
            for (Proveedores proveedor : todosLosProveedores) {
                System.out.println(proveedor);
            }
        }
    }
}