package es.oepm.mao.view.controller;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;


import es.oepm.core.business.BaseVO;
import es.oepm.core.business.mao.vo.MaoUsuAsociadosVO;
import es.oepm.core.business.mao.vo.UsuariosAgenteVO;
import es.oepm.core.business.mao.vo.UsuariosTituRepreVO;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.controller.ListController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.comun.business.service.UsuAsociadosService;
import es.oepm.mao.view.controller.util.JSFPages;


@ManagedBean(name = "consultaUsuariosAsociadosController")
@ViewScoped
public class ConsultaUsuariosAsociadosController extends BaseController implements ListController {

	private static final long serialVersionUID = -6124599126150862386L;

	@ManagedProperty(name = "usuAsociadosService", value = "#{usuAsociadosService}")
	private UsuAsociadosService usuAsociadosService;
	
	// Usuario seleccionado en la lista.
	private MaoUsuAsociadosVO filter;
	private MaoUsuAsociadosVO selectedUsuario = new MaoUsuAsociadosVO();
	
	/** The show error. */
	private boolean showMsg = false;

	private boolean mustLoadList;
	
	private String codAgente = null;		
	private String idTitrep = null;
	private String tipoUsuario= null;	
	
	private List<MaoUsuAsociadosVO> maoUsuAsociadosList;

	/**
	 * Constructor por defecto de la clase.
	 */
	public ConsultaUsuariosAsociadosController() {
		super();
		/*
		 * Por defecto, se inicia el filtro de busqueda si existe algun valor
		 * previo en la session.
		 */
		BaseVO sessionValue = SessionUtil.getSearchFilter(true);
		if ((sessionValue != null) && (sessionValue instanceof MaoUsuAsociadosVO)) {
			setFilter(sessionValue);
			// Se activa el flag para indicar (cuando acceda al getFilter que
			// debe iniciar la lista.
			mustLoadList = true;
		} else {			
			SessionUtil.clearSearchFilter();				
		}
		
		if(!StringUtils.isEmptyOrNull(SessionContext.getTipoUsuario())){				
			tipoUsuario= obtenerTipo(SessionContext.getTipoUsuario());
			if(!StringUtils.isEmptyOrNull(SessionContext.getIdUsuario())){
				if(tipoUsuario.equals("A")){
					codAgente= SessionContext.getIdUsuario();					
				}else{
					idTitrep= SessionContext.getIdUsuario();
				}
			}			
		}		
	}
	
	private String obtenerTipo(String tipoUsuario){
		String tipo="";
		if(tipoUsuario.equals(TipoUsuario.AGENTE.name())){
			tipo= "A";
		}else if(tipoUsuario.equals(TipoUsuario.TITULAR.name())){
			tipo= "T";
		}else if(tipoUsuario.equals(TipoUsuario.REPRESENTANTE.name())){
			tipo= "R";
		}
		return tipo;
	}

	@Override
	public String actionSearch() {
		showMsg = false;
		try {
			if(getAgente()){
				UsuariosAgenteVO maoAgentes= new UsuariosAgenteVO();
				maoAgentes.setCodAgente(codAgente);
				filter.setMaoAgentes(maoAgentes);			
			}else{
				UsuariosTituRepreVO maoTituRepre= new UsuariosTituRepreVO();
				maoTituRepre.setIdTitrep(Long.parseLong(idTitrep));
				filter.setMaoTitrep(maoTituRepre);
			}
			maoUsuAsociadosList = usuAsociadosService.search(filter);
		} catch (Exception exception) {
			String errorMsg = ExceptionUtil.getMessage(exception);
			FacesUtil.addErrorMessage(errorMsg);
		}
		return null;
	}
	
	/**
	 * Action para el boton de limpiar la busqueda usuarios asociados. 
	 * Creado para MAO-459
	 * @param to Parametro opcional para especificar una jsp de destino 
	 * @return
	 */
	public String actionLimpiar(String to) {
		filter = new MaoUsuAsociadosVO();
		selectedUsuario = new MaoUsuAsociadosVO();
		//saveFilterInSession(filter);
		this.maoUsuAsociadosList = new ArrayList<MaoUsuAsociadosVO>();
		if(StringUtils.isEmptyOrNull(to)){
			return JSFPages.USUARIOSASOCIADOS_LIST;
		}
		else {
			return to;
		}
	}
	
	public String actionLimpiar() {
		return actionLimpiar(null);
	}

	@Override
	public String actionNew() {
		saveFilterInSession(filter);
		return JSFPages.USUARIOSASOCIADOS_NEW;
	}

	@Override
	public String actionEdit() {
		saveFilterInSession(filter);
		return JSFPages.USUARIOSASOCIADOS_MODIFY;
	}

	public String actionCons() {		
		saveFilterInSession(filter);
		return JSFPages.USUARIOSASOCIADOS_MODIFY;
	}

	
	@Override
	public String actionDelete() {
		try {
			usuAsociadosService.delete(selectedUsuario);
			maoUsuAsociadosList = null;
			if(getAgente()){
				UsuariosAgenteVO maoAgentes= new UsuariosAgenteVO();
				maoAgentes.setCodAgente(codAgente);
				filter.setMaoAgentes(maoAgentes);			
			}else{
				UsuariosTituRepreVO maoTituRepre= new UsuariosTituRepreVO();
				maoTituRepre.setIdTitrep(Long.parseLong(idTitrep));
				filter.setMaoTitrep(maoTituRepre);
			}
			maoUsuAsociadosList = usuAsociadosService.search(filter);
			FacesUtil.addOKMessage("consultaUsuariosAsociados.info.eliminado");
		} catch (Exception e) {
			OepmLogger.error("Error eliminando usuario. Motivo: " + e.getCause());
			FacesUtil.addErrorMessage("consultaUsuariosAsociados.error.eliminar");
		}
		showMsg = true;
		return null;
	}

	//********************************************************
	/*
	 * Indica si el tipo de usuario es "Agente".
	 */
	public boolean getAgente(){
		boolean res= false;
		if(!StringUtils.isEmptyOrNull(tipoUsuario) && tipoUsuario.equals("A")){
			res= true;
		}
		return res;
	}
	
	//********************************************************
	
	
	/**
	 * Metodo que permite obtener el filtro de busqueda
	 * 
	 * @return filter
	 */
	@Override
	public MaoUsuAsociadosVO getFilter() {
		try {
			if (filter == null) {
				// Si el filtro es nulo no hay valor restaurado de session.
				filter = new MaoUsuAsociadosVO();
			} else {
				/*
				 * Si el filtro esta inicializado y no es nulo, puede ser por
				 * dos motivos:
				 * 
				 * 1.- Se inicio anteriormente en el primer acceso a un atributo
				 * del mismo. 2.- Se inicio en el constructor por carga del
				 * contenido de session.
				 * 
				 * Se empleara un booleano para llevar a cabo este control.
				 */
				if (mustLoadList) {
					/*
					 * Se inicia la lista de resultados la primera vez que
					 * accede con filtro restaurado de session.
					 */
					if(getAgente()){
						UsuariosAgenteVO maoAgentes= new UsuariosAgenteVO();
						maoAgentes.setCodAgente(codAgente);
						filter.setMaoAgentes(maoAgentes);			
					}else{
						UsuariosTituRepreVO maoTituRepre= new UsuariosTituRepreVO();
						maoTituRepre.setIdTitrep(Long.parseLong(idTitrep));
						filter.setMaoTitrep(maoTituRepre);
					}
					maoUsuAsociadosList = this.usuAsociadosService.search(filter);
					mustLoadList = false;
				}
			}
		} catch (Exception exception) {
			String errorMsg = ExceptionUtil.getMessage(exception);
			FacesUtil.addErrorMessage(errorMsg);
		}
		return filter;
	}

	/**
	 * Metodo que permite establecer el filtro de busqueda
	 * 
	 * @param filter
	 */
	@Override
	public void setFilter(final BaseVO filter) {
		this.filter = (MaoUsuAsociadosVO) filter;
	}


	public void setUsuAsociadosService(UsuAsociadosService usuAsociadosService) {
		this.usuAsociadosService = usuAsociadosService;
	}

	public MaoUsuAsociadosVO getSelectedAudit() {
		return selectedUsuario;
	}

	public void setSelectedAudit(MaoUsuAsociadosVO selectedAudit) {
		this.selectedUsuario = selectedAudit;
	}

	public MaoUsuAsociadosVO getselectedUsuario() {
		return selectedUsuario;
	}

	public void setselectedUsuario(MaoUsuAsociadosVO selectedUsuario) {
		this.selectedUsuario = selectedUsuario;
	}

	public boolean isMustLoadList() {
		return mustLoadList;
	}

	public void setMustLoadList(boolean mustLoadList) {
		this.mustLoadList = mustLoadList;
	}

	public List<MaoUsuAsociadosVO> getMaoUsuAsociadosList() {
		return maoUsuAsociadosList;
	}

	public void setMaoUsuAsociadosList(List<MaoUsuAsociadosVO> maoUsuAsociadosList) {
		this.maoUsuAsociadosList = maoUsuAsociadosList;
	}

	public UsuAsociadosService getUsuAsociadosService() {
		return usuAsociadosService;
	}

	public void setFilter(MaoUsuAsociadosVO filter) {
		this.filter = filter;
	}

	public boolean isShowMsg() {
		return showMsg;
	}

	public void setShowMsg(boolean showMsg) {
		this.showMsg = showMsg;
	}

	public MaoUsuAsociadosVO getSelectedUsuario() {
		return selectedUsuario;
	}

	public void setSelectedUsuario(MaoUsuAsociadosVO selectedUsuario) {
		this.selectedUsuario = selectedUsuario;
	}

	public String getCodAgente() {
		return codAgente;
	}

	public void setCodAgente(String codAgente) {
		this.codAgente = codAgente;
	}

	public String getIdTitrep() {
		return idTitrep;
	}

	public void setIdTitrep(String idTitrep) {
		this.idTitrep = idTitrep;
	}

	public String getTipoUsuario() {
		return tipoUsuario;
	}

	public void setTipoUsuario(String tipoUsuario) {
		this.tipoUsuario = tipoUsuario;
	}	

}
