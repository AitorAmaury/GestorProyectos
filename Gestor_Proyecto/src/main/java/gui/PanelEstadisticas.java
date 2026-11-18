package gui;

import idao.IEstadisticasDao;
import idao.IProveedoresDao;
import idao.IProyectosDao;
import models.Gestion;
import models.Proveedores;
import models.Proyectos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PanelEstadisticas extends JPanel {

    private IEstadisticasDao estadisticasDao;
    private IProveedoresDao proveedoresDao;
    private IProyectosDao proyectosDao;

    private List<Proveedores> listaProveedores = new ArrayList<>();
    private List<Proyectos> listaProyectos = new ArrayList<>();

    private DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private DefaultTableModel listaDeChinchetas;
    private JTable pizarraDeCorcho;

    private JComboBox<String> comboInforme;
    private JComboBox<String> comboElemento;
    private JLabel etiquetaResumen;

    public PanelEstadisticas(IEstadisticasDao estadisticasDao,
                             IProveedoresDao proveedoresDao,
                             IProyectosDao proyectosDao) {
        this.estadisticasDao = estadisticasDao;
        this.proveedoresDao = proveedoresDao;
        this.proyectosDao = proyectosDao;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // ----- Arriba: qué informe quiero y sobre quién -----
        JPanel bandejaSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandejaSuperior.add(new JLabel("Informe:"));

        comboInforme = new JComboBox<>(new String[]{
                "Histórico de un proveedor",
                "Análisis de un proyecto",
                "Piezas más suministradas (top 5)",
                "Proveedores más activos (top 5)",
                "Gasto por proyecto",
                "Totales generales"});
        bandejaSuperior.add(comboInforme);

        bandejaSuperior.add(new JLabel("Elegir:"));
        comboElemento = new JComboBox<>();
        comboElemento.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        bandejaSuperior.add(comboElemento);

        JButton botonConsultar = new JButton("Consultar");
        bandejaSuperior.add(botonConsultar);

        add(bandejaSuperior, BorderLayout.NORTH);

        // ----- Centro: tabla (las columnas cambian según el informe) -----
        listaDeChinchetas = new DefaultTableModel();
        pizarraDeCorcho = new JTable(listaDeChinchetas);
        pizarraDeCorcho.setDefaultEditor(Object.class, null);
        add(new JScrollPane(pizarraDeCorcho), BorderLayout.CENTER);

        // ----- Abajo: resumen -----
        etiquetaResumen = new JLabel(" ");
        add(etiquetaResumen, BorderLayout.SOUTH);

        // ----- Timbres -----
        botonConsultar.addActionListener(e -> consultar());
        comboInforme.addActionListener(e -> actualizarElementos());

        recargarDatos();
    }

    // Recarga las listas de proveedores y proyectos (AppGui lo llama al entrar en la pestaña)
    public void recargarDatos() {
        try {
            listaProveedores = proveedoresDao.listarTodos();
            listaProyectos = proyectosDao.listarTodos();
            actualizarElementos();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "No se han podido cargar los datos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Según el informe, el segundo desplegable muestra proveedores, proyectos o nada
    private void actualizarElementos() {
        comboElemento.removeAllItems();
        int informe = comboInforme.getSelectedIndex();

        if (informe == 0) {
            for (Proveedores proveedor : listaProveedores) {
                comboElemento.addItem(proveedor.getCodigo() + " - " + proveedor.getNombre());
            }
            comboElemento.setEnabled(true);
        } else if (informe == 1) {
            for (Proyectos proyecto : listaProyectos) {
                comboElemento.addItem(proyecto.getCodigo() + " - " + proyecto.getNombre());
            }
            comboElemento.setEnabled(true);
        } else {
            comboElemento.setEnabled(false);
        }
    }

    private void consultar() {
        try {
            int informe = comboInforme.getSelectedIndex();
            listaDeChinchetas.setRowCount(0);

            if (informe == 0) {
                // ----- Histórico de proveedor -----
                if (comboElemento.getSelectedIndex() < 0) {
                    JOptionPane.showMessageDialog(this,
                            "No hay ningún proveedor dado de alta.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String codigo = listaProveedores.get(comboElemento.getSelectedIndex()).getCodigo();
                List<Gestion> historico = estadisticasDao.historicoProveedor(codigo);

                listaDeChinchetas.setColumnIdentifiers(
                        new String[]{"Fecha", "Pieza", "Proyecto", "Cantidad", "Importe"});

                int totalUnidades = 0;
                double totalImporte = 0;
                for (Gestion suministro : historico) {
                    double importe = suministro.getCantidad() * suministro.getPieza().getPrecio();
                    Object[] fila = {
                            suministro.getFecha().format(formatoFecha),
                            suministro.getPieza().getNombre(),
                            suministro.getProyecto().getNombre(),
                            suministro.getCantidad(),
                            String.format("%.2f", importe)
                    };
                    listaDeChinchetas.addRow(fila);
                    totalUnidades = totalUnidades + suministro.getCantidad();
                    totalImporte = totalImporte + importe;
                }

                if (historico.isEmpty()) {
                    etiquetaResumen.setText("Este proveedor no tiene suministros.");
                } else {
                    etiquetaResumen.setText("Suministros: " + historico.size()
                            + " | Unidades: " + totalUnidades
                            + " | Importe total: " + String.format("%.2f", totalImporte));
                }

            } else if (informe == 1) {
                // ----- Análisis de proyecto -----
                if (comboElemento.getSelectedIndex() < 0) {
                    JOptionPane.showMessageDialog(this,
                            "No hay ningún proyecto dado de alta.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String codigo = listaProyectos.get(comboElemento.getSelectedIndex()).getCodigo();
                List<Object[]> piezas = estadisticasDao.piezasDeProyecto(codigo);

                listaDeChinchetas.setColumnIdentifiers(
                        new String[]{"Pieza", "Unidades", "Importe"});

                for (Object[] fila : piezas) {
                    double importe = ((Number) fila[2]).doubleValue();
                    Object[] filaBonita = {fila[0], fila[1], String.format("%.2f", importe)};
                    listaDeChinchetas.addRow(filaBonita);
                }

                if (piezas.isEmpty()) {
                    etiquetaResumen.setText("Este proyecto no tiene suministros.");
                } else {
                    double gasto = estadisticasDao.gastoProyecto(codigo);
                    etiquetaResumen.setText("Gasto total del proyecto: " + String.format("%.2f", gasto));
                }

            } else if (informe == 2) {
                // ----- Piezas más suministradas -----
                List<Object[]> piezas = estadisticasDao.piezasMasSuministradas();
                listaDeChinchetas.setColumnIdentifiers(
                        new String[]{"Código", "Pieza", "Unidades"});
                for (Object[] fila : piezas) {
                    listaDeChinchetas.addRow(fila);
                }
                etiquetaResumen.setText("Piezas con más unidades suministradas.");

            } else if (informe == 3) {
                // ----- Proveedores más activos -----
                List<Object[]> proveedores = estadisticasDao.proveedoresMasActivos();
                listaDeChinchetas.setColumnIdentifiers(
                        new String[]{"Código", "Proveedor", "Unidades"});
                for (Object[] fila : proveedores) {
                    listaDeChinchetas.addRow(fila);
                }
                etiquetaResumen.setText("Proveedores con más unidades suministradas.");

            } else if (informe == 4) {
                // ----- Gasto por proyecto -----
                List<Object[]> proyectos = estadisticasDao.gastoPorProyecto();
                listaDeChinchetas.setColumnIdentifiers(
                        new String[]{"Código", "Proyecto", "Importe"});
                double total = 0;
                for (Object[] fila : proyectos) {
                    double importe = ((Number) fila[2]).doubleValue();
                    Object[] filaBonita = {fila[0], fila[1], String.format("%.2f", importe)};
                    listaDeChinchetas.addRow(filaBonita);
                    total = total + importe;
                }
                etiquetaResumen.setText("Gasto total de todos los proyectos: " + String.format("%.2f", total));

            } else {
                // ----- Totales generales -----
                listaDeChinchetas.setColumnIdentifiers(new String[]{"Dato", "Valor"});
                listaDeChinchetas.addRow(new Object[]{"Proveedores", estadisticasDao.totalProveedores()});
                listaDeChinchetas.addRow(new Object[]{"Piezas", estadisticasDao.totalPiezas()});
                listaDeChinchetas.addRow(new Object[]{"Proyectos", estadisticasDao.totalProyectos()});
                listaDeChinchetas.addRow(new Object[]{"Suministros", estadisticasDao.totalSuministros()});
                etiquetaResumen.setText("Totales de la base de datos.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al consultar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}