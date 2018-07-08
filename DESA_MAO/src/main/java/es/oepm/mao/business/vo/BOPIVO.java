package es.oepm.mao.business.vo;

import java.util.Date;

import es.oepm.bopiws.persistencia.vo.Anotacion;
import es.oepm.bopiws.persistencia.vo.Subapartado;
import es.oepm.core.business.BaseVO;

public class BOPIVO extends BaseVO {

	private static final long serialVersionUID = 5181750584039591029L;

	private Anotacion anotacion;

	private String enlace_ucm;

	private Subapartado subapartado;

	/**
	 * @return the anotacion
	 */
	public Anotacion getAnotacion() {
		return anotacion;
	}

	/**
	 * @param anotacion the anotacion to set
	 */
	public void setAnotacion(Anotacion anotacion) {
		this.anotacion = anotacion;
	}

	/**
	 * @return the enlace_ucm
	 */
	public String getEnlace_ucm() {
		return enlace_ucm;
	}

	/**
	 * @param enlace_ucm the enlace_ucm to set
	 */
	public void setEnlace_ucm(String enlace_ucm) {
		this.enlace_ucm = enlace_ucm;
	}

	/**
	 * @return the subapartado
	 */
	public Subapartado getSubapartado() {
		return subapartado;
	}

	/**
	 * @param subapartado the subapartado to set
	 */
	public void setSubapartado(Subapartado subapartado) {
		this.subapartado = subapartado;
	}
	
	
	/*****************************/
	/** Getters para Primefaces **/
	/*****************************/

	public Date getFechaPublicacion()
	{
		return this.anotacion.getFechaPublicacion().getTime();
	}
	
	
	/**
	 * Devuelve el numero de solicitud
	 * @return
	 */
	public String getNumSolicitud()
	{
		return this.anotacion.getModalidad()+this.anotacion.getNumExpediente();
	}
	
	/**
	 * Devuelve el nombre de la anotacion
	 * @return
	 */
	public String getNombreAnotacion()
	{
		return this.anotacion.getNombreAnotacion();
	}
	
	/**
	 * Obtiene el nombre del titular
	 * @return
	 */
	public String getNombreTitular()
	{
		return this.anotacion.getNombreTitular();
	}
	
	/**
	 * Obtiene el nombre del solicitante
	 * @return
	 */
	public String getNombreSolicitante()
	{
		return this.anotacion.getNombreSolicitante();
	}
	
	/**
	 * Obtiene el nombre recurrente
	 * @return
	 */
	public String getNombreRecurrente()
	{
		return this.anotacion.getNombreRecurrente();
	}
	

	
}