package gui;

import dao.PiezasDao;
import dao.ProveedoresDao;
import dao.ProyectosDao;
import idao.IPiezasDao;
import idao.IProveedoresDao;
import idao.IProyectosDao;

import javax.swing.*;

public class AppGui {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> crearVentana());
    }

    private static void crearVentana() {

        // creamos tres DAO; si falla la conexión, avisamos y no abrimos la ventana
        IProveedoresDao proveedoresDao;
        IPiezasDao piezasDao;
        IProyectosDao proyectosDao;
        try {
            proveedoresDao = new ProveedoresDao();
            piezasDao = new PiezasDao();
            proyectosDao = new ProyectosDao();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "No se ha podido conectar con la base de datos: " + e.getMessage()
                            + "\nRevisa que MariaDB está arrancado.",
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFrame ventana = new JFrame("Gestor de proyectos");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(900, 600);
        ventana.setLocationRelativeTo(null);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Proveedores", new PanelProveedores(proveedoresDao));
        pestanas.addTab("Piezas", new PanelPiezas(piezasDao));
        pestanas.addTab("Proyectos", new PanelProyectos(proyectosDao));  

        ventana.add(pestanas);
        ventana.setVisible(true);
    }
}