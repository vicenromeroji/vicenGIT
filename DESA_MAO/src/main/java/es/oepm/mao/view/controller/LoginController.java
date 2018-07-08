package es.oepm.mao.view.controller;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.core.business.BaseVO;
import es.oepm.core.business.mao.vo.SesionesVO;
import es.oepm.core.business.vo.LoginVO;
import es.oepm.core.exceptions.ExceptionErrorCode;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.business.service.LoginService;
import es.oepm.mao.business.vo.UsuarioLogado;
import es.oepm.mao.comun.business.service.SesionesService;
import es.oepm.mao.constants.MaoPropiedadesConf;
import es.oepm.mao.view.controller.util.JSFPages;

/**
 * The Class BaseController
 * 
 * @author AYESA AT
 */
@ManagedBean(name = "loginController")
@ViewScoped
public class LoginController extends BaseController {
	
	private static final long serialVersionUID = -1716040770638714634L;

	/** The user service. */
	@ManagedProperty(name = "loginService", value = "#{loginService}")
	private LoginService loginService;
	
	@ManagedProperty(name = "sesionesService", value = "#{sesionesService}")
	private SesionesService sesionesService;
	
	@ManagedProperty(name = "sessionController", value = "#{sessionController}")
	private SessionController sessionController;

	private LoginVO loginVO = new LoginVO();
	private Md5PasswordEncoder passwordEncoder = null;
	
	/**
	 * Permite el acceso con Usuario y Contraseña
	 */
	private Boolean accesoConUsuario = Configuracion.getPropertyAsBoolean(MaoPropiedadesConf.ACCESO_CON_USUARIO);

	/**
	 * Constructor por defecto de la clase.
	 */
	public LoginController() {
		super();
		
		if (passwordEncoder == null) {
			passwordEncoder = new Md5PasswordEncoder();
		}
	}
	
	@PostConstruct
	public void comprobarAcceso () {
		try {
			if (SecurityContextHolder.getContext()==null 
					|| (SecurityContextHolder.getContext().getAuthentication()!=null 
						&& !SecurityContextHolder.getContext().getAuthentication().isAuthenticated())
					|| StringUtils.isEmptyOrNull(SessionContext.getNombreUsuario())) {
				doCookieLogin ();
			}
		} catch (Exception ignored) {}
	}
	
	/**
	 * Login mediante usuario y clave
	 * @return String
	 */
	public String doLogin() {
		OepmLogger.debug( "------ doLogin(" + loginVO + ") [INICIO]" );

		try {
			//Codificar la clave
			loginVO.setClave( passwordEncoder.encodePassword( loginVO.getClave(), null ) );
			
			//login con usuario y clave
			final UsuarioLogado user = loginService.login( loginVO );
			
			// Si encontramo usuario valido
			if( user != null ) {
				OepmLogger.info( "------ login MAO ------- " + loginVO.getUsuario() );
				/**
				 * MAO-256 
				 */
				
				String  cookie = System.currentTimeMillis() + "-"
						+ RandomStringUtils.randomAlphabetic(10);
				
				SesionesVO sesion = sesionesService.getById(loginVO.getUsuario());
				if (sesion==null) {
					sesion = new SesionesVO();
				}
				sesion.setUltimoAcceso(new Date());
				if (cookie != null) {
					if (StringUtils.isEmptyOrNull(sesion.getMantenerLogado())) {
						sesion.setMantenerLogado(cookie);
					} else {
						cookie = sesion.getMantenerLogado();						
						sesion.setMantenerLogado(cookie);
					}
				}
				sesion.setLoginUsuario(loginVO.getUsuario());
				sesionesService.activarSesion(sesion);
				/******/
				// Iniciaizamos los datos del controler de sesion
				sessionController.loadUserFromSessionContext();
				
				//Si la cuenta no esta verificado mostramos un warning
				////COMENTAMOS EL CODIGO PARA QUE NO APAREZCA EL MENSAJE
				/*if (!SessionContext.getUserDetails().getAuthorities().contains(new SimpleGrantedAuthority(Roles.ROLE_CUENTA_VERIFICADA))) {
					FacesUtil.addWarningMessage("warning.cuentaNoVerificada");
				}*/
			} else {
				// Añadimos el acceso incorrecto
				Integer numIntentos = loginService.createIncorrectAcces(loginVO.getUsuario());
				if (numIntentos != -1) {
					if (numIntentos > 4) {
						String error = ExceptionUtil.getMessage(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
						FacesUtil.addErrorMessageText( error );
					} else {
						FacesUtil.addWarningMessageText("Quedan: " + (5 - numIntentos) + " intentos.");
					}
				}
				
				loginVO.setClave( "" );
				FacesUtil.addErrorMessage( "AbstractUserDetailsAuthenticationProvider.badCredentials" );
				return JSFPages.LOGIN;
			}
		} catch( final Exception e ) {
			loginVO.setClave( "" );
			FacesUtil.addErrorMessage( "login.error", e);
			FacesUtil.getSessionObject().invalidate();
			
			return JSFPages.LOGIN;
		}

		OepmLogger.debug( "------ doLogin [FIN]" );
		
		return JSFPages.INICIO_PRIVADO;
	}
	
	/**
	 * Redirige al usuario al login con clave
	 * @return
	 */
	public String redireccionarLoginAClave() {
		// Seteamos los atributos en la sesion de JSF ya que no hay todavia usuario logado
		HttpServletRequest httpServletRequest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        
		// Seteamos el atributo de sesion para que se realice la redirecion a clave
		httpServletRequest.getSession().setAttribute(MaoPropiedadesConf.CLAVE_REALIZAR_LLAMADA, Boolean.TRUE.toString());
		
		// Seteamos el atributo que indica que realizamos la llamada desde la pantalla de login
		httpServletRequest.getSession().setAttribute(MaoPropiedadesConf.CLAVE_URL_LLAMADA, MaoPropiedadesConf.CLAVE_URL_LLAMADA_LOGIN);
		
		// Redirigimos a la pantalla que realiza la integracion con la pasarela de Cl@ve
		return JSFPages.PASARELA_CLAVE;
	}
	
	/**
	 * Redirige al usuario al login con clave para registrar un nuevo usuario
	 * @return
	 */
	public String redireccionarRegistroAClave() {
		// Seteamos los atributos en la sesion de JSF ya que no hay todavia usuario logado
		HttpServletRequest httpServletRequest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        
		// Seteamos el atributo de sesion para que se realice la redirecion a clave
		httpServletRequest.getSession().setAttribute(MaoPropiedadesConf.CLAVE_REALIZAR_LLAMADA, Boolean.TRUE.toString());
		
		// Seteamos el atributo que indica que realizamos la llamada desde la pantalla de login
		httpServletRequest.getSession().setAttribute(MaoPropiedadesConf.CLAVE_URL_LLAMADA, MaoPropiedadesConf.CLAVE_URL_LLAMADA_REGISTRO);
		
		// Redirigimos a la pantalla que realiza la integracion con la pasarela de Cl@ve
		return JSFPages.PASARELA_CLAVE;
	}
	
	/**
	 * Login automatico mediante cookie de sesion encriptada
	 * 
	 * @return String
	 */
	public void doCookieLogin() {
		OepmLogger.debug( "------ doCookieLogin() [INICIO]" );

		try {			
			//Login con cooki si existe
			String user = null;
			
			if (FacesUtil.getRequestObject()!=null) {
				String userAgent = FacesUtil.getRequestObject().getHeader("user-agent");
				
				Cookie [] cookies = FacesUtil.getRequestObject().getCookies();
				
				for (Cookie cookie : cookies) {
					//Comprobamos si existe la cookie
					if (cookie.getName().equals(SesionesService.COOKIE_MAO)) {
						//Comprobamos que es correcta
						String cookieValue = cookie.getValue();
						
						user = sesionesService.loginWithCookie(userAgent, cookieValue);
						break;
					}
				}
			}
			
			// Si encontramo usuario valido
			if( user != null ) {
				// Logeamos al usuario
				UsuarioLogado usuario = loginService.findByLogin(user);
				OepmLogger.info( "------ login Cookie MAO ------- " + user );
				
				// Logeado de forma correcta
				if (usuario != null) {
					FacesUtil.getResponseObject().sendRedirect(FacesUtil.getRequestObject().getRequestURL().toString());
					FacesUtil.getResponseObject().flushBuffer();
					// Iniciaizamos los datos del controler de sesion
					sessionController.loadUserFromSessionContext();
				}
			} 
		} catch( final Exception e ) {
			if (ExceptionUtil.isErrorControlado(e)) {
				FacesUtil.addErrorMessageText(ExceptionUtil.getMessage(e), e);
			}
			FacesUtil.getSessionObject().invalidate();
		}

		OepmLogger.debug( "------ doCookieLogin [FIN]" );
	}
	
	/**
	 * Redirige al usuario a la pantalla de registro.
	 * @return
	 */
	public String doRegister() {
		return JSFPages.REGISTRARSE;
	}

	public LoginVO getLoginVO() {
		return loginVO;
	}

	public void setLoginVO(LoginVO loginVO) {
		this.loginVO = loginVO;
	}

	@Override
	public BaseVO getFilter() {
		return null;
	}

	@Override
	public void setFilter(BaseVO filter) {
	}

	public Md5PasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}

	public void setPasswordEncoder(Md5PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public void setLoginService(LoginService loginService) {
		this.loginService = loginService;
	}

	public void setSesionesService(SesionesService sesionesService) {
		this.sesionesService = sesionesService;
	}
	
	public void setSessionController(SessionController sessionController) {
		this.sessionController = sessionController;
	}

	public String getUrlLlamadaClave() {
		return MaoPropiedadesConf.CLAVE_URL_LLAMADA_LOGIN;
	}

	public Boolean getAccesoConUsuario() {
		return accesoConUsuario;
	}
}
