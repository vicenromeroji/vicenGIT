package es.oepm.mao.business.vo;

import java.util.Date;

import es.oepm.core.business.BaseVO;
import es.oepm.core.util.DateUtils;
import es.oepm.mao.constants.MaoTrazaGestor;

public class PagosRealizadosFilterVO extends BaseVO {

	private static final long serialVersionUID = -4757184619921199403L;
	
	private String apellido1;
	private String apellido2;
	private String documento;
	private String formaPago;
	private String tasa;
	private Date fechaPagoDesde;
	private Date fechaPagoHasta;
	private String modalidad;
	private String solicitud;
	private String publicacion;
	
	public PagosRealizadosFilterVO() {
	}
	
	public PagosRealizadosFilterVO( String modalidad, String solicitud ) {
		this.modalidad = modalidad;
		this.solicitud = solicitud;
	}

	public String getApellido1() {
		return apellido1;
	}

	public void setApellido1( String apellido1 ) {
		this.apellido1 = apellido1;
	}

	public String getApellido2() {
		return apellido2;
	}

	public void setApellido2( String apellido2 ) {
		this.apellido2 = apellido2;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento( String documento ) {
		this.documento = documento;
	}

	public String getFormaPago() {
		return formaPago;
	}

	public void setFormaPago( String formaPago ) {
		this.formaPago = formaPago;
	}

	public String getTasa() {
		return tasa;
	}

	public void setTasa( String tasa ) {
		this.tasa = tasa;
	}

	public Date getFechaPagoDesde() {
		return fechaPagoDesde;
	}

	public void setFechaPagoDesde( Date fechaPagoDesde ) {
		this.fechaPagoDesde = fechaPagoDesde;
	}

	public Date getFechaPagoHasta() {
		return fechaPagoHasta;
	}

	public void setFechaPagoHasta( Date fechaPagoHasta ) {
		this.fechaPagoHasta = fechaPagoHasta;
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

	/**
	 * Obtiene los parámetros de la clase con formato para las trazas.
	 * 
	 * @return
	 */
	public String toTraze() {
		StringBuilder traze = new StringBuilder();

		if (apellido1 != null) {
			traze.append("Apellido1: ");
			traze.append(apellido1);
			traze.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (apellido2 != null) {
			traze.append("Apellido2: ");
			traze.append(apellido2);
			traze.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (documento != null) {
			traze.append("Documento: ");
			traze.append(documento);
			traze.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (formaPago != null) {
			traze.append("FormaPago: ");
			traze.append(formaPago);
			traze.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (tasa != null) {
			traze.append("Tasa: ");
			traze.append(tasa);
			traze.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (fechaPagoDesde != null) {
			traze.append("FechaPagoDesde: ");
			traze.append(DateUtils.formatFecha(fechaPagoDesde));
			traze.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (fechaPagoHasta != null) {
			traze.append("FechaPagoHasta: ");
			traze.append(DateUtils.formatFecha(fechaPagoHasta));
			traze.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (modalidad != null) {
			traze.append("Modalidad: ");
			traze.append(modalidad);
			traze.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (solicitud != null) {
			traze.append("Solicitud: ");
			traze.append(solicitud);
			traze.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (publicacion != null) {
			traze.append("Publicacion: ");
			traze.append(publicacion);
			traze.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		// Eliminamos la última coma si existe
		if (traze.lastIndexOf(MaoTrazaGestor.TRAZA_SEPARADOR) == traze.length() - 2) {
			traze.delete(traze.length() - 2, traze.length());
		}

		return traze.toString();
	}
	
}