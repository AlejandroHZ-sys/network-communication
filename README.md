# Network Programming in Java (TCP & UDP)

Este repositorio contiene una serie de prácticas de **programación en red en Java**, enfocadas en la implementación directa de **protocolos TCP y UDP** usando sockets, sin frameworks.

El objetivo principal es comprender y aplicar los conceptos fundamentales de comunicación en red a nivel aplicación.

---

## Contenido

Las prácticas están organizadas por protocolo:

### TCP

- **Multi-room chat server (destacado)**  
  Servidor de chat concurrente con:
  - Múltiples clientes simultáneos
  - Salas independientes
  - Mensajes públicos y privados
  - Arquitectura basada en hilos

- **HTTP server**  
  Servidor HTTP:
  - Manejo manual de cabeceras
  - Soporte para GET, POST, PUT y DELETE
  - Envío y recepción de archivos
  - Pool de hilos para concurrencia

- **File transfer system (FTP-style)**  
  Sistema de transferencia de archivos:
  - Canal de control y canal de datos
  - Comandos tipo FTP (STOR, RETR, MKD, DELE, etc.)
  - Implementación con Java NIO y Selector
  - Transferencia de archivos y directorios

---

### UDP

- **Reliable file transfer over UDP**  
  Transferencia confiable sobre UDP utilizando:
  - Ventana deslizante
  - ACKs
  - Reenvío por timeout
  - Control de flujo

- **UDP flow control practice**  
  Práctica enfocada en el manejo de pérdida de paquetes y control de flujo
  sobre un protocolo no confiable.


## Tecnologías y conceptos

- Java Sockets (TCP / UDP)
- Java NIO
- Concurrencia y sincronización
- Protocolos de aplicación
- Transferencia de archivos
- Serialización de datos


---

## Autor

Alejandro HZ    
Ingeniería en Sistemas Computacionales
