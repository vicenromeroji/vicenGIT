package es.oepm.mao.business.service;

import java.io.Serializable;

import es.oepm.core.exceptions.BusinessException;

/**
 * Interfaz para el servicio de trazas de los usuarios gestores.
 * 
 * @author jugonzalez
 *
 */
public interface TrazaGestorService extends Serializable {

	/**
	 * Inserta en la traza del gestor la acción y detalle realizados.
	 * 
	 * @param accion
	 *            Acción realizada.
	 * @param detalle
	 *            Detalle de la acción.
	 * @throws BusinessException
	 */
	public void insertTraze(String accion, String detalle)
			throws BusinessException;
}
