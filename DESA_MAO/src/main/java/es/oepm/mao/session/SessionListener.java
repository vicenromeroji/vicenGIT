package es.oepm.mao.session;

import java.io.Serializable;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


import es.oepm.core.business.mao.vo.SesionesVO;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.spring.ConfigurationConfigProvider;
import es.oepm.mao.comun.business.service.SesionesService;

/**
 * Listener de sesiones
 * 
 * @author AYESA AT
 */
public class SessionListener implements HttpSessionListener, Serializable {

	private static final long serialVersionUID = 899538122992188999L;
	
	private static SesionesService sesionesService;

	@Override
	public void sessionCreated(HttpSessionEvent sesion) {
		OepmLogger.debug("sessionCreated: " + sesion.getSession().getId());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent sesion) {
		OepmLogger.debug("sessionDestroyed: " + sesion.getSession().getId());
		if (sesionesService == null) {
			sesionesService = (SesionesService) ConfigurationConfigProvider
				.getCtx().getBean("sesionesService");
		}
		desactivaSesion(sesion);
	}

	/**
	 * Metodo que limpia la columna "Identificador de sesion" del usuario 
	 * que ha cerrado la sesion.
	 */
	private void desactivaSesion(HttpSessionEvent sessionEvent) {
		String loginUser = SessionContext.getLoginUsuario();
		if (loginUser != null) {
			try {
				SesionesVO sesion = sesionesService.getById(loginUser);
				if (sesion!=null) {
					sesion.setIdentificadorSesion("");
					
					if( SessionContext.getUltimaBusquedaBopi() != null ) {
						sesion.setUltimoAccesoBopi( SessionContext.getUltimaBusquedaBopi() );
					}
					
					sesionesService.update(sesion);
				}
			} catch (Exception e) {
				OepmLogger.error("Error desactivando la sesion. Motivo: " + e.getCause());
			}
		}
	}

}
