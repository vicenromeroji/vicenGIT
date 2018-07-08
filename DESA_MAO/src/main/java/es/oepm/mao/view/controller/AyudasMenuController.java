package es.oepm.mao.view.controller;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;


import es.oepm.core.business.BaseVO;
import es.oepm.core.business.mao.vo.AyudasVO;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.contenido.business.service.AyudasService;

@ManagedBean(name = "ayudasMenuController")
@ViewScoped
public class AyudasMenuController extends BaseController {

	private static final long serialVersionUID = 6852646986881933213L;

	@ManagedProperty(name = "ayudasService", value = "#{ayudasService}")
	private AyudasService ayudasService;

	/** The show error. */
	private boolean showMsg = false;

	private boolean mustLoadList;

	private List<AyudasVO> ayudasList;
	
	/**
	 * Constructor por defecto de la clase.
	 */
	public AyudasMenuController() {
		super();
	}

	public String actionSearch() {
		showMsg = false;
		try {
			AyudasVO filter = new AyudasVO();
			ayudasList = ayudasService.search(filter);
		} catch (Exception exception) {
			String errorMsg = ExceptionUtil.getMessage(exception);
			FacesUtil.addErrorMessage(errorMsg);
		}
		return null;
	}
	
	public void navegar(ActionEvent actionEvent){
		showMsg = false;
	}

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

	public List<AyudasVO> getAyudasList() {
		if (this.ayudasList == null) {
			actionSearch();
		}

		return ayudasList;
	}

	public void setAyudasList(List<AyudasVO> ayudasList) {
		this.ayudasList = ayudasList;
	}

	public AyudasService getAyudasService() {
		return ayudasService;
	}

	public boolean isShowMsg() {
		return showMsg;
	}

	public void setShowMsg(boolean showMsg) {
		this.showMsg = showMsg;
	}

	@Override
	public BaseVO getFilter() {
		return null;
	}

	@Override
	public void setFilter(BaseVO filter) {
	}

}
