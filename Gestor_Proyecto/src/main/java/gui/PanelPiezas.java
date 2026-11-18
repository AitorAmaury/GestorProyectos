package gui;

import idao.IPiezasDao;
import models.Piezas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelPiezas extends JPanel {

    private IPiezasDao piezasDao;

    // Los datos (chinchetas) y la pizarra que los muestra
    private DefaultTableModel listaDeChinchetas;
    private JTable pizarraDeCorcho;

    // Formulario
    private JTextField campoCodigo;
    private JTextField campoNombre;
    private JTextField campoPrecio;
    private JTextField campoDescripcion;

    // Búsqueda
    private JComboBox<String> comboCriterio;
    private JTextField campoBusqueda;

    public PanelPiezas(IPiezasDao dao) {
        this.piezasDao = dao;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Arriba: búsqueda
        JPanel bandejaDeBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandejaDeBusqueda.add(new JLabel("Buscar por:"));

        comboCriterio = new JComboBox<>(new String[]{"Código", "Nombre"});
        bandejaDeBusqueda.add(comboCriterio);

        campoBusqueda = new JTextField(20);
        bandejaDeBusqueda.add(campoBusqueda);

        JButton botonBuscar = new JButton("Buscar");
        bandejaDeBusqueda.add(botonBuscar);

        JButton botonVerTodos = new JButton("Ver todos");
        bandejaDeBusqueda.add(botonVerTodos);

        add(bandejaDeBusqueda, BorderLayout.NORTH);

        //Centro: tabla
        String[] columnas = {"Código", "Nombre", "Precio", "Descripción"};
        listaDeChinchetas = new DefaultTableModel(columnas, 0);
        pizarraDeCorcho = new JTable(listaDeChinchetas);
        pizarraDeCorcho.setDefaultEditor(Object.class, null);
        pizarraDeCorcho.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(pizarraDeCorcho), BorderLayout.CENTER);

        // Abajo: formulario y botones
        JPanel bandejaDelFormulario = new JPanel(new GridLayout(4, 2, 5, 5));

        bandejaDelFormulario.add(new JLabel("Código:"));
        campoCodigo = new JTextField();
        bandejaDelFormulario.add(campoCodigo);

        bandejaDelFormulario.add(new JLabel("Nombre:"));
        campoNombre = new JTextField();
        bandejaDelFormulario.add(campoNombre);

        bandejaDelFormulario.add(new JLabel("Precio:"));
        campoPrecio = new JTextField();
        bandejaDelFormulario.add(campoPrecio);

        bandejaDelFormulario.add(new JLabel("Descripción (opcional):"));
        campoDescripcion = new JTextField();
        bandejaDelFormulario.add(campoDescripcion);

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

        // Timbres
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

        verTodos();
    }

    private void mostrarTabla(List<Piezas> lista) {
        listaDeChinchetas.setRowCount(0);
        for (Piezas pieza : lista) {
            // La descripción puede ser null: en la tabla la mostramos vacía
            String descripcion = "";
            if (pieza.getDescripcion() != null) {
                descripcion = pieza.getDescripcion();
            }
            Object[] fila = {
                    pieza.getCodigo(),
                    pieza.getNombre(),
                    pieza.getPrecio(),
                    descripcion
            };
            listaDeChinchetas.addRow(fila);
        }
    }

    private void verTodos() {
        try {
            mostrarTabla(piezasDao.listarTodos());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "No se han podido cargar las piezas: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarFila() {
        int fila = pizarraDeCorcho.getSelectedRow();
        if (fila < 0) {
            return;
        }
        campoCodigo.setText(listaDeChinchetas.getValueAt(fila, 0).toString());
        campoNombre.setText(listaDeChinchetas.getValueAt(fila, 1).toString());
        campoPrecio.setText(listaDeChinchetas.getValueAt(fila, 2).toString());
        campoDescripcion.setText(listaDeChinchetas.getValueAt(fila, 3).toString());
        campoCodigo.setEditable(false);
    }

    private void limpiarFormulario() {
        campoCodigo.setText("");
        campoNombre.setText("");
        campoPrecio.setText("");
        campoDescripcion.setText("");
        campoCodigo.setEditable(true);
        pizarraDeCorcho.clearSelection();
    }

    // Convierte el texto del precio a número; devuelve -1 si no es válido
    // (el precio válido va de 0 a 1.000.000, así que -1 nunca es un precio real)
    private double leerPrecio() {
        String texto = campoPrecio.getText().trim().replace(",", ".");
        try {
            double precio = Double.parseDouble(texto);
            if (Double.isNaN(precio) || Double.isInfinite(precio)) {
                return -1;
            }
            if (precio < 0 || precio > 1000000) {
                return -1;
            }
            return precio;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean datosValidos() {
        String codigo = campoCodigo.getText().trim();
        String nombre = campoNombre.getText().trim();
        String descripcion = campoDescripcion.getText().trim();

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
        if (leerPrecio() < 0) {
            JOptionPane.showMessageDialog(this,
                    "Precio no válido: escribe un número entre 0 y 1000000 (con coma o punto).",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            campoPrecio.requestFocus();
            return false;
        }
        if (descripcion.length() > 255) {
            JOptionPane.showMessageDialog(this,
                    "Descripción no válida: no puede superar 255 caracteres.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            campoDescripcion.requestFocus();
            return false;
        }
        return true;
    }

    // Crea la pieza con los datos del formulario (descripción vacía = null)
    private Piezas piezaDelFormulario() {
        String descripcion = campoDescripcion.getText().trim();
        if (descripcion.isEmpty()) {
            descripcion = null;
        }
        return new Piezas(
                campoCodigo.getText().trim(),
                campoNombre.getText().trim(),
                leerPrecio(),
                descripcion);
    }

    private void buscar() {
        try {
            String texto = campoBusqueda.getText().trim();
            if (texto.isEmpty() || texto.length() > 100) {
                JOptionPane.showMessageDialog(this,
                        "Escribe un texto para buscar (máximo 100 caracteres).",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<Piezas> resultados = new ArrayList<>();

            if (comboCriterio.getSelectedIndex() == 0) {
                if (!texto.matches("[A-Za-z0-9]{1,10}")) {
                    JOptionPane.showMessageDialog(this,
                            "Código no válido: usa solo letras y números, de 1 a 10 caracteres.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Piezas encontrada = piezasDao.buscarPorCodigo(texto);
                if (encontrada != null) {
                    resultados.add(encontrada);
                }
            } else {
                resultados = piezasDao.buscarPorNombre(texto);
            }

            mostrarTabla(resultados);

            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se ha encontrado ninguna pieza.",
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
            if (!campoCodigo.isEditable()) {
                JOptionPane.showMessageDialog(this,
                        "Hay una pieza seleccionada. Pulsa Limpiar para dar de alta una nueva.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!datosValidos()) {
                return;
            }

            String codigo = campoCodigo.getText().trim();
            if (piezasDao.buscarPorCodigo(codigo) != null) {
                JOptionPane.showMessageDialog(this,
                        "Ya existe una pieza con ese código.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean guardada = piezasDao.insertar(piezaDelFormulario());

            if (guardada) {
                JOptionPane.showMessageDialog(this,
                        "Pieza guardada correctamente.",
                        "Alta", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                verTodos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se ha podido guardar la pieza. Revisa la consola.",
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
            if (campoCodigo.isEditable()) {
                JOptionPane.showMessageDialog(this,
                        "Selecciona primero una pieza de la tabla.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!datosValidos()) {
                return;
            }

            boolean modificada = piezasDao.modificar(piezaDelFormulario());

            if (modificada) {
                JOptionPane.showMessageDialog(this,
                        "Pieza modificada correctamente.",
                        "Modificar", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                verTodos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se ha podido modificar la pieza. Revisa la consola.",
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
            if (campoCodigo.isEditable()) {
                JOptionPane.showMessageDialog(this,
                        "Selecciona primero una pieza de la tabla.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String codigo = campoCodigo.getText().trim();

            if (piezasDao.tieneSuministros(codigo)) {
                JOptionPane.showMessageDialog(this,
                        "No se puede eliminar: la pieza tiene suministros asociados.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int respuesta = JOptionPane.showConfirmDialog(this,
                    "¿Seguro que quieres eliminar la pieza " + codigo + "?",
                    "Confirmar baja", JOptionPane.YES_NO_OPTION);

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }

            boolean eliminada = piezasDao.eliminar(codigo);

            if (eliminada) {
                JOptionPane.showMessageDialog(this,
                        "Pieza eliminada correctamente.",
                        "Baja", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                verTodos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se ha podido eliminar la pieza. Revisa la consola.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al eliminar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}