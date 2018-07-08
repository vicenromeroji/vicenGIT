package es.oepm.mao.business.vo;

import java.util.Date;

import es.oepm.core.business.BaseVO;

public class PagosEsperadosFilterVO extends BaseVO {

	private static final long serialVersionUID = 7856378180456104282L;
	
	private Date fechaPresentExpDesde;
	private Date fechaPresentExpHasta;
	private String modalidad;
	private String solicitud;
	private String publicacion;
	
	public Date getFechaPresentExpDesde() {
		return fechaPresentExpDesde;
	}

	public void setFechaPresentExpDesde( Date fechaPresentExpDesde ) {
		this.fechaPresentExpDesde = fechaPresentExpDesde;
	}

	public Date getFechaPresentExpHasta() {
		return fechaPresentExpHasta;
	}

	public void setFechaPresentExpHasta( Date fechaPresentExpHasta ) {
		this.fechaPresentExpHasta = fechaPresentExpHasta;
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

}