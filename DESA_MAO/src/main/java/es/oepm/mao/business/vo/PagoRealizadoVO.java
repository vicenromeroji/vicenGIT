package es.oepm.mao.business.vo;

import java.util.Date;

import es.oepm.core.business.BaseVO;

public class PagoRealizadoVO extends BaseVO {

	private static final long serialVersionUID = -1174021652510861917L;
	
	private String codigoBarras;
	private String solicitudNumero;
	private Date fechaPago;
	private double importeDevuelto;
	private double importePagado;
	private String tasaCodigo;
	private String tasaDescripcion;
	private String estado;

	public String getCodigoBarras() {
		return codigoBarras;
	}

	public void setCodigoBarras( String codigoBarras ) {
		this.codigoBarras = codigoBarras;
	}

	public String getSolicitudNumero() {
		return solicitudNumero;
	}

	public void setSolicitudNumero( String solicitudNumero ) {
		this.solicitudNumero = solicitudNumero;
	}

	public Date getFechaPago() {
		return fechaPago;
	}

	public void setFechaPago( Date fechaPago ) {
		this.fechaPago = fechaPago;
	}

	public double getImporteDevuelto() {
		return importeDevuelto;
	}

	public void setImporteDevuelto( double importeDevuelto ) {
		this.importeDevuelto = importeDevuelto;
	}

	public double getImportePagado() {
		return importePagado;
	}

	public void setImportePagado( double importePagado ) {
		this.importePagado = importePagado;
	}

	public String getTasaCodigo() {
		return tasaCodigo;
	}

	public void setTasaCodigo( String tasaCodigo ) {
		this.tasaCodigo = tasaCodigo;
	}

	public String getTasaDescripcion() {
		return tasaDescripcion;
	}

	public void setTasaDescripcion( String tasaDescripcion ) {
		this.tasaDescripcion = tasaDescripcion;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado( String estado ) {
		this.estado = estado;
	}

}