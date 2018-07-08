package es.oepm.mao.facade;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.dxd.oepm.plataval.validacion.CampoBean;
import es.dxd.oepm.plataval.validacion.services.ValidacionWS;
import es.dxd.oepm.plataval.validacion.services.ValidacionWSService;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.exceptions.ExceptionErrorCode;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.util.StringUtils;
import es.oepm.wservices.core.util.WSUtils;

public class PlatavalFacade implements Serializable {

	private static final long serialVersionUID = 3468272837036606534L;
	
	private static ValidacionWS platavalPort;

	/**
	 * @return the platavalPort
	 * @throws Exception
	 */
	public static ValidacionWS getPlatavalPort() throws BusinessException {
		comprobarClientePlataval();
		return platavalPort;
	}

	/**
	 * Crear el Cliente de WS de Plataval
	 */
	private static void comprobarClientePlataval() throws BusinessException {
		String endpoint = "";
		try {
			endpoint = Configuracion.getPropertyAsString("url.bus.plataval");
			
			if (!WSUtils.isAvailable(endpoint)) {
				ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_SERVICIO_NODISPONIBLE);
			}
			
			if (platavalPort == null) {
				if (!StringUtils.isEmptyOrNull(endpoint)) {
					ValidacionWSService ss = new ValidacionWSService(new URL(endpoint));
					platavalPort = ss.getValidacionWSSoapPort();
					
					int receiveTimeout = Configuracion
							.getPropertyAsInteger("ws.default.recieveTimeout");
					int connectionTimeout = Configuracion
							.getPropertyAsInteger("ws.default.connectionTimeout");

					WSUtils.setBindingEndpoint(platavalPort, endpoint);
					WSUtils.setBindingTimeouts(platavalPort, receiveTimeout,connectionTimeout);
				} else {
					OepmLogger.error("Error al instanciar el wsclient de Plataval. No esta definida en la configuración la variable url.bus.plataval");
				}
			}
			
		} catch (Exception e) {
			platavalPort = null;
			OepmLogger.error(
					"Error al instanciar el wsclient de Plataval para la dirección "
							+ endpoint, e);
			ExceptionUtil.throwBusinessException((Exception)e);
		}
	}
	
	/**
	 * Obtiene el campo concreto de los resultados
	 * 
	 * @param campos
	 * @param string
	 * @return
	 */
	public static String obtenerCampo (List<CampoBean> campos, String nombre) {
		if (campos != null) {
			for (CampoBean campo : campos) {
				if (campo.getNombre().equalsIgnoreCase(nombre)) {
					return campo.getValor();
				}
			}
		}
		return null;
	}
}
