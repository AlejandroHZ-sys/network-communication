
import java.io.*;
import java.net.*;
import java.util.*;


public class Servidor {

    
     private static Map<String, Sala> salas = new HashMap<>();
    public static void main(String args[]) {
        try {
            int ptControl = 2000;

            ServerSocket s = new ServerSocket(ptControl);
            s.setReuseAddress(true);
            System.out.println("--- Servidor iniciado ---");
            
            for (;;) {
                Socket clControl = s.accept();
                System.out.println("Cliente conectado desde " + clControl.getInetAddress() + ":" + clControl.getPort());
                
                Thread manejador = new Thread(new Manejador(clControl));
                manejador.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    static class Manejador implements Runnable {

        Socket cl;
        Sala salaActual;
        String nombreUsuario, consola;

        BufferedReader br;
        PrintWriter pw; 
        public Manejador(Socket cl) {
            this.cl = cl;
        }//constructor

        public void run() {
            try {

                br = new BufferedReader(new InputStreamReader(cl.getInputStream(), "UTF-8"));
                pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream(), "UTF-8"));

                pw.println("Bienvenido. Ingresa tu nombre de usuario:");pw.flush();
                nombreUsuario = br.readLine();

                pw.println("Hola " + nombreUsuario + ". Estos son los comandos para chatear:");            pw.flush();
                pw.println("---------- COMANDOS DEL CHAT ----------");            pw.flush();
                pw.println("LISTAR_SALAS");            pw.flush();
                pw.println("ENTRAR_SALA");            pw.flush();
                pw.println("CREAR_SALA");            pw.flush();
                pw.println("MENSAJE_PUBLICO");pw.flush();
                pw.println("MENSAJE_PRIVADO");pw.flush();
                pw.println("SALIR");pw.flush();

                while (true) {
                    consola = br.readLine();
                    //System.out.println(consola);

                    if (consola.startsWith("LISTAR_SALAS")) {
                        pw.println("TRABAJANDO EN ELLO....");pw.flush();
                        if (salas.isEmpty()) {
                            pw.println("No hay salas disponibles.");pw.flush();
                        } else {
                            pw.println("Salas disponibles:");pw.flush();
                            for (Map.Entry<String, Sala> entrada : salas.entrySet()) {
                                pw.println("- " + entrada.getKey());pw.flush();
                            }
                        }
                    } else if (consola.startsWith("ENTRAR_SALA")) {
                        
                        String[] partes = consola.split(" ");
                        if (partes.length < 2) {
                            pw.println("Uso: ENTRAR_SALA nombreSala");pw.flush();
                            continue;
                        }
                        String nombreSala = partes[1];
                        Sala sala = salas.get(nombreSala);                //quitar Servidor
                        if (sala != null) {
                            if (salaActual != null) {
                                salaActual.eliminarCliente(this);
                            }
                            sala.agregarCliente(this);
                            salaActual = sala;
                            pw.println("Has entrado a la sala " + nombreSala); pw.flush();
                            } else {
                                pw.println("La sala no existe.");pw.flush();
                            }

                    } else if (consola.startsWith("CREAR_SALA")) {

                        pw.println("TRABAJANDO EN ELLO....");pw.flush();
                        String[] partes = consola.split(" ");

                        if (partes.length < 2) {
                            pw.println("Uso: CREAR_SALA nombreSala");pw.flush();
                            continue;
                        }
                        String nombreSala = partes[1];
                        if (!salas.containsKey(nombreSala)) {                         //servidor
                            salas.put(nombreSala, new Sala(nombreSala));          //servidor
                            pw.println("Sala " + nombreSala + " creada.");pw.flush();
                        } else {
                            pw.println("La sala ya existe.");pw.flush();
                        }

                    } else if (consola.startsWith("MENSAJE_PUBLICO")) {
                        pw.println("TRABAJANDO EN ELLO....");pw.flush();
                        if (salaActual != null) {
                            String texto = consola.substring("MENSAJE_PUBLICO".length()).trim();
                            salaActual.broadcast(nombreUsuario + ": " + texto, this);
                        } else {
                            pw.println("No estás en ninguna sala.");pw.flush();
                        }


                    } else if (consola.startsWith("MENSAJE_PRIVADO")) {
                        pw.println("TRABAJANDO EN ELLO....");pw.flush();
                        if (salaActual != null) {
                            String[] partes = consola.split(" ", 3);
                            if (partes.length < 3) {
                                pw.println("Uso: MENSAJE_PRIVADO destinatario mensaje");pw.flush();
                                continue;
                            }
                            salaActual.enviarPrivado(partes[1], nombreUsuario + " [privado]: " + partes[2], this);
                        } else {
                            pw.println("No estás en ninguna sala.");pw.flush();
                        }

                    } else if(consola.equals("SALIR")){
                        pw.println("SALIENDO DEL CHAT");pw.flush();
                        if (salaActual != null) {
                            salaActual.eliminarCliente(this);
                        }
                        break;
                    }else if(consola.startsWith("LISTAR_CLIENTES")){
                        pw.println("USUARIOS DE LA SALA:");pw.flush();
                        salaActual.imprimirUsuarios(this);
                    }else{
                        pw.println("Comando no reconocido");pw.flush();
                    }
                }
                cl.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void enviar(String mensaje){
            pw.println(mensaje);pw.flush();
        }

        public String getNombre() {
            return nombreUsuario;
        }
    }

    static class Sala {

        String nombre;
        Set<Manejador> clientes = new HashSet<>();

        public Sala(String nombre) {
            this.nombre = nombre;
        }

        public synchronized void agregarCliente(Manejador c) {
            clientes.add(c);
            broadcast("[Servidor]: " + c.getNombre() + " se ha unido a la sala.", c);
        }
        
        public synchronized void imprimirUsuarios(Manejador solicitante) {
            for (Manejador m : clientes) {
                solicitante.enviar("- "+m.getNombre());
                    
                
            }
        }


        public synchronized void eliminarCliente(Manejador c) {
            clientes.remove(c);
            broadcast("[Servidor]: " + c.getNombre() + " ha salido de la sala.", c);
        }


        public synchronized void broadcast(String mensaje, Manejador remitente) {
            for (Manejador c : clientes) {
                if (c != remitente) {
                    c.enviar(mensaje);
                }
            }
        }

        public synchronized void enviarPrivado(String destinatario, String mensaje, Manejador remitente) {
            for (Manejador c : clientes) {
                if (c.getNombre().equals(destinatario)) {
                    c.enviar(mensaje);
                    remitente.enviar("[Tú -> " + destinatario + "]: " + mensaje);
                    return;
                }
            }
            remitente.enviar("Usuario " + destinatario + " no encontrado en la sala.");
        }
    }
}