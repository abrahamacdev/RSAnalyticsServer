package com.rsanalytics.modelo.pojo.rest;

import javax.persistence.*;
import java.sql.Date;
import java.time.ZoneId;
import java.util.Objects;

@Entity
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "emisor_id")
    private Usuario emisor;

    @ManyToOne
    @JoinColumn(name = "receptor_id", nullable = false)
    private Usuario receptor;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "accion_id")
    private Accion accion;

    @Column(name = "mensaje")
    private String mensaje;

    @Column(name = "fecha_envio", nullable = false)
    private Date fechaEnvio = new Date(System.currentTimeMillis());

    @Column(name = "leida")
    private boolean leida = false;

    public Notificacion(){}

    public Notificacion(Usuario emisor, Usuario receptor, Accion accion) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.accion = accion;
    }

    public Notificacion(Usuario emisor, Usuario receptor, Accion accion, String mensaje){
        this(emisor,receptor,accion,mensaje,new Date(System.currentTimeMillis()),false);
    }

    public Notificacion(Usuario emisor, Usuario receptor, Accion accion, String mensaje, Date fechaEnvio, boolean leida) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.accion = accion;
        this.mensaje = mensaje;
        this.fechaEnvio = fechaEnvio;
        this.leida = leida;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notificacion notificacion = (Notificacion) o;
        return Objects.equals(id, notificacion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Transient
    public long fechaEnvio2Millis(){
        return this.fechaEnvio.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getEmisor() {
        return emisor;
    }

    public void setEmisor(Usuario emisor) {
        this.emisor = emisor;
    }

    public Usuario getReceptor() {
        return receptor;
    }

    public void setReceptor(Usuario receptor) {
        this.receptor = receptor;
    }

    public Accion getAccion() {
        return accion;
    }

    public void setAccion(Accion accion) {
        this.accion = accion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Date getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Date fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }
}
