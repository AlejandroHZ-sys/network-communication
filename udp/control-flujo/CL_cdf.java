import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;


public class CL_cdf{
    public static void main(String[] args) {
        try {
            int pto = 1234;
            String dir = "127.0.0.1";
            InetAddress dst = InetAddress.getByName(dir);
            int tam = 1000;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            DatagramSocket cl = new DatagramSocket();
            cl.setSoTimeout(2000); // Timeout de 2 segundos para ACKs
            boolean recibe=false;
        
            final int TAM_VENTANA = 4;
            Map<Integer, byte[]> ventana = new HashMap<>();   //Para guardar una copia del paquete, en caso de que el ack no llegue
            Set<Integer> ackRecibidos = new HashSet<>();      //Identificar que paquetes ya recibieron el ack
            System.out.println("TAMAÑO DEL PAQUEE: "+ tam/1000+"Kb");
            
            while (true) {
                System.out.println("Ingresa el la ruta del archivo, <Enter> para enviar, \"salir\" para terminar");
                String msj = br.readLine();

                if (msj.equalsIgnoreCase("salir")) {
                    System.out.println("Terminando programa");
                    br.close();
                    cl.close();
                    System.exit(0);
                }
                
                String rutaF = msj;
                File f = new File(rutaF);
                
                String nombre = f.getName();
                System.out.println(nombre);
                //String path = f.getAbsolutePath();
       
                ackRecibidos.clear();
                ventana.clear();
                byte[] b = Files.readAllBytes(f.toPath());
                 
                int paquetesTotales = (int) Math.ceil((double) b.length / tam);
                System.out.println("PAQUETES TOTALES QUE SE ENVIAN "+paquetesTotales);
                //byte[] b = msj.getBytes();                
                //int paquetesTotales = (int) Math.ceil((double) b.length / tam);
                double tamaFE=(double)b.length/1000;
                System.out.println("TAMANO DEL ARCHIVO: "+ tamaFE+"Kb");
                int base = 0;
                int nextP=0;
/************************************************************************* MANEJO DE RECHAZO SELECTIVO *************************************************************************/           
                while(base<paquetesTotales){
                    
                    while (nextP < base + TAM_VENTANA && nextP < paquetesTotales){

                        /*******************************************************************************************************************************/
                                      
                        byte[] tmp = Arrays.copyOfRange(b, nextP * tam, Math.min((nextP + 1) * tam, b.length));   //evitar calcular el sobrante
                        Metadatos mt = new Metadatos(paquetesTotales, nextP, tmp.length, tmp,nombre);                     //Objeto que incluye los metadatos

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();        //Flujo de datos
                        ObjectOutputStream oos = new ObjectOutputStream(baos);        //Flujo de objeto
                        

                        oos.writeObject(mt);               //Escribe en el flujo de salida para enviarlo a través del socket
                        oos.flush();                          //No esperar a que se llene el buffer               

                        byte[] data = baos.toByteArray();    //Contenido de los datos serializados
                        baos.reset();                        //Resetear el flujo de bytes
                        oos.close();                         //Cerrar el flujo de objetos
                        
                        Random r=new Random();
                        int var= r.nextInt(100)%3;
                        System.out.println(var);
                        /*******************************************************************************************************************************/

                        /*******************************************************************************************************************************/
                        ventana.put(nextP, data);
                        //nextP++; 
                        if(var==0){
                            
                            //ventana.put(nextP, data);
                             
                            System.out.println("numPaquete que no se envia "+ nextP);
                            //nextP++;
                           // continue;
                        }else{
                        DatagramPacket p = new DatagramPacket(data, data.length, dst, pto);     //Crear el paquete a enviar
                        
                        cl.send(p);                                                                               //enviar el paquete
                        System.out.println("Enviando fragmento " + nextP+" de tamano: "+(double)tmp.length/1000+"Kb");
                        //nextP++; 
                        }
                        
                        //ventana.put(nextP, data);    //Guardar una copia del paquete que se mando; con su numero de paquete y el contenido
                        nextP++;                            //avanzar en el siguiente paquete a enviar
                        
                        
                        /*******************************************************************************************************************************/  
                    }//Termina el while, mandando el numero de paquetes indicados en "TAM_VENTANA" 
                        
                    
                    /*************************************COMPROBAR SI RECIBIO LOS PAQUETES ENVIADOS***********************************************/
                    try {
                        while(true){
                            /******************************************************************************************************/
                            byte[] ackBuffer = new byte[10];
                            DatagramPacket ackP = new DatagramPacket(ackBuffer, ackBuffer.length);
                            cl.receive(ackP);     //Si se excede el timepo que puso como limite en el Timeout, arrogara la excepcion
                            String ack = new String(ackP.getData(), 0, ackP.getLength());   //Almacenar el ack en un string. Recibira ACK(i)
                            /******************************************************************************************************/


                            if (ack.startsWith("ACK")) {       //  ack.equals("ACK"+nextP)
                                int ackNum = Integer.parseInt(ack.substring(3));   //Obtener el numero de ack que se recibio
                                System.out.println("ACK recibido para el paquete #" + ackNum);
                                ackRecibidos.add(ackNum);                                  //agregarlo elnumero de ack recibido al conjunto 
                                ventana.remove(ackNum);                                   //si ya se recibio el ack(i), se quita del arreglo que almacena una copia

                                while (ackRecibidos.contains(base)) {             //Verificar que paquetes regresaron el ack de recibido
                                    base++;                                         //Ir recorriendo la ventana de acuerdo a los paquetes que se recibieron
                                }

                            }
                        }
                    } catch (SocketTimeoutException e) {
//                        recibe=true;
                     // while(recibe){  
                        for (int i = base; i < nextP; i++) {
                            if (!ackRecibidos.contains(i)) {
                                //byte[] dataReenvio = ventana.get(i);         //Obtener el paquete que no se envio, del conjunto de copias
                                byte[] dataReenvio = ventana.get(i);
                                DatagramPacket pReenvio = new DatagramPacket(dataReenvio, dataReenvio.length, dst, pto);
                                System.out.println("Timeout: reenviando paquete "+ i+" de tamano: "+(double)dataReenvio.length/1000+ "Kb");
                                cl.send(pReenvio);   
                                /*********************************************/
                              /*  byte[] ackBuffer1 = new byte[10];
                                DatagramPacket ackP1 = new DatagramPacket(ackBuffer1, ackBuffer1.length);
                                cl.receive(ackP1);

                                String ack1 = new String(ackP1.getData(), 0, ackP1.getLength());

                                if (ack1.startsWith("ACK")) {       //  ack.equals("ACK"+nextP)
                                    int ackNum = Integer.parseInt(ack1.substring(3));   //Obtener el numero de ack que se recibio
                                    System.out.println("ACK recibido para el paquete #" + ackNum);
                                    ackRecibidos.add(ackNum);                                  
                                    ventana.remove(ackNum);        ///checar si es encesario?                            
                                    recibe=true;
                                    while (ackRecibidos.contains(base)) {             
                                        base++;                                         
                                    }
//                                if (base >= nextP) {
//                                    recibe = false;
//                                    break;
//                                }
                                }*/
                                /*********************************************/
                            }
                        }
                       /* try{
                            while(true){
                           
                                byte[] ackBuffer1 = new byte[10];
                                DatagramPacket ackP1 = new DatagramPacket(ackBuffer1, ackBuffer1.length);
                                cl.receive(ackP1);

                                String ack1 = new String(ackP1.getData(), 0, ackP1.getLength());

                                if (ack1.startsWith("ACK")) {       //  ack.equals("ACK"+nextP)
                                    int ackNum = Integer.parseInt(ack1.substring(3));   //Obtener el numero de ack que se recibio
                                    System.out.println("ACK recibido para el paquete #" + ackNum);
                                    ackRecibidos.add(ackNum);                                  
                                    ventana.remove(ackNum);        ///checar si es encesario?                            
                                    recibe=true;
                                    while (ackRecibidos.contains(base)) {             
                                        base++;                                         
                                    }
//                                if (base >= nextP) {
//                                    recibe = false;
//                                    break;
//                                }
                                }
                            }
                        }catch(Exception o){
                            System.out.println("");
                        }*/
                      //}
                        
                    }    
                    /******************************************************************************************************************************/
                }  
/*******************************************************************************************************************************************************************************/                      
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




