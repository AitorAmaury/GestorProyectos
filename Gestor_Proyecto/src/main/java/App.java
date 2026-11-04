import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;

public class App {

    public static void main(String[] args) {

        // Se crea una sola vez: lee persistence.xml y se conecta a la base de datos
        EntityManagerFactory fabrica = null;

        // Con este objeto se hace cada operación contra la base de datos
        EntityManager gestor = null;

        try {
            // Al construir la fábrica, Hibernate se conecta y crea las tablas que falten
            fabrica = Persistence.createEntityManagerFactory("default");

            // Abrimos un gestor para hacer una operación
            gestor = fabrica.createEntityManager();

            // Consulta mínima para comprobar que la base de datos responde
            Object respuesta = gestor.createNativeQuery("SELECT 1").getSingleResult();

            System.out.println("Conexión correcta con MariaDB. Respuesta de prueba: " + respuesta);

        } catch (PersistenceException e) {
            // Fallo de conexión, de configuración o de mapeo de las entidades
            System.out.println("No se ha podido conectar con la base de datos.");
            System.out.println("Motivo: " + e.getMessage());

        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());

        } finally {
            // Cerramos siempre el gestor y la fábrica, haya ido bien o mal
            if (gestor != null && gestor.isOpen()) {
                gestor.close();
            }
            if (fabrica != null && fabrica.isOpen()) {
                fabrica.close();
            }
        }
    }
}