package gui;

import dao.ProveedoresDao;
import idao.IProveedoresDao;

import javax.swing.*;

public class AppGui {

    public static void main(String[] args) {
        // Swing arranca en su hilo propio: la cola del pan donde se dibuja todo
        SwingUtilities.invokeLater(() -> crearVentana());
    }

    private static void crearVentana() {

        // Creamos los DAO; si falla la conexión, avisamos y no abrimos la ventana
        IProveedoresDao proveedoresDao;
        try {
            proveedoresDao = new ProveedoresDao();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "No se ha podido conectar con la base de datos: " + e.getMessage()
                            + "\nRevisa que MariaDB está arrancado.",
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // El marco del cuadro: la ventana principal
        JFrame ventana = new JFrame("Gestor de proyectos");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(900, 600);
        ventana.setLocationRelativeTo(null);

        // Las pestañas: una por módulo
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Proveedores", new PanelProveedores(proveedoresDao));

        ventana.add(pestanas);
        ventana.setVisible(true);
    }
}