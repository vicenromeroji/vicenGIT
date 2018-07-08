package es.oepm.mao.view.controller;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import es.oepm.ceo.ws.anotacionesexp.AnotacionBonoDTO;
import es.oepm.ceo.ws.anotacionesexp.parameters.ConsultaAnotacionBonoAlfaResponse;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.business.service.AnotacionesService;
import es.oepm.mao.view.controller.util.JSFPages;

@ManagedBean(name = "consultaBonoController")
@ViewScoped
public class ConsultaBonoController implements Serializable {
	
	private static final long serialVersionUID = -9196318266977140727L;

	private String idAnotacionBono;
	
	private transient AnotacionBonoDTO anotacionBono;
	
	@ManagedProperty( "#{anotacionesService}" )
	private AnotacionesService anotacionesService;
	
	public ConsultaBonoController() {
		idAnotacionBono = FacesUtil.getParameter( "idAnotacion" );
	}
	
	public void setAnotacionesService( AnotacionesService anotacionesService ) {
		this.anotacionesService = anotacionesService;
	}
	
	public AnotacionBonoDTO getAnotacionBono() {
		if( idAnotacionBono != null && anotacionBono == null ) {
			ConsultaAnotacionBonoAlfaResponse response = null;
			
			try {
				response = anotacionesService.getAnotacionBono( new BigDecimal( idAnotacionBono ) );
			} catch (BusinessException be) {
				OepmLogger.error(be);

				FacesUtil.addErrorMessage("pagos.bonos.error.recuperarDatos");
			}
			
			if (response != null) {
				anotacionBono = response.getAnotacionBono();
			}
		}
		
		return anotacionBono;
	}
	
	public String back() {
		return JSFPages.PAGOS_BONOS_LIST;
	}
	
}