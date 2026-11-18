package gui;

import dao.EstadisticasDao;       
import dao.GestionDao;
import dao.PiezasDao;
import dao.ProveedoresDao;
import dao.ProyectosDao;
import idao.IEstadisticasDao;
import idao.IGestionDao;
import idao.IPiezasDao;
import idao.IProveedoresDao;
import idao.IProyectosDao;

import javax.swing.*;

public class AppGui {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> crearVentana());
    }

    private static void crearVentana() {

        IProveedoresDao proveedoresDao;
        IPiezasDao piezasDao;
        IProyectosDao proyectosDao;
        IGestionDao gestionDao;
        IEstadisticasDao estadisticasDao;
        try {
            proveedoresDao = new ProveedoresDao();
            piezasDao = new PiezasDao();
            proyectosDao = new ProyectosDao();
            gestionDao = new GestionDao();
            estadisticasDao = new EstadisticasDao();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "No se ha podido conectar con la base de datos: " + e.getMessage()
                            + "\nRevisa que MariaDB está arrancado.",
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFrame ventana = new JFrame("Gestor de proyectos");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(900, 700);
        ventana.setLocationRelativeTo(null);

        PanelSuministros panelSuministros =
                new PanelSuministros(gestionDao, proveedoresDao, piezasDao, proyectosDao);


        PanelEstadisticas panelEstadisticas =
                new PanelEstadisticas(estadisticasDao, proveedoresDao, proyectosDao);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Proveedores", new PanelProveedores(proveedoresDao));
        pestanas.addTab("Piezas", new PanelPiezas(piezasDao));
        pestanas.addTab("Proyectos", new PanelProyectos(proyectosDao));
        pestanas.addTab("Suministros", panelSuministros);
        pestanas.addTab("Estadísticas", panelEstadisticas);

        // Al entrar en Suministros o en Estadísticas se recargan sus datos
        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedComponent() == panelSuministros) {
                panelSuministros.recargarDatos();
            } else if (pestanas.getSelectedComponent() == panelEstadisticas) {
                panelEstadisticas.recargarDatos();
            }
        });

        ventana.add(pestanas);
        ventana.setVisible(true);
    }
}