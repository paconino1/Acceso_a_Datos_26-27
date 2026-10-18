<div align="center">

# 📂 Explorador de archivos en Java

### Dos ejercicios para descubrir el sistema de archivos con `java.nio`

**Acceso a Datos · Curso 2026–2027**

Recorre tu carpeta personal, consulta información de sus archivos y experimenta
con filtros, límites y profundidad sin modificar ni borrar su contenido.

</div>

---

## ✨ Las actividades

| Programa | Recorrido | Para qué sirve |
|---|---|---|
| `ListadoArchivos` | No recursivo | Lista los elementos que están directamente en la carpeta personal. |
| `ListadoArchivos2` | Recursivo | Explora una ruta y sus subdirectorios, con máximo de resultados y profundidad configurables. |

Ambos programas detectan automáticamente la carpeta personal mediante
`System.getProperty("user.home")`. No dependen de una ruta fija como `C:\Users\Lucia`.

## 🚀 Requisitos y puesta en marcha

- **JDK 11 o posterior** (se utiliza `Path.of` y `String.repeat`).
- PowerShell, Terminal o una consola compatible.

Abre la terminal en la carpeta del proyecto y compila los dos programas:

```powershell
javac .\ListadoArchivos.java .\ListadoArchivos2.java
```

Después, ejecuta el programa que quieras probar:

```powershell
java -cp . ListadoArchivos
java -cp . ListadoArchivos2
```

Al no indicar una ruta, ambos parten de la carpeta personal del usuario actual.

## 1 · Listado directo

`ListadoArchivos` muestra **solo los elementos inmediatos** de la carpeta personal.
No entra en las subcarpetas.

- Los directorios se muestran únicamente con su nombre.
- Los archivos muestran nombre, fecha de modificación, propietario y tamaño.
- Se excluyen los elementos ocultos y los enlaces simbólicos.
- El máximo cuenta los elementos visibles; si no se especifica, no se aplica un límite práctico.
- No se ordenan los resultados: el orden puede variar entre ejecuciones.

### Ejemplos

```powershell
# Mostrar todos los elementos visibles de la carpeta personal
java -cp . ListadoArchivos

# Mostrar como máximo cinco elementos visibles
java -cp . ListadoArchivos 5
```

El máximo puede ser cero (no se muestran elementos); los números negativos y los
valores que no sean enteros se rechazan con un mensaje.

## 2 · Listado recursivo

`ListadoArchivos2` recorre una ruta y sus subdirectorios. Cada archivo muestra
su ruta relativa, fecha de modificación, propietario y tamaño. Los directorios
se muestran solo con su ruta relativa; la sangría ayuda a reconocer los niveles.

- Omite archivos y carpetas ocultos, y no muestra ni sigue enlaces simbólicos.
- Aplica el máximo **después** de excluir los elementos no visibles; los directorios
  mostrados también cuentan para el máximo.
- La carpeta inicial está a profundidad `0` y no cuenta como resultado.
- No se ordenan los resultados.

### Argumentos

```text
ListadoArchivos2 [ruta] [máximoResultados] [profundidad]
```

Los argumentos son posicionales: para especificar un máximo o una profundidad,
también hay que proporcionar la ruta. Si la ruta contiene espacios, escríbela
entre comillas.

| Argumento | Si se omite | Valores válidos |
|---|---|---|
| `ruta` | Carpeta personal del usuario | Ruta existente a un archivo o directorio |
| `máximoResultados` | Sin límite práctico | Entero mayor que `0` |
| `profundidad` | Sin límite práctico | Entero mayor o igual que `0` |

### Cómo se interpreta la profundidad

| Profundidad máxima | Elementos que se pueden mostrar |
|---:|---|
| `0` | Ninguno |
| `1` | Contenido directo de la ruta |
| `2` | Contenido directo y contenido de sus subdirectorios |
| `3` | También el contenido de los subdirectorios de segundo nivel |

La profundidad y el máximo se aplican a la vez. Por ejemplo, con profundidad `3`
y máximo `2`, se muestran como mucho dos elementos visibles dentro de esos niveles.

### Ejemplos

```powershell
# Recorrer la carpeta personal completa
java -cp . ListadoArchivos2

# Recorrer una carpeta hasta profundidad 2, con un máximo de 20 resultados
java -cp . ListadoArchivos2 "C:\Users\Lucia\Documents" 20 2

# Profundidad 1: solo los elementos directos de la carpeta personal
java -cp . ListadoArchivos2 "$env:USERPROFILE" 50 1
```

Los valores inválidos terminan el programa de forma controlada y muestran un
mensaje explicativo.

## 🧾 Tamaños y fechas

El primer ejercicio presenta tamaños en `B`, `KB`, `MB`, `GB` o `TB`, con
divisiones entre 1024. El segundo utiliza las etiquetas binarias `KiB`, `MiB`,
`GiB` y siguientes, también con base 1024 y dos decimales. Por ejemplo,
`1536` bytes se muestran como `1,50 KiB` en el listado recursivo.

Las fechas se obtienen del sistema de archivos. En el segundo ejercicio se
presentan como `dd/MM/yyyy HH:mm`, utilizando la zona horaria local del equipo.

## 🛡️ Un recorrido de solo lectura

Los programas consultan nombres y atributos; no borran, mueven ni modifican
archivos. Un elemento oculto se omite tanto en la salida como en la recursividad.
Un enlace simbólico se excluye para evitar recorrer su destino. Un acceso directo
`.lnk` de Windows no es necesariamente un enlace simbólico.

## 🧠 Conceptos practicados

`Path` y `Files` · `System.getProperty("user.home")` · `Files.list` · atributos
del sistema de archivos · `Stream` · manejo de excepciones · recursividad ·
validación de argumentos · conversión de bytes a unidades legibles.

---

<div align="center">

Hecho por **Francisco Niño Caballero** · Módulo de **Acceso a Datos**

</div>
