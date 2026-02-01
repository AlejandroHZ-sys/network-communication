/**********************PAQUETES**********************/

import java.net.*;   
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/****************************************************/



public class Cliente {
    
    /************************************************************************************************************************************************************************/
    /******************************************************************INICIO DEL MAIN***************************************************************************************/
    /************************************************************************************************************************************************************************/
    
    /**
     * @param args**********************************************************************************************************************************************************************/

    public static void main(String[] args) {
        try {
            /********************************************Crear socket de control********************************************************/
            
            int ptoControl = 2000,ptoDatos = 3000;                              //Variables para puerto de los sockets de control y de datos
            String dir = "127.0.0.1";                                           //Host
            Socket clControl = new Socket(dir, ptoControl);             //Crear el Socket de control
            System.out.println("Conexion con servidor establecida (socketControl)" + clControl.getPort());
      
            /****************************************************************************************************************************/
            
            
            /********************************************Flujos de lectura y escritura***************************************************/
            
            BufferedReader consola = new BufferedReader(new InputStreamReader(System.in));                                    //Instancia para leer comando de la consola del Cliente
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(clControl.getOutputStream(), "ISO-8859-1"));     //Instancia para escribir la primitiva por  el socket de control
            BufferedReader br = new BufferedReader(new InputStreamReader(clControl.getInputStream(), "ISO-8859-1")); //Instancia para leer la respuesta por el socket de control
            
            /****************************************************************************************************************************/
            
            
            /************************************************Objetos para archivos*******************************************************/
            
            File archivoRenombrarC = null;                                      // Variable de tipo File para almacenar el nombre del archivo a renombrar, indicado en RNFR    
            //Ruta de la carpeta local del Cliente
            String rutaCCliente;
            if (args.length > 0) {
                rutaCCliente = args[0];
            } else {
                rutaCCliente = "Cliente_local"; // carpeta por defecto
            }


            //String rutaCClienteRAIZ = "C:\\Users\\aleja\\Downloads\\Cliente_local";    //Descomentar cuando se especifica la carpeta
            //System.out.print("Especifique la ruta de la carpeta local: ");
            //String rutaCCliente=consola.readLine();                     //Para especificar una carpeta local
            //RutaCCliente=consola.readLine();
            //File carpetaC = new File(rutaCCliente); 
            
            File carpetaC = new File(rutaCCliente);                      //Crear el objeto de la carpeta del Cliente
            if (!carpetaC.exists() || !carpetaC.isDirectory()) {
                System.out.println("Error: la carpeta local no existe.");
                return;
            }
            
            //System.out.print("Especifique la ruta de la carpeta remota: ");
            //String rutaCarpetaRemota=consola.readLine();                     //Para especificar una carpeta local
            //pw.println(rutaCarpetaRemota);                  
            //pw.flush();  
            /****************************************************************************************************************************/
            
            String comando;  // Objeto String para almacenar el comando que ingresa el cliente
            
            
            /****************************************************************************************************************************/
            /*************************************************Lectura de comandos********************************************************/
            /****************************************************************************************************************************/

            while (true) {   // ciclo para leer comandos que ingrese el cliente
                
                /***********************************************Identificar comando******************************************************/
                
                System.out.print("> ");
                comando = consola.readLine();                //Leer comando ingresado


                String[] partes = comando.split(" ");                  //Separa la primitivas y el/los argumento/s
                String primitiva = partes.length > 0 ? partes[0] : "";      //primitiva que se manda por el socket de control
                String argumento = (partes.length > 1) ? partes[1] : "";    //argumento de la primitiva (en caso de ls, no recibe argumento)
                String argumentoC = (partes.length > 2) ? partes[2] : "";   //argumento para saber si el comando se ejecuta localmente o se manda por el socket de control
                System.out.println(argumentoC);

                if (!argumento.equals("-c")) {        // Verificar si es un comando local o para el servidor (-c -> para identificar que es un comando en la carpeta local)
                    pw.println(comando);                   // Mandar comando por el socket de control (comando= primitiva argumento)
                    pw.flush();                              
                }
                
                /****************************************************************************************************************************/
                //Socket clDatos = new Socket(dir, ptoDatos);
                
                switch (primitiva) {
                    case "ls": 
                        
                        if (argumento.equals("-c")) {
                            ls(carpetaC, 0);                                       //Metodo local, para mostrar listado de archivos de la carpeta del Cliente
                        } else {
                            try (Socket clDatos = new Socket(dir, ptoDatos)) {     // Establecer conexión de datos para recibir el listado de archivos
                             
                                BufferedReader brDatos = new BufferedReader(new InputStreamReader(clDatos.getInputStream(), "ISO-8859-1"));
                                System.out.println("Recibiendo listado de archivos...");
                                String linea;
                                while ((linea = brDatos.readLine()) != null) {
                                    System.out.println(linea);
                                }
                                
                                brDatos.close();    //Se cierra flujo de escritura de Datos
                                
                            }
                        }
                        
                    break;

                    case "MKD":
                        
                        if (argumento.equals("-c")) {
                            if (!argumentoC.isEmpty()) {
                                MKD(rutaCCliente, argumentoC);
                                System.out.println("Directorio creado: " + argumentoC);
                            } else {
                                System.out.println("Es necesario especificar el nombre del directorio Local");
                            }
                        } else {
                            System.out.println(br.readLine());                //Respuesta del servidor  por socket de control
                        }
                        
                    break;
                    
                    /*********************************Crear archivo***********************************/
                    case "FILE":
                        
                        if (argumento.equals("-c")) {
                            if (!argumentoC.isEmpty()) {
                                String rutaCarpeta =rutaCCliente + File.separator + argumentoC;
                                File archivo = new File(rutaCarpeta);
                                if (archivo.createNewFile()) {
                                    System.out.println("Archivo creado: " + archivo.getName());
                                } else {
                                    System.out.println("El archivo ya existe.");
                                }
                               // rutaCarpeta=null;
                            } else {
                                System.out.println("Es necesario especificar el nombre del archivo");
                            }
                        } else {
                            System.out.println(br.readLine());                //Respuesta del servidor  por socket de control
                        }
                        
                    break;
                    /***************************************************************************/
                    
                    case "RMD":
                        
                        if (argumento.equals("-c")) {
                            if (!argumentoC.isEmpty()) {
                                String rutaRmd = rutaCCliente + File.separator + argumentoC;

                                File rmd = new File(rutaRmd);
                                if (RMD(rmd)) {
                                    System.out.println("Directorio eliminado: " + argumentoC);
                                } else {
                                    System.out.println("Es necesario especificar el nombre correcto del directorio.");
                                }
                            }
                        } else {
                            System.out.println(br.readLine());                //Respuesta del servidor  por socket de control
                        }
                        
                    break;
                    
                    case "DELE":
                        
                        if (argumento.equals("-c")) {
                            if (!argumentoC.isEmpty()) {
                                String rutaDele = rutaCCliente + File.separator + argumentoC;
                                File dele = new File(rutaDele);
                                DELE(dele);
                            } else {
                                System.out.println("Es necesario especificar el nombre del archivo.");
                            }
                        } else {        
                            System.out.println(br.readLine());                //Respuesta del servidor  por socket de control
                        }
                        
                    break;
                        
                    case "RNTO":
                        if (argumento.equals("-c")) {
                            if (archivoRenombrarC != null && !argumentoC.isEmpty()) {
                                File nuevoArchivo = new File(carpetaC, argumentoC);
                                if (archivoRenombrarC.renameTo(nuevoArchivo)) {
                                    System.out.println("Archivo renombrado exitosamente");
                                } else {
                                    System.out.println("Error al renombrar archivo");
                                }
                            } else {
                                System.out.println("RNFR no especificado o argumento inválido");
                            }
                            archivoRenombrarC = null; // Reiniciar la variable
                        } else {
                            System.out.println(br.readLine());                //Respuesta del servidor  por socket de control
                        }
                        
                    break;
                    
                    case "CWD":
                        
                        if (argumento.equals("-c")) {
                            if (!argumentoC.isEmpty()) {
                                if (argumentoC.equals("/")) {
                                    rutaCCliente = new File(".").getCanonicalPath();  //comentar cuando se especifica la ruta
                                    carpetaC = new File(rutaCCliente);
                                    System.out.println("Estamos en el directorio raiz: "+ rutaCCliente);
                                } else {
                                    //argumento = argumento.replace("\\", "\\\\");
                                    //System.out.println(argumentoC);
                                    
//                                    String nuevaRuta = rutaCCliente + "\\" + argumentoC;
//                                    File nuevaCarpeta = new File(nuevaRuta);
//                                    if (nuevaCarpeta.exists() && nuevaCarpeta.isDirectory()) {
//                                        rutaCCliente = nuevaRuta;
//                                        carpetaC = nuevaCarpeta;
//                                        System.out.println("Se encuentra en la ruta: " + rutaCCliente);
//                                    } else {
//                                        System.out.println("Error: La ruta especificada no existe.");
//                                    }
                                    rutaCCliente = rutaCCliente + File.separator + argumentoC;
                                    carpetaC = new File(rutaCCliente);
                                    System.out.println("Se encuentra en la ruta:" + rutaCCliente);
                                }
                            }
                        } else {
                            System.out.println(br.readLine());                //Respuesta del servidor  por socket de control
                        }

                    break;

                    case "RNFR":
                        
                        if (argumento.equals("-c")) {
                            if (!argumentoC.isEmpty()) {
                                archivoRenombrarC = new File(carpetaC, argumentoC);
                                if (archivoRenombrarC.exists()) {
                                    System.out.println("Ingrese el nuevo nombre con RNTO");
                                } else {
                                    System.out.println("El archivo no existe");
                                    archivoRenombrarC = null;
                                }
                            } else {
                                System.out.println("Se requiere un nombre de archivo");
                            }
                        } else {
                            if (!argumento.isEmpty()) {
                                //System.out.println("Enviando RNFR...");
                                System.out.println(br.readLine());            //Respuesta del servidor  por socket de control
                            } else {
                                System.out.println("Debe especificar un archivo o carpeta.");
                            }
                        }

                    break;

                    case "STOR":                                                //Comando para subir un archivo al servidor

                        if (!argumento.isEmpty()) {
                            /*********************************Conexion del socket de Datos*******************************************/
                            
                            try (Socket clDatos = new Socket(dir, ptoDatos)) {  
                                
                                String rutaF = rutaCCliente + File.separator + argumento;
                                File f = new File(rutaF);
                                if (f.isDirectory()) {                                                       //Si es un directorio, se crea el .ZIP
                                    
                                    String carpetaAComprimir = rutaCCliente + File.separator + argumento;              // Comprimir la carpeta antes de enviarla
                                    File archivoComprimido = ComprimirZIP(carpetaAComprimir);

                                    if (archivoComprimido != null) {
                                        f = archivoComprimido;  // Usar el archivo comprimido para el envío
                                    } else {
                                        System.out.println("Error al comprimir la carpeta.");
                                        return;
                                    }
                                }
                                String nombre = f.getName();
                                String path = f.getAbsolutePath();
                                long tam = f.length();
                                //System.out.println("Preparandose pare enviar archivo " + path + " de " + tam + " bytes\n\n");
                                DataOutputStream dos = new DataOutputStream(clDatos.getOutputStream());
                                DataInputStream dis = new DataInputStream(new FileInputStream(path));
                                dos.writeUTF(nombre);
                                dos.flush();
                                dos.writeLong(tam);
                                dos.flush();
                                long enviados = 0;
                                int l = 0;
                                while (enviados < tam) {
                                    byte[] b = new byte[3500];
                                    l = dis.read(b);
                                    dos.write(b, 0, l);
                                    dos.flush();
                                    enviados = enviados + l;
                                }
                                System.out.println("\nArchivo enviado al servidor");
                                dis.close();
                                dos.close();
                            }
                            
                            /********************************************************************************************************/
                        } else {
                            System.out.println("Debe especificar un archivo o carpeta.");
                        }

                        break;
                    case "RETR":                                                //Comando para bajar un archivo del servidor
                        if (!argumento.isEmpty()) {
                            String ruta_archivos;
                            String nombre;
                            
                            /*********************************Conexion del socket de Datos*******************************************/
                            
                            try (Socket clDatos = new Socket(dir, ptoDatos)) {
                                File fr = new File(rutaCCliente);
                                ruta_archivos = fr.getAbsolutePath();
                                ruta_archivos = ruta_archivos + File.separator;
                                fr.setWritable(true);
                                DataInputStream dis = new DataInputStream(clDatos.getInputStream());
                                nombre = dis.readUTF();
                                long tam = dis.readLong();
                                DataOutputStream dos = new DataOutputStream(new FileOutputStream(ruta_archivos + nombre));
                                long recibidos = 0;
                                int l = 0;
                                while (recibidos < tam) {
                                    byte[] b = new byte[3500];
                                    l = dis.read(b);
                                    //System.out.println("leidos: " + l);
                                    dos.write(b, 0, l); //dos.write(b);
                                    dos.flush();
                                    recibidos = recibidos + l;
                                }
                                System.out.println("Archivo bajado del servidor..");
                                dos.close();
                                dis.close();
                            }
                            
                            /********************************************************************************************************/

                            if (nombre.endsWith(".zip")) {
                                //System.out.println("Archivo ZIP detectado. Descomprimiendo...");
                                File archivoZip = new File(ruta_archivos + nombre);
                                String destino = ruta_archivos; // Carpeta destino para descomprimir
                                File carpetaDescomprimida = DescomprimirZIP(archivoZip, destino);

                                if (carpetaDescomprimida != null) {
                                    if (!archivoZip.delete()) {
                                        // System.out.println("Archivo ZIP eliminado correctamente.");
                                        //} else {
                                        System.out.println("Error al eliminar el archivo ZIP.");
                                    }
                                    //System.out.println("Archivo descomprimido correctamente en: " + carpetaDescomprimida.getAbsolutePath());
                                } else {
                                    System.out.println("Hubo un error al descomprimir el archivo.");
                                }
                            }

                        } else {
                            System.out.println("Es necesario especificar el nombre del directorio.");
                        }

                        break;
                    case "QUIT":
                        System.out.println("Cerrando conexión...");
                        //clControl.close();
                        consola.close();
                        pw.close();
                        br.close();
                        return;
                    default:
                        System.out.println("Comando no reconocido.");
                        break;
                }
            }
            
            /****************************************************************************************************************************/
            /**********************************************Fin de lectura de comandos****************************************************/
            /****************************************************************************************************************************/

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    /************************************************************************************************************************************************************************/
    /******************************************************************FIN DEL MAIN******************************************************************************************/
    /************************************************************************************************************************************************************************/
    
    
    /********************************************************************METODOS*********************************************************************************************/
    

    public static void ls(File dir, int nivel) {
        String lineaArchivo;
        if (dir.isDirectory()) {
            File[] archivos = dir.listFiles();
            if (archivos == null) {
                System.out.println("Error: No se pudo acceder al directorio.");
                return;
            }
            for (File archivo : archivos) {
                lineaArchivo = "";
                for (int i = 0; i < nivel; i++) {
                    lineaArchivo += "  ";
                }
                lineaArchivo += "|--" + archivo.getName() + (archivo.isDirectory() ? "/" : "");           
                System.out.println(lineaArchivo);
                if (archivo.isDirectory()) {
                    ls(archivo, nivel + 1);
                }
            }
        }
    }

    public static void MKD(String rutaCarpeta, String nuevaCarpeta) {
        rutaCarpeta += File.separator + nuevaCarpeta;
        File nuevoDir = new File(rutaCarpeta);
        if (nuevoDir.mkdir()) {
            System.out.println("El directorio " + nuevaCarpeta + " fue creado con éxito.");
        } else {
            System.out.println("No se pudo crear el directorio.");
        }
    }
    

    public static boolean RMD(File rmd) {
        if (rmd.isDirectory()) {
            for (File archivo : rmd.listFiles()) {
                if (archivo.isDirectory()) {
                    RMD(archivo);
                } else {
                    archivo.delete();
                }
            }
        }
        return rmd.delete();
    }

    public static void DELE(File dele) {
        boolean borrar = dele.delete();
        if (borrar) {
            System.out.println("archivo borrado exitosamente");
        } else {
            System.out.println("Error al borrar el archivo");
        }
    }

    public static File ComprimirZIP(String carpetaOrigen) {
        File carpeta = new File(carpetaOrigen);
        if (!carpeta.exists() || !carpeta.isDirectory()) {
            System.out.println("Error: La carpeta no existe o no es un directorio.");
            return null;
        }

        String nombreZip = carpeta.getName() + ".zip";
        File archivoZip = new File(nombreZip);

        try (
                FileOutputStream fos = new FileOutputStream(archivoZip); ZipOutputStream zos = new ZipOutputStream(fos)) {
            comprimirCarpeta(carpeta, carpeta.getName(), zos);
            zos.close();
            //System.out.println("Carpeta comprimida con éxito en: " + archivoZip.getAbsolutePath());
            return archivoZip; 

        } catch (IOException e) {
            System.out.println("Error al comprimir -> " + e.getMessage());
            return null;
        }
    }

    private static void comprimirCarpeta(File carpeta, String nombreBase, ZipOutputStream zos) throws IOException {
        File[] archivos = carpeta.listFiles();
        if (archivos == null) {
            return;
        }
        for (File archivo : archivos) {
            String rutaZip = nombreBase + "/" + archivo.getName();

            if (archivo.isDirectory()) {
                comprimirCarpeta(archivo, rutaZip, zos);
            } else {
                try (FileInputStream fis = new FileInputStream(archivo)) {
                    ZipEntry zipEntry = new ZipEntry(rutaZip);
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int bytesLeidos;
                    while ((bytesLeidos = fis.read(buffer)) >= 0) {
                        zos.write(buffer, 0, bytesLeidos);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    public static File DescomprimirZIP(File archivoZip, String destino) {
        File carpetaDestino = new File(destino);
        if (!carpetaDestino.exists()) {
            carpetaDestino.mkdirs();  // Crea la carpeta si no existe
        }
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(archivoZip))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                File archivoNuevo = new File(carpetaDestino, zipEntry.getName());

                // Si es una carpeta, la crea
                if (zipEntry.isDirectory()) {
                    archivoNuevo.mkdirs();
                } else {
                    // Crea las carpetas padre si no existen
                    archivoNuevo.getParentFile().mkdirs();

                    // Escribe el archivo
                    try (FileOutputStream fos = new FileOutputStream(archivoNuevo)) {
                        byte[] buffer = new byte[1024];
                        int bytesLeidos;
                        while ((bytesLeidos = zis.read(buffer)) >= 0) {
                            fos.write(buffer, 0, bytesLeidos);
                        }
                    }
                }
                zis.closeEntry();
            }
            System.out.println("Descompresión completa en: " + carpetaDestino.getAbsolutePath());
            return carpetaDestino;  // Retorna la carpeta descomprimida

        } catch (IOException e) {
            System.out.println("Error al descomprimir -> " + e.getMessage());
            return null;
        }
    }
    
}
