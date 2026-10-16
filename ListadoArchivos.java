import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ListadoArchivos {

    public static void main(String[] args) {

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
                ficheros.forEach ( f -> {
                    try {
                        if (!Files.isSymbolicLink(f) && !f.getFileName().toString().startsWith(".") && !Files.isHidden(f)) {
                            System.out.println(f);
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
}