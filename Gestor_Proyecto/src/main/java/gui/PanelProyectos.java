package gui;

import idao.IProyectosDao;
import models.Proyectos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelProyectos extends JPanel {

    private IProyectosDao proyectosDao;

    private DefaultTableModel listaDeChinchetas;
    private JTable pizarraDeCorcho;

    private JTextField campoCodigo;
    private JTextField campoNombre;
    private JTextField campoCiudad;

    private JComboBox<String> comboCriterio;
    private JTextField campoBusqueda;

    public PanelProyectos(IProyectosDao dao) {
        this.proyectosDao = dao;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Arriba: búsqueda
        JPanel bandejaDeBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandejaDeBusqueda.add(new JLabel("Buscar por:"));

        comboCriterio = new JComboBox<>(new String[]{"Código", "Nombre", "Ciudad"});
        bandejaDeBusqueda.add(comboCriterio);

        campoBusqueda = new JTextField(20);
        bandejaDeBusqueda.add(campoBusqueda);

        JButton botonBuscar = new JButton("Buscar");
        bandejaDeBusqueda.add(botonBuscar);

        JButton botonVerTodos = new JButton("Ver todos");
        bandejaDeBusqueda.add(botonVerTodos);

        add(bandejaDeBusqueda, BorderLayout.NORTH);

        // Centro: tabla
        String[] columnas = {"Código", "Nombre", "Ciudad"};
        listaDeChinchetas = new DefaultTableModel(columnas, 0);
        pizarraDeCorcho = new JTable(listaDeChinchetas);
        pizarraDeCorcho.setDefaultEditor(Object.class, null);
        pizarraDeCorcho.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(pizarraDeCorcho), BorderLayout.CENTER);

        // Abajo: formulario y botones
        JPanel bandejaDelFormulario = new JPanel(new GridLayout(3, 2, 5, 5));

        bandejaDelFormulario.add(new JLabel("Código:"));
        campoCodigo = new JTextField();
        bandejaDelFormulario.add(campoCodigo);

        bandejaDelFormulario.add(new JLabel("Nombre:"));
        campoNombre = new JTextField();
        bandejaDelFormulario.add(campoNombre);

        bandejaDelFormulario.add(new JLabel("Ciudad:"));
        campoCiudad = new JTextField();
        bandejaDelFormulario.add(campoCiudad);

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

    private void mostrarTabla(List<Proyectos> lista) {
        listaDeChinchetas.setRowCount(0);
        for (Proyectos proyecto : lista) {
            Object[] fila = {
                    proyecto.getCodigo(),
                    proyecto.getNombre(),
                    proyecto.getCiudad()
            };
            listaDeChinchetas.addRow(fila);
        }
    }

    private void verTodos() {
        try {
            mostrarTabla(proyectosDao.listarTodos());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "No se han podido cargar los proyectos: " + e.getMessage(),
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
        campoCiudad.setText(listaDeChinchetas.getValueAt(fila, 2).toString());
        campoCodigo.setEditable(false);
    }

    private void limpiarFormulario() {
        campoCodigo.setText("");
        campoNombre.setText("");
        campoCiudad.setText("");
        campoCodigo.setEditable(true);
        pizarraDeCorcho.clearSelection();
    }

    private boolean datosValidos() {
        String codigo = campoCodigo.getText().trim();
        String nombre = campoNombre.getText().trim();
        String ciudad = campoCiudad.getText().trim();

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
        if (ciudad.isEmpty() || ciudad.length() > 100) {
            JOptionPane.showMessageDialog(this,
                    "Ciudad no válida: no puede estar vacía ni superar 100 caracteres.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            campoCiudad.requestFocus();
            return false;
        }
        return true;
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

            int criterio = comboCriterio.getSelectedIndex();
            List<Proyectos> resultados = new ArrayList<>();

            if (criterio == 0) {
                if (!texto.matches("[A-Za-z0-9]{1,10}")) {
                    JOptionPane.showMessageDialog(this,
                            "Código no válido: usa solo letras y números, de 1 a 10 caracteres.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Proyectos encontrado = proyectosDao.buscarPorCodigo(texto);
                if (encontrado != null) {
                    resultados.add(encontrado);
                }
            } else if (criterio == 1) {
                resultados = proyectosDao.buscarPorNombre(texto);
            } else {
                resultados = proyectosDao.buscarPorCiudad(texto);
            }

            mostrarTabla(resultados);

            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se ha encontrado ningún proyecto.",
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
                        "Hay un proyecto seleccionado. Pulsa Limpiar para dar de alta uno nuevo.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!datosValidos()) {
                return;
            }

            String codigo = campoCodigo.getText().trim();
            if (proyectosDao.buscarPorCodigo(codigo) != null) {
                JOptionPane.showMessageDialog(this,
                        "Ya existe un proyecto con ese código.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Proyectos proyectoNuevo = new Proyectos(
                    codigo,
                    campoNombre.getText().trim(),
                    campoCiudad.getText().trim());

            boolean guardado = proyectosDao.insertar(proyectoNuevo);

            if (guardado) {
                JOptionPane.showMessageDialog(this,
                        "Proyecto guardado correctamente.",
                        "Alta", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                verTodos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se ha podido guardar el proyecto. Revisa la consola.",
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
                        "Selecciona primero un proyecto de la tabla.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!datosValidos()) {
                return;
            }

            Proyectos proyectoConCambios = new Proyectos(
                    campoCodigo.getText().trim(),
                    campoNombre.getText().trim(),
                    campoCiudad.getText().trim());

            boolean modificado = proyectosDao.modificar(proyectoConCambios);

            if (modificado) {
                JOptionPane.showMessageDialog(this,
                        "Proyecto modificado correctamente.",
                        "Modificar", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                verTodos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se ha podido modificar el proyecto. Revisa la consola.",
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
                        "Selecciona primero un proyecto de la tabla.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String codigo = campoCodigo.getText().trim();

            if (proyectosDao.tieneSuministros(codigo)) {
                JOptionPane.showMessageDialog(this,
                        "No se puede eliminar: el proyecto tiene suministros asociados.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int respuesta = JOptionPane.showConfirmDialog(this,
                    "¿Seguro que quieres eliminar el proyecto " + codigo + "?",
                    "Confirmar baja", JOptionPane.YES_NO_OPTION);

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }

            boolean eliminado = proyectosDao.eliminar(codigo);

            if (eliminado) {
                JOptionPane.showMessageDialog(this,
                        "Proyecto eliminado correctamente.",
                        "Baja", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                verTodos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se ha podido eliminar el proyecto. Revisa la consola.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al eliminar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}