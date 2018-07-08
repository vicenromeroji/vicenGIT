package es.oepm.mao.view.controller;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import es.oepm.core.session.SessionUtil;
import es.oepm.core.util.StringUtils;
import es.oepm.mao.view.controller.util.JSFPages;
import es.oepm.maoceo.comun.view.controller.IVolverDetalleExpedienteController;

@ManagedBean(name = "volverDetalleExpedienteController")
@SessionScoped
public class VolverDetalleExpedienteController implements IVolverDetalleExpedienteController {
	
	private static final long serialVersionUID = -263986413460175051L;

	/**
	 * Permite obtener la pagina de busqueda a la que se debe regresar desde una pagina de detalle de un expediente
	 * @return Pagina de busqueda
	 */
	public String paginaBusqueda(){
		String paginaBusquedaExpediente = (String) SessionUtil
				.getFromSession("paginaBusquedaExpediente");
		if(StringUtils.isEmptyOrNull(paginaBusquedaExpediente))
				paginaBusquedaExpediente= JSFPages.BUSQ_EXP_RESULTADOS;
		
		return paginaBusquedaExpediente;
		
	}
}
