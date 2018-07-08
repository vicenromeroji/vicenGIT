package es.oepm.mao.view.controller;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import org.springframework.security.core.userdetails.UserDetails;

import es.oepm.core.business.BaseVO;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.spring.ConfigurationConfigProvider;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.controller.BaseController;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.comun.business.service.SesionesService;
import es.oepm.mao.constants.MaoPropiedadesConf;
import es.oepm.maoceo.comun.view.controller.ISessionController;

/**
 * 
 * Session controller.
 *
 */
@ManagedBean(name = "sessionController")
@SessionScoped
public class SessionController extends BaseController implements ISessionController  {
	
	private static final long serialVersionUID = 8495037301047183816L;

	@ManagedProperty(name = "sesionesService", value = "#{sesionesService}")
	private SesionesService sesionesService;
	
	private String usuarioLogado;
	private String tipoUsuario;
	private String usuarioLogadoReal;
	private String nombreParaEnvio;
	private String documentoParaEnvio;
	private String emailParaEnvio;
	private String apellido1ParaEnvio;
	private String apellido2ParaEnvio;
	private String apellidosParaEnvio;
	private String telefonoParaEnvio;
	private String direccionParaEnvio;
	private String cnaeParaEnvio;
	private String pymeParaEnvio;
	private String faxParaEnvio;

	/**
	 * Constructor por defecto de la clase.
	 */
	public SessionController() {
		super();
	}
	
	// --------------------------------------------
	/**
	 * Inicializa los datos de usuario desde SessionContext
	 */
	public void loadUserFromSessionContext() {
		// Cargamos toda la información del usuario
		obtenerUsuarioLogado();
		obtenerTipoUsuario();
		obtenerUsuarioLogadoReal();
		obtenerNombreParaEnvio();
		obtenerDocumentoParaEnvio();
		obtenerEmailParaEnvio();
		obtenerApellido1ParaEnvio();
		obtenerApellido2ParaEnvio();
		obtenerApellidosParaEnvio();
		obtenerTelefonoParaEnvio();
		obtenerDireccionParaEnvio();
		obtenerCnaeParaEnvio();
		obtenerPymeParaEnvio();
		obtenerFaxParaEnvio();
	}
	
	/**
	 * Obtiene el nombre del usuario logado
	 */
	private void obtenerUsuarioLogado() {
		OepmLogger.debug("-------------getUsuarioLogado [INICIO]");

		try {
			usuarioLogado = SessionContext.getNombreUsuario();
			
			if (StringUtils.isEmptyOrNull(usuarioLogado)) {
				final UserDetails obj = SessionContext.getUserDetails();
	
				usuarioLogado = obj.getUsername();
				OepmLogger.debug("-->usuarioLogado-->" + usuarioLogado);
			}
			
			usuarioLogado = usuarioLogado.toUpperCase();

		} catch (final Exception e) {
			OepmLogger.error("--->getUsuarioLogado 1 -->" + e.toString(), e);
		}

		OepmLogger.debug("-------------getUsuarioLogado [FIN]");
	}
	
	/**
	 * Obtiene el tipo del usuario logado.
	 * 
	 * @return String
	 */
	private void obtenerTipoUsuario() {
		OepmLogger.debug("[INICIO]");

		try {
			tipoUsuario = SessionContext.getTipoUsuario();
		} catch (final Exception e) {
			OepmLogger.error(e);
		}

		OepmLogger.debug("[FIN]");
	}
	
	/**
	 * Obtiene el nombre del usuario creador del autorizado.
	 */
	private void obtenerUsuarioLogadoReal() {
		OepmLogger.debug("-------------getUsuarioLogadoReal [INICIO]");

		try {
			//Se supone que el usuario es un usuario autorizado
			usuarioLogadoReal = SessionContext.getNombreUsuarioReal();
			
			if (usuarioLogadoReal != null) {
				usuarioLogadoReal=usuarioLogadoReal.toUpperCase();
			}
			
		} catch (final Exception e) {
			OepmLogger.error("--->getUsuarioLogadoReal -->" + e.toString(), e);
		}

		OepmLogger.debug("-------------getUsuarioLogadoReal [FIN]");
	}
	
	/**
	 * Obtiene el nombre del usuario que hay que enviar a otras aplicaciones.
	 */
	private void obtenerNombreParaEnvio() {
		OepmLogger.debug("[INICIO]");
		
		try {
			//Se supone que el usuario es un usuario autorizado
			nombreParaEnvio = SessionContext.getValue(SessionContext.NOMBRE_ENVIO);
			nombreParaEnvio = nombreParaEnvio.toUpperCase();

		} catch (final Exception e) {
			OepmLogger.error(e);
		}
		
		OepmLogger.debug("[FIN]");
	}
	
	/**
	 * Obtiene el documento del usuario que hay que enviar a otras aplicaciones.
	 */
	private void obtenerDocumentoParaEnvio() {
		OepmLogger.debug("[INICIO]");

		try {
			documentoParaEnvio = SessionContext.getValue(SessionContext.DOCUMENTO_ENVIO);
		} catch (final Exception e) {
			OepmLogger.error(e);
		}

		OepmLogger.debug("[FIN]");
	}
	
	/**
	 * Obtiene el email del usuario que hay que enviar a otras aplicaciones.
	 */
	private void obtenerEmailParaEnvio() {
		OepmLogger.debug("[INICIO]");

		try {
			emailParaEnvio = SessionContext.getValue(SessionContext.EMAIL_ENVIO);
		} catch (final Exception e) {
			OepmLogger.error(e);
		}

		OepmLogger.debug("[FIN]");
	}
	
	/**
	 * Devuelve el apellido1 del usuario que hay que enviar a otras aplicaciones.
	 */
	private void obtenerApellido1ParaEnvio() {
		OepmLogger.debug("[INICIO]");

		try {
			apellido1ParaEnvio = SessionContext.getValue(SessionContext.APELLIDO1_ENVIO);
		} catch (final Exception e) {
			OepmLogger.error(e);
		}

		OepmLogger.debug("[FIN]");
	}

	/**
	 * Devuelve el apellido2 del usuario que hay que enviar a otras aplicaciones.
	 */
	private void obtenerApellido2ParaEnvio() {
		OepmLogger.debug("[INICIO]");

		try {
			apellido2ParaEnvio = SessionContext.getValue(SessionContext.APELLIDO2_ENVIO);
		} catch (final Exception e) {
			OepmLogger.error(e);
		}

		OepmLogger.debug("[FIN]");
	}
	
	/**
	 * Devuelve los apellidos del usuario que hay que enviar a otras aplicaciones.
	 */
	private void obtenerApellidosParaEnvio() {
		OepmLogger.debug("[INICIO]");

		try {
			apellidosParaEnvio = SessionContext.getValue(SessionContext.APELLIDO1_ENVIO) +" " +SessionContext.getValue(SessionContext.APELLIDO2_ENVIO);
		} catch (final Exception e) {
			OepmLogger.error(e);
		}

		OepmLogger.debug("[FIN]");
	}
	
	/**
	 * Devuelve el telefono del usuario que hay que enviar a otras aplicaciones.
	 */
	private void obtenerTelefonoParaEnvio() {
		OepmLogger.debug("[INICIO]");

		try {
			telefonoParaEnvio = SessionContext.getValue(SessionContext.TELEFONO_ENVIO);
		} catch (final Exception e) {
			OepmLogger.error(e);
		}

		OepmLogger.debug("[FIN]");
	}
	
	/**
	 * Devuelve el direccion del usuario que hay que enviar a otras aplicaciones
	 */
	private void obtenerDireccionParaEnvio() {
		OepmLogger.debug("[INICIO]");

		try {
			direccionParaEnvio = SessionContext.getValue(SessionContext.DIRECCION_ENVIO);
		} catch (final Exception e) {
			OepmLogger.error(e);
		}

		OepmLogger.debug("[FIN]");
	}
	
	/**
	 * Devuelve el CNAE del usuario que hay que enviar a otras aplicaciones.
	 */
	private void obtenerCnaeParaEnvio() {
		OepmLogger.debug("[INICIO]");

		try {
			cnaeParaEnvio = SessionContext.getValue(SessionContext.CNAE_ENVIO);
		} catch (final Exception e) {
			OepmLogger.error(e);
		}
		
		OepmLogger.debug("[FIN]");
	}
	
	/**
	 * Devuelve el indicador de si es PYME el usuario que hay que enviar a otras aplicaciones.
	 */
	private void obtenerPymeParaEnvio() {
		OepmLogger.debug("[INICIO]");

		try {
			pymeParaEnvio = SessionContext.getValue(SessionContext.PYME_ENVIO);
		} catch (final Exception e) {
			OepmLogger.error(e);
		}

		OepmLogger.debug("[FIN]");
	}
	
	/**
	 * Devuelve el fax del usuario que hay que enviar a otras aplicaciones.
	 */
	private void obtenerFaxParaEnvio() {
		OepmLogger.debug("[INICIO]");

		try {
			faxParaEnvio = SessionContext.getValue(SessionContext.FAX_ENVIO);
		} catch (final Exception e) {
			OepmLogger.error(e);
		}
		
		OepmLogger.debug("[FIN]");
	}
	
	/**
	 * Indica si el usuario es un usuario asociado
	 * 
	 * @return boolean
	 */
	public boolean isUsuarioAutorizado() {
		OepmLogger.debug("-------------isUsuarioautorizado [INICIO]");

		Boolean isAutorizado = Boolean.FALSE;
		try {
			if (TipoUsuario.ASOCIADO.name().equals(tipoUsuario)) {
				isAutorizado = Boolean.TRUE;
			}
		} catch (final Exception e) {
			OepmLogger.error("--->isUsuarioautorizado -->" + e.toString());
		}

		OepmLogger.debug("-------------isUsuarioautorizado [FIN]");

		return isAutorizado;
	}
	
	public String getUsuarioLogado() {
		return usuarioLogado;
	}

	public String getTipoUsuario() {
		return tipoUsuario;
	}

	public String getUsuarioLogadoReal() {
		return usuarioLogadoReal;
	}

	public String getNombreParaEnvio() {
		return nombreParaEnvio;
	}

	public String getDocumentoParaEnvio() {
		return documentoParaEnvio;
	}

	public String getEmailParaEnvio() {
		return emailParaEnvio;
	}

	public String getApellido1ParaEnvio() {
		return apellido1ParaEnvio;
	}

	public String getApellido2ParaEnvio() {
		return apellido2ParaEnvio;
	}

	public String getApellidosParaEnvio() {
		return apellidosParaEnvio;
	}

	public String getTelefonoParaEnvio() {
		return telefonoParaEnvio;
	}

	public String getDireccionParaEnvio() {
		return direccionParaEnvio;
	}

	public String getCnaeParaEnvio() {
		return cnaeParaEnvio;
	}

	public String getPymeParaEnvio() {
		return pymeParaEnvio;
	}

	public String getFaxParaEnvio() {
		return faxParaEnvio;
	}

	@Override
	public BaseVO getFilter() {
		return null;
	}

	@Override
	public void setFilter(BaseVO filter) {
	}

	public SesionesService getSesionesService() {
		if (sesionesService == null) {
			sesionesService = (SesionesService)ConfigurationConfigProvider.getCtx().getBean("sesionesService");
		}
		return sesionesService;
	}

	public void setSesionesService(SesionesService sesionesService) {
		this.sesionesService = sesionesService;
	}

	public String getUrlLlamadaClave() {
		return MaoPropiedadesConf.CLAVE_URL_LLAMADA_LOGIN;
	}

}
