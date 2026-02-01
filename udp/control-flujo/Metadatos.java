import java.io.Serializable;

class Metadatos implements Serializable{
    int total_paquetes;
    int num_paquete;
    int tam_cadena;
    byte[] datos;
    String nombreArchivo;
    public Metadatos(int tp,int np,int tmp,byte[] d,String nombreFile){
        this.total_paquetes=tp;
        this.num_paquete=np;
        this.tam_cadena=tmp;
        this.datos=d;   
        this.nombreArchivo = nombreFile;
    }
}