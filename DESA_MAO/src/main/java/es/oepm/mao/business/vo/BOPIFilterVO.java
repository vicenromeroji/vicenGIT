package es.oepm.mao.business.vo;

import java.util.Date;

import es.oepm.core.business.BaseVO;
import es.oepm.core.util.DateUtils;
import es.oepm.mao.constants.MaoTrazaGestor;

public class BOPIFilterVO extends BaseVO {

	private static final long serialVersionUID = 8000380045324042691L;
	
	private Date fechaPublicacionDesde;
	private Date fechaPublicacionHasta;
	private String modalidad;
	private String solicitud;
	private String publicacion;
	private String tomo;
	private String sumario;
	private String titular;
//	private String agente;
	
	public BOPIFilterVO( String tomo ) {
		this.tomo = tomo;
	}
	
	public BOPIFilterVO( String tomo, String modalidad, String solicitud ) {
		this( tomo );
		
		this.modalidad = modalidad;
		this.solicitud = solicitud;
	}
	
	/**
	 * Obtiene los parámetros de la clase con formato para las trazas.
	 * 
	 * @return
	 */
	public String toTraze() {
		StringBuilder traze = new StringBuilder();

		if (fechaPublicacionDesde != null) {
			traze.append("FechaPublicacionDesde: ")
					.append(DateUtils.formatFecha(fechaPublicacionDesde))
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (fechaPublicacionHasta != null) {
			traze.append("FechaPublicacionHasta: ")
					.append(DateUtils.formatFecha(fechaPublicacionHasta))
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
			traze.append("Publicacion: ").append(publicacion)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (tomo != null) {
			traze.append("Tomo: ").append(tomo)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (sumario != null) {
			traze.append("Sumario: ").append(sumario)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		// Eliminamos la última coma si existe
		if (traze.lastIndexOf(MaoTrazaGestor.TRAZA_SEPARADOR) == traze.length() - 2) {
			traze.delete(traze.length() - 2, traze.length());
		}

		return traze.toString();
	}

	public Date getFechaPublicacionDesde() {
		return fechaPublicacionDesde;
	}

	public void setFechaPublicacionDesde( Date fechaPublicacionDesde ) {
		this.fechaPublicacionDesde = fechaPublicacionDesde;
	}

	public Date getFechaPublicacionHasta() {
		return fechaPublicacionHasta;
	}

	public void setFechaPublicacionHasta( Date fechaPublicacionHasta ) {
		this.fechaPublicacionHasta = fechaPublicacionHasta;
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

	public String getTomo() {
		return tomo;
	}

	public void setTomo( String tomo ) {
		this.tomo = tomo;
	}

	public String getSumario() {
		return sumario;
	}

	public void setSumario( String sumario ) {
		this.sumario = sumario;
	}

	public String getTitular() {
		return titular;
	}

	public void setTitular( String titular ) {
		this.titular = titular;
	}

//	public String getAgente() {
//		return agente;
//	}

//	public void setAgente( String agente ) {
//		this.agente = agente;
//	}

}