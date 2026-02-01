import java.net.*;
import java.io.*;
import javax.swing.*; // GUI
import java.awt.*;    // GUI
import java.awt.event.*; // GUI
//PR
public class Cliente {
    //  Componentes y variables para GUI
    private Socket cl;
    private PrintWriter pw;
    private BufferedReader br;
    private JTextArea areaMensajes;  // donde se mostrarán los mensajes
    private JTextField campoTexto;   // donde se escriben los mensajes

    public Cliente() {
        //  === INTERFAZ GRÁFICA ===
        JFrame frame = new JFrame("Cliente");

        areaMensajes = new JTextArea(15, 30);
        areaMensajes.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaMensajes);

        campoTexto = new JTextField(25);
        JButton botonEnviar = new JButton("Enviar");

        JPanel panelInferior = new JPanel();
        panelInferior.add(campoTexto);
        panelInferior.add(botonEnviar);

        frame.add(scroll, BorderLayout.CENTER);
        frame.add(panelInferior, BorderLayout.SOUTH);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        //  === FIN INTERFAZ ===

        try {
            cl = new Socket("127.0.0.1", 2000);
            areaMensajes.append("Conectado al servidor\n");

            pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream(), "UTF-8"), true);
            br = new BufferedReader(new InputStreamReader(cl.getInputStream(), "UTF-8"));

            // Enviar mensaje inicial
            //pw.println("Holá é íó pu");

            //  === HILO PARA ESCUCHAR MENSAJES DEL SERVIDOR ===
            Thread lector = new Thread(() -> { // 🔁 Aquí usé una lambda (equivale a Runnable)
                try {
                    String respuesta;
                    while ((respuesta = br.readLine()) != null) {
                        areaMensajes.append(respuesta + "\n"); // mostrar en GUI
                    }
                } catch (IOException e) {
                    areaMensajes.append("Conexión cerrada.\n");
                }
            });
            
            lector.start();
            //  === FIN HILO ===

            //  === EVENTO PARA ENVIAR MENSAJE DESDE LA GUI ===
            botonEnviar.addActionListener(e -> {
                String mensaje = campoTexto.getText();
                if (!mensaje.isEmpty()) {
                    pw.println(mensaje);
                    areaMensajes.append("Yo: " + mensaje + "\n");
                    campoTexto.setText("");

                    if (mensaje.equals("SALIR")) {
                        try {
                            cl.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });

            // Permitir enviar también con Enter
            campoTexto.addActionListener(e -> botonEnviar.doClick());
            //  === FIN EVENTO DE ENVÍO ===

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error al conectar con el servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Iniciar GUI en el hilo principal de Swing
        SwingUtilities.invokeLater(Cliente::new);
    }
}
