
import java.net.*;
import java.io.*;
import java.util.*;

public class SE_cdf {

    public static void main(String[] args) {
        try {
            int pto = 1234;            int i = 0; 

            String msj = "",nombreA="";
            DatagramSocket s = new DatagramSocket(pto);
            s.setReuseAddress(true);
            System.out.println("Servidor iniciado... esperando datagramas..");
            
            Map<Integer, byte[]> bufferMensajes = new TreeMap<>();
            int totalPaquetesEsperados = -1;
            
            for (;;) {
                DatagramPacket p = new DatagramPacket(new byte[65535], 65535);
                s.receive(p);

                /*******************************************************************/
               
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(p.getData(), 0, p.getLength()));
                
                
                // Leer el objeto Metadatos
                Metadatos mtR = (Metadatos) ois.readObject();
                
                // Extraer los datos del objeto Metadatos
                //int totalp=mtR.total_paquetes;
                int n = mtR.num_paquete;  // Número de paquete
                int tam = mtR.tam_cadena;  // Tamaño de los datos
                byte[] datos = mtR.datos;  // Los datos del paquete
                
                if (totalPaquetesEsperados == -1) {
                    totalPaquetesEsperados = mtR.total_paquetes;
                }
                bufferMensajes.put(n, datos);  //poner los paquetes en orden

                // Crear el mensaje de la cadena desde los datos recibidos
                //String cadena = new String(datos);
                System.out.println("Paquete recibido con los datos: #paquete->" + n + " con " + tam);

                
                
                InetAddress clientAddress = p.getAddress();
                int clientPort = p.getPort();
                String ackMsg = "ACK"+n; // Mensaje de confirmación con el número de paquete
                byte[] ackData = ackMsg.getBytes();
                
               
                DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, clientAddress, clientPort);
                s.send(ackPacket); // Enviar ACK
               
               
                System.out.println("ACK enviado para el paquete #" + n);

                if (bufferMensajes.size() == totalPaquetesEsperados) {
                    System.out.println("\n--- MENSAJE COMPLETO RECIBIDO ---");
                    //StringBuilder mensajeFinal = new StringBuilder();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (byte[] parte : bufferMensajes.values()) {
                        //mensajeFinal.append(new String(parte));
                        baos.write(parte);
                    }
                    byte[] archivoCompleto = baos.toByteArray();
                    
                    nombreA = mtR.nombreArchivo;

                    String ruta="C:\\Users\\aleja\\Desktop\\";
                    String nombreArchivo = ruta+nombreA; 
                    System.out.println(nombreArchivo);
                    FileOutputStream fos = new FileOutputStream(nombreArchivo);
                    fos.write(archivoCompleto);
                    fos.close();
                    
                    System.out.println("Archivo recibido en: "+nombreArchivo);
                    System.out.println("----------------------------------\n");

                    // Limpiar para recibir otro archivo
                    bufferMensajes.clear();
                    totalPaquetesEsperados = -1;
                }
                
                ois.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }//catch

    }//main
}
