// Importamos Path y Files del paquete java.nio.file.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

public class ListadoArchivos2 {

    public static void main(String[] args) {

        // Si hemos pasado algún argumento, utilizamos args[0] como ruta.
        // Si no hemos pasado ninguno, utilizamos por defecto el directorio personal del usuario ("user.home").
        String ruta = (args.length > 0) ? args[0] : System.getProperty("user.home");

        // Creamos un objeto Path que representa la ruta indicada.
        Path fich = Path.of(ruta);

        // Comprobamos si la ruta existe realmente en el sistema.
        if (!Files.exists(fich)) {
            System.out.printf(
                "No existe el fichero o directorio (%s).\n",
                ruta
            );
            return;
        }

        // Si es un fichero común
        if (Files.isRegularFile(fich)) {
            System.out.printf(
                "%s es un fichero regular.\n",
                ruta
            );

        // Si es un directorio
        } else if (Files.isDirectory(fich)) {

            System.out.printf(
                "Contenido del directorio %s (filtrando ocultos y enlaces simbólicos):\n",
                ruta
            );
            System.out.println("-----------------------------------------------------------------------------------------");

            try (Stream<Path> ficheros = Files.list(fich)) {

                ficheros.filter(f -> {
                    try {
                        // 1. Eliminar enlaces simbólicos
                        if (Files.isSymbolicLink(f)) {
                            return false;
                        }
                        // 2. Eliminar archivos u ocultos (que empiecen por '.' o tengan atributo oculto)
                        String nombreArchivo = f.getFileName().toString();
                        if (nombreArchivo.startsWith(".") || Files.isHidden(f)) {
                            return false;
                        }
                    } catch (IOException e) {
                        return false;
                    }
                    return true;
                }).forEach(f -> {
                    String nombre = f.getFileName().toString();

                    // Si el elemento es un directorio, se muestra sólo el nombre (añadiendo "/")
                    if (Files.isDirectory(f)) {
                        System.out.printf("[DIRECTORIO] %s/\n", nombre);
                    } else {
                        // Si es un archivo, obtenemos sus atributos específicos
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(f, BasicFileAttributes.class);
                            
                            // Fecha de modificación formateada
                            LocalDateTime fechaMod = LocalDateTime.ofInstant(
                                attrs.lastModifiedTime().toInstant(), 
                                ZoneId.systemDefault()
                            );
                            String fechaStr = fechaMod.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                            // Propietario del archivo
                            UserPrincipal propietario = Files.getOwner(f);
                            String propStr = (propietario != null) ? propietario.getName() : "Desconocido";

                            // Tamaño formateado en la unidad más apropiada
                            String tamanoStr = formatearTamano(attrs.size());

                            // Imprimir información detallada del archivo
                            System.out.printf("  - Archivo    : %s\n", nombre);
                            System.out.printf("    Modificación: %s | Propietario: %s | Tamaño: %s\n", fechaStr, propStr, tamanoStr);
                            System.out.println("-----------------------------------------------------------------------------------------");

                        } catch (IOException e) {
                            System.out.printf("  - %s (Error al leer atributos: %s)\n", nombre, e.getMessage());
                        }
                    }
                });

            } catch (IOException e) {
                System.out.println(
                    "Error al acceder al directorio: " + e.getMessage()
                );
            }
        }
    }

    // Método auxiliar para formatear los bytes a la unidad más adecuada (Bytes, KB, MB, GB, etc.)
    private static String formatearTamano(long bytes) {
        if (bytes <= 0) return "0 B";
        String[] unidades = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(bytes / Math.pow(1024, digitGroups)) + " " + unidades[digitGroups];
    }
}
