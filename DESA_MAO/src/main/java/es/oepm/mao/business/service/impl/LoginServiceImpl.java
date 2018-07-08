package es.oepm.mao.business.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.core.business.mao.vo.SesionesVO;
import es.oepm.core.business.vo.LoginVO;
import es.oepm.core.constants.Roles;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.exceptions.ExceptionErrorCode;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.business.service.LoginService;
import es.oepm.mao.business.vo.UsuarioLogado;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.comun.business.service.SesionesService;
import es.oepm.mao.comun.business.service.TiposmodalidadService;
import es.oepm.mao.comun.business.service.TipospermisosService;
import es.oepm.mao.constants.MaoPropiedadesConf;
import es.oepm.mao.session.CacheAccesosIncorrectos;
import es.oepm.mao.view.controller.util.MAOConfiguracion;
import es.oepm.persistencia.mao.MaoAgentesV;
import es.oepm.persistencia.mao.MaoGestor;
import es.oepm.persistencia.mao.MaoTiposmodalidad;
import es.oepm.persistencia.mao.MaoTiposperfiles;
import es.oepm.persistencia.mao.MaoTipospermisos;
import es.oepm.persistencia.mao.MaoUsuAsociados;
import es.oepm.persistencia.mao.MaoUsuTitrep;
import es.oepm.persistencia.mao.dao.MaoAgentesVDAO;
import es.oepm.persistencia.mao.dao.MaoGestorDAO;
import es.oepm.persistencia.mao.dao.MaoUsuAsociadosDAO;
import es.oepm.persistencia.mao.dao.MaoUsuTitrepDAO;
import es.oepm.wservices.core.logger.ToStringHelper;

@Service(value = "loginService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class LoginServiceImpl implements LoginService, UserDetailsService {
	
	private static final long serialVersionUID = 1457140984011141515L;

	@Autowired
	private MaoAgentesVDAO maoAgentesVDAO;

	@Autowired
	private MaoUsuAsociadosDAO maoUsuAsociadosDAO;
	@Autowired
	private MaoUsuTitrepDAO maoUsuTitrepDAO;
	@Autowired
	private MaoGestorDAO maoGestorDAO;

	@Autowired
	private SesionesService sesionesService;
	@Autowired
	private TipospermisosService tipospermisosService;
	@Autowired
	private TiposmodalidadService tiposmodalidadService;

	public LoginServiceImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.oepm.mao.business.service.impl.LoginService#login(LoginVO)
	 */
	@Override
	public UsuarioLogado login(final LoginVO login) throws BusinessException {
		UsuarioLogado usuLogado = null;

		try {
			final String txLogin = login.getUsuario();
			final String txClave = login.getClave();
			
			//TODO ELIMINAR PROVISIONAL POR ERROR EN LA PASARELA DE CLAVE
			//FIXME ELIMINAR COMENTARIO CUANDO YA NO SEA NECESARIO
			/*if (txLogin.equals("usuarioGestorSinClave")) {
				// LOGIN GESTORES
				MaoGestor usuarioGestor = maoGestorDAO.findByNumeroDocumento("99999999R");
				if (usuarioGestor != null) {
					// Comprobamos si está bloqueado
					if (usuarioGestor.getBoBloqueocuenta() == 'S') {
						ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
					}
					
					// Comprobamos si está bloqueado por fecha
					if (usuarioGestor.getFhBloqueoauto() != null && usuarioGestor.getFhBloqueoauto().before(new Date())) {
						ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
					}
					
					usuLogado = crearUsuarioLogadoGestor(usuarioGestor);
				}
			}*/

			// LOGIN DE AGENTES
			MaoAgentesV usuarioAgente = maoAgentesVDAO.login(txLogin, txClave);

			
			if (usuarioAgente != null) {
				// MAO-450
				if (!"C".equalsIgnoreCase(usuarioAgente.getSenage()) && 
					!"N".equalsIgnoreCase(usuarioAgente.getSenage()) && 
					!"P".equalsIgnoreCase(usuarioAgente.getSenage()) ) { //No colegiado
					ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_AGENTE_NO_COLEGIADO);
				}
				if (usuarioAgente.getBoBloqueocuenta()!='N') {
					ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
				}
				
				usuLogado = crearUsuarioLogadoAgente(usuarioAgente);

			} else {
				// LOGIN DE ASOCIADOS
				MaoUsuAsociados usuarioAsociado = maoUsuAsociadosDAO.login(txLogin, txClave);

				if (usuarioAsociado != null) {
					if (usuarioAsociado.getMaoTitrep()!=null) {
						if (usuarioAsociado.getMaoTitrep().getBoMverificado()!='S') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CORREO_NO_VERIFICADO);
						}
						if (usuarioAsociado.getMaoTitrep().getBoBloqueocuenta()!='N') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
						}
					} else if (usuarioAsociado.getMaoAgentesV()!=null) {	
						// MAO-450
						if (!"C".equalsIgnoreCase(usuarioAsociado.getMaoAgentesV().getSenage()) && 
							!"N".equalsIgnoreCase(usuarioAsociado.getMaoAgentesV().getSenage()) && 
							!"P".equalsIgnoreCase(usuarioAsociado.getMaoAgentesV().getSenage()) ) { //No colegiado
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_AGENTE_NO_COLEGIADO);
						}
						if (usuarioAsociado.getMaoAgentesV().getBoBloqueocuenta()!='N') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
						}
					}
					
					if (usuarioAsociado.getBoBloqueocuenta()!='N') {
						ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
					}
					usuLogado = crearUsuarioLogadoAsociado(usuarioAsociado);

				} else {
					// LOGIN TITULARES REPRESENTANTES
					MaoUsuTitrep maoUsuTitrep = maoUsuTitrepDAO.login(txLogin,
							txClave);

					if (maoUsuTitrep != null) {
						if (maoUsuTitrep.getBoMverificado()!='S') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CORREO_NO_VERIFICADO);
						}
						if (maoUsuTitrep.getBoBloqueocuenta()!='N') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
						}
						
						usuLogado = crearUsuarioLogadoTitRep(maoUsuTitrep);
					}
				}
			}
			
			// Comprobamos si se ha realizado el login de manera correcta
			if (usuLogado != null) {
				// Si el usuario existe creamos la sesion 
				createUser(usuLogado);
				
				// Creamos la sesion en base de datos
				createDataBaseSesion(usuLogado.getTxLogin());
				
				// Limpiar posibles accesos incorrectos
				CacheAccesosIncorrectos.limpiarAccesosIncorrectos(usuLogado.getTxLogin());
			}
		} catch (final Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}

		return usuLogado;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.LoginService#findByNumeroDocumento(java.lang.String)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void loginByNumeroDocumento(String numeroDocumento) throws BusinessException{
		UsuarioLogado usuLogado = null;
		
		try {
			// LOGIN DE AGENTES
			MaoAgentesV usuarioAgente = maoAgentesVDAO.findByNumeroDocumento(numeroDocumento);
	
			if (usuarioAgente != null) {
				// MAO-450
				if (!"C".equalsIgnoreCase(usuarioAgente.getSenage()) && 
					!"N".equalsIgnoreCase(usuarioAgente.getSenage()) && 
					!"P".equalsIgnoreCase(usuarioAgente.getSenage()) ) { //No colegiado
					ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_AGENTE_NO_COLEGIADO);
				}
				if (usuarioAgente.getBoBloqueocuenta()!='N') {
					ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
				}
				
				usuLogado = crearUsuarioLogadoAgente(usuarioAgente);
	
			} else {
				// LOGIN DE ASOCIADOS
				MaoUsuAsociados usuarioAsociado = maoUsuAsociadosDAO
						.loginByNif(numeroDocumento);
	
				if (usuarioAsociado != null) {
					if (usuarioAsociado.getMaoTitrep()!=null){
						if (usuarioAsociado.getMaoTitrep().getBoMverificado()!='S') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CORREO_NO_VERIFICADO);
						}
						if (usuarioAsociado.getMaoTitrep().getBoBloqueocuenta()!='N') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
						}
					} else if (usuarioAsociado.getMaoAgentesV()!=null) {					
						// MAO-450
						if (!"C".equalsIgnoreCase(usuarioAsociado.getMaoAgentesV().getSenage()) && 
							!"N".equalsIgnoreCase(usuarioAsociado.getMaoAgentesV().getSenage()) && 
							!"P".equalsIgnoreCase(usuarioAsociado.getMaoAgentesV().getSenage()) ) { //No colegiado
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_AGENTE_NO_COLEGIADO);
						}
						if (usuarioAsociado.getMaoAgentesV().getBoBloqueocuenta()!='N') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
						}
					}
					
					if (usuarioAsociado.getBoBloqueocuenta()!='N') {
						ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
					}
					usuLogado = crearUsuarioLogadoAsociado(usuarioAsociado);
	
				} else {
					// LOGIN TITULARES REPRESENTANTES
					MaoUsuTitrep maoUsuTitrep = maoUsuTitrepDAO
							.loginByNif(numeroDocumento);
					
					if (maoUsuTitrep != null) {
						if (maoUsuTitrep.getBoMverificado()!='S') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CORREO_NO_VERIFICADO);
						}
						if (maoUsuTitrep.getBoBloqueocuenta()!='N') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
						}
						
						//Si la cuenta no esta verificada, no puede entrar con certificado
						if (maoUsuTitrep.getBoCverificada() != 'S') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_NO_VERIFICADA_CERTIFICADO);
						}
		
						usuLogado = crearUsuarioLogadoTitRep(maoUsuTitrep);
					}  else {
						// LOGIN GESTORES
						MaoGestor usuarioGestor = maoGestorDAO.findByNumeroDocumento(numeroDocumento);
						if (usuarioGestor != null) {
							// Comprobamos si está bloqueado
							if (usuarioGestor.getBoBloqueocuenta() == 'S') {
								ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
							}
							
							// Comprobamos si está bloqueado por fecha
							if (usuarioGestor.getFhBloqueoauto() != null && usuarioGestor.getFhBloqueoauto().before(new Date())) {
								ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
							}
							
							usuLogado = crearUsuarioLogadoGestor(usuarioGestor);
						}
					}
				}
			}
			
			// Comprobamos si se ha realizado el login de manera correcta
			if (usuLogado != null) {
				// Si el usuario existe creamos la sesion 
				createUser(usuLogado);
				
				// Creamos la sesion en base de datos
				createDataBaseSesion(usuLogado.getTxLogin());
			} else {
				// No existe usuario para el nif proporcionado por clave
				ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_NO_EXISTE_CUENTA_NUMERO_DOCUMENTO);
			}
		} catch (final Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.LoginService#findByLogin(java.lang.String)
	 */
	@Override
	public UsuarioLogado findByLogin(String usuario) throws BusinessException {
		UsuarioLogado usuLogado = null;

		try {
			// LOGIN DE AGENTES
			MaoAgentesV usuarioAgente = maoAgentesVDAO.login(usuario);

			if (usuarioAgente != null) {		
				// MAO-450
				if (!"C".equalsIgnoreCase(usuarioAgente.getSenage()) && 
					!"N".equalsIgnoreCase(usuarioAgente.getSenage()) && 
					!"P".equalsIgnoreCase(usuarioAgente.getSenage()) ) { //No colegiado
					ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_AGENTE_NO_COLEGIADO);
				}
				if (usuarioAgente.getBoBloqueocuenta()!='N') {
					ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
				}
				
				usuLogado = crearUsuarioLogadoAgente(usuarioAgente);

			} else {
				// LOGIN DE ASOCIADOS
				MaoUsuAsociados usuarioAsociado = maoUsuAsociadosDAO
						.login(usuario);

				if (usuarioAsociado != null) {
					if (usuarioAsociado.getMaoTitrep()!=null) {
						if (usuarioAsociado.getMaoTitrep().getBoMverificado()!='S') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CORREO_NO_VERIFICADO);
						}
						if (usuarioAsociado.getMaoTitrep().getBoBloqueocuenta()!='N') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
						}
					} else if (usuarioAsociado.getMaoAgentesV()!=null) {
						// MAO-450
						if (!"C".equalsIgnoreCase(usuarioAsociado.getMaoAgentesV().getSenage()) && 
							!"N".equalsIgnoreCase(usuarioAsociado.getMaoAgentesV().getSenage()) && 
							!"P".equalsIgnoreCase(usuarioAsociado.getMaoAgentesV().getSenage()) ) { //No colegiado
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_AGENTE_NO_COLEGIADO);
						}
						if (usuarioAsociado.getMaoAgentesV().getBoBloqueocuenta()!='N') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
						}
					}
					
					if (usuarioAsociado.getBoBloqueocuenta()!='N') {
						ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
					}
					usuLogado = crearUsuarioLogadoAsociado(usuarioAsociado);

				} else {
					MaoUsuTitrep maoUsuTitrep = maoUsuTitrepDAO.login(usuario);

					if (maoUsuTitrep != null) {
						if (maoUsuTitrep.getBoMverificado()!='S') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CORREO_NO_VERIFICADO);
						}
						if (maoUsuTitrep.getBoBloqueocuenta()!='N') {
							ExceptionUtil.throwBusinessException(ExceptionErrorCode.ERROR_CUENTA_BLOQUEADA);
						}
						
						usuLogado = crearUsuarioLogadoTitRep(maoUsuTitrep);
					}
				}
			}
			
			if (usuLogado != null) {
				//Añadimos o activamos la sesion del usuario en la tabla MAO _SESIONES.
				createDataBaseSesion(usuario);
			}

		} catch (final Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}

		return usuLogado;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.LoginService#verificarCuentaConCertificado(java.lang.String)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void verificarCuentaUsuarioLogado (String numDocumento) throws BusinessException {
		OepmLogger
			.info("Verificando cuenta de usuario conectado...");
		try {
			OepmLogger
					.info("Verificando cuenta de usuario conectado...");
			OepmLogger
					.info("Verificando que el usuario y el proporcionado coinciden...");
			
			String documentoUsuario = SessionContext.getDocumento();
			
			if (StringUtils.isEmptyOrNull(numDocumento)
					|| !numDocumento.trim().equalsIgnoreCase(
							documentoUsuario.trim())) {
				ExceptionUtil
						.throwBusinessException(ExceptionErrorCode.ERROR_CERTIFICADO_NO_COINCIDE);
			} else {
				OepmLogger.info("Verificando cuenta del usuario: "
						+ SessionContext.getLoginUsuario());

				MaoUsuTitrep usuario = maoUsuTitrepDAO.find(Long
						.parseLong(SessionContext.getIdUsuario()));
				usuario.setBoCverificada('S');
				maoUsuTitrepDAO.update(usuario);

				// Cargamos el usuario de nuevo en sesion
				this.createUser(crearUsuarioLogadoTitRep(usuario));

				OepmLogger.info("Cuenta verificada.");
			}
		} catch (final Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.LoginService#createIncorrectAcces(java.lang.String)
	 */
	@Override
	public Integer createIncorrectAcces(String usuario) throws BusinessException {
		// Comprobar si existe el usuario para añadir acceso incorrecto y bloquear en su caso
		UsuarioLogado usuarioNologado = this.findByLogin(usuario);
		if (usuarioNologado != null) {
			// Bloqueamos el usuario si procede
			Integer numIntentos = CacheAccesosIncorrectos.addAccesoIncorrecto(usuarioNologado.getTxLogin());
			if (numIntentos > 4) {
				this.bloquearUsuario(usuarioNologado.getTxLogin());
			}

			return numIntentos;
		} else {
			return -1;
		}
	}

	/**
	 * Creacion del usuario logado para titulares y/o representantes
	 * 
	 * @param maoUsuTitrep
	 * @return UsuarioLogado
	 */
	private UsuarioLogado crearUsuarioLogadoTitRep(MaoUsuTitrep maoUsuTitrep) {
		UsuarioLogado usuLogado = new UsuarioLogado();

		usuLogado.setIdUsuario(maoUsuTitrep.getIdTitrep().toString());
		if (StringUtils.isTrue(maoUsuTitrep.getBoRepresentante())) {
			usuLogado.setTipoUsuario(TipoUsuario.REPRESENTANTE);
		} else {
			usuLogado.setTipoUsuario(TipoUsuario.TITULAR);
		}
		usuLogado.setTxApellido1(maoUsuTitrep.getTxApellido1());
		usuLogado.setTxApellido2(maoUsuTitrep.getTxApellido2());
		usuLogado.setTxNombre(maoUsuTitrep.getTxNombre());
		usuLogado.setTxDocumento(maoUsuTitrep.getTxDocumento());
		usuLogado.setTxEmail(maoUsuTitrep.getTxEmail());
		usuLogado.setTxLogin(maoUsuTitrep.getTxLogin());

		usuLogado.setUsuario(maoUsuTitrep);

		return usuLogado;
	}

	/**
	 * Creacion del usuario logado para asociados
	 * 
	 * @param usuarioAsociado
	 * @return UsuarioLogado
	 */
	private UsuarioLogado crearUsuarioLogadoAsociado(
			MaoUsuAsociados usuarioAsociado) {
		UsuarioLogado usuLogado = new UsuarioLogado();
		usuLogado.setIdUsuario(usuarioAsociado.getIdAsociado().toString());
		usuLogado.setTipoUsuario(TipoUsuario.ASOCIADO);
		usuLogado.setTxApellido1(usuarioAsociado.getTxApellido1());
		usuLogado.setTxApellido2(usuarioAsociado.getTxApellido2());
		usuLogado.setTxNombre(usuarioAsociado.getTxNombre());
		usuLogado.setTxDocumento(usuarioAsociado.getTxDocumento());
		usuLogado.setTxEmail(usuarioAsociado.getTxEmail());
		usuLogado.setTxLogin(usuarioAsociado.getTxLogin());

		// Consultar todos los datos
		usuarioAsociado.setMaoTiposmodalidads(usuarioAsociado
				.getMaoTiposmodalidads());
		usuarioAsociado.setMaoTipospermisoses(usuarioAsociado
				.getMaoTipospermisoses());
		usuarioAsociado.setMaoAgentesV(usuarioAsociado.getMaoAgentesV());
		if (usuarioAsociado.getMaoAgentesV() != null
				&& usuarioAsociado.getMaoAgentesV().getCodAgente() != null) {
			OepmLogger.info("Agente del asociado: "
					+ usuarioAsociado.getMaoAgentesV().getCodAgente() + " "
					+ usuarioAsociado.getMaoAgentesV().getNif());
		}
		usuarioAsociado.setMaoTiposperfiles(usuarioAsociado
				.getMaoTiposperfiles());
		usuarioAsociado.setMaoTitrep(usuarioAsociado.getMaoTitrep());
		if (usuarioAsociado.getMaoTitrep() != null
				&& usuarioAsociado.getMaoTitrep().getTxDocumento() != null) {
			OepmLogger.info("Titular/Rep. del asociado: "
					+ usuarioAsociado.getMaoTitrep().getTxDocumento() + " "
					+ usuarioAsociado.getMaoTitrep().getTxCodclipag());
		}

		// Añadir las modalidades del asociado personalizadas, finalizando con
		// el COD_MODALIDAD o ID
		List<MaoTiposmodalidad> modalidades = usuarioAsociado
				.getMaoTiposmodalidads();
		for (MaoTiposmodalidad modal : modalidades) {
			OepmLogger.info("Modalidad: " + modal.getCodModalidad());
		}

		// Añadir los permisos del asociado personalizados, finalizando con el
		// COD_PERMISO o ID
		List<MaoTipospermisos> permisos = usuarioAsociado
				.getMaoTipospermisoses();
		for (MaoTipospermisos perm : permisos) {
			OepmLogger.info("Permiso: " + perm.getCodPermiso());
		}

		usuLogado.setUsuario(usuarioAsociado);

		return usuLogado;
	}

	/**
	 * Creacion del usuario logado para agentes
	 * 
	 * @param usuarioAgente
	 * @return UsuarioLogado
	 */
	private UsuarioLogado crearUsuarioLogadoAgente(MaoAgentesV usuarioAgente) {
		UsuarioLogado usuLogado = new UsuarioLogado();

		usuLogado.setIdUsuario(String.valueOf(usuarioAgente.getCodAgente()));
		usuLogado.setTipoUsuario(TipoUsuario.AGENTE);
		usuLogado.setTxApellido1(usuarioAgente.getApe1Agente());
		usuLogado.setTxApellido2(usuarioAgente.getApe2Agente());
		usuLogado.setTxNombre(usuarioAgente.getNomAgente());
		usuLogado.setTxDocumento(usuarioAgente.getNif());
		usuLogado.setTxEmail(usuarioAgente.getEmail());
		usuLogado.setTxLogin(usuarioAgente.getLoginMao());

		usuLogado.setUsuario(usuarioAgente);

		return usuLogado;
	}
	
	/**
	 * Creacion del usuario logado para gestores
	 * 
	 * @param usuarioAgente
	 * @return UsuarioLogado
	 */
	private UsuarioLogado crearUsuarioLogadoGestor(MaoGestor usuarioGestor) {
		UsuarioLogado usuLogado = new UsuarioLogado();

		usuLogado.setIdUsuario(usuarioGestor.getIdGestor().toString());
		usuLogado.setTxLogin(usuarioGestor.getTxDocumento());
		usuLogado.setTipoUsuario(TipoUsuario.GESTOR);
		usuLogado.setTxApellido1(usuarioGestor.getTxApellido1());
		usuLogado.setTxApellido2(usuarioGestor.getTxApellido2());
		usuLogado.setTxNombre(usuarioGestor.getTxNombre());
		usuLogado.setTxDocumento(usuarioGestor.getTxDocumento());
		usuLogado.setTxEmail(usuarioGestor.getTxEmail());
		usuLogado.setTxRol(usuarioGestor.getTxRol());

		usuLogado.setUsuario(usuarioGestor);

		return usuLogado;
	}

	/**
	 * Bloquea la cuenta de un usuario.
	 * 
	 * @param login Usuario al que bloquear la cuenta.
	 * @throws BusinessException
	 */
	private void bloquearUsuario(String login) throws BusinessException {
		try {
			// LOGIN DE AGENTES
			MaoAgentesV usuarioAgente = maoAgentesVDAO.login(login);

			if (usuarioAgente != null) {
				usuarioAgente.setBoBloqueocuenta('S');
				maoAgentesVDAO.update(usuarioAgente);

			} else {
				// LOGIN DE ASOCIADOS
				MaoUsuAsociados usuarioAsociado = maoUsuAsociadosDAO.login(login);

				if (usuarioAsociado != null) {
					usuarioAsociado.setBoBloqueocuenta('S');
					maoUsuAsociadosDAO.update(usuarioAsociado);
				} else {
					MaoUsuTitrep maoUsuTitrep = maoUsuTitrepDAO.login(login);
					
					if (maoUsuTitrep != null) {
						maoUsuTitrep.setBoBloqueocuenta('S');
						maoUsuTitrepDAO.update(maoUsuTitrep);
					}
				}
			}

		} catch (final Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}		
	}
	
	/**
	 * Crear usuario en sesion.
	 * 
	 * @param user El usuario logeado que se creara en sesion
	 * @throws BusinessException
	 */
	private void createUser( final UsuarioLogado user ) throws BusinessException {
		final String login = user.getTxLogin();
		final String password = user.getTxLogin();
		final List<GrantedAuthority> authList = getAuthList( user );
		
		UserDetails userDetails = new org.springframework.security.core.userdetails.User( login, password, true, true, true, true, authList );
		
		Map<String, Object> extraDetails = new HashMap<String, Object>();
		
		extraDetails.put( SessionContext.NOMBRE_USUARIO, user.getTxNombre() + " " + user.getTxApellido1() + " " + (user.getTxApellido2() == null ? "" : user.getTxApellido2()) );
		extraDetails.put( SessionContext.ID_USUARIO, user.getIdUsuario() );
		extraDetails.put( SessionContext.TIPO_USUARIO, user.getTipoUsuario().name() );
		extraDetails.put( SessionContext.DOCUMENTO, user.getTxDocumento() );
		extraDetails.put( SessionContext.NOMBRE_ENVIO, user.getTxNombre() );
		extraDetails.put( SessionContext.APELLIDO1_ENVIO, user.getTxApellido1() );
		extraDetails.put( SessionContext.APELLIDO2_ENVIO, user.getTxApellido2() );
		extraDetails.put( SessionContext.DOCUMENTO_ENVIO, user.getTxDocumento() );
		extraDetails.put( SessionContext.EMAIL_ENVIO, user.getTxEmail() );
				
		if (user.getTipoUsuario() == TipoUsuario.AGENTE) {
			MaoAgentesV agente = (MaoAgentesV)user.getUsuario();
			extraDetails.put( SessionContext.CODIGO_AGENTE, agente.getCodAgente());
			extraDetails.put( SessionContext.EMAIL_AGENTE, agente.getEmail() );
			extraDetails.put( SessionContext.TELEFONO_ENVIO, agente.getTfono() );
			extraDetails.put( SessionContext.DIRECCION_ENVIO, agente.getDomicilio());
			extraDetails.put( SessionContext.FAX_ENVIO, agente.getFax());
			
		
		} else if (user.getTipoUsuario() == TipoUsuario.REPRESENTANTE) {
			MaoUsuTitrep repre =(MaoUsuTitrep) user.getUsuario();  
			extraDetails.put( SessionContext.DOCUMENTO_REPRESENTANTE, repre.getTxDocumento() );
			extraDetails.put( SessionContext.EMAIL_REPRESENTANTE, repre.getTxEmail() );
			extraDetails.put( SessionContext.TELEFONO_ENVIO, repre.getNmNumcuenta() );
			extraDetails.put( SessionContext.DIRECCION_ENVIO, repre.getTxDireccion() );
			extraDetails.put( SessionContext.CNAE_ENVIO, repre.getTxCnae());
			extraDetails.put( SessionContext.PYME_ENVIO, repre.getBoPyme());
			extraDetails.put( SessionContext.FAX_ENVIO, repre.getNmFax());
			
		} else if (user.getTipoUsuario() == TipoUsuario.TITULAR) {
			MaoUsuTitrep titu =(MaoUsuTitrep) user.getUsuario();
			extraDetails.put( SessionContext.DOCUMENTO_TITULAR, titu.getTxDocumento() );
			extraDetails.put( SessionContext.EMAIL_TITULAR, titu.getTxEmail() );
			extraDetails.put( SessionContext.TELEFONO_ENVIO, titu.getNmTelefono() );
			extraDetails.put( SessionContext.DIRECCION_ENVIO, titu.getTxDireccion() );
			extraDetails.put( SessionContext.CNAE_ENVIO, titu.getTxCnae());
			extraDetails.put( SessionContext.PYME_ENVIO, titu.getBoPyme());
			extraDetails.put( SessionContext.FAX_ENVIO, titu.getNmFax());
		} else if (user.getTipoUsuario() == TipoUsuario.ASOCIADO) {
			MaoUsuAsociados autorizado = (MaoUsuAsociados)user.getUsuario();
			if (autorizado.getMaoAgentesV()!=null && autorizado.getMaoAgentesV().getCodAgente()!=null) {
				extraDetails.put( SessionContext.CODIGO_AGENTE, autorizado.getMaoAgentesV().getCodAgente() );
				extraDetails.put( SessionContext.DOCUMENTO_AGENTE, autorizado.getMaoAgentesV().getNif() );
				extraDetails.put( SessionContext.EMAIL_AGENTE, autorizado.getMaoAgentesV().getEmail() );
				extraDetails.put( SessionContext.NOMBRE_USUARIO_REAL, autorizado.getMaoAgentesV().getNomAgente() + " " + autorizado.getMaoAgentesV().getApe1Agente() + " " + (autorizado.getMaoAgentesV().getApe2Agente() == null ? "" : autorizado.getMaoAgentesV().getApe2Agente()) );
				extraDetails.put( SessionContext.TELEFONO_ENVIO, autorizado.getMaoAgentesV().getTfono() );
				extraDetails.put( SessionContext.DIRECCION_ENVIO, autorizado.getMaoAgentesV().getDomicilio());
				extraDetails.put( SessionContext.FAX_ENVIO, autorizado.getMaoAgentesV().getFax());				
				
			} else if (autorizado.getMaoTitrep()!=null && autorizado.getMaoTitrep().getIdTitrep()!=null) {
				if (StringUtils.isTrue(autorizado.getMaoTitrep().getBoRepresentante())) {
					extraDetails.put( SessionContext.DOCUMENTO_REPRESENTANTE, autorizado.getMaoTitrep().getTxDocumento() );
					extraDetails.put( SessionContext.EMAIL_REPRESENTANTE, autorizado.getMaoTitrep().getTxEmail() );
					
				} else {
					extraDetails.put( SessionContext.DOCUMENTO_TITULAR, autorizado.getMaoTitrep().getTxDocumento() );
					extraDetails.put( SessionContext.EMAIL_TITULAR, autorizado.getMaoTitrep().getTxEmail() );
				}
				extraDetails.put( SessionContext.CLIGPAG_TITULAR_REPRESENTANTE, autorizado.getMaoTitrep().getTxCodclipag() );
				extraDetails.put( SessionContext.NOMBRE_USUARIO_REAL, autorizado.getMaoTitrep().getTxNombre() + " " + autorizado.getMaoTitrep().getTxApellido1() + " " + (autorizado.getMaoTitrep().getTxApellido2() == null ? "" : autorizado.getMaoTitrep().getTxApellido2()) );
				extraDetails.put( SessionContext.TELEFONO_ENVIO, autorizado.getMaoTitrep().getNmNumcuenta() );
				extraDetails.put( SessionContext.DIRECCION_ENVIO, autorizado.getMaoTitrep().getTxDireccion() );
				extraDetails.put( SessionContext.CNAE_ENVIO, autorizado.getMaoTitrep().getTxCnae());
				extraDetails.put( SessionContext.PYME_ENVIO, autorizado.getMaoTitrep().getBoPyme());
				extraDetails.put( SessionContext.FAX_ENVIO, autorizado.getMaoTitrep().getNmFax());
			}
		} else if (user.getTipoUsuario().equals(TipoUsuario.GESTOR)) {
			extraDetails.put(SessionContext.ID_GESTOR, user.getIdUsuario());
		}
		
		SessionContext.setContext(userDetails, authList, extraDetails);
	}

	/**
	 * Obtener los Roles de autorizacion del usuario.
	 * 
	 * @param user Usuario para el que obtener los roles

	 * @return List<GrantedAuthority> Lista de roles del usuario
	 * @throws BusinessException
	 */
	private List<GrantedAuthority> getAuthList( final UsuarioLogado user ) throws BusinessException {
		final List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>( 1 );
		final Boolean funcionalidadActivaSignos = !MAOConfiguracion.getSitamarIsDisabled();
		//final Boolean funcionalidadActivaSignos = Boolean.TRUE;
		final Boolean funcionalidadActivaDisenos = !MAOConfiguracion.getSitamodIsDisabled();
		//final Boolean funcionalidadActivaDisenos = Boolean.TRUE;
		final Boolean funcionalidadActivaBopi = Configuracion.getPropertyAsBoolean(MaoPropiedadesConf.FUNCIONALIDAD_ACTIVA_BOPI);
		
		String rolePrincipal = null; 
		boolean addTodosPermilesYPermisos = false;
		
		if( user.getTipoUsuario() == TipoUsuario.AGENTE  ) {
			rolePrincipal = Roles.ROLE_MAO_AGENTE;
			authList.add( new SimpleGrantedAuthority( Roles.ROLE_MAO_LECTURAESCRITURA ) );
			////COMENTADO MAO-309
			//authList.add( new SimpleGrantedAuthority( Roles.ROLE_CUENTA_VERIFICADA ) );
			addTodosPermilesYPermisos = true;
		} else if (user.getTipoUsuario().equals(TipoUsuario.GESTOR)) {
			rolePrincipal = user.getTxRol();
			authList.add( new SimpleGrantedAuthority( Roles.ROLE_CUENTA_VERIFICADA ) );
			////COMENTADO MAO-309
			//authList.add( new SimpleGrantedAuthority( Roles.ROLE_MAO_SOLOLECTURA ) );
			addTodosPermilesYPermisos = true;
		} else if( user.getTipoUsuario() == TipoUsuario.ASOCIADO ) {
			rolePrincipal = Roles.ROLE_MAO_ASOCIADO;
			
			// Roles de asociados dependiendo de los atributos
			MaoUsuAsociados usuarioAsociado = (MaoUsuAsociados)user.getUsuario();
			if (StringUtils.isTrue(usuarioAsociado.getBoSololectura())) {
				authList.add( new SimpleGrantedAuthority( Roles.ROLE_MAO_SOLOLECTURA ) );
			} else {
				authList.add( new SimpleGrantedAuthority( Roles.ROLE_MAO_LECTURAESCRITURA ) );
			}

			// Comprobacion de modalidaddes y permisos segun tipo de usuario asociado
			MaoTiposperfiles perfil = usuarioAsociado.getMaoTiposperfiles();
			if (perfil.getCodPerfilasoc()=='A') { //Administrador
				addTodosPermilesYPermisos = true;
			} else {
				// Añadir las modalidades del asociado personalizadas, finalizando con el COD_MODALIDAD o ID
				List<MaoTiposmodalidad> modalidades = usuarioAsociado.getMaoTiposmodalidads();
				for (MaoTiposmodalidad modal : modalidades) {
					if ((modal.getCodModalidad() != 'S' && modal.getCodModalidad() != 'D')
							|| ((modal.getCodModalidad() == 'S' && funcionalidadActivaSignos) || (modal
									.getCodModalidad() == 'D' && funcionalidadActivaDisenos))) {
						authList.add(new SimpleGrantedAuthority(
								Roles.ROLE_MAO_MODALIDAD_ + modal.getCodModalidad()));
					}
				}
				
				// Añadir los permisos del asociado personalizados, finalizando con el COD_PERMISO o ID
				List<MaoTipospermisos> permisos = usuarioAsociado.getMaoTipospermisoses();
				for (MaoTipospermisos perm : permisos) {
					authList.add( new SimpleGrantedAuthority( Roles.ROLE_MAO_PERMISO_ + perm.getCodPermiso() ) );
				}
			}
			
			//Cuenta verificada (acceso con certificado), si el titular o representante o agente tiene cuenta verificada
			if (usuarioAsociado.getMaoTitrep() != null) { //De titular o representante
				if (usuarioAsociado.getMaoTitrep().getBoCverificada()=='S') {
					//authList.add( new SimpleGrantedAuthority( Roles.ROLE_CUENTA_VERIFICADA ) );
				} 
			} else if (usuarioAsociado.getMaoAgentesV() != null) { // De agente, si tiene acceso el agente
				//if (usuarioAsociado.getMaoAgentes().getBoBloqueocuenta()=='N') { //Comprobado en el login del autorizado
					//authList.add( new SimpleGrantedAuthority( Roles.ROLE_CUENTA_VERIFICADA ) );
				//} 
			}
			
		} else if( user.getTipoUsuario() == TipoUsuario.REPRESENTANTE ) {
			rolePrincipal = Roles.ROLE_MAO_REPRESENTANTE;
			authList.add( new SimpleGrantedAuthority( Roles.ROLE_MAO_LECTURAESCRITURA ) );
			//MaoUsuTitrep usuario = (MaoUsuTitrep)user.getUsuario();
			//Cuenta verificada (acceso con certificado)
			////COMENTADO MAO-309
			/*if (usuario.getBoCverificada()=='S') {
				authList.add( new SimpleGrantedAuthority( Roles.ROLE_CUENTA_VERIFICADA ) );
			}*/
			addTodosPermilesYPermisos = true;
			
		} else if( user.getTipoUsuario() == TipoUsuario.TITULAR ) {
			rolePrincipal = Roles.ROLE_MAO_TITULAR;
			authList.add( new SimpleGrantedAuthority( Roles.ROLE_MAO_LECTURAESCRITURA ) );
			//MaoUsuTitrep usuario = (MaoUsuTitrep)user.getUsuario();
			//Cuenta verificada (acceso con certificado)
			////COMENTADO MAO-309
			/*if (usuario.getBoCverificada()=='S') {
				authList.add( new SimpleGrantedAuthority( Roles.ROLE_CUENTA_VERIFICADA ) );
			}*/
			addTodosPermilesYPermisos = true;
		}
		
		// En otros casos no asociados con permisos personalizados, tienen todos los permisos y modalidades
		if (addTodosPermilesYPermisos) {
			List<MaoTiposmodalidad> modalidades = tiposmodalidadService.obtenerTiposModalidad();
			for (MaoTiposmodalidad modal : modalidades) {
				if ((modal.getCodModalidad() != 'S' && modal.getCodModalidad() != 'D')
						|| ((modal.getCodModalidad() == 'S' && funcionalidadActivaSignos) || (modal
								.getCodModalidad() == 'D' && funcionalidadActivaDisenos))) {
					authList.add(new SimpleGrantedAuthority(
							Roles.ROLE_MAO_MODALIDAD_ + modal.getCodModalidad()));
				}
			}
			
			List<MaoTipospermisos> permisos = tipospermisosService.obtenerTiposPermisos();
			for (MaoTipospermisos perm : permisos) {
				// Comprobamos la funcionalidad activa bopi
				if ((perm.getCodPermiso() != 'B')
						|| (perm.getCodPermiso() == 'B' && funcionalidadActivaBopi)) {
					authList.add(new SimpleGrantedAuthority(
							Roles.ROLE_MAO_PERMISO_ + perm.getCodPermiso()));
				}
			}
		}

		authList.add( new SimpleGrantedAuthority( rolePrincipal ) );
		
		OepmLogger.info("Permisos asociados: " + ToStringHelper.toString(authList) );

		return authList;
	}
	
	/**
	 * Crea una nueva sesion del usuario en la tabla de sesiones.
	 * 
	 * @param usuario
	 * @throws BusinessException
	 */
	private void createDataBaseSesion(String usuario) throws BusinessException {
		//Añadimos o activamos la sesion del usuario en la tabla MAO _SESIONES.
		SesionesVO sesion= new SesionesVO();
		sesion.setIdentificadorSesion("SID: " + FacesUtil.getSessionObject().getId());
		sesion.setLoginUsuario(usuario);
		sesion.setUltimoAcceso(new Date());
		sesionesService.activarSesion(sesion);
	}

	@Override
	public UserDetails loadUserByUsername(String arg0)
			throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public MaoAgentesVDAO getMaoAgentesVDAO() {
		return maoAgentesVDAO;
	}

	public void setMaoAgentesVDAO(MaoAgentesVDAO maoAgentesVDAO) {
		this.maoAgentesVDAO = maoAgentesVDAO;
	}

	public MaoUsuAsociadosDAO getMaoUsuAsociadosDAO() {
		return maoUsuAsociadosDAO;
	}

	public void setMaoUsuAsociadosDAO(MaoUsuAsociadosDAO maoUsuAsociadosDAO) {
		this.maoUsuAsociadosDAO = maoUsuAsociadosDAO;
	}

	public MaoUsuTitrepDAO getMaoUsuTitrepDAO() {
		return maoUsuTitrepDAO;
	}

	public void setMaoUsuTitrepDAO(MaoUsuTitrepDAO maoUsuTitrepDAO) {
		this.maoUsuTitrepDAO = maoUsuTitrepDAO;
	}

	public MaoGestorDAO getMaoGestorDAO() {
		return maoGestorDAO;
	}

	public void setMaoGestorDAO(MaoGestorDAO maoGestorDAO) {
		this.maoGestorDAO = maoGestorDAO;
	}
}
