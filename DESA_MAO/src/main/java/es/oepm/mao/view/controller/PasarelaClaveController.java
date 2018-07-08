package es.oepm.mao.view.controller;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.core.logger.OepmLogger;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.business.service.LoginService;
import es.oepm.mao.comun.business.service.UsuariosService;
import es.oepm.mao.constants.MaoPropiedadesConf;
import es.oepm.mao.view.controller.util.JSFPages;
import es.oepm.maoceo.comun.view.controller.IPasarelaClaveController;

/**
 * 
 * Controller de la pasarela a cl@ve
 *
 */
@ManagedBean( name = "pasarelaClaveController" )
@ViewScoped
public class PasarelaClaveController implements IPasarelaClaveController {
	
	private static final long serialVersionUID = 7147866092001343834L;

	@ManagedProperty(name = "loginService", value = "#{loginService}")
	private LoginService loginService;
	
	@ManagedProperty(name = "usuariosService", value = "#{usuariosService}")
	private UsuariosService usuariosService;
	
	@ManagedProperty(name = "sessionController", value = "#{sessionController}")
	private SessionController sessionController;
	
	// Parametros de base de datos para la llamada a clave
	private String urlLoginClave;
	private String urlRetorno;
	private String aplicacion;
	private String nqaa;
	private String codigoApp;
	private String idpAfirma;
	private String idpStork;
	private String idpAEAT;
	private String idpGISS;
	
	// Parametros internos de sesion
	private Boolean realizarLlamadaClave;
	private String urlLlamadaClave;
	
	// Parametros para redireccionar o mostrar error
	private String urlRedireccionKO;
	private String urlRedireccionOK;
	private Boolean resultadoOk;
	private String mensajeOk;
	private Boolean documentoOk;
	
	// Parametros con la informacion proporcionada por Cl@ve
	private String nombreClave;
	private String apellidosClave;
	private String numDocumentoClave;
	private String mensajeInfoClave;
	
	/**
	 * Constructor de la clase
	 */
	public PasarelaClaveController() {
		super();
	}
	
	/**
	 * Carga los datos del controller
	 */
	@PostConstruct
	public void init() {
		// Recogemos los parametros de la sesion de JSF por si el usuario no esta logueado
		HttpServletRequest httpServletRequest = (HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();
		realizarLlamadaClave = Boolean.valueOf((String) httpServletRequest
				.getSession().getAttribute(MaoPropiedadesConf.CLAVE_REALIZAR_LLAMADA));
		urlLlamadaClave = (String) httpServletRequest.getSession()
				.getAttribute(MaoPropiedadesConf.CLAVE_URL_LLAMADA);
		
		// Seteamos las rutas de vuelta y redireccion
		if (urlLlamadaClave.equals(MaoPropiedadesConf.CLAVE_URL_LLAMADA_LOGIN)) {
			urlRedireccionKO = JSFPages.LOGIN;
			urlRedireccionOK = JSFPages.INICIO_PRIVADO;
		} else if(urlLlamadaClave.equals(MaoPropiedadesConf.CLAVE_URL_LLAMADA_VERIFICAR)) {
			urlRedireccionKO = JSFPages.VERIFICAR_CUENTA;
			urlRedireccionOK = JSFPages.INICIO_PRIVADO;
		} else if(urlLlamadaClave.equals(MaoPropiedadesConf.CLAVE_URL_LLAMADA_REGISTRO)) {
			urlRedireccionKO = JSFPages.LOGIN;
			urlRedireccionOK = JSFPages.REGISTRARSE;
		} 
		
		// Seteamos el parametro de llamada a false en sesion para que no se realice otra llamada
		httpServletRequest.getSession().setAttribute(
				MaoPropiedadesConf.CLAVE_REALIZAR_LLAMADA,
				Boolean.FALSE.toString());
		
		// Comprobamos si venimos del exterior de la pasarela de clave o de la aplicación
		if (realizarLlamadaClave) {
			// Recogemos los parametros de configuracion para realizar la llamada a clave
			urlLoginClave = Configuracion.getPropertyAsString(MaoPropiedadesConf.CLAVE_URL_LOGIN);
			urlRetorno = Configuracion.getPropertyAsString(MaoPropiedadesConf.CLAVE_URL_RETORNO);
			//urlRetorno = "http://localhost:8181/mio/jsp/login/pasarelaClave.xhtml";
			aplicacion = Configuracion.getPropertyAsString(MaoPropiedadesConf.CLAVE_APLICACION);
			nqaa = Configuracion.getPropertyAsString(MaoPropiedadesConf.CLAVE_NQAA);
			codigoApp = Configuracion.getPropertyAsString(MaoPropiedadesConf.CLAVE_CODIGO_APP);
			idpAfirma = Configuracion.getPropertyAsString(MaoPropiedadesConf.CLAVE_IDP_AFIRMA);
			idpStork = Configuracion.getPropertyAsString(MaoPropiedadesConf.CLAVE_IDP_STORK);
			idpAEAT = Configuracion.getPropertyAsString(MaoPropiedadesConf.CLAVE_IDP_AEAT);
			idpGISS = Configuracion.getPropertyAsString(MaoPropiedadesConf.CLAVE_IDP_GISS);
		} else {
			// Recogemos los paramteros
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			nombreClave = (String) externalContext.getRequestParameterMap().get("nombre");
			mensajeInfoClave = (String) externalContext.getRequestParameterMap().get("mensajeInfo");
			apellidosClave = (String) externalContext.getRequestParameterMap().get("apellidos");
			String eid = (String) externalContext.getRequestParameterMap().get("eid");
			if (!StringUtils.isEmptyOrNull(eid)) {
				String[] id = eid.split("/");
				numDocumentoClave = id[id.length-1];
			}
			
			// Comprobamos si se ha recuperado el numero de documento
			if (!StringUtils.isEmptyOrNull(numDocumentoClave)) {
				// Comprobamos desde donde se realiza la llamada a clave 
				if (urlLlamadaClave.equals(MaoPropiedadesConf.CLAVE_URL_LLAMADA_LOGIN)) {
					// Comprobamos si el usuario existe en bbdd y si es asi lo seteamos en caso contrario mostramos el error
					this.logearUsuarioPasarela(numDocumentoClave);
				} else if(urlLlamadaClave.equals(MaoPropiedadesConf.CLAVE_URL_LLAMADA_VERIFICAR)) {
					// Verificamos la cuenta
					this.verificarCuentaPasarela(numDocumentoClave);
				} else if(urlLlamadaClave.equals(MaoPropiedadesConf.CLAVE_URL_LLAMADA_REGISTRO)) {
					//Comprobamos si el número de documento ya existe en BBDD
					try{
						documentoOk = usuariosService.validateNumDocumentoMAOUnico(numDocumentoClave);						
						
						if(documentoOk){
							// Añadimos los datos a la sesión de JSF para pasarlos a la pantalla de registro
							httpServletRequest.getSession()
							.setAttribute(MaoPropiedadesConf.REGISTRO_NUMERO_DOCUMENTO, numDocumentoClave);
							httpServletRequest.getSession()
							.setAttribute(MaoPropiedadesConf.REGISTRO_NOMBRE, nombreClave);
							httpServletRequest.getSession()
							.setAttribute(MaoPropiedadesConf.REGISTRO_APELLIDOS, apellidosClave);
							httpServletRequest.getSession()
							.setAttribute(MaoPropiedadesConf.REGISTRO_CLAVE, Boolean.TRUE.toString());
							resultadoOk = Boolean.TRUE;
							}else{
								FacesUtil.addErrorMessage("pasarela_clave.error.usuario.registrado");
								resultadoOk = Boolean.FALSE;
						}
					} catch( final Exception e ) {
						resultadoOk = Boolean.FALSE;
						FacesUtil.addErrorMessage( "login.error", e);
						FacesUtil.getSessionObject().invalidate();
						// Restauramos el objeto de la sesión
						httpServletRequest = (HttpServletRequest) FacesContext
								.getCurrentInstance().getExternalContext().getRequest();
						httpServletRequest.getSession()
						.setAttribute(MaoPropiedadesConf.CLAVE_URL_LLAMADA, urlLlamadaClave);
					}
	
				}
			} else {
				if (!StringUtils.isEmptyOrNull(mensajeInfoClave)) {
					FacesUtil.addErrorMessageText(mensajeInfoClave);
				}
				FacesUtil.addErrorMessage("pasarela_clave.error");
				resultadoOk = Boolean.FALSE;
			}
		}
		
		OepmLogger.info("Enviamos al usuario al login con clave");
	}
	
	/**
	 * Logea al usuario mediante el num de documento recuperado de la pasarela Cl@ve
	 * 
	 * @param numeroDocumento Num de documento recuperado de la pasarela
	 */
	private void logearUsuarioPasarela(String numeroDocumento) {
		OepmLogger.debug( "------ cargarUsuarioPasarela(" + numeroDocumento + ") [INICIO]" );

		try {
			// Login por número de documento obtenido mediante Cl@ve
			loginService.loginByNumeroDocumento(numeroDocumento);
			// Iniciaizamos los datos del controler de sesion
			sessionController.loadUserFromSessionContext();
			resultadoOk = Boolean.TRUE;
		} catch( final Exception e ) {
			resultadoOk = Boolean.FALSE;
			FacesUtil.addErrorMessage( "login.error", e);
			FacesUtil.getSessionObject().invalidate();
			// Restauramos el objeto de la sesión
			HttpServletRequest httpServletRequest = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			httpServletRequest.getSession()
			.setAttribute(MaoPropiedadesConf.CLAVE_URL_LLAMADA, urlLlamadaClave);
		}

		OepmLogger.debug( "------ cargarUsuarioPasarela [FIN]" );
	}
	
	/**
	 * Logea al usuario mediante el num de documento recuperado de la pasarela Cl@ve
	 * 
	 * @param numeroDocumento Num de documento recuperado de la pasarela
	 */
	private void verificarCuentaPasarela(String numeroDocumento) {
		OepmLogger.debug( "------ cargarUsuarioPasarela(" + numeroDocumento + ") [INICIO]" );

		try {
			// Verificamos la cuenta
			loginService.verificarCuentaUsuarioLogado(numeroDocumento);
			resultadoOk = Boolean.TRUE;
		} catch( final Exception e ) {
			resultadoOk = Boolean.FALSE;
			FacesUtil.addErrorMessage( "verificarCuenta.error", e);
			FacesUtil.getSessionObject().invalidate();
			// Restauramos el objeto de la sesión
			HttpServletRequest httpServletRequest = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			httpServletRequest.getSession()
			.setAttribute(MaoPropiedadesConf.CLAVE_URL_LLAMADA, urlLlamadaClave);
		}

		OepmLogger.debug( "------ cargarUsuarioPasarela [FIN]" );
	}

	public void setLoginService(LoginService loginService) {
		this.loginService = loginService;
	}

	public void setUsuariosService(UsuariosService usuariosService) {
		this.usuariosService = usuariosService;
	}
	
	public void setSessionController(SessionController sessionController) {
		this.sessionController = sessionController;
	}

	public String getUrlLoginClave() {
		return urlLoginClave;
	}

	public String getUrlRetorno() {
		return urlRetorno;
	}

	public String getAplicacion() {
		return aplicacion;
	}

	public String getNqaa() {
		return nqaa;
	}

	public String getCodigoApp() {
		return codigoApp;
	}

	public String getIdpAfirma() {
		return idpAfirma;
	}

	public String getIdpStork() {
		return idpStork;
	}

	public String getIdpAEAT() {
		return idpAEAT;
	}

	public String getIdpGISS() {
		return idpGISS;
	}

	public Boolean getRealizarLlamadaClave() {
		return realizarLlamadaClave;
	}

	public String getUrlRedireccionKO() {
		return urlRedireccionKO;
	}

	public String getUrlRedireccionOK() {
		return urlRedireccionOK;
	}

	public Boolean getResultadoOk() {
		return resultadoOk;
	}

	public String getMensajeOk() {
		return mensajeOk;
	}
	
	public void setMensajeOk(String mensajeOk) {
		this.mensajeOk = mensajeOk;
	}

	public String getNombreClave() {
		return nombreClave;
	}

	public String getApellidosClave() {
		return apellidosClave;
	}

	public String getNumDocumentoClave() {
		return numDocumentoClave;
	}

	public String getMensajeInfoClave() {
		return mensajeInfoClave;
	}

}