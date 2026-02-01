import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;


/**
 *
 * @author aleja
 */
public class ServidorWeb1 {

    public static final int PUERTO = 8000;
    private static final int NUM_HILOS = 10;
    private ServerSocket servidor;
    private ExecutorService pool;

    public ServidorWeb1() throws IOException {
        System.out.println("Iniciando Servidor HTTP en puerto " + PUERTO);
        servidor = new ServerSocket(PUERTO);
        pool = Executors.newFixedThreadPool(NUM_HILOS);

        while (true) {
            Socket cliente = servidor.accept();
            pool.execute(new Manejador(cliente)); // pool de hilos
        }
    }

    public static void main(String[] args) throws IOException {
        new ServidorWeb1();
    }
}
