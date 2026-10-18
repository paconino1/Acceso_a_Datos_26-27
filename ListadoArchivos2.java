import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Stream;

public class ListadoArchivos2 {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        // Los argumentos opcionales son: ruta, máximo de resultados y profundidad.
        if (args.length > 3) {
            mostrarUso();
            return;
        }

        String ruta = args.length > 0 ? args[0] : System.getProperty("user.home");
        long maxResultados = Long.MAX_VALUE;
        int maxProfundidad = Integer.MAX_VALUE;

        if (args.length > 1) {
            try {
                maxResultados = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("El máximo de resultados debe ser un número entero mayor que 0.");
                return;
            }
            if (maxResultados <= 0) {
                System.out.println("El máximo de resultados debe ser un número entero mayor que 0.");
                return;
            }
        }

        if (args.length > 2) {
            try {
                maxProfundidad = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.out.println("La profundidad debe ser un número entero mayor o igual que 0.");
                return;
            }
            if (maxProfundidad < 0) {
                System.out.println("La profundidad debe ser un número entero mayor o igual que 0.");
                return;
            }
        }

        Path directorio = Path.of(ruta);

        // Comprobamos que la ruta existe y no es un enlace simbólico.
        if (!Files.exists(directorio) || Files.isSymbolicLink(directorio)) {
            System.out.printf("No existe la ruta o es un enlace simbólico (%s).%n", ruta);
            return;
        }

        if (Files.isRegularFile(directorio)) {
            if (maxResultados > 0) {
                mostrarArchivo(directorio, directorio, 0);
            }
        } else if (Files.isDirectory(directorio)) {
            System.out.printf("Contenido de %s (sin ocultos ni enlaces simbólicos):%n", directorio);
            System.out.println("-----------------------------------------------------------------------------------------");

            // La ruta inicial está a profundidad 0; profundidad 1 lista sus hijos directos.
            listarDirectorio(directorio, directorio, 0, maxProfundidad, maxResultados, 0);
        } else {
            System.out.printf("La ruta no es un archivo regular ni un directorio (%s).%n", ruta);
        }
    }

    private static void mostrarUso() {
        System.out.println("Uso: java ListadoArchivos2 [ruta] [máximoResultados] [profundidad]");
        System.out.println("Si no se indica ruta, se utiliza el directorio personal del usuario.");
    }

    // Recorre el contenido de un directorio sin superar los límites indicados.
    private static long listarDirectorio(
            Path raiz,
            Path directorio,
            int profundidadActual,
            int maxProfundidad,
            long maxResultados,
            long resultados) {
        if (profundidadActual >= maxProfundidad || resultados >= maxResultados) {
            return resultados;
        }

        try (Stream<Path> contenido = Files.list(directorio)) {
            var indice = contenido.iterator();

            while (indice.hasNext() && resultados < maxResultados) {
                Path elemento = indice.next();
                if (!esVisible(elemento)) {
                    continue;
                }

                int profundidadElemento = profundidadActual + 1;
                if (Files.isDirectory(elemento)) {
                    mostrarDirectorio(raiz, elemento, profundidadElemento);
                    resultados++;

                    if (profundidadElemento < maxProfundidad && resultados < maxResultados) {
                        resultados = listarDirectorio(
                                raiz,
                                elemento,
                                profundidadElemento,
                                maxProfundidad,
                                maxResultados,
                                resultados);
                    }
                } else {
                    mostrarArchivo(raiz, elemento, profundidadElemento);
                    resultados++;
                }
            }
        } catch (IOException e) {
            System.out.printf("Error al acceder al directorio %s: %s%n", directorio, e.getMessage());
        } catch (DirectoryIteratorException e) {
            System.out.printf("Error al recorrer el directorio %s: %s%n", directorio, e.getMessage());
        }

        return resultados;
    }

    // Excluye enlaces simbólicos y archivos ocultos.
    private static boolean esVisible(Path fichero) {
        try {
            return !Files.isSymbolicLink(fichero)
                    && !fichero.getFileName().toString().startsWith(".")
                    && !Files.isHidden(fichero);
        } catch (IOException e) {
            System.out.printf("Error al comprobar el archivo %s: %s%n", fichero, e.getMessage());
            return false;
        }
    }

    // Muestra un directorio solo con su nombre, usando sangría para distinguir los niveles.
    private static void mostrarDirectorio(Path raiz, Path directorio, int profundidad) {
        System.out.printf("%s%s%n", "  ".repeat(profundidad), raiz.relativize(directorio));
    }

    // Muestra los atributos solicitados para un archivo.
    private static void mostrarArchivo(Path raiz, Path fichero, int profundidad) {
        try {
            BasicFileAttributes atributos = Files.readAttributes(fichero, BasicFileAttributes.class);
            LocalDateTime fechaModificacion = LocalDateTime.ofInstant(
                    atributos.lastModifiedTime().toInstant(),
                    ZoneId.systemDefault());
            String fecha = fechaModificacion.format(FORMATO_FECHA);
            UserPrincipal propietario = Files.getOwner(fichero);
            String nombrePropietario = propietario == null ? "Desconocido" : propietario.getName();

            System.out.printf(
                    "%s%s | Modificación: %s | Propietario: %s | Tamaño: %s%n",
                    "  ".repeat(profundidad),
                    nombreRelativo(raiz, fichero),
                    fecha,
                    nombrePropietario,
                    formatearTamano(atributos.size()));
        } catch (IOException e) {
            System.out.printf(
                    "%s%s (Error al leer los atributos: %s)%n",
                    "  ".repeat(profundidad),
                    nombreRelativo(raiz, fichero),
                    e.getMessage());
        }
    }

    // Devuelve una ruta relativa y conserva el nombre si el elemento es la raíz.
    private static Path nombreRelativo(Path raiz, Path elemento) {
        return raiz.equals(elemento) ? elemento.getFileName() : raiz.relativize(elemento);
    }

    // Convierte el tamaño en bytes a la unidad más apropiada.
    private static String formatearTamano(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        String[] unidades = {"KiB", "MiB", "GiB", "TiB", "PiB", "EiB"};
        double tamano = bytes;
        int indice = -1;

        do {
            tamano /= 1024;
            indice++;
        } while (tamano >= 1024 && indice < unidades.length - 1);

        return String.format(Locale.forLanguageTag("es-ES"), "%.2f %s", tamano, unidades[indice]);
    }
}
