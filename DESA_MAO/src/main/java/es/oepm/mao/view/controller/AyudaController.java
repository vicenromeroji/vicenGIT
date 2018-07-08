package es.oepm.mao.view.controller;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;


import es.oepm.core.business.BaseVO;
import es.oepm.core.business.mao.vo.AyudasVO;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.contenido.business.service.AyudasService;

@ManagedBean(name = "ayudaController")
@ViewScoped
public class AyudaController extends BaseController {

	private static final long serialVersionUID = -3210160575799108000L;


	@ManagedProperty(name = "ayudasService", value = "#{ayudasService}")
	private AyudasService ayudasService;
	
	
	/** The show error. */
	//private boolean showMsg = false;

	private boolean mustLoadList;
	
	//Ayuda seleccionada
	private AyudasVO ayudaSeleccionada;

	/**
	 * Constructor por defecto de la clase.
	 */
	public AyudaController() {		
		super();
	}

	/*public String actionSearch(Long id) {
		showMsg = false;
		try {			
			ayudasList = ayudasService.getById(id);
		} catch (Exception exception) {
			String errorMsg = ExceptionUtil.getMessage(exception);
			FacesUtil.addErrorMessage(errorMsg);
		}
		return null;
	}*/

	// **********************GET SET**********************************

	public void setAyudasService(AyudasService ayudasService) {
		this.ayudasService = ayudasService;
	}

	public boolean isMustLoadList() {
		return mustLoadList;
	}

	public void setMustLoadList(boolean mustLoadList) {
		this.mustLoadList = mustLoadList;
	}

	
	public AyudasService getAyudasService() {
		return ayudasService;
	}

	/*public boolean isShowMsg() {
		return showMsg;
	}

	public void setShowMsg(boolean showMsg) {
		this.showMsg = showMsg;
	}*/

	@Override
	public BaseVO getFilter() {
		return null;
	}

	@Override
	public void setFilter(BaseVO filter) {
	}

	public AyudasVO getAyudaSeleccionada() {
		try {
			String uri= FacesUtil.getParameter("paginaActual");
			
			int finNombrePagina = uri.indexOf(".xhtml");
			if(finNombrePagina==-1)
				finNombrePagina= uri.length();
			int inicioNombrePagina = uri.lastIndexOf("/");
			String pagina = uri.substring(inicioNombrePagina +1, finNombrePagina);
			AyudasVO filter = new AyudasVO();
			filter.setPagina(pagina);
			List <AyudasVO> ayudas = ayudasService.search(filter);
			if(ayudas!=null && !ayudas.isEmpty())
				ayudaSeleccionada = ayudasService.search(filter).get(0);
			else
				ayudaSeleccionada=null;
			
		} catch (Exception exception) {
			String errorMsg = ExceptionUtil.getMessage(exception);
			FacesUtil.addErrorMessage(errorMsg);
		}
		return ayudaSeleccionada;
	}

	public void setAyudaSeleccionada(AyudasVO ayudaSeleccionada) {
		this.ayudaSeleccionada = ayudaSeleccionada;
	}

	public String getPaginaActual(){
		return FacesUtil.getParameter("paginaActual");
	}
	

}
