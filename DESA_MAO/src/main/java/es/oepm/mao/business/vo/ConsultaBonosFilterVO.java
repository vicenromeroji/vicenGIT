package es.oepm.mao.business.vo;

import java.util.Date;

import es.oepm.core.business.BaseVO;
import es.oepm.core.util.DateUtils;
import es.oepm.mao.constants.MaoTrazaGestor;

public class ConsultaBonosFilterVO extends BaseVO {

	private static final long serialVersionUID = -8696143370064130103L;
	
	private String ageCodigo;
	private String ageEmail;
	private String repDocumento;
	private String repEmail;
	private String titularDocumento;
	private String titEmail;
	private String titularApellido1;
	private String titularApellido2;
	private Date fechaBonoDesde;
	private Date fechaBonoHasta;
	private String modalidad;
	private String solicitud;
	private String publicacion;
	
	/**
	 * Obtiene los parámetros de la clase con formato para las trazas.
	 * 
	 * @return
	 */
	public String toTraze() {
		StringBuilder traze = new StringBuilder();

		if (ageCodigo != null) {
			traze.append("Cod. Agente: ").append(ageCodigo)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (ageEmail != null) {
			traze.append("Email Agente: ").append(ageEmail)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (repDocumento != null) {
			traze.append("Documento rep.: ").append(repDocumento)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (repEmail != null) {
			traze.append("Email rep.: ").append(repEmail)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (titularDocumento != null) {
			traze.append("Documento tit.: ").append(titularDocumento)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (titularApellido1 != null) {
			traze.append("Tit. ap1: ").append(titularApellido1)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (titularApellido2 != null) {
			traze.append("Tit. ap2: ").append(titularApellido2)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (fechaBonoDesde != null) {
			traze.append("Fecha bono desde: ")
					.append(DateUtils.formatFecha(fechaBonoDesde))
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (fechaBonoHasta != null) {
			traze.append("Fecha bono hasta: ")
					.append(DateUtils.formatFecha(fechaBonoHasta))
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (modalidad != null) {
			traze.append("Modalidad: ").append(modalidad)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (solicitud != null) {
			traze.append("Solicitud: ").append(solicitud)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (publicacion != null) {
			traze.append("Publicación: ").append(publicacion)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		// Eliminamos la última coma si existe
		if (traze.lastIndexOf(MaoTrazaGestor.TRAZA_SEPARADOR) == traze.length() - 2) {
			traze.delete(traze.length() - 2, traze.length());
		}

		return traze.toString();
	}
	
	public String getAgeCodigo() {
		return ageCodigo;
	}

	public void setAgeCodigo(String ageCodigo) {
		this.ageCodigo = ageCodigo;
	}

	public String getAgeEmail() {
		return ageEmail;
	}

	public void setAgeEmail(String ageEmail) {
		this.ageEmail = ageEmail;
	}

	public String getRepDocumento() {
		return repDocumento;
	}

	public void setRepDocumento(String repDocumento) {
		this.repDocumento = repDocumento;
	}

	public String getRepEmail() {
		return repEmail;
	}

	public void setRepEmail(String repEmail) {
		this.repEmail = repEmail;
	}

	public String getTitEmail() {
		return titEmail;
	}

	public void setTitEmail(String titEmail) {
		this.titEmail = titEmail;
	}
	
	public String getTitularApellido1() {
		return titularApellido1;
	}

	public void setTitularApellido1( String titularApellido1 ) {
		this.titularApellido1 = titularApellido1;
	}

	public String getTitularApellido2() {
		return titularApellido2;
	}

	public void setTitularApellido2( String titularApellido2 ) {
		this.titularApellido2 = titularApellido2;
	}

	public String getTitularDocumento() {
		return titularDocumento;
	}

	public void setTitularDocumento( String titularDocumento ) {
		this.titularDocumento = titularDocumento;
	}

	public Date getFechaBonoDesde() {
		return fechaBonoDesde;
	}

	public void setFechaBonoDesde( Date fechaBonoDesde ) {
		this.fechaBonoDesde = fechaBonoDesde;
	}

	public Date getFechaBonoHasta() {
		return fechaBonoHasta;
	}

	public void setFechaBonoHasta( Date fechaBonoHasta ) {
		this.fechaBonoHasta = fechaBonoHasta;
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