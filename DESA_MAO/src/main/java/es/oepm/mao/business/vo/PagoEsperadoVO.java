package es.oepm.mao.business.vo;

import java.util.Date;

import es.oepm.core.business.BaseVO;

public class PagoEsperadoVO extends BaseVO {
	
	private static final long serialVersionUID = -4194608527689791038L;
	
	private String tasaPagar;
	private int unidades;
	private boolean aplazamientoTasas;
	private boolean concedido;
	private String id;
	private String modalidad;
	private String solicitud;
	private String publicacion;
	private Date fechaInicioPago;
	private Date fechaFinPago;
	private Date fechaRecargo25;
	private Date fechaRecargo50;
	private Date fechaRehabilitacion;

	public String getTasaPagar() {
		return tasaPagar;
	}

	public void setTasaPagar( String tasaPagar ) {
		this.tasaPagar = tasaPagar;
	}
	
	public int getUnidades() {
		return unidades;
	}

	public void setUnidades( int unidades ) {
		this.unidades = unidades;
	}

	public boolean isAplazamientoTasas() {
		return aplazamientoTasas;
	}

	public void setAplazamientoTasas( boolean aplazamientoTasas ) {
		this.aplazamientoTasas = aplazamientoTasas;
	}

	public boolean isConcedido() {
		return concedido;
	}

	public void setConcedido( boolean concedido ) {
		this.concedido = concedido;
	}
	
	public String getModalidad() {
		return modalidad;
	}

	public void setModalidad( String modalidad ) {
		this.modalidad = modalidad;
	}

	public String getSolicitud() {
		return solicitud;
	}

	public void setSolicitud( String solicitud ) {
		this.solicitud = solicitud;
	}

	public String getPublicacion() {
		return publicacion;
	}

	public void setPublicacion( String publicacion ) {
		this.publicacion = publicacion;
	}

	public Date getFechaInicioPago() {
		return fechaInicioPago;
	}

	public void setFechaInicioPago( Date fechaInicioPago ) {
		this.fechaInicioPago = fechaInicioPago;
	}

	public Date getFechaFinPago() {
		return fechaFinPago;
	}

	public void setFechaFinPago( Date fechaFinPago ) {
		this.fechaFinPago = fechaFinPago;
	}

	public Date getFechaRecargo25() {
		return fechaRecargo25;
	}

	public void setFechaRecargo25( Date fechaRecargo25 ) {
		this.fechaRecargo25 = fechaRecargo25;
	}

	public Date getFechaRecargo50() {
		return fechaRecargo50;
	}

	public void setFechaRecargo50( Date fechaRecargo50 ) {
		this.fechaRecargo50 = fechaRecargo50;
	}

	public Date getFechaRehabilitacion() {
		return fechaRehabilitacion;
	}

	public void setFechaRehabilitacion( Date fechaRehabilitacion ) {
		this.fechaRehabilitacion = fechaRehabilitacion;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}