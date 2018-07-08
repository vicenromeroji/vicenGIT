package es.oepm.mao.business.vo;

import java.util.Date;

import es.oepm.core.business.BaseVO;

public class ConsultaTasasFilterVO extends BaseVO {

	private static final long serialVersionUID = 7828593316497247346L;
	
	private String codigo;
	private String descripcion;
	private Date fecha;
	private String modalidad;

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo( String codigo ) {
		this.codigo = codigo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion( String descripcion ) {
		this.descripcion = descripcion;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha( Date fecha ) {
		this.fecha = fecha;
	}

	public String getModalidad() {
		return modalidad;
	}

	public void setModalidad( String modalidad ) {
		this.modalidad = modalidad;
	}
	
}