package es.oepm.mao.application;

import java.io.Serializable;

import org.springframework.context.event.ContextRefreshedEvent;


import es.oepm.core.logger.OepmLogger;
import es.oepm.core.spring.ConfigurationConfigProvider;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.comun.business.service.SesionesService;


public class RefreshedEventListener implements org.springframework.context.ApplicationListener<ContextRefreshedEvent>,
Serializable{
	
	private static final long serialVersionUID = 3212285155433529740L;
	
	private SesionesService sesionesService;
	
	public void onApplicationEvent(ContextRefreshedEvent event) {
		System.out.println("ContextRefreshedEvent Received.....");
	    //Vacia la tabla MAO_SESIONES  
		try {
			if (sesionesService == null) {
				sesionesService= (SesionesService)ConfigurationConfigProvider.getCtx().getBean("sesionesService");
			}
			sesionesService.deleteAll();
		}catch (final Exception e) {
			FacesUtil.addErrorMessage("Error borrando los datos de la tabla Sesión.");
			OepmLogger.error(e);
		}
	     
	} 
	
   
}
