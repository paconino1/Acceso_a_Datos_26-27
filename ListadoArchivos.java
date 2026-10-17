import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ListadoArchivos {

    public static void main(String[] args) {

        long maxResultados = Long.MAX_VALUE;

        if (args.length > 0) {
            try {
                maxResultados = Long.parseLong(args[0]);

                if (maxResultados < 0) {
                    System.out.println("El límite no puede ser negativo.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("El límite debe ser un número entero.");
                return;
            }
        }

        Path fich = Path.of(System.getProperty("user.home"));

        if (!Files.exists(fich)) {
            System.out.printf("No existe el fichero o directorio (%s).\n", fich);
            return;
        }

        if (Files.isRegularFile(fich)) {
            System.out.printf("%s es un fichero regular.\n", fich);
        } else if (Files.isDirectory(fich)) {
            
            System.out.printf("Contenido del directorio %s:\n", fich);
            System.out.println("-----------------------------------------------------------------------------------------");
            
            try (Stream<Path> ficheros = Files.list(fich)) {
                ficheros
                .filter(f -> {
                    try {
                        return !Files.isSymbolicLink(f)
                                && !f.getFileName().toString().startsWith(".")
                                && !Files.isHidden(f);
                    } catch (IOException e) {
                        System.out.printf("Error al comprobar el archivo: %s%n", e.getMessage());
                        return false;
                    }
                })
                .limit(maxResultados)
                .forEach ( f -> {
                    try {
                        if (!Files.isSymbolicLink(f) && !f.getFileName().toString().startsWith(".") && !Files.isHidden(f)) {
                            if (Files.isDirectory(f)) {
                                System.out.println(f.getFileName());
                            } else {
                                System.out.printf("%s | Modificación: %s | Propietario: %s | Tamaño: %s%n", f.getFileName(), Files.getLastModifiedTime(f), Files.getOwner(f).getName(), formatearTamaño(Files.size(f)));
                            }
                        }
                    } catch (IOException e) {
                        System.out.printf("Error al listar el contenido del directorio: %s\n", e.getMessage());
                    }
                });

            } catch (IOException e) {
                System.out.printf("Error al listar el contenido del directorio: %s\n", e.getMessage());
            } catch (Exception e) {
                System.out.printf("Error al listar el contenido del directorio: %s\n", e.getMessage());
            }
        }

    }

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