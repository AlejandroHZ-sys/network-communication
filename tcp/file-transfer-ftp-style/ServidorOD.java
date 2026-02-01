/**********************PAQUETES**********************/

import java.io.*;
import java.net.*;
import java.net.ServerSocket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/****************************************************/



public class ServidorOD {
    
    /************************************************************************************************************************************************************************/
    /******************************************************************INICIO DEL MAIN***************************************************************************************/

    /**
     * @param args**********************************************************************************************************************************************************************/

    public static void main(String[] args) {
        try {
            
            /************************************************Objetos para archivos*******************************************************/
            
            File archivoRenombrar = null;                                       // Variable de tipo File para almacenar el nombre del archivo a renombrar, indicado en RNFR  
            //String rutaCarpeta = "C:\\Users\\aleja\\Desktop\\ServidorOD";       
            //File carpeta = new File(rutaCarpeta);                        //Crear el objeto de la carpeta del servidor   //comentar cuando se especifica la  carpeta

            String rutaCarpeta;
            if (args.length > 0) {
                rutaCarpeta = args[0];
            } else {
                rutaCarpeta = "ServidorOD"; // carpeta por defecto    //Ruta de la carpeta local de la carpeta remota
            }

            File carpeta = new File(rutaCarpeta);

            if (!carpeta.exists() || !carpeta.isDirectory()) {
                System.out.println("Error: la carpeta del servidor no existe.");
                return;
            }

            /****************************************************************************************************************************/
            
            /********************************************Crear socket de control********************************************************/
            
            int ptoControl = 2000, ptoDatos = 3000;                             //Variables para puerto de los sockets de control y de datos
            ServerSocket sControl = new ServerSocket(ptoControl);           //Crear el Socket de control
            ServerSocket sDatos = new ServerSocket(ptoDatos);
            sControl.setReuseAddress(true);
            System.out.println("Servidor iniciado");
            
            /****************************************************************************************************************************/
            
            
            /*******************************************************Conexiones***********************************************************/
            /****************************************************************************************************************************/
            
            for (;;) {
                
                Socket clControl = sControl.accept();                           //Descriptor del socket de control
                System.out.println("Cliente conectado desde " + clControl.getInetAddress() + ": " + clControl.getPort());    
                
                /********************************************Flujos de lectura y escritura***************************************************/
                
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(clControl.getOutputStream(), "ISO-8859-1"));
                BufferedReader br = new BufferedReader(new InputStreamReader(clControl.getInputStream(), "ISO-8859-1")); //LEER DESDE EL SOCKET_c
                //ServerSocket sDatos = new ServerSocket(ptoDatos);
                
                /****************************************************************************************************************************/
                
               
                 //rutaCarpeta = br.readLine();                                   //Ruta de la carpeta remota que especifica el cliente
                 //File carpeta = new File(rutaCarpeta);                   //Crear el objeto de la carpeta del servidor
                 
                while (true) {          
                    
                    /***********************************************Identificar comando******************************************************/
                    
                    String comando = br.readLine();                             //Leer el comando que manda el cliente
                    String[] partes = comando.split(" ");
                    if (partes.length > 2) {
                        System.out.println("Argumento debe ir sin espacios, solo se toma la primer palabra despues de la primitiva");
                    }
                    String primitiva = partes.length > 0 ? partes[0] : "";        //Primitiva  
                    String argumento = partes.length > 1 ? partes[1] : "";      //Argumento  (ls no lleva argumento)
                    
                    /****************************************************************************************************************************/
                    Socket clDatos=null;
//                    if(primitiva.equals("ls") || primitiva.equals("STOR") || primitiva.equals("RETR")){
//                        clDatos = sDatos.accept();
//                    }
                    
                    switch (primitiva) {
                        
                        case "ls":                                              //ListarDirectorios Archivos
    
                           clDatos = sDatos.accept();
                            System.out.println("Abriendo socket de datos");
                            PrintWriter pwDatos = new PrintWriter(new OutputStreamWriter(clDatos.getOutputStream(), "ISO-8859-1"));
                            if (argumento.isEmpty()) {
                                ls(carpeta, 0, pwDatos);
                            } else {
                                System.out.println("Error en primitiva ls");
                                //pw.println("Error en la primitiva 'ls'");
                                //pw.flush();
                            }
                            pwDatos.close();                                    //Cierra el flujo de datos
                            clDatos.close();                                    //Cierra la conexión de datos
                            
                        break;
                        
                        case "CWD":                                             //Cambiar de ruta
                            
                            if(!argumento.isEmpty()){
                                System.out.println("argumento antes del if: " +argumento);
                                if (argumento.equals("/")) {
                                    rutaCarpeta = new File(".").getCanonicalPath();
                                    carpeta = new File(rutaCarpeta);
                                    pw.println("Estamos en el directorio raiz");
                                    pw.flush();
                                }
                                else{
                                    //argumento = argumento.replace("\\", "\\\\");
                                    System.out.println(argumento);
                                    rutaCarpeta=rutaCarpeta+File.separator+argumento;
                                    carpeta = new File(rutaCarpeta);
                                    pw.println("Se encuentra en la ruta:" + rutaCarpeta);
                                    pw.flush();
                                }
                            }
                            
                        break;
                        
                        case "MKD":                                             //Crear directorio
                            
                            if (!argumento.isEmpty()) {
                                MKD(rutaCarpeta, argumento);
                                pw.println("Directorio creado: " + argumento);
                                pw.flush();
                            } else {
                                pw.println("Es necesario especificar el nombre del directorio.");
                                pw.flush();
                            }
                            
                        break;
                        
                        case "RMD":                                             //Eliminar directorio
                            
                            if (!argumento.isEmpty()) {
                                String rutaRmd = rutaCarpeta + File.separator + argumento;
                                File rmd = new File(rutaRmd);
                                if (RMD(rmd)) {
                                    pw.println("Directorio eliminado: " + argumento);
                                    pw.flush();
                                } else {
                                    pw.println("Es necesario especificar el nombre conrrecto del directorio.");
                                    pw.flush();
                                }
                            }
                            
                        break;
                        
                        /*********************************Crear archivo***********************************/
                    case "FILE":
                      
                            if (!argumento.isEmpty()) {
                                String rutaCarpetaS =rutaCarpeta + File.separator + argumento;
                                File archivo = new File(rutaCarpetaS);
                                if (archivo.createNewFile()) {
                                    pw.println("Archivo creado: " + archivo.getName());
                                    pw.flush();
                                } else {
                                    pw.println("El archivo ya existe.");
                                    pw.flush();
                                }
                            } else {
                                pw.println("Es necesario especificar el nombre del archivo");
                                pw.flush();
                            }
                        
                    break;
                    /***************************************************************************/

                        case "DELE":                                            //Eliminar archivo
                            
                            if (!argumento.isEmpty()) {
                                String rutaDele = rutaCarpeta + File.separator + argumento;
                                File dele = new File(rutaDele);
                                if (dele.exists()) {
                                    DELE(dele);
                                    pw.println("Archivo eliminado correctamente");
                                    pw.flush();
                                } else {
                                    pw.println("El archivo que se intenta eliminar no existe");
                                    pw.flush();
                                }

                            } else {
                                pw.println("Es necesario especificar el nombre del archivo.");
                                pw.flush();
                            }
                            
                        break;
                        
                        case "RNFR":                                            //Comando para indicar el archivo/carpeta a renombrar
                            
                            if (!argumento.isEmpty()) {
                                archivoRenombrar = new File(carpeta, argumento);
                                if (archivoRenombrar.exists()) {
                                    pw.println("> ");
                                    pw.flush();
                                } else {
                                    pw.println("El archivo no existe");
                                    pw.flush();
                                    archivoRenombrar = null;
                                }
                            } else {
                                pw.println("Se requiere especificar el archivo o carpeta");
                                pw.flush();
                            }
                            
                        break;
                        
                        case "RNTO":                                            //Comando para renombrar el archivo/carpeta que se indico con RNFR
                            
                            if (archivoRenombrar != null && !argumento.isEmpty()) {
                                File nuevoArchivo = new File(carpeta, argumento);
                                if (archivoRenombrar.renameTo(nuevoArchivo)) {
                                    pw.println("Archivo renombrado exitosamente");
                                    pw.flush();
                                } else {
                                    pw.println("Error al renombrar archivo");
                                    pw.flush();
                                }
                            } else {
                                pw.println("RNFR no especificado o argumento inválido");
                                pw.flush();
                            }
                            archivoRenombrar = null;                            // Reiniciar la variable
                            
                        break;

                        case "STOR":                                            //Subir archivos/directorios al servidor
                            
                            if (!argumento.isEmpty()) {
                                clDatos = sDatos.accept();
                                File f = new File(rutaCarpeta);
                                String ruta_archivos = f.getAbsolutePath();

                                ruta_archivos = ruta_archivos + File.separator;
                                System.out.println("ruta:" + ruta_archivos);
                                f.setWritable(true);
                                
                                DataInputStream dis = new DataInputStream(clDatos.getInputStream());
                                String nombre = dis.readUTF();
                                long tam = dis.readLong();
                                DataOutputStream dos = new DataOutputStream(new FileOutputStream(ruta_archivos + nombre));
                                long recibidos = 0;
                                int l = 0;
                                while (recibidos < tam) {
                                    byte[] b = new byte[3500];
                                    l = dis.read(b);
                                    System.out.println("leidos: " + l);
                                    dos.write(b, 0, l); //dos.write(b);
                                    dos.flush();
                                    recibidos = recibidos + l;
                                }
                                dos.close();
                                dis.close();
                                //clDatos.close();

                                if (nombre.endsWith(".zip")) {
                                File archivoZip = new File(ruta_archivos + nombre);
                                String destino = ruta_archivos ; // Carpeta destino para descomprimir
                                File carpetaDescomprimida = DescomprimirZIP(archivoZip, destino);

                                if (carpetaDescomprimida != null) {
                                    if (!archivoZip.delete()) {
                                        //System.out.print("Archivo ZIP eliminado correctamente.");
                                    //} else {
                                        System.out.println("Error al eliminar el archivo ZIP.");
                                    }
                                    //System.out.println("Archivo descomprimido correctamente en: " + carpetaDescomprimida.getAbsolutePath());
                                } else {
                                    System.out.println("Hubo un error al descomprimir el archivo.");
                                }
                            }
                            clDatos.close();                                  // Cierra la conexión de datos 
                            } else {
                                System.out.println("Es necesario especificar el nombre del directorio.");
                            }
                            
                        break;
                        
                        case "RETR":

                            if (!argumento.isEmpty()) {
                                clDatos = sDatos.accept();
                                String rutaF = rutaCarpeta + File.separator + argumento;
                                File fr = new File(rutaF);

                                if (fr.isDirectory()) {                         // Comprimir la carpeta antes de enviarla
                                    String carpetaAComprimir = rutaCarpeta + File.separator + argumento;
                                    File archivoComprimido = ComprimirZIP(carpetaAComprimir);

                                    if (archivoComprimido != null) {
                                        fr = archivoComprimido;  // Usar el archivo comprimido para el envío
                                    } else {
                                        System.out.println("Error al comprimir la carpeta.");
                                        return;
                                    }
                                }

                                String nombre = fr.getName();
                                String path = fr.getAbsolutePath();
                                long tam = fr.length();

                                System.out.println("Preparandose pare enviar archivo " + path + " de " + tam + " bytes\n\n");
                                DataOutputStream dos = new DataOutputStream(clDatos.getOutputStream());
                                DataInputStream dis = new DataInputStream(new FileInputStream(path));
                                dos.writeUTF(nombre);
                                dos.flush();
                                dos.writeLong(tam);
                                dos.flush();
                                long enviados = 0;
                                int l = 0, porcentaje = 0;
                                
                                while (enviados < tam) {
                                    byte[] b = new byte[3500];
                                    l = dis.read(b);
                                    System.out.println("enviados: " + l);
                                    dos.write(b, 0, l);// dos.write(b);
                                    dos.flush();
                                    enviados = enviados + l;
                                    porcentaje = (int) ((enviados * 100) / tam);
                                    System.out.print("\rEnviado el " + porcentaje + " % del archivo");
                                }
                                
                                System.out.println("\nArchivo enviado al cliente..");
                                dis.close();
                                dos.close();
                                clDatos.close();                                  // Cierra la conexión de datos
                            } else {
                                System.out.println("Debe especificar un archivo o carpeta.");
                            }
                            
                        break;
                        
                        case "QUIT":
                            
                            pw.println("Desconectando...");
                            pw.flush();
                            br.close();
                            pw.close();
                            clControl.close();
                            sDatos.close();
                            sControl.close();
                            System.out.println("Cliente desconectado.");
                            return;
                            
                        default:
                            System.out.println("Comando no valido");
                            break;
                    }
                }
            }
            
            /*****************************************************Fin conexiones*********************************************************/
            /****************************************************************************************************************************/
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    
    /************************************************************************************************************************************************************************/
    /******************************************************************FIN DEL MAIN******************************************************************************************/
    /************************************************************************************************************************************************************************/

    
    
    /********************************************************************METODOS*********************************************************************************************/
    
    
    /*******************Listar directorios/Archivos******************************/
    public static void ls(File dir, int nivel, PrintWriter pwDatos) {
        String lineaArchivo;
        if (dir.isDirectory()) {
            File[] archivos = dir.listFiles();
            if (archivos == null) {
                pwDatos.println("Error: No se pudo acceder al directorio.");
                pwDatos.flush();
                return;
            }
            for (File archivo : archivos) {
                lineaArchivo = "";
                for (int i = 0; i < nivel; i++) {
                    lineaArchivo += "  ";
                }
                lineaArchivo += "|--" + archivo.getName() + (archivo.isDirectory() ? "/" : "");
                pwDatos.println(lineaArchivo);
                pwDatos.flush();
                if (archivo.isDirectory()) {
                    ls(archivo, nivel + 1, pwDatos);
                }
            }
        }
        //pwDatos.close();
    }

    /*****************************************************************************/
   
    
    /*********************Eliminar directorios************************************/
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
    }  //NOTA: falta poner else y no borrar cuando es archivo

    /*****************************************************************************/
    
    /*************************Eliminar archivos***********************************/
    public static void DELE(File dele) {
        boolean borrar = dele.delete();
        if (borrar) {
            System.out.println("archivo borrado exitosamente");
        } else {
            System.out.println("Error al borrar el archivo");
        }
    }
    
    /*****************************************************************************/
    
    
   /***************************Crear directorio***********************************/
    public static void MKD(String rutaCarpeta, String nuevaCarpeta) {
        rutaCarpeta += File.separator + nuevaCarpeta;
        File nuevoDir = new File(rutaCarpeta);

        if (nuevoDir.mkdir()) {
            System.out.println("El directorio " + nuevaCarpeta + " fue creado con éxito.");
        } else {
            System.out.println("No se pudo crear el directorio.");
        }
    }
    
    /*****************************************************************************/

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
            return archivoZip;  // Retorna el archivo ZIP creado

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
            //System.out.println("Descompresión completa en: " + carpetaDestino.getAbsolutePath());
            return carpetaDestino;  // Retorna la carpeta descomprimida

        } catch (IOException e) {
            System.out.println("Error al descomprimir -> " + e.getMessage());
            return null;
        }
    }
}
