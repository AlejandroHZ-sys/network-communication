import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Manejador implements Runnable {
    private final Socket socket;

    public Manejador(Socket socket) {
        this.socket = socket;
    }

    @Override
public void run() {
    try (
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        DataInputStream dis = new DataInputStream(input);
        BufferedOutputStream bos = new BufferedOutputStream(output)
    ) {
        byte[] buffer = new byte[65536];
        int bytesRead = dis.read(buffer);     //Lectura de la petición
        if (bytesRead == -1) {
            enviarRespuesta(bos, "400 Bad Request", "Solicitud vacía o inválida.");
            return;
        }
        
        

        String peticion = new String(buffer, 0, bytesRead); //Convertir solicitud de bytes a texto
        String[] lineas = peticion.split("\r?\n");    //Se identifica las partes de la URL
        
        System.out.println("===== CABECERA COMPLETA DE LA PETICIÓN =====");
        System.out.println(peticion);
        System.out.println("============================================");

        if (lineas.length == 0) {
            enviarRespuesta(bos, "400 Bad Request", "Solicitud malformada.");
            return;
        }

        StringTokenizer tokenizer = new StringTokenizer(lineas[0]);
        String metodo = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
        String ruta = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";

        // Limpiar ruta
        // Limpiar ruta y validar nombre de archivo
        int fin = ruta.indexOf("?");
        if (fin != -1) ruta = ruta.substring(0, fin);

        ruta = ruta.replaceAll("[<>:\"/\\\\|?*]", ""); // Quita caracteres prohibidos
        ruta = ruta.replaceAll("[\\r\\n]", "");        // Quita saltos de línea
        ruta = ruta.trim();                            // Elimina espacios al inicio/final
        ruta = URLDecoder.decode(ruta, "UTF-8");

if (ruta.isEmpty()) {
    ruta = "index.htm";
}

// Asegurar que no tiene espacios internos ni símbolos raros
if (!ruta.matches("^[a-zA-Z0-9_.-]+$")) {
    System.out.println("[ERROR] Ruta inválida: " + ruta);
    enviarRespuesta(bos, "400 Bad Request", "Nombre de archivo no permitido.");
    return;
}


        // Leer Content-Length
        int contentLength = 0;
        for (String linea : lineas) {
            if (linea.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(linea.split(":")[1].trim());
                break;
            }
        }

        // Extraer cuerpo
        StringBuilder body = new StringBuilder();
        boolean cuerpoEmpieza = false;
        for (String linea : lineas) {
            if (cuerpoEmpieza) body.append(linea).append("\n");
            if (linea.isEmpty()) cuerpoEmpieza = true;
        }
        if (body.length() > contentLength) body.setLength(contentLength);

        switch (metodo.toUpperCase()) {
            case "GET":
                manejarGET(bos, ruta);
                break;
            case "POST":
                manejarPOST(bos, ruta, body.toString());
                break;
            case "PUT":
                manejarPUT(bos, ruta, body.toString());
                break;
            case "DELETE":
                manejarDELETE(bos, ruta);
                break;
            default:
                enviarRespuesta(bos, "501 Not Implemented", "Método no soportado: " + metodo);
        }

    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


    private void manejarGET(BufferedOutputStream bos, String ruta) throws IOException {
        File archivo = new File(ruta);
        //System.out.println("Ruta buscada: " + archivo.getAbsolutePath());

        if (!archivo.exists() || archivo.isDirectory()) {
            enviarRespuesta(bos, "404 Not Found", "Archivo no encontrado.");
            return;
        }

        byte[] contenido = Files.readAllBytes(archivo.toPath());
        // Detectar tipo MIME según extensión
    String contentType = "text/plain";
    if (ruta.endsWith(".htm") || ruta.endsWith(".html")) {
        contentType = "text/html";
    }

    String cabecera = "HTTP/1.1 200 OK\r\n" +
                      "Content-Type: " + contentType + "\r\n" +
                      "Content-Length: " + contenido.length + "\r\n\r\n";

        bos.write(cabecera.getBytes());
        bos.write(contenido);
        bos.flush();
    }

   /* private void manejarPOST(BufferedOutputStream bos, String ruta, String body) throws IOException {
        //System.out.println("[POST] Intentando crear archivo: " + ruta);

        File archivo = new File(ruta);
        if (archivo.exists()) {
            System.out.println("[POST] El archivo ya existe. Abortando.");
            enviarRespuesta(bos, "409 Conflict", "El archivo ya existe.");
            return;
        }

        System.out.println("[POST] Escribiendo contenido en el archivo...");
        System.out.println("[POST] Contenido del body:\n" + body);

        Files.write(archivo.toPath(), body.getBytes());

        System.out.println("[POST] Archivo creado exitosamente.");
        enviarRespuesta(bos, "201 Created", "Archivo creado exitosamente.");
    }*/
    
    /*private void manejarPOST(BufferedOutputStream bos, String ruta, String body) throws IOException {
    File archivo = new File(ruta);

    if (!archivo.exists() || archivo.isDirectory()) {
        enviarRespuesta(bos, "404 Not Found", "Archivo no encontrado.");
        return;
    }

    byte[] contenido = Files.readAllBytes(archivo.toPath());

    // Detectar tipo MIME como en GET
    String contentType = "text/plain";
    if (ruta.endsWith(".htm") || ruta.endsWith(".html")) {
        contentType = "text/html";
    }

    String cabecera = "HTTP/1.1 200 OK\r\n" +
                      "Content-Type: " + contentType + "\r\n" +
                      "Content-Length: " + contenido.length + "\r\n\r\n";

    bos.write(cabecera.getBytes());
    bos.write(contenido);
    bos.flush();
}*/
    
    private void manejarPOST(BufferedOutputStream bos, String ruta, String body) throws IOException {
    // Mostrar la ruta y el contenido recibido
    System.out.println("===== MANEJO DE POST =====");
    System.out.println("Ruta solicitada: " + ruta);
    System.out.println("Contenido recibido (body):");
    System.out.println(body);
    System.out.println("===================================");

    // Responder al cliente que el POST fue recibido correctamente
    String respuesta = "Datos recibidos correctamente por POST.\n\nContenido enviado:\n" + body;
    String cabecera = "HTTP/1.1 200 OK\r\n" +
                      "Content-Type: text/plain\r\n" +
                      "Content-Length: " + respuesta.length() + "\r\n\r\n";

    bos.write(cabecera.getBytes());
    bos.write(respuesta.getBytes());
    bos.flush();
}



    private void manejarPUT(BufferedOutputStream bos, String ruta, String body) throws IOException {
        File archivo = new File(ruta);
        Files.write(archivo.toPath(), body.getBytes());
        enviarRespuesta(bos, "200 OK", "Archivo creado o sobrescrito.");
    }

    private void manejarDELETE(BufferedOutputStream bos, String ruta) throws IOException {
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            enviarRespuesta(bos, "404 Not Found", "Archivo no existe.");
            return;
        }

        if (archivo.delete()) {
            enviarRespuesta(bos, "200 OK", "Archivo eliminado exitosamente.");
        } else {
            enviarRespuesta(bos, "500 Internal Server Error", "No se pudo eliminar el archivo.");
        }
    }

    private void enviarRespuesta(BufferedOutputStream bos, String estado, String mensaje) throws IOException {
        String respuesta = "HTTP/1.1 " + estado + "\r\n" +
                           "Content-Type: text/plain\r\n" +
                           "Content-Length: " + mensaje.length() + "\r\n\r\n" +
                           mensaje;
        bos.write(respuesta.getBytes());
        bos.flush();
    }
}
