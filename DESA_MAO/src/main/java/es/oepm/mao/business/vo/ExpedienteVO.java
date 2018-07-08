package es.oepm.mao.business.vo;

import java.io.Serializable;
import java.util.Date;

import es.oepm.busmule.ws.client.ceo.BusExpediente;

public class ExpedienteVO extends BusExpediente implements Serializable {
	
	private static final long serialVersionUID = -5156008586751856139L;

	public ExpedienteVO(BusExpediente exp) {
		this.fechaUltActo = exp.getFechaUltActo();
		this.id = exp.getId();
		this.modalidad = exp.getModalidad();
		this.modalidadDesc = exp.getModalidadDesc();
		this.numeroPublicacion = exp.getNumeroPublicacion();
		this.numeroSolicitud = exp.getNumeroSolicitud();
		this.titulares = exp.getTitulares();
		this.tituloInvencion = exp.getTituloInvencion();
		this.bis = exp.getBis();
		this.numeroSolicitudFormateado = exp.getNumeroSolicitudFormateado();
		this.clasesVigentes = exp.getClasesVigentes();
		this.fechaAnotacionPublicacionContinuacion = exp.getFechaAnotacionPublicacionContinuacion();
		this.fechaPresentacion = exp.getFechaPresentacion();
		this.fechaPrioridad = exp.getFechaPrioridad();
		this.fechaProxRenova = exp.getFechaProxRenova();
	}

	/**
     * Obtiene el valor de la propiedad fechaUltActo en fecha
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
	public Date getFechaUltActoDate() {
		return fechaUltActo == null ? null : fechaUltActo.toGregorianCalendar().getTime();
	}

	/**
	 * Devolver como cadena
	 * 
	 * @return String
	 */
    public String getTitularesString () {
    	StringBuffer tits = new StringBuffer();
		
		if (titulares != null && titulares.size()>0) {
			tits.append(titulares.get(0));
			
			for (int i=1; i<titulares.size(); i++) {
				tits.append(", " + titulares.get(i));
			}
		}
		
		return tits.toString();
    }
}
