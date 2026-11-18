package gui;

import idao.IGestionDao;
import idao.IPiezasDao;
import idao.IProveedoresDao;
import idao.IProyectosDao;
import models.Gestion;
import models.Piezas;
import models.Proveedores;
import models.Proyectos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public class PanelSuministros extends JPanel {

    private IGestionDao gestionDao;
    private IProveedoresDao proveedoresDao;
    private IPiezasDao piezasDao;
    private IProyectosDao proyectosDao;

    // Las listas de verdad: el desplegable muestra textos, pero aquí están los objetos
    // y el número de opción elegida en el desplegable es la posición en su lista
    private List<Proveedores> listaProveedores = new ArrayList<>();
    private List<Piezas> listaPiezas = new ArrayList<>();
    private List<Proyectos> listaProyectos = new ArrayList<>();

    // Fechas en formato dd/MM/aaaa, estricto: rechaza el 31/02
    private DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    // Los datos (chinchetas) y la pizarra que los muestra
    private DefaultTableModel listaDeChinchetas;
    private JTable pizarraDeCorcho;

    // Formulario
    private JTextField campoId;
    private JTextField campoFecha;
    private JTextField campoCantidad;
    private JComboBox<String> comboProveedor;
    private JComboBox<String> comboPieza;
    private JComboBox<String> comboProyecto;

    // Búsqueda
    private JComboBox<String> comboCriterio;
    private JTextField campoBusqueda;

    public PanelSuministros(IGestionDao gestionDao, IProveedoresDao proveedoresDao,
                            IPiezasDao piezasDao, IProyectosDao proyectosDao) {
        this.gestionDao = gestionDao;
        this.proveedoresDao = proveedoresDao;
        this.piezasDao = piezasDao;
        this.proyectosDao = proyectosDao;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Arriba: búsqueda
        JPanel bandejaDeBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandejaDeBusqueda.add(new JLabel("Buscar por:"));

        comboCriterio = new JComboBox<>(new String[]{
                "Id", "Código de proveedor", "Código de pieza", "Código de proyecto"});
        bandejaDeBusqueda.add(comboCriterio);

        campoBusqueda = new JTextField(15);
        bandejaDeBusqueda.add(campoBusqueda);

        JButton botonBuscar = new JButton("Buscar");
        bandejaDeBusqueda.add(botonBuscar);

        JButton botonVerTodos = new JButton("Ver todos");
        bandejaDeBusqueda.add(botonVerTodos);

        add(bandejaDeBusqueda, BorderLayout.NORTH);

        // Centro: tabla
        String[] columnas = {"Id", "Fecha", "Proveedor", "Pieza", "Proyecto", "Cantidad"};
        listaDeChinchetas = new DefaultTableModel(columnas, 0);
        pizarraDeCorcho = new JTable(listaDeChinchetas);
        pizarraDeCorcho.setDefaultEditor(Object.class, null);
        pizarraDeCorcho.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(pizarraDeCorcho), BorderLayout.CENTER);

        //Abajo: formulario y botones
        JPanel bandejaDelFormulario = new JPanel(new GridLayout(6, 2, 5, 5));

        bandejaDelFormulario.add(new JLabel("Id (automático):"));
        campoId = new JTextField();
        campoId.setEditable(false);
        bandejaDelFormulario.add(campoId);

        bandejaDelFormulario.add(new JLabel("Fecha (dd/mm/aaaa):"));
        campoFecha = new JTextField();
        bandejaDelFormulario.add(campoFecha);

        bandejaDelFormulario.add(new JLabel("Cantidad:"));
        campoCantidad = new JTextField();
        bandejaDelFormulario.add(campoCantidad);

        bandejaDelFormulario.add(new JLabel("Proveedor:"));
        comboProveedor = new JComboBox<>();
        bandejaDelFormulario.add(comboProveedor);

        bandejaDelFormulario.add(new JLabel("Pieza:"));
        comboPieza = new JComboBox<>();
        bandejaDelFormulario.add(comboPieza);

        bandejaDelFormulario.add(new JLabel("Proyecto:"));
        comboProyecto = new JComboBox<>();
        bandejaDelFormulario.add(comboProyecto);

        JPanel bandejaDeBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton botonAlta = new JButton("Alta");
        JButton botonModificar = new JButton("Modificar");
        JButton botonBaja = new JButton("Baja");
        JButton botonLimpiar = new JButton("Limpiar");
        bandejaDeBotones.add(botonAlta);
        bandejaDeBotones.add(botonModificar);
        bandejaDeBotones.add(botonBaja);
        bandejaDeBotones.add(botonLimpiar);

        JPanel zonaDeAbajo = new JPanel(new BorderLayout(5, 5));
        zonaDeAbajo.add(bandejaDelFormulario, BorderLayout.CENTER);
        zonaDeAbajo.add(bandejaDeBotones, BorderLayout.SOUTH);
        add(zonaDeAbajo, BorderLayout.SOUTH);

        //Timbres
        botonBuscar.addActionListener(e -> buscar());
        botonVerTodos.addActionListener(e -> verTodos());
        botonAlta.addActionListener(e -> alta());
        botonModificar.addActionListener(e -> modificar());
        botonBaja.addActionListener(e -> baja());
        botonLimpiar.addActionListener(e -> limpiarFormulario());

        pizarraDeCorcho.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });

        recargarDatos();
    }

    // Vuelve a llenar los tres desplegables y la tabla con lo que haya ahora en la base de datos.
    // AppGui lo llama cada vez que se entra en esta pestaña.
    public void recargarDatos() {
        try {
            listaProveedores = proveedoresDao.listarTodos();
            listaPiezas = piezasDao.listarTodos();
            listaProyectos = proyectosDao.listarTodos();

            comboProveedor.removeAllItems();
            for (Proveedores proveedor : listaProveedores) {
                comboProveedor.addItem(proveedor.getCodigo() + " - " + proveedor.getNombre());
            }

            comboPieza.removeAllItems();
            for (Piezas pieza : listaPiezas) {
                comboPieza.addItem(pieza.getCodigo() + " - " + pieza.getNombre());
            }

            comboProyecto.removeAllItems();
            for (Proyectos proyecto : listaProyectos) {
                comboProyecto.addItem(proyecto.getCodigo() + " - " + proyecto.getNombre());
            }

            limpiarFormulario();
            verTodos();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "No se han podido cargar los datos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarTabla(List<Gestion> lista) {
        listaDeChinchetas.setRowCount(0);
        for (Gestion suministro : lista) {
            // Si algún dato faltara (no debería), mostramos un texto vacío en vez de fallar
            String fecha = "";
            if (suministro.getFecha() != null) {
                fecha = suministro.getFecha().format(formatoFecha);
            }
            String codigoProveedor = "";
            if (suministro.getProveedor() != null) {
                codigoProveedor = suministro.getProveedor().getCodigo();
            }
            String codigoPieza = "";
            if (suministro.getPieza() != null) {
                codigoPieza = suministro.getPieza().getCodigo();
            }
            String codigoProyecto = "";
            if (suministro.getProyecto() != null) {
                codigoProyecto = suministro.getProyecto().getCodigo();
            }

            Object[] fila = {
                    suministro.getId(),
                    fecha,
                    codigoProveedor,
                    codigoPieza,
                    codigoProyecto,
                    suministro.getCantidad()
            };
            listaDeChinchetas.addRow(fila);
        }
    }

    private void verTodos() {
        try {
            mostrarTabla(gestionDao.listarTodos());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "No se han podido cargar los suministros: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Pasa la fila marcada al formulario, eligiendo en cada desplegable el código que toca
    private void seleccionarFila() {
        int fila = pizarraDeCorcho.getSelectedRow();
        if (fila < 0) {
            return;
        }
        campoId.setText(listaDeChinchetas.getValueAt(fila, 0).toString());
        campoFecha.setText(listaDeChinchetas.getValueAt(fila, 1).toString());
        campoCantidad.setText(listaDeChinchetas.getValueAt(fila, 5).toString());

        String codigoProveedor = listaDeChinchetas.getValueAt(fila, 2).toString();
        for (int i = 0; i < listaProveedores.size(); i++) {
            if (listaProveedores.get(i).getCodigo().equals(codigoProveedor)) {
                comboProveedor.setSelectedIndex(i);
            }
        }

        String codigoPieza = listaDeChinchetas.getValueAt(fila, 3).toString();
        for (int i = 0; i < listaPiezas.size(); i++) {
            if (listaPiezas.get(i).getCodigo().equals(codigoPieza)) {
                comboPieza.setSelectedIndex(i);
            }
        }

        String codigoProyecto = listaDeChinchetas.getValueAt(fila, 4).toString();
        for (int i = 0; i < listaProyectos.size(); i++) {
            if (listaProyectos.get(i).getCodigo().equals(codigoProyecto)) {
                comboProyecto.setSelectedIndex(i);
            }
        }
    }

    private void limpiarFormulario() {
        campoId.setText("");
        campoFecha.setText("");
        campoCantidad.setText("");
        pizarraDeCorcho.clearSelection();
    }

    // Comprueba que haya un proveedor, una pieza y un proyecto elegidos
    private boolean hayElegidos() {
        if (comboProveedor.getSelectedIndex() < 0
                || comboPieza.getSelectedIndex() < 0
                || comboProyecto.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this,
                    "Hace falta tener al menos un proveedor, una pieza y un proyecto dados de alta.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    // Convierte el texto de la fecha; devuelve null si no es válida
    private LocalDate leerFecha() {
        try {
            return LocalDate.parse(campoFecha.getText().trim(), formatoFecha);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // Convierte el texto de la cantidad; devuelve -1 si no es válida
    private int leerCantidad() {
        try {
            int cantidad = Integer.parseInt(campoCantidad.getText().trim());
            if (cantidad < 1 || cantidad > 1000000) {
                return -1;
            }
            return cantidad;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean datosValidos() {
        if (!hayElegidos()) {
            return false;
        }
        if (leerFecha() == null) {
            JOptionPane.showMessageDialog(this,
                    "Fecha no válida: usa el formato dd/mm/aaaa con una fecha que exista.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            campoFecha.requestFocus();
            return false;
        }
        if (leerCantidad() < 0) {
            JOptionPane.showMessageDialog(this,
                    "Cantidad no válida: escribe un número entero entre 1 y 1000000.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            campoCantidad.requestFocus();
            return false;
        }
        return true;
    }

    // Crea el suministro con lo que hay en el formulario
    private Gestion suministroDelFormulario() {
        Proveedores proveedorElegido = listaProveedores.get(comboProveedor.getSelectedIndex());
        Piezas piezaElegida = listaPiezas.get(comboPieza.getSelectedIndex());
        Proyectos proyectoElegido = listaProyectos.get(comboProyecto.getSelectedIndex());

        return new Gestion(leerFecha(), leerCantidad(),
                proveedorElegido, piezaElegida, proyectoElegido);
    }

    private void buscar() {
        try {
            String texto = campoBusqueda.getText().trim();
            if (texto.isEmpty() || texto.length() > 20) {
                JOptionPane.showMessageDialog(this,
                        "Escribe un texto para buscar (máximo 20 caracteres).",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int criterio = comboCriterio.getSelectedIndex();
            List<Gestion> resultados = new ArrayList<>();

            if (criterio == 0) {
                // Búsqueda por id: el id es un número entero positivo
                int id;
                try {
                    id = Integer.parseInt(texto);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "Id no válido: escribe un número entero.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (id < 1) {
                    JOptionPane.showMessageDialog(this,
                            "Id no válido: debe ser 1 o mayor.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Gestion encontrado = gestionDao.buscarPorId(id);
                if (encontrado != null) {
                    resultados.add(encontrado);
                }
            } else {
                // Las otras tres búsquedas son por código
                if (!texto.matches("[A-Za-z0-9]{1,10}")) {
                    JOptionPane.showMessageDialog(this,
                            "Código no válido: usa solo letras y números, de 1 a 10 caracteres.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (criterio == 1) {
                    resultados = gestionDao.buscarPorProveedor(texto);
                } else if (criterio == 2) {
                    resultados = gestionDao.buscarPorPieza(texto);
                } else {
                    resultados = gestionDao.buscarPorProyecto(texto);
                }
            }

            mostrarTabla(resultados);

            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se ha encontrado ningún suministro.",
                        "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al buscar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alta() {
        try {
            if (!campoId.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Hay un suministro seleccionado. Pulsa Limpiar para dar de alta uno nuevo.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!datosValidos()) {
                return;
            }

            boolean guardado = gestionDao.insertar(suministroDelFormulario());

            if (guardado) {
                JOptionPane.showMessageDialog(this,
                        "Suministro guardado correctamente.",
                        "Alta", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                verTodos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se ha podido guardar el suministro. Revisa la consola.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificar() {
        try {
            if (campoId.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Selecciona primero un suministro de la tabla.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!datosValidos()) {
                return;
            }

            Gestion suministroConCambios = suministroDelFormulario();
            suministroConCambios.setId(Integer.parseInt(campoId.getText()));

            boolean modificado = gestionDao.modificar(suministroConCambios);

            if (modificado) {
                JOptionPane.showMessageDialog(this,
                        "Suministro modificado correctamente.",
                        "Modificar", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                verTodos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se ha podido modificar el suministro. Revisa la consola.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al modificar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void baja() {
        try {
            if (campoId.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Selecciona primero un suministro de la tabla.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = Integer.parseInt(campoId.getText());

            int respuesta = JOptionPane.showConfirmDialog(this,
                    "¿Seguro que quieres eliminar el suministro " + id + "?",
                    "Confirmar baja", JOptionPane.YES_NO_OPTION);

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }

            boolean eliminado = gestionDao.eliminar(id);

            if (eliminado) {
                JOptionPane.showMessageDialog(this,
                        "Suministro eliminado correctamente.",
                        "Baja", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                verTodos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se ha podido eliminar el suministro. Revisa la consola.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al eliminar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}