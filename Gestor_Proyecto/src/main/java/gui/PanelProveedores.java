package gui;

import idao.IProveedoresDao;
import models.Proveedores;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelProveedores extends JPanel {

    // El DAO con el que hablamos con la base de datos
    private IProveedoresDao proveedoresDao;

    // La lista de chinchetas (los datos) y la pizarra de corcho (cómo se ven)
    private DefaultTableModel listaDeChinchetas;
    private JTable pizarraDeCorcho;

    // Campos del formulario de abajo
    private JTextField campoCodigo;
    private JTextField campoNombre;
    private JTextField campoApellidos;
    private JTextField campoDireccion;

    // Controles de la búsqueda de arriba
    private JComboBox<String> comboCriterio;
    private JTextField campoBusqueda;

    public PanelProveedores(IProveedoresDao dao) {
        this.proveedoresDao = dao;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // ----- Zona de arriba: búsqueda -----
        JPanel bandejaDeBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandejaDeBusqueda.add(new JLabel("Buscar por:"));

        comboCriterio = new JComboBox<>(new String[]{"Código", "Nombre", "Dirección"});
        bandejaDeBusqueda.add(comboCriterio);

        campoBusqueda = new JTextField(20);
        bandejaDeBusqueda.add(campoBusqueda);

        JButton botonBuscar = new JButton("Buscar");
        bandejaDeBusqueda.add(botonBuscar);

        JButton botonVerTodos = new JButton("Ver todos");
        bandejaDeBusqueda.add(botonVerTodos);

        add(bandejaDeBusqueda, BorderLayout.NORTH);

        // ----- Zona central: la tabla -----
        String[] columnas = {"Código", "Nombre", "Apellidos", "Dirección postal"};
        listaDeChinchetas = new DefaultTableModel(columnas, 0);
        pizarraDeCorcho = new JTable(listaDeChinchetas);

        // La tabla solo se mira: las celdas no se editan a mano
        pizarraDeCorcho.setDefaultEditor(Object.class, null);
        pizarraDeCorcho.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(pizarraDeCorcho), BorderLayout.CENTER);

        // ----- Zona de abajo: formulario y botones -----
        JPanel bandejaDelFormulario = new JPanel(new GridLayout(4, 2, 5, 5));

        bandejaDelFormulario.add(new JLabel("Código:"));
        campoCodigo = new JTextField();
        bandejaDelFormulario.add(campoCodigo);

        bandejaDelFormulario.add(new JLabel("Nombre:"));
        campoNombre = new JTextField();
        bandejaDelFormulario.add(campoNombre);

        bandejaDelFormulario.add(new JLabel("Apellidos:"));
        campoApellidos = new JTextField();
        bandejaDelFormulario.add(campoApellidos);

        bandejaDelFormulario.add(new JLabel("Dirección postal:"));
        campoDireccion = new JTextField();
        bandejaDelFormulario.add(campoDireccion);

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

        // ----- Los timbres: qué hace cada botón al pulsarlo -----
        botonBuscar.addActionListener(e -> buscar());
        botonVerTodos.addActionListener(e -> verTodos());
        botonAlta.addActionListener(e -> alta());
        botonModificar.addActionListener(e -> modificar());
        botonBaja.addActionListener(e -> baja());
        botonLimpiar.addActionListener(e -> limpiarFormulario());

        // Al hacer clic en una fila, sus datos pasan al formulario
        pizarraDeCorcho.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });

        // Al abrir el panel, mostramos todos los proveedores
        verTodos();
    }

    // Pone en la tabla la lista que le pasemos (borra las chinchetas y pone las nuevas)
    private void mostrarTabla(List<Proveedores> lista) {
        listaDeChinchetas.setRowCount(0);
        for (Proveedores proveedor : lista) {
            Object[] fila = {
                    proveedor.getCodigo(),
                    proveedor.getNombre(),
                    proveedor.getApellidos(),
                    proveedor.getDireccionPostal()
            };
            listaDeChinchetas.addRow(fila);
        }
    }

    // Muestra todos los proveedores
    private void verTodos() {
        try {
            mostrarTabla(proveedoresDao.listarTodos());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "No se han podido cargar los proveedores: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Pasa los datos de la fila marcada al formulario; el código no se puede cambiar
    private void seleccionarFila() {
        int fila = pizarraDeCorcho.getSelectedRow();
        if (fila < 0) {
            return;
        }
        campoCodigo.setText(listaDeChinchetas.getValueAt(fila, 0).toString());
        campoNombre.setText(listaDeChinchetas.getValueAt(fila, 1).toString());
        campoApellidos.setText(listaDeChinchetas.getValueAt(fila, 2).toString());
        campoDireccion.setText(listaDeChinchetas.getValueAt(fila, 3).toString());
        campoCodigo.setEditable(false);
    }

    // Vacía el formulario y deja de marcar la fila
    private void limpiarFormulario() {
        campoCodigo.setText("");
        campoNombre.setText("");
        campoApellidos.setText("");
        campoDireccion.setText("");
        campoCodigo.setEditable(true);
        pizarraDeCorcho.clearSelection();
    }

    // Comprueba los cuatro campos antes de tocar la base de datos
    private boolean datosValidos() {
        String codigo = campoCodigo.getText().trim();
        String nombre = campoNombre.getText().trim();
        String apellidos = campoApellidos.getText().trim();
        String direccion = campoDireccion.getText().trim();

        if (!codigo.matches("[A-Za-z0-9]{1,10}")) {
            JOptionPane.showMessageDialog(this,
                    "Código no válido: usa solo letras y números, de 1 a 10 caracteres.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            campoCodigo.requestFocus();
            return false;
        }
        if (nombre.isEmpty() || nombre.length() > 100) {
            JOptionPane.showMessageDialog(this,
                    "Nombre no válido: no puede estar vacío ni superar 100 caracteres.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            campoNombre.requestFocus();
            return false;
        }
        if (apellidos.isEmpty() || apellidos.length() > 100) {
            JOptionPane.showMessageDialog(this,
                    "Apellidos no válidos: no pueden estar vacíos ni superar 100 caracteres.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            campoApellidos.requestFocus();
            return false;
        }
        if (direccion.isEmpty() || direccion.length() > 150) {
            JOptionPane.showMessageDialog(this,
                    "Dirección no válida: no puede estar vacía ni superar 150 caracteres.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            campoDireccion.requestFocus();
            return false;
        }
        return true;
    }

    // Búsqueda según el criterio elegido en el desplegable
    private void buscar() {
        try {
            String texto = campoBusqueda.getText().trim();
            if (texto.isEmpty() || texto.length() > 150) {
                JOptionPane.showMessageDialog(this,
                        "Escribe un texto para buscar (máximo 150 caracteres).",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int criterio = comboCriterio.getSelectedIndex();
            List<Proveedores> resultados = new ArrayList<>();

            if (criterio == 0) {
                // Búsqueda por código: devuelve uno o ninguno
                if (!texto.matches("[A-Za-z0-9]{1,10}")) {
                    JOptionPane.showMessageDialog(this,
                            "Código no válido: usa solo letras y números, de 1 a 10 caracteres.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Proveedores encontrado = proveedoresDao.buscarPorCodigo(texto);
                if (encontrado != null) {
                    resultados.add(encontrado);
                }
            } else if (criterio == 1) {
                resultados = proveedoresDao.buscarPorNombre(texto);
            } else {
                resultados = proveedoresDao.buscarPorDireccion(texto);
            }

            mostrarTabla(resultados);

            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se ha encontrado ningún proveedor.",
                        "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al buscar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Alta: valida, comprueba que el código no exista y guarda
    private void alta() {
        try {
            if (!campoCodigo.isEditable()) {
                JOptionPane.showMessageDialog(this,
                        "Hay un proveedor seleccionado. Pulsa Limpiar para dar de alta uno nuevo.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!datosValidos()) {
                return;
            }

            String codigo = campoCodigo.getText().trim();
            if (proveedoresDao.buscarPorCodigo(codigo) != null) {
                JOptionPane.showMessageDialog(this,
                        "Ya existe un proveedor con ese código.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Proveedores proveedorNuevo = new Proveedores(
                    codigo,
                    campoNombre.getText().trim(),
                    campoApellidos.getText().trim(),
                    campoDireccion.getText().trim());

            boolean guardado = proveedoresDao.insertar(proveedorNuevo);

            if (guardado) {
                JOptionPane.showMessageDialog(this,
                        "Proveedor guardado correctamente.",
                        "Alta", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                verTodos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se ha podido guardar el proveedor. Revisa la consola.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Modificar: hay que haber elegido antes una fila de la tabla
    private void modificar() {
        try {
            if (campoCodigo.isEditable()) {
                JOptionPane.showMessageDialog(this,
                        "Selecciona primero un proveedor de la tabla.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!datosValidos()) {
                return;
            }

            Proveedores proveedorConCambios = new Proveedores(
                    campoCodigo.getText().trim(),
                    campoNombre.getText().trim(),
                    campoApellidos.getText().trim(),
                    campoDireccion.getText().trim());

            boolean modificado = proveedoresDao.modificar(proveedorConCambios);

            if (modificado) {
                JOptionPane.showMessageDialog(this,
                        "Proveedor modificado correctamente.",
                        "Modificar", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                verTodos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se ha podido modificar el proveedor. Revisa la consola.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al modificar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Baja: pide confirmación y no deja borrar si tiene suministros
    private void baja() {
        try {
            if (campoCodigo.isEditable()) {
                JOptionPane.showMessageDialog(this,
                        "Selecciona primero un proveedor de la tabla.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String codigo = campoCodigo.getText().trim();

            if (proveedoresDao.tieneSuministros(codigo)) {
                JOptionPane.showMessageDialog(this,
                        "No se puede eliminar: el proveedor tiene suministros asociados.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int respuesta = JOptionPane.showConfirmDialog(this,
                    "¿Seguro que quieres eliminar al proveedor " + codigo + "?",
                    "Confirmar baja", JOptionPane.YES_NO_OPTION);

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }

            boolean eliminado = proveedoresDao.eliminar(codigo);

            if (eliminado) {
                JOptionPane.showMessageDialog(this,
                        "Proveedor eliminado correctamente.",
                        "Baja", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                verTodos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se ha podido eliminar el proveedor. Revisa la consola.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al eliminar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}