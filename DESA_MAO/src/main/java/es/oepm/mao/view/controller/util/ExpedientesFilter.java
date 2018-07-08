package es.oepm.mao.view.controller.util;

import java.io.Serializable;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import es.oepm.core.util.DateUtils;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.constants.MaoTrazaGestor;

public class ExpedientesFilter implements Serializable {

	private static final long serialVersionUID = 6407427766835631813L;

	public ExpedientesFilter() {
	}

	public TipoUsuario tipoUsuario;
	public String codAgente;
	public String emailAgente;
	public String codRepresentante;
	public String emailRepresentante;
	public String codTitular;
	public String emailTitular;
	public String nombre;
	public String modalidad;
	public String numeroSolicitud;
	public String numeroPublicacion;
	public Date fhPresentacionInicio;
	public Date fhPresentacionFin;
	public XMLGregorianCalendar calFecPresentExpDesde;
	public XMLGregorianCalendar calFecPresentExpHasta;
	
	/**
	 * Obtiene los parámetros de la clase con formato para las trazas.
	 * 
	 * @return
	 */
	public String toTraze() {
		StringBuilder traze = new StringBuilder();

		if (tipoUsuario != null) {
			traze.append("Tipo usuario: ").append(tipoUsuario.name())
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (codAgente != null) {
			traze.append("Cod. agente: ").append(codAgente)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (emailAgente != null) {
			traze.append("Email agente: ").append(emailAgente)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (codRepresentante != null) {
			traze.append("Cod. repre: ").append(codRepresentante)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (emailRepresentante != null) {
			traze.append("Email repre: ").append(emailRepresentante)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (codTitular != null) {
			traze.append("Cod. titular: ").append(codTitular)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (emailTitular != null) {
			traze.append("Email titular: ").append(emailTitular)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (nombre != null) {
			traze.append("Nombre: ").append(nombre)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (modalidad != null) {
			traze.append("Modalidad: ").append(modalidad)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (numeroSolicitud != null) {
			traze.append("Num solicitud: ").append(numeroSolicitud)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (numeroPublicacion != null) {
			traze.append("Num publicacion: ").append(numeroPublicacion)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (fhPresentacionInicio != null) {
			traze.append("F. presentación ini: ")
					.append(DateUtils.formatFecha(fhPresentacionInicio))
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (fhPresentacionFin != null) {
			traze.append("F. presentación fin: ").append(
					DateUtils.formatFecha(fhPresentacionFin));
		}

		// Eliminamos la última coma si existe
		if (traze.lastIndexOf(MaoTrazaGestor.TRAZA_SEPARADOR) == traze.length() - 2) {
			traze.delete(traze.length() - 2, traze.length());
		}

		return traze.toString();
	}
}
