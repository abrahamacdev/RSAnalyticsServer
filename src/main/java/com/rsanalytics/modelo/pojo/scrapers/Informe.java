package com.rsanalytics.modelo.pojo.scrapers;

import com.rsanalytics.modelo.pojo.rest.Usuario;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "informe")
public class Informe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipoContrato_id")
    private TipoContrato tipoContrato;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "fecha_realizacion")
    private long fechaRealizacion;

    @Column(name = "fecha_creacion_solicitud")
    private long fechaCreacionSolicitud;

    @Column(name = "ruta_archivo")
    private String rutaArchivo;

    public Informe(){}

    public Informe(Usuario usuario, TipoContrato tipoContrato, String nombre, long fechaCreacionSolicitud) {
        this.usuario = usuario;
        this.tipoContrato = tipoContrato;
        this.nombre = nombre;
        this.fechaCreacionSolicitud = fechaCreacionSolicitud;
    }

    public Informe(Usuario usuario, TipoContrato tipoContrato, String nombre, long fechaCreacionSolicitud, long fechaRealizacion, String rutaArchivo) {
        this.usuario = usuario;
        this.tipoContrato = tipoContrato;
        this.nombre = nombre;
        this.fechaCreacionSolicitud = fechaCreacionSolicitud;
        this.fechaRealizacion = fechaRealizacion;
        this.rutaArchivo = rutaArchivo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Informe informe = (Informe) o;
        return Objects.equals(id, informe.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public long getFechaRealizacion() {
        return fechaRealizacion;
    }

    public void setFechaRealizacion(long fechaRealizacion) {
        this.fechaRealizacion = fechaRealizacion;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public TipoContrato getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(TipoContrato tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    public long getFechaCreacionSolicitud() {
        return fechaCreacionSolicitud;
    }

    public void setFechaCreacionSolicitud(long fechaCreacionSolicitud) {
        this.fechaCreacionSolicitud = fechaCreacionSolicitud;
    }
}
