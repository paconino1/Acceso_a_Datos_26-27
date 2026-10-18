import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ListadoArchivos {

    public static void main(String[] args) {
        // Si se indica un argumento, se utiliza como máximo de resultados.
        // Si no, Long.MAX_VALUE permite mostrar todos los elementos encontrados.
        long maxResultados = Long.MAX_VALUE;

        if (args.length > 0) {
            try {
                maxResultados = Long.parseLong(args[0]);

                // El límite no puede ser negativo.
                if (maxResultados < 0) {
                    System.out.println("El límite no puede ser negativo.");
                    return;
                }
            } catch (NumberFormatException e) {
                // El argumento debe poder convertirse a un número entero.
                System.out.println("El límite debe ser un número entero.");
                return;
            }
        }

        // Utilizamos por defecto el directorio personal del usuario.
        Path fich = Path.of(System.getProperty("user.home"));

        // Comprobamos si la ruta del directorio personal existe.
        if (!Files.exists(fich)) {
            System.out.printf("No existe el fichero o directorio (%s).%n", fich);
            return;
        }

        // Si la ruta es un fichero, mostramos esa información.
        if (Files.isRegularFile(fich)) {
            System.out.printf("%s es un fichero.%n", fich);

        // Si es un directorio, listamos únicamente su contenido directo.
        } else if (Files.isDirectory(fich)) {
            System.out.printf("Contenido del directorio %s:%n", fich);
            System.out.println("-----------------------------------------------------------------------------------------");

            try (Stream<Path> ficheros = Files.list(fich)) {
                ficheros
                    // Excluimos elementos ocultos y enlaces simbólicos.
                    .filter(ListadoArchivos::esVisible)
                    // Limitamos el número de elementos después de aplicar el filtro.
                    .limit(maxResultados)
                    .forEach(ListadoArchivos::mostrar);
            } catch (IOException e) {
                System.out.printf("Error al listar el contenido del directorio: %s%n", e.getMessage());
            }
        }
    }

    // Determina si el elemento debe incluirse en el listado.
    private static boolean esVisible(Path fichero) {
        try {
            return !Files.isSymbolicLink(fichero)
                    && !fichero.getFileName().toString().startsWith(".")
                    && !Files.isHidden(fichero);
        } catch (IOException e) {
            System.out.printf("Error al comprobar el archivo: %s%n", e.getMessage());
            return false;
        }
    }

    // Muestra solo el nombre de los directorios o los detalles de cada archivo.
    private static void mostrar(Path fichero) {
        if (Files.isDirectory(fichero)) {
            System.out.println(fichero.getFileName());
            return;
        }

        try {
            System.out.printf(
                    "%s | Modificación: %s | Propietario: %s | Tamaño: %s%n",
                    fichero.getFileName(),
                    Files.getLastModifiedTime(fichero),
                    Files.getOwner(fichero).getName(),
                    formatearTamaño(Files.size(fichero)));
        } catch (IOException e) {
            System.out.printf("Error al listar el contenido del directorio: %s%n", e.getMessage());
        }
    }

    // Convierte el tamaño en bytes a una unidad más legible.
    private static String formatearTamaño(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        String[] unidades = {"KB", "MB", "GB", "TB"};
        double tamaño = bytes;
        int indice = -1;

        do {
            tamaño /= 1024;
            indice++;
        } while (tamaño >= 1024 && indice < unidades.length - 1);

        return String.format("%.1f %s", tamaño, unidades[indice]);
    }
}