package es.oepm.mao.ws;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.jws.HandlerChain;
import javax.jws.WebService;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;


import es.oepm.core.business.mao.vo.SesionesVO;
import es.oepm.core.business.vo.LoginVO;
import es.oepm.core.constants.Mensaje;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.util.StringUtils;
import es.oepm.mao.business.service.LoginService;
import es.oepm.mao.business.vo.UsuarioLogado;
import es.oepm.mao.comun.business.service.SesionesService;
import es.oepm.mao.ws.parameters.LogWithMAORequest;
import es.oepm.mao.ws.parameters.LogWithMAOResponse;
import es.oepm.persistencia.mao.MaoAgentesV;
import es.oepm.persistencia.mao.MaoUsuAsociados;
import es.oepm.persistencia.mao.MaoUsuTitrep;
import es.oepm.wservices.core.BaseWSResponse;
import es.oepm.wservices.core.mensajes.Mensajes;
import es.oepm.wservices.core.util.WSConstants;

@WebService(endpointInterface = "es.oepm.mao.ws.ILogWithMAOWS", targetNamespace = "http://logwithmao.ws.mao.oepm.es/", portName = "LogWithMAOPort", serviceName = "LogWithMAO")
@HandlerChain(file = WSConstants.HANDLER_CHAIN_PATH)
@org.apache.cxf.annotations.Logging
public class LogWithMAOWSImpl implements ILogWithMAOWS {

	private static final long serialVersionUID = -8740989563533109351L;
	
	public static final int RESULTADO_ERROR_NOENCONTRADO = -2;
	public static final int RESULTADO_ERROR_PARAMETROSINCORRECTOS = -3;

	private static Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();

	@Autowired
	private LoginService loginService;

	@Autowired
	private SesionesService sesionesService;

	@PostConstruct
	public void init() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@SuppressWarnings("unused")
	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public LogWithMAOResponse logWithMAO(LogWithMAORequest request) {
		LogWithMAOResponse response = new LogWithMAOResponse(
				BaseWSResponse.ERROR_GENERICO, null);
		response.setCookieName(SesionesService.COOKIE_MAO);
		UsuarioLogado usuarioLogado = null;
		String cookie = null;
		String cookieEncriptada = null;

		try {
			OepmLogger.info("logWithMAO: (" + request.getCookie() + ", "
					+ request.getLogin() + ", ***********)");

			// Login mediante COOKIE
			if (!StringUtils.isEmptyOrNull(request.getCookie())) {
				OepmLogger.debug("logWithMAO: loginWithCookie");
				String user = getSesionesService().loginWithCookie(null,
						request.getCookie());

				// Consulta por cookie
				if (StringUtils.isEmptyOrNull(user)) {
					response.setResultado(RESULTADO_ERROR_NOENCONTRADO);
					response.setMensajes(new Mensajes[] { new Mensajes(
							Mensaje.WS_ERROR_COOCKIE_NO_VALIDA.getCriticidad(),
							Mensaje.WS_ERROR_COOCKIE_NO_VALIDA.getCodigo(),
							Mensaje.WS_ERROR_COOCKIE_NO_VALIDA.getDescripcion()) });
					return response;
				} else { // Consulta del usuario de la cookie
					usuarioLogado = getLoginService().findByLogin(user);
					OepmLogger.info("------ login Cookie MAO ------- " + user);

					if (usuarioLogado == null) {
						OepmLogger
								.debug("logWithMAO: RESULTADO_ERROR_NOENCONTRADO");
						response.setResultado(RESULTADO_ERROR_NOENCONTRADO);
						response.setMensajes(new Mensajes[] { new Mensajes(
								Mensaje.WS_ERROR_COOCKIE_NO_VALIDA.getCriticidad(),
								Mensaje.WS_ERROR_COOCKIE_NO_VALIDA.getCodigo(),
								Mensaje.WS_ERROR_COOCKIE_NO_VALIDA.getDescripcion()) });
						return response;
					}
				}

				// Consulta por usuario y password
			} else if (!StringUtils.isEmptyOrNull(request.getLogin())
					&& !StringUtils.isEmptyOrNull(request.getPassword())) {
				OepmLogger.debug("logWithMAO: login");
				LoginVO loginVO = new LoginVO();
				loginVO.setUsuario(request.getLogin());
				loginVO.setClave(passwordEncoder.encodePassword(
						request.getPassword(), null));

				// login con usuario y clave
				usuarioLogado = getLoginService().login(loginVO);

				// Si encontramo usuario valido
				if (usuarioLogado == null) {
					OepmLogger
							.debug("logWithMAO: RESULTADO_ERROR_NOENCONTRADO");
					response.setResultado(RESULTADO_ERROR_NOENCONTRADO);
					response.setMensajes(new Mensajes[] { new Mensajes(
							Mensaje.WS_ERROR_LOGIN_NO_VALIDO.getCriticidad(),
							Mensaje.WS_ERROR_LOGIN_NO_VALIDO.getCodigo(),
							Mensaje.WS_ERROR_LOGIN_NO_VALIDO.getDescripcion()) });
					return response;
				} else {
					cookie = System.currentTimeMillis() + "-"
							+ RandomStringUtils.randomAlphabetic(10);
					cookieEncriptada = getSesionesService().encriptarCookie(
							cookie);
				}

			} else { // ERROR DE DATOS
				OepmLogger
						.debug("logWithMAO: RESULTADO_ERROR_PARAMETROSINCORRECTOS");
				response.setResultado(RESULTADO_ERROR_PARAMETROSINCORRECTOS);
				response.setMensajes(new Mensajes[] { new Mensajes(
						Mensaje.WS_ERROR_LOGIN_NO_VALIDO.getCriticidad(),
						Mensaje.WS_ERROR_LOGIN_NO_VALIDO.getCodigo(),
						Mensaje.WS_ERROR_LOGIN_NO_VALIDO.getDescripcion()) });
				return response;
			}

			// Usuario logado != null
			OepmLogger
					.debug("logWithMAO: Añadimos o activamos la sesión del usuario en la tabla MAO _SESIONES.");
			SesionesVO sesion = getSesionesService().getById(usuarioLogado.getTxLogin());
			if (sesion==null) {
				sesion = new SesionesVO();
			}
			sesion.setUltimoAcceso(new Date());
			if (cookie != null) {
				if (StringUtils.isEmptyOrNull(sesion.getMantenerLogado())) {
					sesion.setMantenerLogado(cookie);
				} else {
					cookie = sesion.getMantenerLogado();
					cookieEncriptada = getSesionesService().encriptarCookie(
							cookie);
					sesion.setMantenerLogado(cookie);
				}
			}
			sesion.setLoginUsuario(usuarioLogado.getTxLogin());
			getSesionesService().activarSesion(sesion);

			// Añadimos los datos a la respuesta
			OepmLogger.debug("logWithMAO: Creamos la respuesta");
			if (cookieEncriptada != null) {
				response.setCookie(cookieEncriptada);
			} else {
				response.setCookie(request.getCookie());
			}
			// Datos del usuario, segun tipo
			response.setApellido1(usuarioLogado.getTxApellido1());
			response.setApellido2(usuarioLogado.getTxApellido2());
			response.setDocumento(usuarioLogado.getTxDocumento());
			response.setEmail(usuarioLogado.getTxEmail());
			response.setLogin(usuarioLogado.getTxLogin());
			response.setNombre(usuarioLogado.getTxNombre());
			response.setTipoUsuario(usuarioLogado.getTipoUsuario() == null ? null : usuarioLogado.getTipoUsuario().toString());
			
			if (usuarioLogado.getUsuario() instanceof MaoAgentesV) {
				MaoAgentesV agente = (MaoAgentesV) usuarioLogado.getUsuario();
				response.setDomicilio(agente.getDomicilio());
				response.setCodAgente(agente.getCodAgente());
				response.setCodPostal(agente.getCodPostal());
				response.setCodProvincia(agente.getOrProvincias() == null ? null
						: agente.getOrProvincias().getCodProvincia());
				response.setProvincia(agente.getOrProvincias() == null ? null
						: agente.getOrProvincias().getNomProvincia());
				response.setCodPais(agente.getPais());
				response.setPais(agente.getOrPaises() == null ? null : agente
						.getOrPaises().getNomPais());
				
				response.setCuentaBancaria(agente.getCodCuenta());
			} else if (usuarioLogado.getUsuario() instanceof MaoUsuAsociados) {
				MaoUsuAsociados asociado = (MaoUsuAsociados) usuarioLogado
						.getUsuario();

				//Rellenar los datos desde el agente o titular/representante?
				/*if (asociado.getMaoAgentes() != null) {
					MaoAgentes agente = asociado.getMaoAgentes();
					response.setDomicilio(agente.getDomicilio());
					response.setCodAgente(agente.getCodAgente());
					response.setCodPostal(agente.getCodPostal());
					response.setCodProvincia(agente.getOrProvincias() == null ? null
							: agente.getOrProvincias().getCodProvincia());
					response.setProvincia(agente.getOrProvincias() == null ? null
							: agente.getOrProvincias().getNomProvincia());
					response.setCodPais(agente.getCodPais());
					response.setPais(agente.getOrPaises() == null ? null : agente
							.getOrPaises().getNomPais());
					
					response.setCuentaBancaria(agente.getCodCuenta());
				} else if (asociado.getMaoTitrep() != null) {
					MaoUsuTitrep titRep = asociado.getMaoTitrep();
					response.setDomicilio(titRep.getTxDireccion());
					response.setCodPostal(titRep.getTxCodpostal());
					response.setCodProvincia(titRep.getOrProvincias() == null ? null
							: titRep.getOrProvincias().getCodProvincia());
					response.setProvincia(titRep.getOrProvincias() == null ? null
							: titRep.getOrProvincias().getNomProvincia());
					response.setCodPais(titRep.getCodPais());
					response.setPais(titRep.getOrPaises() == null ? null : titRep
							.getOrPaises().getNomPais());
					response.setMunicipio(titRep.getTxPoblacion());
					response.setCuentaBancaria(titRep.getNmNumcuenta());
				}*/
			} else if (usuarioLogado.getUsuario() instanceof MaoUsuTitrep) {
				MaoUsuTitrep titRep = (MaoUsuTitrep) usuarioLogado.getUsuario();
				response.setDomicilio(titRep.getTxDireccion());
				response.setCodPostal(titRep.getTxCodpostal());
				response.setCodProvincia(titRep.getOrProvincias() == null ? null
						: titRep.getOrProvincias().getCodProvincia());
				response.setProvincia(titRep.getOrProvincias() == null ? null
						: titRep.getOrProvincias().getNomProvincia());
				response.setCodPais(titRep.getCodPais());
				response.setPais(titRep.getOrPaises() == null ? null : titRep
						.getOrPaises().getNomPais());
				response.setMunicipio(titRep.getTxPoblacion());
				response.setCuentaBancaria(titRep.getNmNumcuenta());
			}

			response.setResultado(BaseWSResponse.RESULTADO_OK);

		} catch (final Exception e) {
			OepmLogger.error("Error en detalleExpedientews", e);
			if (ExceptionUtil.isErrorControlado(e)) {
				response = new LogWithMAOResponse(
						BaseWSResponse.ERROR_GENERICO, null);
				response.setMensajes(new Mensajes[] { new Mensajes(
						Mensaje.WS_ERROR_GENERICO.getCriticidad(),
						Mensaje.WS_ERROR_GENERICO.getCodigo(),
						Mensaje.WS_ERROR_GENERICO.getDescripcion()) });
			} else {
				response = new LogWithMAOResponse(
						BaseWSResponse.ERROR_GENERICO, null);
				response.setMensajes(new Mensajes[] { new Mensajes(
						Mensaje.WS_ERROR_GENERICO.getCriticidad(),
						Mensaje.WS_ERROR_GENERICO.getCodigo(),
						Mensaje.WS_ERROR_GENERICO.getDescripcion()) });
			}
		}

		return response;
	}

	/**
	 * @return the loginService
	 */
	public LoginService getLoginService() {
		return loginService;
	}

	/**
	 * @param loginService
	 *            the loginService to set
	 */
	public void setLoginService(LoginService loginService) {
		this.loginService = loginService;
	}

	/**
	 * @return the sesionesService
	 */
	public SesionesService getSesionesService() {
		return sesionesService;
	}

	/**
	 * @param sesionesService
	 *            the sesionesService to set
	 */
	public void setSesionesService(SesionesService sesionesService) {
		this.sesionesService = sesionesService;
	}

}