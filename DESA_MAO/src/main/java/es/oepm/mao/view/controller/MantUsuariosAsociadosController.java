package es.oepm.mao.view.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.springframework.security.authentication.encoding.Md5PasswordEncoder;

import es.oepm.core.business.BaseVO;
import es.oepm.core.business.mao.vo.MaoTiposdocVO;
import es.oepm.core.business.mao.vo.MaoTiposperfilesVO;
import es.oepm.core.business.mao.vo.MaoUsuAsociadosVO;
import es.oepm.core.business.mao.vo.TiposmodalidadVO;
import es.oepm.core.business.mao.vo.UsuariosAgenteVO;
import es.oepm.core.business.mao.vo.UsuariosTituRepreVO;
import es.oepm.core.exceptions.OepmException;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.controller.EditController;
import es.oepm.core.view.controller.NewController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.core.view.faces.SelectItemList;
import es.oepm.core.view.util.HTMLUtil;
import es.oepm.mao.comun.business.service.TiposPerfilesService;
import es.oepm.mao.comun.business.service.TiposdocService;
import es.oepm.mao.comun.business.service.TiposmodalidadService;
import es.oepm.mao.comun.business.service.UsuAsociadosService;
import es.oepm.mao.comun.business.service.UsuariosAgenteService;
import es.oepm.mao.comun.business.service.UsuariosService;
import es.oepm.mao.comun.business.service.UsuariosTituRepreService;
import es.oepm.mao.comun.transformers.UsuAsociadosTransformer;
import es.oepm.mao.view.controller.util.JSFPages;

/**
 * Controlador de edicion de usuarios asociados
 */
@ManagedBean(name = "mantUsuariosAsociadosController")
@ViewScoped
public class MantUsuariosAsociadosController extends BaseController implements NewController, EditController {

	private static final long serialVersionUID = -8347946352044428093L;
	
	@ManagedProperty(name = "usuAsociadosService", value = "#{usuAsociadosService}")
	private UsuAsociadosService usuAsociadosService;
	@ManagedProperty(name = "tiposdocService", value = "#{tiposdocService}")
	private TiposdocService tiposdocService;
	@ManagedProperty(name = "tiposPerfilesService", value = "#{tiposPerfilesService}")
	private TiposPerfilesService tiposPerfilesService;
	@ManagedProperty(name = "usuariosAgenteService", value = "#{usuariosAgenteService}")
	private UsuariosAgenteService usuariosAgenteService;
	@ManagedProperty(name = "usuariosTituRepreService", value = "#{usuariosTituRepreService}")
	private UsuariosTituRepreService usuariosTituRepreService;
	@ManagedProperty(name = "tiposmodalidadService", value = "#{tiposmodalidadService}")
	private TiposmodalidadService tiposmodalidadService; 
	
	@ManagedProperty(name = "usuariosService", value = "#{usuariosService}")
	private UsuariosService usuariosService;
	
	// Usuario seleccionado en la lista.
	private MaoUsuAsociadosVO filter = new MaoUsuAsociadosVO();
	private MaoUsuAsociadosVO original = new MaoUsuAsociadosVO();
	private String idAsociado = null;
	private String consulta = null;
	private String modificacion = null;
	private String codAgente= null;
	private String idTitrep= null;
	private Date fhBloqueoauto= null;

	public MantUsuariosAsociadosController() {
		idAsociado = FacesUtil.getParameter("idAsociado");
		consulta = FacesUtil.getParameter("consulta");
		modificacion = FacesUtil.getParameter("modificacion");
		codAgente =	SessionContext.getCodigoAgente();// MAO-254
		idTitrep = SessionContext.getIdUsuario();// MAO-255
	}

	@Override
	public String back() {
		return JSFPages.USUARIOSASOCIADOS_LIST;
	}

	@Override
	public void create(final ActionEvent param) {
		OepmLogger.debug("create - creando Usuario Asociado");		
		try {
			/**
			 * MAO-254
			 */
			if(filter.getIdAsociado()!=null){
				filter.setIdAsociado(null);
			}			
			
			// Comprobamos si es un autorizado de agente o representante titular
			if(!StringUtils.isEmptyOrNull(codAgente)){					
				UsuariosAgenteVO filterAgente = usuariosAgenteService.getById(codAgente);
				filter.setMaoAgentes(filterAgente);
			}else{
				UsuariosTituRepreVO filterTituRepre = usuariosTituRepreService.getById(Long.parseLong(idTitrep));
				filter.setMaoTitrep(filterTituRepre);
			}	
			// No se utilizan login y clave por lo que hay que asignarles valor
			filter.setTxLogin(filter.getTxDocumento());
			
			// HCS: MAO-486. En lugar de una clave aleatoria, fijamos
			// que la clave sea igual al login. 
			//String clave = UUID.randomUUID().toString();
			String clave = filter.getTxLogin();
			
			
			filter.setTxClave(codificarClave(clave));
			filter = usuAsociadosService.create(filter);
			
			if (filter!=null && filter.getIdAsociado()!=null) {
				idAsociado = filter.getIdAsociado().toString(); // Para cargarlo en modo edicion
			}			
			FacesUtil.addOKMessage("mantUsuariosAsociados.info.alta");
		} catch (final OepmException e) {
			FacesUtil.addErrorMessage("mantUsuariosAsociados.error.alta");
			OepmLogger.error(e);
		} catch (final Exception e) {
			FacesUtil.addErrorMessage("mantUsuariosAsociados.error.alta");
			OepmLogger.error(e);
		}
		OepmLogger.debug("create - fin");
		/**
		 * MAO-254
		 */
		if (filter == null) {
			filter = new MaoUsuAsociadosVO();
		}
		
		filter.setTxApellido1("");
		filter.setTxApellido2("");
		filter.setTxNombre("");
		filter.setTxEmail("");
		filter.setTxDocumento("");
		
		filter.getSelectedPermisosExpedientes().clear();
		filter.getSelectedPermisosGenerales().clear();
		filter.getSelectedModalidades().clear();
		filter.getSelectedPermisosTotales().clear();	
	}
	
	@Override
	public void save(ActionEvent param) {
		OepmLogger.debug("save - guardando Usuario Asociado");
		try {
			if(filter.getFhBloqueoauto()!=null && fhBloqueoauto!=null && (filter.getFhBloqueoauto().compareTo(fhBloqueoauto)!= 0)){
				filter.setFhBloqueoauto(null);
			}
			// No se utilizan login y clave
			// if(filter.getTxClave()!=null && !"".equals(filter.getTxClave()))
			//	filter.setTxClave(codificarClave(filter.getTxClave()));	
			copyViewUsuAsociadosVO(filter, original);
			usuAsociadosService.update(original);
			FacesUtil.addOKMessage("mantUsuariosAsociados.info.actualizar");
		} catch (final OepmException e) {
			FacesUtil.addErrorMessage("mantUsuariosAsociados.error.actualizar");
			OepmLogger.error(e);
		} catch (final Exception e) {
			FacesUtil.addErrorMessage("mantUsuariosAsociados.error.actualizar");
			OepmLogger.error(e);
		}
		OepmLogger.debug("save - fin");
	}
	
	/**
	 * Codifica la clave en MD5.
	 * 
	 * @param claveSinCodificar
	 *            Clave en texto plano a codificar.
	 * @return
	 */
	private String codificarClave(String claveSinCodificar){
		//Codificamos la clave en MD5
		Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();		
		return passwordEncoder.encodePassword( claveSinCodificar, null);	      
	}
	
	/**
	 * Copia los valores mostrados en la vista en el objeto original.
	 * 
	 * @param filter
	 * @param original
	 * 
	 */
	public static void copyViewUsuAsociadosVO(MaoUsuAsociadosVO filter, MaoUsuAsociadosVO original){		
		original.setMaoTiposperfiles(filter.getMaoTiposperfiles());
		// Campos comentados para que no sea posible su edición
		//original.setTxNombre(filter.getTxNombre());
		//original.setTxApellido1(filter.getTxApellido1());
		//original.setTxApellido2(filter.getTxApellido2());		
		//original.setMaoTiposdoc(filter.getMaoTiposdoc());
		//original.setTxDocumento(filter.getTxDocumento());
		//original.setTxEmail(filter.getTxEmail());
		original.setIdAsociado(filter.getIdAsociado());
		original.setSelectedPermisosTotales(filter.getSelectedPermisosTotales());
		original.setSelectedModalidades(filter.getSelectedModalidades());
		original.setSelectedPermisosExpedientes(filter.getSelectedPermisosExpedientes());
		original.setSelectedPermisosGenerales(filter.getSelectedPermisosGenerales());
		original.setBoSololectura(filter.isBoSololectura());
		original.setBoBloqueocuenta(filter.isBoBloqueocuenta());		
	}
	
	/************************Metodos generales*******************************/

	/**
	 * Indica si esta en modo edicion o no
	 * @return
	 */
	public boolean getEdicion () {
		if (modificacion!=null && consulta==null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indica si esta en modo consulta o no
	 * @return
	 */
	public boolean getConsul () {
		if (modificacion==null && consulta!=null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indica si esta en modo creacion o no
	 * @return
	 */
	public boolean getNuevo () {
		if (modificacion==null && consulta==null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indica si esta en modo creacion o no
	 * @return
	 */
	public boolean getConsOrEdic() {
		if (getConsul() || getEdicion()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Indica si el usuario tiene perfil de Administrador
	 * @return
	 */
	public boolean getAdministrador () {
		if (filter.getMaoTiposperfiles().getCodPerfilasoc()=='A') {
			return true;
		} else {
			return false;
		}
	}
	
	/************************LISTAS DE TIPOS***********************************/
	    
    /**
     * Metodo que permite obtener el listado de tipos de documentos
     * 
     * @return Listado.
     */
     public List<SelectItem> getTiposDocumentosList() {
        List<SelectItem> tipoDocumentosList = new ArrayList<SelectItem>();
        
        try {    	  
        	tipoDocumentosList =  HTMLUtil.getSelectItemList(SelectItemList.createDefault()
    				,  tiposdocService.search(new MaoTiposdocVO()), MaoTiposdocVO.class, "txDescripcion", "codTipodoc");   
        } catch (Exception exception) {
			FacesUtil.addErrorMessage();
			OepmLogger.error(exception);
        }
        return tipoDocumentosList;
        
    }  
     
     /**
      * Metodo que permite obtener el listado de tipos de perfiles
      * 
      * @return Listado.
      */
      public List<SelectItem> getTiposPerfilesList() {
         List<SelectItem> tipoPerfilesList = new ArrayList<SelectItem>();
         
         try {    	   
        	 tipoPerfilesList =  HTMLUtil.getSelectItemList(SelectItemList.createDefault()
     				,  tiposPerfilesService.search(new MaoTiposperfilesVO()), MaoTiposperfilesVO.class, "txDescripcion", "codPerfilasoc");   
         } catch (Exception exception) { 
 			FacesUtil.addErrorMessage();
 			OepmLogger.error(exception);
         }
         return tipoPerfilesList;
         
     }  
      
      /**
  	 * Muestra/carga la lista de modalidades
  	 * @return
  	 */  	
  	public Map <String, String> getModalidades () {
  		Map<String, String> modalidades = new HashMap<String, String>();     
  		
  		try{         		
  			List<TiposmodalidadVO> listaModalidades = tiposmodalidadService.search(new TiposmodalidadVO());
  			java.util.Iterator<TiposmodalidadVO> it= listaModalidades.iterator();
  			while(it.hasNext()){
  				TiposmodalidadVO mod= (TiposmodalidadVO)it.next();  				
  				modalidades.put(mod.getTxDescripcion(), String.valueOf(mod.getCodModalidad()));
  			}  	
  			
  		}catch(Exception exception){
			FacesUtil.addErrorMessage();
			OepmLogger.error(exception);
  		}

  		return modalidades;
  	}
      
	/****************************** metodos GET/SET ******************************/

	public UsuAsociadosService getUsuAsociadosService() {
		return usuAsociadosService;
	}

	public void setUsuAsociadosService(UsuAsociadosService usuAsociadosService) {
		this.usuAsociadosService = usuAsociadosService;
	}

	public MaoUsuAsociadosVO getFilter() {
		if ((filter == null || filter.getIdAsociado()==null) && idAsociado!=null) {
			try {
				original = usuAsociadosService.getById(Long.valueOf(idAsociado));
				UsuAsociadosTransformer.copyUsuAsociadosVO(original, filter);
				if (filter != null) {
					filter.setTxClave(null);
					filter.setTxClaveSeg(null);
					fhBloqueoauto = filter.getFhBloqueoauto();
				}
			} catch (final Exception e) {
				FacesUtil.addErrorMessage("mantUsuariosAsociados.error.getById");
				OepmLogger.error(e);
			}
		}
		return filter;
	}

	@Override
	public void setFilter(BaseVO filter) {
		this.filter = (MaoUsuAsociadosVO)filter;
	}

	public String getIdAsociado() {
		return idAsociado;
	}

	public void setIdAsociado(String idAsociado) {
		this.idAsociado = idAsociado;
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

	public TiposdocService getTiposdocService() {
		return tiposdocService;
	}

	public void setTiposdocService(TiposdocService tiposdocService) {
		this.tiposdocService = tiposdocService;
	}

	public TiposPerfilesService getTiposPerfilesService() {
		return tiposPerfilesService;
	}

	public void setTiposPerfilesService(TiposPerfilesService tiposPerfilesService) {
		this.tiposPerfilesService = tiposPerfilesService;
	}

	public String getConsulta() {
		return consulta;
	}

	public void setConsulta(String consulta) {
		this.consulta = consulta;
	}

	public String getModificacion() {
		return modificacion;
	}

	public void setModificacion(String modificacion) {
		this.modificacion = modificacion;
	}

	public Date getFhBloqueoauto() {
		return fhBloqueoauto;
	}

	public void setFhBloqueoauto(Date fhBloqueoauto) {
		this.fhBloqueoauto = fhBloqueoauto;
	}

	public UsuariosAgenteService getUsuariosAgenteService() {
		return usuariosAgenteService;
	}

	public void setUsuariosAgenteService(UsuariosAgenteService usuariosAgenteService) {
		this.usuariosAgenteService = usuariosAgenteService;
	}

	public UsuariosTituRepreService getUsuariosTituRepreService() {
		return usuariosTituRepreService;
	}

	public void setUsuariosTituRepreService(
			UsuariosTituRepreService usuariosTituRepreService) {
		this.usuariosTituRepreService = usuariosTituRepreService;
	}

	public TiposmodalidadService getTiposmodalidadService() {
		return tiposmodalidadService;
	}

	public void setTiposmodalidadService(TiposmodalidadService tiposmodalidadService) {
		this.tiposmodalidadService = tiposmodalidadService;
	}

	public UsuariosService getUsuariosService() {
		return usuariosService;
	}

	public void setUsuariosService(UsuariosService usuariosService) {
		this.usuariosService = usuariosService;
	}

	public Boolean getCampoRequerido() {
		return Boolean.TRUE;
	}
	
	public Boolean getCampoNoRequerido() {
		return Boolean.FALSE;
	}
}
