package es.oepm.mao.view.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;

import es.oepm.core.business.BaseVO;
import es.oepm.core.business.mao.vo.MaoTiposdocVO;
import es.oepm.core.business.mao.vo.UsuarioGestorVO;
import es.oepm.core.business.mao.vo.UsuariosAgenteVO;
import es.oepm.core.business.mao.vo.UsuariosTituRepreVO;
import es.oepm.core.business.vo.OrPaisesVO;
import es.oepm.core.business.vo.OrProvinciasVO;
import es.oepm.core.constants.TipoUsuariosRegistro;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.exceptions.OepmException;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.controller.EditController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.core.view.faces.SelectItemList;
import es.oepm.core.view.util.HTMLUtil;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.comun.business.service.PaisesService;
import es.oepm.mao.comun.business.service.ProvinciasService;
import es.oepm.mao.comun.business.service.TiposdocService;
import es.oepm.mao.comun.business.service.UsuAsociadosService;
import es.oepm.mao.comun.business.service.UsuarioGestorService;
import es.oepm.mao.comun.business.service.UsuariosAgenteService;
import es.oepm.mao.comun.business.service.UsuariosTituRepreService;
import es.oepm.mao.constants.MaoPropiedadesConf;
import es.oepm.mao.view.controller.util.JSFPages;

/**
 * Controlador de Registro de Nuevos Usuarios (Titulares, Representantes o
 * Agentes)
 */
@ManagedBean(name = "registrarUsuarioController")
@ViewScoped
public class RegistrarUsuarioController extends BaseController implements
		EditController {
	
	private static final long serialVersionUID = -1772895257737243358L;

	@ManagedProperty(name = "usuariosTituRepreService", value = "#{usuariosTituRepreService}")
	private UsuariosTituRepreService usuariosTituRepreService;

	@ManagedProperty(name = "usuariosAgenteService", value = "#{usuariosAgenteService}")
	private UsuariosAgenteService usuariosAgenteService;

	@ManagedProperty(name = "provinciasService", value = "#{provinciasService}")
	private ProvinciasService provinciasService;

	@ManagedProperty(name = "paisesService", value = "#{paisesService}")
	private PaisesService paisesService;

	@ManagedProperty(name = "tiposdocService", value = "#{tiposdocService}")
	private TiposdocService tiposdocService;

	@ManagedProperty(name = "usuAsociadosService", value = "#{usuAsociadosService}")
	private UsuAsociadosService usuAsociadosService;
	
	@ManagedProperty(name = "usuarioGestorService", value = "#{usuarioGestorService}")	
	private UsuarioGestorService usuarioGestorService;
	
	// Usuario seleccionado en la lista.
	private UsuariosTituRepreVO filter = new UsuariosTituRepreVO();
	private UsuariosAgenteVO filterAgente = new UsuariosAgenteVO();
	private UsuariosTituRepreVO filterAux = new UsuariosTituRepreVO();
	private UsuariosAgenteVO filterAgenteAux = new UsuariosAgenteVO();
	
	// Logado como usuario Gestor. Datos solo lectura.
	private UsuarioGestorVO gestorVO = new UsuarioGestorVO();

	private String tipoUsuario = "";
	private String codAgente = null;
	private String idTitrep = null;
	private boolean edicion = false;
	private Boolean registroClave = Boolean.FALSE;
	
	// Listado de combos
	private List<SelectItem> paisesList = new ArrayList<SelectItem>();
	private List<SelectItem> provinciasList = new ArrayList<SelectItem>();
	private List<SelectItem> tipoDocumentosList = new ArrayList<SelectItem>();
	
	
	public RegistrarUsuarioController() {
		// Comprobamos si es edicion
		if (isUsuarioLogado()) {
			if (!StringUtils.isEmptyOrNull(SessionContext.getTipoUsuario())) {
				edicion = true;
				tipoUsuario = obtenerTipo(SessionContext.getTipoUsuario());
				if (!StringUtils.isEmptyOrNull(SessionContext.getIdUsuario())) {
					if (tipoUsuario.equals("A")) {
						codAgente = SessionContext.getIdUsuario();
						if (codAgente.length() < 4) {
							codAgente = String.format("%04d", Integer.parseInt(codAgente));
						}
					} else {
						idTitrep = SessionContext.getIdUsuario();
					}
				}
			}
		} else {
			HttpServletRequest httpServletRequest = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			
			// Comprobamos si el registro se realiza mediante Cl@ve
			registroClave = Boolean.valueOf((String) httpServletRequest
					.getSession().getAttribute(MaoPropiedadesConf.REGISTRO_CLAVE));
			// Seteamos los datos del usuario proporcionados por Cl@ve
			if (registroClave.equals(Boolean.TRUE)) {
				String nombre = (String) httpServletRequest.getSession()
						.getAttribute(MaoPropiedadesConf.REGISTRO_NOMBRE);
				String apellidos = (String) httpServletRequest.getSession()
						.getAttribute(MaoPropiedadesConf.REGISTRO_APELLIDOS);
				String numDocumento = (String) httpServletRequest.getSession()
						.getAttribute(MaoPropiedadesConf.REGISTRO_NUMERO_DOCUMENTO);
				filter.setTxNombre(nombre);
				filter.setTxApellido1(apellidos);
				filter.setTxDocumento(numDocumento);
			}
			
			// Limpiamos el registro clave
			httpServletRequest.getSession().setAttribute(
					MaoPropiedadesConf.REGISTRO_CLAVE, null);
		}
	}
	
	@PostConstruct
	private void init()
	{
		// Cargamos los combos 
		try {
			if (CollectionUtils.isEmpty(paisesList)) {
				paisesList = HTMLUtil.getSelectItemList(
						SelectItemList.createDefault(),
						paisesService.search(new OrPaisesVO()), OrPaisesVO.class,
						"nomPais", "codPais");
			}
			
			if (CollectionUtils.isEmpty(provinciasList)) {
				provinciasList = HTMLUtil.getSelectItemList(
						SelectItemList.createDefault(),
						provinciasService.search(new OrProvinciasVO()),
						OrProvinciasVO.class, "nomProvincia", "codProvincia");
			}
			
			if (CollectionUtils.isEmpty(tipoDocumentosList)) {
				tipoDocumentosList = HTMLUtil.getSelectItemList(
						SelectItemList.createDefault(),
						tiposdocService.search(new MaoTiposdocVO()),
						MaoTiposdocVO.class, "txDescripcion", "codTipodoc");
			}
			
		} catch (Exception exception) {
			FacesUtil.addErrorMessage();
			OepmLogger.error(exception);
		}
		
		// Cargamos el usuario titular/representante
		if ((filter == null || filter.getIdTitrep() == null)
				&& idTitrep != null) {
			
			try {
				filter = usuariosTituRepreService.getById(Long
						.valueOf(idTitrep));
				
				filterAux = usuariosTituRepreService.getById(Long
						.valueOf(idTitrep));
				
			} catch (final Exception e) {
				FacesUtil.addErrorMessage("registrarUsuario.error.getById");
				OepmLogger.error(e);
			}
		}
		
		// Cargamos el agente
		if ((filterAgente == null || filterAgente.getCodAgente() == null)
				&& codAgente != null) {
			try {
				filterAgente = usuariosAgenteService.getById(codAgente);
				
				filterAgenteAux = usuariosAgenteService.getById(codAgente);
			} catch (final Exception e) {
				FacesUtil.addErrorMessage("registrarUsuario.error.getById");
				OepmLogger.error(e);
			}
		}	
	}

	@Override
	public String back() {
		tipoUsuario = "";
		return JSFPages.REGISTRARSE;
	}
	
	public String backToHome() {
		tipoUsuario = "";
		return JSFPages.INICIO_PRIVADO;
	}

	
	public String create() throws BusinessException {
			
		UsuariosTituRepreVO auxUsuario = null;
		
		if (filter.getIdTitrep() != null) {
			auxUsuario = usuariosTituRepreService.getById(filter.getIdTitrep());
		}
		
		// Si el usuario existe, eso quiere decir que estamos actualizando los datos
		if (auxUsuario!= null && auxUsuario.getIdTitrep() != null
			&& auxUsuario.getIdTitrep().toString().equals(SessionContext.getIdUsuario())) {
			
			OepmLogger.info("Actualizando usuario...");		
			
			// No queremos que nos mande otro email.
			usuariosTituRepreService.actualizaUsuario(filter,null, false);
			FacesUtil.addOKMessage("registrarUsuario.info.actualizar");
			return JSFPages.REGISTRARSE;
				
		}
		// El usuario no existe, lo creamos
		else 
		{
			OepmLogger.debug("create - creando Usuario");	
			
			try {
				Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();

				if (getRepresentante()) {
					filter.setBoRepresentante('S');
				} else {
					filter.setBoRepresentante('N');
				}
				filter.setBoMverificado('N');
				// Si es un registro mediante Cl@ve marcamos la cuenta como verificada
				filter.setBoCverificada(registroClave);
				filter.setTipoUsu(tipoUsuario);
				// No se utilizan login y clave por lo que hay que asignarles valor
				filter.setTxLogin(filter.getTxDocumento());
				
				// HCS: MAO-486. En lugar de una clave aleatoria, fijamos
				// que la clave sea igual al login. 
				//String clave = UUID.randomUUID().toString();
				String clave = filter.getTxLogin();
				
				filter.setTxClave(clave);
				filter.setTxClave(passwordEncoder.encodePassword(filter.getTxClave(), null));
				filter = usuariosTituRepreService.create(filter);
				FacesUtil.addOKMessage("registrarUsuario.info.alta");
				

			} catch (final OepmException e) {
				FacesUtil.addErrorMessage("registrarUsuario.error.alta");
				OepmLogger.error(e);
			} catch (final Exception e) {
				FacesUtil.addErrorMessage("registrarUsuario.error.alta");
				OepmLogger.error(e);
			}
			OepmLogger.debug("create - fin");
			
		}	
	
		filter= null;
		return JSFPages.LOGIN;

	}

	@Override
	public void save(ActionEvent param) {
		OepmLogger.debug("save - guardando Usuario");
		try {
			if (getAgente()) {
				
				if (emailModificado()) {
					filterAgente.setBoBloqueocuenta(true);
				}
				
				usuariosAgenteService.actualizaUsuario(filterAgente,filterAgenteAux.getEmail());

				if (emailModificado()) {
					SecurityContextHolder.getContext().setAuthentication(null);
					FacesUtil.addOKMessage("registrarUsuario.info.actualizarConf");
				} else {
					FacesUtil.addOKMessage("registrarUsuario.info.actualizar");
				}
				
				if(emailModificado() ){
					String context= FacesContext.getCurrentInstance().getExternalContext().getContextName();
					FacesContext.getCurrentInstance().getExternalContext().redirect("/"+context+ JSFPages.LOGIN + ".xhtml");
				}
			}
		} catch (final OepmException e) {
			FacesUtil.addErrorMessage("registrarUsuario.error.actualizar");
			OepmLogger.error(e);
		} catch (final Exception e) {
			FacesUtil.addErrorMessage("registrarUsuario.error.actualizar");
			OepmLogger.error(e);
		}
		OepmLogger.debug("save - fin");
	}

	/*
	 * Metodo que bloquea la cuenta del usuario.
	 */
	public String desactivarCta() {
		String pag = "";
		try {
			if (getAgente()) {
				filterAgenteAux.setBoBloqueocuenta(true);
				usuariosAgenteService.update(filterAgenteAux);
			} else {
				filterAux.setBoBloqueocuenta(true);
				usuariosTituRepreService.update(filterAux);
			}
			pag = JSFPages.LOGIN_OUT + "?faces-redirect=true";
		} catch (final OepmException e) {
			FacesUtil.addErrorMessage("registrarUsuario.error.actualizar");
			OepmLogger.error(e);
		} catch (final Exception e) {
			FacesUtil.addErrorMessage("registrarUsuario.error.actualizar");
			OepmLogger.error(e);
		}
		OepmLogger.debug("save - fin");
		return pag;
	}

	/******************************* METODOS AUXILIARES ************************/

	private String obtenerTipo(String tipoUsuario) {
		String tipo = "";
		if (tipoUsuario.equals(TipoUsuario.AGENTE.name())) {
			tipo = "A";
		} else if (tipoUsuario.equals(TipoUsuario.TITULAR.name())) {
			tipo = "T";
		} else if (tipoUsuario.equals(TipoUsuario.REPRESENTANTE.name())) {
			tipo = "R";
		} else if (TipoUsuario.GESTOR.name().equals(tipoUsuario)) {
			tipo = "G";
		}
		return tipo;
	}

	/*
	 * Indica si el usuario esta ya logado en el sistema.
	 */
	private boolean isUsuarioLogado() {
		return SecurityContextHolder.getContext().getAuthentication()
				.isAuthenticated()
				&& SecurityContextHolder.getContext().getAuthentication()
						.getPrincipal() != null
				&& !"anonymousUser".equalsIgnoreCase(SecurityContextHolder
						.getContext().getAuthentication().getPrincipal()
						.toString());
	}


	/*
	 * Indica si el usuario ha modificado el email en sus datos personales.
	 */
	public boolean emailModificado() {
		if (getAgente()) {
			return !(filterAgenteAux.getEmail().equals(filterAgente.getEmail()));
		} else {
			return !(filterAux.getTxEmail().equals(filter.getTxEmail()));
		}
	}

	/*
	 * Indica si el tipo de usuario es "Representante".
	 */
	public boolean getRepresentante() {
		boolean res = false;
		if (tipoUsuario.equals("R")) {
			res = true;
		}
		return res;
	}

	/*
	 * Indica si el tipo de usuario es "Titular".
	 */
	public boolean getTitular() {
		boolean res = false;
		if (tipoUsuario.equals("T")) {
			res = true;
		}
		return res;
	}

	/*
	 * Indica si el tipo de usuario es "Agente".
	 */
	public boolean getAgente() {
		boolean res = false;
		if (tipoUsuario.equals("A")) {
			res = true;
		}
		return res;
	}
	
	/*
	 * Indica si el tipo de usuario es "Gestor".
	 */
	public boolean getGestor() {
		boolean res = false;
		if (tipoUsuario.equals("G")) {
			res = true;
		}
		return res;
	}

	public boolean getTituRepre() {
		boolean res = false;
		if (tipoUsuario.equals("T") || tipoUsuario.equals("R")) {
			res = true;
		}
		return res;
	}

	public boolean getTipoUsuarioRelleno() {
		return !StringUtils.isEmptyOrNull(tipoUsuario);
	}

	/*
	 * Comprueba que el tipo de usuario esta relleno y el usuario no sea un
	 * Agente
	 */
	public boolean getRellenoNoAgente() {
		return !StringUtils.isEmptyOrNull(tipoUsuario) && !getAgente();
	}

	/************************ LISTAS DE TIPOS ***********************************/

	/**
	 * Muestra/carga la lista de tipos de usuarios
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<SelectItem> getTiposUsuarios() {
		TipoUsuariosRegistro[] tiposUsuarios = TipoUsuariosRegistro.values();

		List<TipoUsuariosRegistro> listaTiposUsuarios = (List<TipoUsuariosRegistro>) CollectionUtils
				.arrayToList(tiposUsuarios);

		return HTMLUtil.getSelectItemList(SelectItemList.createDefault(),
				listaTiposUsuarios, TipoUsuariosRegistro.class, "textoTipo",
				"tipo");
	}

	/**
	 * Metodo que permite obtener el listado de las provincias
	 * 
	 * @return Listado.
	 */
	public List<SelectItem> getProvinciasList() {
		return provinciasList;

	}

	/**
	 * Metodo que permite obtener el listado de los paises
	 * 
	 * @return Listado.
	 */
	public List<SelectItem> getPaisesList() {
		return paisesList;

	}

	/**
	 * Metodo que permite obtener el listado de tipos de documentos
	 * 
	 * @return Listado.
	 */
	public List<SelectItem> getTiposDocumentosList() {
		return tipoDocumentosList;
	}
	
	public UsuarioGestorVO getGestorVO() throws BusinessException {
		UsuarioGestorVO gestor = new UsuarioGestorVO();
		List<UsuarioGestorVO> list = new ArrayList<UsuarioGestorVO>();
		
		gestor.setDocumento(SessionContext.getDocumento());
		try {
			list = usuarioGestorService.search(gestor);
			
			if (list != null && list.size() > 0 && list.get(0) != null) {
				gestorVO = list.get(0);
			} else {
				FacesUtil.addErrorMessage( "mantUsuariosGestor.error.getDataGestor" );
			}
		} catch (BusinessException e) {
			FacesUtil.addErrorMessage( "mantUsuariosGestor.error.getDataGestor" );
			return null;
		}
		return gestorVO;
	}

	/****************************** metodos GET/SET ******************************/

	@Override
	public UsuariosTituRepreVO getFilter() {
		return filter;
	}

	@Override
	public void setFilter(BaseVO filter) {
		this.filter = (UsuariosTituRepreVO) filter;
	}

	public UsuariosTituRepreService getUsuariosTituRepreService() {
		return usuariosTituRepreService;
	}

	public void setUsuariosTituRepreService(
			UsuariosTituRepreService usuariosTituRepreService) {
		this.usuariosTituRepreService = usuariosTituRepreService;
	}

	public ProvinciasService getProvinciasService() {
		return provinciasService;
	}

	public void setProvinciasService(ProvinciasService provinciasService) {
		this.provinciasService = provinciasService;
	}

	public PaisesService getPaisesService() {
		return paisesService;
	}

	public void setPaisesService(PaisesService paisesService) {
		this.paisesService = paisesService;
	}

	public TiposdocService getTiposdocService() {
		return tiposdocService;
	}

	public void setTiposdocService(TiposdocService tiposdocService) {
		this.tiposdocService = tiposdocService;
	}

	public String getTipoUsuario() {
		return tipoUsuario;
	}

	public void setTipoUsuario(String tipoUsuario) {
		this.tipoUsuario = tipoUsuario;
	}

	public UsuariosAgenteVO getFilterAgente() {
		return filterAgente;
	}

	public void setFilterAgente(UsuariosAgenteVO filterAgente) {
		this.filterAgente = filterAgente;
	}

	public boolean getEdicion() {
		return edicion;
	}

	public void setEdicion(boolean edicion) {
		this.edicion = edicion;
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

	public UsuariosAgenteService getUsuariosAgenteService() {
		return usuariosAgenteService;
	}

	public void setUsuariosAgenteService(
			UsuariosAgenteService usuariosAgenteService) {
		this.usuariosAgenteService = usuariosAgenteService;
	}

	public UsuariosTituRepreVO getFilterAux() {
		return filterAux;
	}

	public void setFilterAux(UsuariosTituRepreVO filterAux) {
		this.filterAux = filterAux;
	}

	public UsuariosAgenteVO getFilterAgenteAux() {
		return filterAgenteAux;
	}

	public void setFilterAgenteAux(UsuariosAgenteVO filterAgenteAux) {
		this.filterAgenteAux = filterAgenteAux;
	}

	public UsuAsociadosService getUsuAsociadosService() {
		return usuAsociadosService;
	}

	public void setUsuAsociadosService(UsuAsociadosService usuAsociadosService) {
		this.usuAsociadosService = usuAsociadosService;
	}

	public Boolean getRegistroClave() {
		return registroClave;
	}

	public Boolean getCampoRequired() {
		return Boolean.TRUE;
	}
	
	public Boolean getCampoNoRequired() {
		return Boolean.FALSE;
	}

	/**
	 * @param usuarioGestorService the usuarioGestorService to set
	 */
	public void setUsuarioGestorService(UsuarioGestorService usuarioGestorService) {
		this.usuarioGestorService = usuarioGestorService;
	}
}
