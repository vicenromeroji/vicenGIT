package es.oepm.mao.view.controller;

import java.io.Serializable;

import es.oepm.core.constants.Mensaje;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.wservices.core.mensajes.Mensajes;

/**
 * Padre de detalles de expedientes
 * 
 * @author AYESA AT
 */
public class DetalleExpedienteController implements Serializable {
	
	private static final long serialVersionUID = -3558983746672502297L;
	
	protected String idExpediente = null;
	protected String modalidad = null;

	/**
	 * Procesa los mensajes como mensajes faces
	 * 
	 * @param mensajes
	 */
	protected void procesarMensajesDetalleExpediente(Mensajes[] mensajes) {
		if (mensajes != null) {
			for (Mensajes mensaje : mensajes) {
				switch (mensaje.getCodigo()) {
					case Mensaje.COD_WS_ERROR_DETALLE_EXP_PARAM_INCORRECTOS:
						FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
								"busqueda.error");
						break;
					case Mensaje.COD_WS_ERROR_DETALLE_EXP_MOD_NO_ENCONTRADA:
						FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
								"busqueda.error");
						break;
					case Mensaje.COD_WS_ERROR_DETALLE_EXP_EXP_NO_ENCONTRADO:
						FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
								"busqueda.error");
						break;
					case Mensaje.COD_WS_ERROR_GENERICO:
						FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
								"busqueda.error");
						break;
					case Mensaje.COD_ERROR_RECUPERAR_FIGURAS:
						FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
								"error.al.recuperar.figuras");
						break;
				}
			}
		}
	}
	
}
