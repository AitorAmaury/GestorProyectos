import dao.GestionDao;
import dao.PiezasDao;
import dao.ProveedoresDao;
import dao.ProyectosDao;
import idao.IGestionDao;
import idao.IPiezasDao;
import idao.IProveedoresDao;
import idao.IProyectosDao;
import models.Gestion;
import models.Piezas;
import models.Proveedores;
import models.Proyectos;

// NUEVO: imports para trabajar con fechas
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import java.util.List;
import java.util.Scanner;

public class App {

    // DAO de cada módulo: los usan los métodos de sus menús
    // Se crean dentro de main para poder controlar un fallo de conexión
    private static IProveedoresDao proveedoresDao;
    private static IPiezasDao piezasDao;
    private static IProyectosDao proyectosDao;
    private static IGestionDao gestionDao;         // NUEVO

    // NUEVO: formato de fecha dd/mm/aaaa en modo estricto (rechaza fechas imposibles como 31/02/2026)
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static void main(String[] args) {

        Scanner teclado = new Scanner(System.in);

        // Crear los DAO abre las conexiones; si falla, lo capturamos y salimos con un mensaje
        try {
            proveedoresDao = new ProveedoresDao();
            piezasDao = new PiezasDao();
            proyectosDao = new ProyectosDao();
            gestionDao = new GestionDao();         // NUEVO
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
                    menuPiezas(teclado);
                    break;
                case 3:
                    menuProyectos(teclado);
                    break;
                case 4:
                    menuSuministros(teclado);   // MODIFICADO: ya lleva a su módulo
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

        // Solo cerramos el teclado
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

    // ---------- Submenú de piezas ----------

    private static void menuPiezas(Scanner teclado) {

        boolean volver = false;

        while (!volver) {
            System.out.println();
            System.out.println("----- Piezas -----");
            System.out.println("1. Alta de pieza");
            System.out.println("2. Baja de pieza");
            System.out.println("3. Modificar pieza");
            System.out.println("4. Buscar por código");
            System.out.println("5. Buscar por nombre");
            System.out.println("6. Listar todas");
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
                    altaPieza(teclado);
                    break;
                case 2:
                    bajaPieza(teclado);
                    break;
                case 3:
                    modificarPieza(teclado);
                    break;
                case 4:
                    buscarPiezaPorCodigo(teclado);
                    break;
                case 5:
                    buscarPiezaPorNombre(teclado);
                    break;
                case 6:
                    listarPiezas();
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

    // Alta de pieza: pide los datos, los valida y llama al DAO
    private static void altaPieza(Scanner teclado) {

        System.out.println("Alta de pieza (escribe volver en cualquier campo para cancelar).");

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
        if (piezasDao.buscarPorCodigo(codigo) != null) {
            System.out.println("Ya existe una pieza con ese código.");
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

        // Precio: número entre 0 y 1000000; se acepta punto o coma como separador decimal
        double precio = 0;
        while (true) {
            System.out.println("Precio (número, por ejemplo 12.50):");
            String textoPrecio = teclado.nextLine().trim();
            if (textoPrecio.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            try {
                precio = Double.parseDouble(textoPrecio.replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Precio no válido: escribe un número, por ejemplo 12.50.");
                continue;
            }
            if (Double.isNaN(precio) || Double.isInfinite(precio) || precio < 0 || precio > 1000000) {
                System.out.println("Precio no válido: debe estar entre 0 y 1000000.");
                continue;
            }
            break;
        }

        // Descripción: opcional (Enter para dejarla vacía), máximo 255 caracteres
        String descripcion;
        while (true) {
            System.out.println("Descripción (Enter para dejarla vacía):");
            descripcion = teclado.nextLine().trim();
            if (descripcion.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (descripcion.length() <= 255) {
                break;
            }
            System.out.println("Descripción no válida: no puede superar 255 caracteres.");
        }

        // Con todos los datos validados, se los pasamos al DAO
        Piezas piezaNueva = new Piezas(codigo, nombre, precio, descripcion);
        boolean guardada = piezasDao.insertar(piezaNueva);

        if (guardada) {
            System.out.println("Pieza guardada correctamente.");
        }
    }

    // Baja de pieza: busca la pieza, la muestra, pide confirmación y la elimina
    private static void bajaPieza(Scanner teclado) {

        System.out.println("Baja de pieza (escribe volver para cancelar).");

        String codigo;
        while (true) {
            System.out.println("Código de la pieza a eliminar:");
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

        Piezas piezaEncontrada = piezasDao.buscarPorCodigo(codigo);
        if (piezaEncontrada == null) {
            System.out.println("No existe ninguna pieza con ese código.");
            return;
        }

        System.out.println(piezaEncontrada);
        System.out.println("¿Seguro que quieres eliminarla? (s/n):");
        String confirmacion = teclado.nextLine().trim();

        if (confirmacion.equalsIgnoreCase("s")) {
            boolean eliminada = piezasDao.eliminar(codigo);
            if (eliminada) {
                System.out.println("Pieza eliminada correctamente.");
            }
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    // Modificar pieza: muestra la pieza y pide los datos nuevos; Enter mantiene el valor actual
    private static void modificarPieza(Scanner teclado) {

        System.out.println("Modificar pieza (escribe volver en cualquier campo para cancelar).");

        String codigo;
        while (true) {
            System.out.println("Código de la pieza a modificar:");
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

        Piezas piezaActual = piezasDao.buscarPorCodigo(codigo);
        if (piezaActual == null) {
            System.out.println("No existe ninguna pieza con ese código.");
            return;
        }

        System.out.println("Datos actuales: " + piezaActual);

        // Nombre nuevo (Enter = mantener el actual)
        String nuevoNombre;
        while (true) {
            System.out.println("Nuevo nombre (Enter para mantener \"" + piezaActual.getNombre() + "\"):");
            nuevoNombre = teclado.nextLine().trim();
            if (nuevoNombre.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (nuevoNombre.isEmpty()) {
                nuevoNombre = piezaActual.getNombre();
                break;
            }
            if (nuevoNombre.length() <= 100) {
                break;
            }
            System.out.println("Nombre no válido: no puede superar 100 caracteres.");
        }

        // Precio nuevo (Enter = mantener el actual)
        double nuevoPrecio = piezaActual.getPrecio();
        while (true) {
            System.out.println("Nuevo precio (Enter para mantener " + piezaActual.getPrecio() + "):");
            String textoPrecio = teclado.nextLine().trim();
            if (textoPrecio.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (textoPrecio.isEmpty()) {
                break;
            }
            try {
                nuevoPrecio = Double.parseDouble(textoPrecio.replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Precio no válido: escribe un número, por ejemplo 12.50.");
                continue;
            }
            if (Double.isNaN(nuevoPrecio) || Double.isInfinite(nuevoPrecio) || nuevoPrecio < 0 || nuevoPrecio > 1000000) {
                System.out.println("Precio no válido: debe estar entre 0 y 1000000.");
                nuevoPrecio = piezaActual.getPrecio();
                continue;
            }
            break;
        }

        // Descripción nueva (Enter = mantener la actual)
        String nuevaDescripcion;
        while (true) {
            System.out.println("Nueva descripción (Enter para mantener la actual):");
            nuevaDescripcion = teclado.nextLine().trim();
            if (nuevaDescripcion.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (nuevaDescripcion.isEmpty()) {
                nuevaDescripcion = piezaActual.getDescripcion();
                break;
            }
            if (nuevaDescripcion.length() <= 255) {
                break;
            }
            System.out.println("Descripción no válida: no puede superar 255 caracteres.");
        }

        Piezas piezaConCambios = new Piezas(codigo, nuevoNombre, nuevoPrecio, nuevaDescripcion);
        boolean modificada = piezasDao.modificar(piezaConCambios);

        if (modificada) {
            System.out.println("Pieza modificada correctamente.");
        }
    }

    // Búsqueda de pieza por código
    private static void buscarPiezaPorCodigo(Scanner teclado) {

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

        Piezas piezaEncontrada = piezasDao.buscarPorCodigo(codigo);
        if (piezaEncontrada == null) {
            System.out.println("No existe ninguna pieza con ese código.");
        } else {
            System.out.println(piezaEncontrada);
        }
    }

    // Búsqueda de pieza por nombre (coincidencia parcial)
    private static void buscarPiezaPorNombre(Scanner teclado) {

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

        List<Piezas> piezasEncontradas = piezasDao.buscarPorNombre(texto);
        if (piezasEncontradas.isEmpty()) {
            System.out.println("No se ha encontrado ninguna pieza con ese nombre.");
        } else {
            for (Piezas pieza : piezasEncontradas) {
                System.out.println(pieza);
            }
        }
    }

    // Lista todas las piezas
    private static void listarPiezas() {

        List<Piezas> todasLasPiezas = piezasDao.listarTodos();
        if (todasLasPiezas.isEmpty()) {
            System.out.println("Todavía no hay piezas guardadas.");
        } else {
            for (Piezas pieza : todasLasPiezas) {
                System.out.println(pieza);
            }
        }
    }

    // ---------- Submenú de proyectos ----------

    private static void menuProyectos(Scanner teclado) {

        boolean volver = false;

        while (!volver) {
            System.out.println();
            System.out.println("----- Proyectos -----");
            System.out.println("1. Alta de proyecto");
            System.out.println("2. Baja de proyecto");
            System.out.println("3. Modificar proyecto");
            System.out.println("4. Buscar por código");
            System.out.println("5. Buscar por nombre");
            System.out.println("6. Buscar por ciudad");
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
                    altaProyecto(teclado);
                    break;
                case 2:
                    bajaProyecto(teclado);
                    break;
                case 3:
                    modificarProyecto(teclado);
                    break;
                case 4:
                    buscarProyectoPorCodigo(teclado);
                    break;
                case 5:
                    buscarProyectoPorNombre(teclado);
                    break;
                case 6:
                    buscarProyectoPorCiudad(teclado);
                    break;
                case 7:
                    listarProyectos();
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

    // Alta de proyecto: pide los tres datos, los valida y llama al DAO
    private static void altaProyecto(Scanner teclado) {

        System.out.println("Alta de proyecto (escribe volver en cualquier campo para cancelar).");

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
        if (proyectosDao.buscarPorCodigo(codigo) != null) {
            System.out.println("Ya existe un proyecto con ese código.");
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

        // Ciudad: obligatoria, máximo 100 caracteres
        String ciudad;
        while (true) {
            System.out.println("Ciudad:");
            ciudad = teclado.nextLine().trim();
            if (ciudad.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (!ciudad.isEmpty() && ciudad.length() <= 100) {
                break;
            }
            System.out.println("Ciudad no válida: no puede estar vacía ni superar 100 caracteres.");
        }

        // Con todos los datos validados, se los pasamos al DAO
        Proyectos proyectoNuevo = new Proyectos(codigo, nombre, ciudad);
        boolean guardado = proyectosDao.insertar(proyectoNuevo);

        if (guardado) {
            System.out.println("Proyecto guardado correctamente.");
        }
    }

    // Baja de proyecto: busca el proyecto, lo muestra, pide confirmación y lo elimina
    private static void bajaProyecto(Scanner teclado) {

        System.out.println("Baja de proyecto (escribe volver para cancelar).");

        String codigo;
        while (true) {
            System.out.println("Código del proyecto a eliminar:");
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

        Proyectos proyectoEncontrado = proyectosDao.buscarPorCodigo(codigo);
        if (proyectoEncontrado == null) {
            System.out.println("No existe ningún proyecto con ese código.");
            return;
        }

        System.out.println(proyectoEncontrado);
        System.out.println("¿Seguro que quieres eliminarlo? (s/n):");
        String confirmacion = teclado.nextLine().trim();

        if (confirmacion.equalsIgnoreCase("s")) {
            boolean eliminado = proyectosDao.eliminar(codigo);
            if (eliminado) {
                System.out.println("Proyecto eliminado correctamente.");
            }
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    // Modificar proyecto: muestra el proyecto y pide los datos nuevos; Enter mantiene el valor actual
    private static void modificarProyecto(Scanner teclado) {

        System.out.println("Modificar proyecto (escribe volver en cualquier campo para cancelar).");

        String codigo;
        while (true) {
            System.out.println("Código del proyecto a modificar:");
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

        Proyectos proyectoActual = proyectosDao.buscarPorCodigo(codigo);
        if (proyectoActual == null) {
            System.out.println("No existe ningún proyecto con ese código.");
            return;
        }

        System.out.println("Datos actuales: " + proyectoActual);

        // Nombre nuevo (Enter = mantener el actual)
        String nuevoNombre;
        while (true) {
            System.out.println("Nuevo nombre (Enter para mantener \"" + proyectoActual.getNombre() + "\"):");
            nuevoNombre = teclado.nextLine().trim();
            if (nuevoNombre.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (nuevoNombre.isEmpty()) {
                nuevoNombre = proyectoActual.getNombre();
                break;
            }
            if (nuevoNombre.length() <= 100) {
                break;
            }
            System.out.println("Nombre no válido: no puede superar 100 caracteres.");
        }

        // Ciudad nueva (Enter = mantener la actual)
        String nuevaCiudad;
        while (true) {
            System.out.println("Nueva ciudad (Enter para mantener \"" + proyectoActual.getCiudad() + "\"):");
            nuevaCiudad = teclado.nextLine().trim();
            if (nuevaCiudad.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (nuevaCiudad.isEmpty()) {
                nuevaCiudad = proyectoActual.getCiudad();
                break;
            }
            if (nuevaCiudad.length() <= 100) {
                break;
            }
            System.out.println("Ciudad no válida: no puede superar 100 caracteres.");
        }

        Proyectos proyectoConCambios = new Proyectos(codigo, nuevoNombre, nuevaCiudad);
        boolean modificado = proyectosDao.modificar(proyectoConCambios);

        if (modificado) {
            System.out.println("Proyecto modificado correctamente.");
        }
    }

    // Búsqueda de proyecto por código
    private static void buscarProyectoPorCodigo(Scanner teclado) {

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

        Proyectos proyectoEncontrado = proyectosDao.buscarPorCodigo(codigo);
        if (proyectoEncontrado == null) {
            System.out.println("No existe ningún proyecto con ese código.");
        } else {
            System.out.println(proyectoEncontrado);
        }
    }

    // Búsqueda de proyecto por nombre (coincidencia parcial)
    private static void buscarProyectoPorNombre(Scanner teclado) {

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

        List<Proyectos> proyectosEncontrados = proyectosDao.buscarPorNombre(texto);
        if (proyectosEncontrados.isEmpty()) {
            System.out.println("No se ha encontrado ningún proyecto con ese nombre.");
        } else {
            for (Proyectos proyecto : proyectosEncontrados) {
                System.out.println(proyecto);
            }
        }
    }

    // Búsqueda de proyecto por ciudad (coincidencia parcial)
    private static void buscarProyectoPorCiudad(Scanner teclado) {

        String texto;
        while (true) {
            System.out.println("Ciudad (o parte del nombre de la ciudad) a buscar (o volver para cancelar):");
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

        List<Proyectos> proyectosEncontrados = proyectosDao.buscarPorCiudad(texto);
        if (proyectosEncontrados.isEmpty()) {
            System.out.println("No se ha encontrado ningún proyecto en esa ciudad.");
        } else {
            for (Proyectos proyecto : proyectosEncontrados) {
                System.out.println(proyecto);
            }
        }
    }

    // Lista todos los proyectos
    private static void listarProyectos() {

        List<Proyectos> todosLosProyectos = proyectosDao.listarTodos();
        if (todosLosProyectos.isEmpty()) {
            System.out.println("Todavía no hay proyectos guardados.");
        } else {
            for (Proyectos proyecto : todosLosProyectos) {
                System.out.println(proyecto);
            }
        }
    }

    // ---------- NUEVO: Submenú de suministros ----------

    private static void menuSuministros(Scanner teclado) {

        boolean volver = false;

        while (!volver) {
            System.out.println();
            System.out.println("----- Suministros -----");
            System.out.println("1. Alta de suministro");
            System.out.println("2. Baja de suministro");
            System.out.println("3. Modificar suministro");
            System.out.println("4. Consultar por identificador");
            System.out.println("5. Consultar por proveedor");
            System.out.println("6. Consultar por pieza");
            System.out.println("7. Consultar por proyecto");
            System.out.println("8. Listar todos");
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
                    altaSuministro(teclado);
                    break;
                case 2:
                    bajaSuministro(teclado);
                    break;
                case 3:
                    modificarSuministro(teclado);
                    break;
                case 4:
                    buscarSuministroPorId(teclado);
                    break;
                case 5:
                    consultarSuministrosPorProveedor(teclado);
                    break;
                case 6:
                    consultarSuministrosPorPieza(teclado);
                    break;
                case 7:
                    consultarSuministrosPorProyecto(teclado);
                    break;
                case 8:
                    listarSuministros();
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

    // NUEVO: Alta de suministro: elige proveedor, pieza y proyecto que ya existan, y pide fecha y cantidad
    private static void altaSuministro(Scanner teclado) {

        System.out.println("Alta de suministro (escribe volver en cualquier campo para cancelar).");

        // Si no hay datos en alguna de las tres tablas, no se puede dar de alta
        if (proveedoresDao.listarTodos().isEmpty()) {
            System.out.println("Primero tienes que dar de alta al menos un proveedor.");
            return;
        }
        if (piezasDao.listarTodos().isEmpty()) {
            System.out.println("Primero tienes que dar de alta al menos una pieza.");
            return;
        }
        if (proyectosDao.listarTodos().isEmpty()) {
            System.out.println("Primero tienes que dar de alta al menos un proyecto.");
            return;
        }

        // Proveedor: tiene que existir
        Proveedores proveedorElegido = null;
        while (true) {
            System.out.println("Código del proveedor:");
            String codigoProveedor = teclado.nextLine().trim();
            if (codigoProveedor.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (!codigoProveedor.matches("[A-Za-z0-9]{1,10}")) {
                System.out.println("Código no válido: usa solo letras y números, de 1 a 10 caracteres.");
                continue;
            }
            proveedorElegido = proveedoresDao.buscarPorCodigo(codigoProveedor);
            if (proveedorElegido == null) {
                System.out.println("No existe ningún proveedor con ese código.");
                continue;
            }
            break;
        }
        System.out.println("Proveedor elegido: " + proveedorElegido);

        // Pieza: tiene que existir
        Piezas piezaElegida = null;
        while (true) {
            System.out.println("Código de la pieza:");
            String codigoPieza = teclado.nextLine().trim();
            if (codigoPieza.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (!codigoPieza.matches("[A-Za-z0-9]{1,10}")) {
                System.out.println("Código no válido: usa solo letras y números, de 1 a 10 caracteres.");
                continue;
            }
            piezaElegida = piezasDao.buscarPorCodigo(codigoPieza);
            if (piezaElegida == null) {
                System.out.println("No existe ninguna pieza con ese código.");
                continue;
            }
            break;
        }
        System.out.println("Pieza elegida: " + piezaElegida);

        // Proyecto: tiene que existir
        Proyectos proyectoElegido = null;
        while (true) {
            System.out.println("Código del proyecto:");
            String codigoProyecto = teclado.nextLine().trim();
            if (codigoProyecto.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (!codigoProyecto.matches("[A-Za-z0-9]{1,10}")) {
                System.out.println("Código no válido: usa solo letras y números, de 1 a 10 caracteres.");
                continue;
            }
            proyectoElegido = proyectosDao.buscarPorCodigo(codigoProyecto);
            if (proyectoElegido == null) {
                System.out.println("No existe ningún proyecto con ese código.");
                continue;
            }
            break;
        }
        System.out.println("Proyecto elegido: " + proyectoElegido);

        // Fecha: formato dd/mm/aaaa y que sea una fecha real
        LocalDate fecha = null;
        while (true) {
            System.out.println("Fecha del suministro (formato dd/mm/aaaa, por ejemplo 03/11/2026):");
            String textoFecha = teclado.nextLine().trim();
            if (textoFecha.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            try {
                fecha = LocalDate.parse(textoFecha, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                System.out.println("Fecha no válida: usa el formato dd/mm/aaaa con una fecha real.");
                continue;
            }
            break;
        }

        // Cantidad: número entero entre 1 y 1000000
        int cantidad = 0;
        while (true) {
            System.out.println("Cantidad (número entero entre 1 y 1000000):");
            String textoCantidad = teclado.nextLine().trim();
            if (textoCantidad.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            try {
                cantidad = Integer.parseInt(textoCantidad);
            } catch (NumberFormatException e) {
                System.out.println("Cantidad no válida: escribe un número entero.");
                continue;
            }
            if (cantidad < 1 || cantidad > 1000000) {
                System.out.println("Cantidad no válida: debe estar entre 1 y 1000000.");
                continue;
            }
            break;
        }

        // Con todos los datos validados, se los pasamos al DAO
        Gestion suministroNuevo = new Gestion(fecha, cantidad, proveedorElegido, piezaElegida, proyectoElegido);
        boolean guardado = gestionDao.insertar(suministroNuevo);

        if (guardado) {
            System.out.println("Suministro guardado correctamente.");
        }
    }

    // NUEVO: Baja de suministro: busca el suministro, lo muestra, pide confirmación y lo elimina
    private static void bajaSuministro(Scanner teclado) {

        System.out.println("Baja de suministro (escribe volver para cancelar).");

        int id = 0;
        while (true) {
            System.out.println("Identificador del suministro a eliminar:");
            String textoId = teclado.nextLine().trim();
            if (textoId.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            try {
                id = Integer.parseInt(textoId);
            } catch (NumberFormatException e) {
                System.out.println("Identificador no válido: escribe un número entero.");
                continue;
            }
            if (id < 1) {
                System.out.println("Identificador no válido: debe ser un número mayor que 0.");
                continue;
            }
            break;
        }

        Gestion suministroEncontrado = gestionDao.buscarPorId(id);
        if (suministroEncontrado == null) {
            System.out.println("No existe ningún suministro con ese identificador.");
            return;
        }

        System.out.println(suministroEncontrado);
        System.out.println("¿Seguro que quieres eliminarlo? (s/n):");
        String confirmacion = teclado.nextLine().trim();

        if (confirmacion.equalsIgnoreCase("s")) {
            boolean eliminado = gestionDao.eliminar(id);
            if (eliminado) {
                System.out.println("Suministro eliminado correctamente.");
            }
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    // NUEVO: Modificar suministro: muestra el suministro y pide los datos nuevos; Enter mantiene el valor actual
    private static void modificarSuministro(Scanner teclado) {

        System.out.println("Modificar suministro (escribe volver en cualquier campo para cancelar).");

        int id = 0;
        while (true) {
            System.out.println("Identificador del suministro a modificar:");
            String textoId = teclado.nextLine().trim();
            if (textoId.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            try {
                id = Integer.parseInt(textoId);
            } catch (NumberFormatException e) {
                System.out.println("Identificador no válido: escribe un número entero.");
                continue;
            }
            if (id < 1) {
                System.out.println("Identificador no válido: debe ser un número mayor que 0.");
                continue;
            }
            break;
        }

        Gestion suministroActual = gestionDao.buscarPorId(id);
        if (suministroActual == null) {
            System.out.println("No existe ningún suministro con ese identificador.");
            return;
        }

        System.out.println("Datos actuales: " + suministroActual);

        // Proveedor nuevo (Enter = mantener el actual)
        Proveedores nuevoProveedor = suministroActual.getProveedor();
        while (true) {
            System.out.println("Nuevo código de proveedor (Enter para mantener \"" + suministroActual.getProveedor().getCodigo() + "\"):");
            String codigoProveedor = teclado.nextLine().trim();
            if (codigoProveedor.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (codigoProveedor.isEmpty()) {
                break;
            }
            if (!codigoProveedor.matches("[A-Za-z0-9]{1,10}")) {
                System.out.println("Código no válido: usa solo letras y números, de 1 a 10 caracteres.");
                continue;
            }
            Proveedores proveedorBuscado = proveedoresDao.buscarPorCodigo(codigoProveedor);
            if (proveedorBuscado == null) {
                System.out.println("No existe ningún proveedor con ese código.");
                continue;
            }
            nuevoProveedor = proveedorBuscado;
            break;
        }

        // Pieza nueva (Enter = mantener la actual)
        Piezas nuevaPieza = suministroActual.getPieza();
        while (true) {
            System.out.println("Nuevo código de pieza (Enter para mantener \"" + suministroActual.getPieza().getCodigo() + "\"):");
            String codigoPieza = teclado.nextLine().trim();
            if (codigoPieza.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (codigoPieza.isEmpty()) {
                break;
            }
            if (!codigoPieza.matches("[A-Za-z0-9]{1,10}")) {
                System.out.println("Código no válido: usa solo letras y números, de 1 a 10 caracteres.");
                continue;
            }
            Piezas piezaBuscada = piezasDao.buscarPorCodigo(codigoPieza);
            if (piezaBuscada == null) {
                System.out.println("No existe ninguna pieza con ese código.");
                continue;
            }
            nuevaPieza = piezaBuscada;
            break;
        }

        // Proyecto nuevo (Enter = mantener el actual)
        Proyectos nuevoProyecto = suministroActual.getProyecto();
        while (true) {
            System.out.println("Nuevo código de proyecto (Enter para mantener \"" + suministroActual.getProyecto().getCodigo() + "\"):");
            String codigoProyecto = teclado.nextLine().trim();
            if (codigoProyecto.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (codigoProyecto.isEmpty()) {
                break;
            }
            if (!codigoProyecto.matches("[A-Za-z0-9]{1,10}")) {
                System.out.println("Código no válido: usa solo letras y números, de 1 a 10 caracteres.");
                continue;
            }
            Proyectos proyectoBuscado = proyectosDao.buscarPorCodigo(codigoProyecto);
            if (proyectoBuscado == null) {
                System.out.println("No existe ningún proyecto con ese código.");
                continue;
            }
            nuevoProyecto = proyectoBuscado;
            break;
        }

        // Fecha nueva (Enter = mantener la actual)
        LocalDate nuevaFecha = suministroActual.getFecha();
        while (true) {
            System.out.println("Nueva fecha dd/mm/aaaa (Enter para mantener " + suministroActual.getFecha().format(FORMATO_FECHA) + "):");
            String textoFecha = teclado.nextLine().trim();
            if (textoFecha.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (textoFecha.isEmpty()) {
                break;
            }
            try {
                nuevaFecha = LocalDate.parse(textoFecha, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                System.out.println("Fecha no válida: usa el formato dd/mm/aaaa con una fecha real.");
                continue;
            }
            break;
        }

        // Cantidad nueva (Enter = mantener la actual)
        int nuevaCantidad = suministroActual.getCantidad();
        while (true) {
            System.out.println("Nueva cantidad (Enter para mantener " + suministroActual.getCantidad() + "):");
            String textoCantidad = teclado.nextLine().trim();
            if (textoCantidad.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (textoCantidad.isEmpty()) {
                break;
            }
            int cantidadLeida = 0;
            try {
                cantidadLeida = Integer.parseInt(textoCantidad);
            } catch (NumberFormatException e) {
                System.out.println("Cantidad no válida: escribe un número entero.");
                continue;
            }
            if (cantidadLeida < 1 || cantidadLeida > 1000000) {
                System.out.println("Cantidad no válida: debe estar entre 1 y 1000000.");
                continue;
            }
            nuevaCantidad = cantidadLeida;
            break;
        }

        // Montamos la ficha con los cambios y le ponemos el id del suministro que modificamos
        Gestion suministroConCambios = new Gestion(nuevaFecha, nuevaCantidad, nuevoProveedor, nuevaPieza, nuevoProyecto);
        suministroConCambios.setId(id);
        boolean modificado = gestionDao.modificar(suministroConCambios);

        if (modificado) {
            System.out.println("Suministro modificado correctamente.");
        }
    }

    // NUEVO: Consulta de un suministro por identificador
    private static void buscarSuministroPorId(Scanner teclado) {

        int id = 0;
        while (true) {
            System.out.println("Identificador a buscar (o volver para cancelar):");
            String textoId = teclado.nextLine().trim();
            if (textoId.equalsIgnoreCase("volver")) {
                System.out.println("Operación cancelada.");
                return;
            }
            try {
                id = Integer.parseInt(textoId);
            } catch (NumberFormatException e) {
                System.out.println("Identificador no válido: escribe un número entero.");
                continue;
            }
            if (id < 1) {
                System.out.println("Identificador no válido: debe ser un número mayor que 0.");
                continue;
            }
            break;
        }

        Gestion suministroEncontrado = gestionDao.buscarPorId(id);
        if (suministroEncontrado == null) {
            System.out.println("No existe ningún suministro con ese identificador.");
        } else {
            System.out.println(suministroEncontrado);
        }
    }

    // NUEVO: Consulta de los suministros de un proveedor
    private static void consultarSuministrosPorProveedor(Scanner teclado) {

        String codigo;
        while (true) {
            System.out.println("Código del proveedor (o volver para cancelar):");
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

        List<Gestion> suministrosEncontrados = gestionDao.buscarPorProveedor(codigo);
        if (suministrosEncontrados.isEmpty()) {
            System.out.println("No hay suministros de ese proveedor.");
        } else {
            for (Gestion suministro : suministrosEncontrados) {
                System.out.println(suministro);
            }
        }
    }

    // NUEVO: Consulta de los suministros de una pieza
    private static void consultarSuministrosPorPieza(Scanner teclado) {

        String codigo;
        while (true) {
            System.out.println("Código de la pieza (o volver para cancelar):");
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

        List<Gestion> suministrosEncontrados = gestionDao.buscarPorPieza(codigo);
        if (suministrosEncontrados.isEmpty()) {
            System.out.println("No hay suministros de esa pieza.");
        } else {
            for (Gestion suministro : suministrosEncontrados) {
                System.out.println(suministro);
            }
        }
    }

    // NUEVO: Consulta de los suministros de un proyecto
    private static void consultarSuministrosPorProyecto(Scanner teclado) {

        String codigo;
        while (true) {
            System.out.println("Código del proyecto (o volver para cancelar):");
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

        List<Gestion> suministrosEncontrados = gestionDao.buscarPorProyecto(codigo);
        if (suministrosEncontrados.isEmpty()) {
            System.out.println("No hay suministros de ese proyecto.");
        } else {
            for (Gestion suministro : suministrosEncontrados) {
                System.out.println(suministro);
            }
        }
    }

    // NUEVO: Lista todos los suministros
    private static void listarSuministros() {

        List<Gestion> todosLosSuministros = gestionDao.listarTodos();
        if (todosLosSuministros.isEmpty()) {
            System.out.println("Todavía no hay suministros guardados.");
        } else {
            for (Gestion suministro : todosLosSuministros) {
                System.out.println(suministro);
            }
        }
    }
}