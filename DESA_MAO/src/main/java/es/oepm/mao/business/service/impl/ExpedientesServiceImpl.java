package es.oepm.mao.business.service.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;

import org.apache.cxf.common.util.CollectionUtils;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.busmule.ceo.ws.IWSBusDetalleExpediente;
import es.oepm.busmule.ceo.ws.WSBusDetalleExpediente;
import es.oepm.busmule.ws.client.ceo.BusSistemas;
import es.oepm.busmule.ws.client.ceo.IWSBusConsultaExpedientes;
import es.oepm.busmule.ws.client.ceo.WSBusConsultaExpedientes;
import es.oepm.busmule.ws.client.ceo.parameters.BusConsultarExpedientesRequest;
import es.oepm.busmule.ws.client.ceo.parameters.BusConsultarExpedientesResponse;
import es.oepm.busmule.ws.client.ceo.parameters.BusObtenerEstadosAlfaResponse;
import es.oepm.ceo.ws.detalleexp.parameters.DetalleExpedienteRequest;
import es.oepm.ceo.ws.detalleexp.parameters.DetalleExpedienteResponse;
import es.oepm.core.business.ceo.vo.CeAlrCesionMvVO;
import es.oepm.core.business.ceo.vo.CeAlrExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeAlrLicenciasMvVO;
import es.oepm.core.business.ceo.vo.CeDirExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeDirTransferCesioMvVO;
import es.oepm.core.business.ceo.vo.CeMnrExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeMnrTransferCesioMvVO;
import es.oepm.core.business.ceo.vo.FiltroBusquedaExpedienteVO;
import es.oepm.core.business.mao.vo.NumSolicitudVO;
import es.oepm.core.business.vo.DetalleExpedienteResponseVO;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.util.DateUtils;
import es.oepm.core.util.ExpedienteUtils;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.business.service.ExpedientesService;
import es.oepm.mao.business.service.TrazaGestorService;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.constants.MaoTrazaGestor;
import es.oepm.mao.view.controller.util.ExpedientesFilter;
import es.oepm.mao.view.controller.util.MAOConfiguracion;
import es.oepm.wservices.core.mensajes.Mensajes;
import es.oepm.wservices.core.util.WSUtils;

@Service(value = "expedientesService")
@Transactional(propagation = Propagation.SUPPORTS)
public class ExpedientesServiceImpl implements ExpedientesService {

	private static final long serialVersionUID = 3885461078854948777L;

	/**
	 * Bean del mapeador de objetos.
	 */
	@Autowired 
	private transient DozerBeanMapper dozerBeanMapper;
	
	@Autowired
	private TrazaGestorService trazaGestorService;

	//private static Logger log = Logger.getLogger(ExpedientesServiceImpl.class);

	private static IWSBusConsultaExpedientes wsConExpedientes;
	private static IWSBusDetalleExpediente wsDetExpedientes;

	private transient Object lockWsDetExpedientes = new Object();

	public ExpedientesServiceImpl() {
			
	}
	
	/**
	 * Crear el Cliente de WS de Consulta de Expediente
	 */
	private void comprobarClienteConsultaExpediente()  throws BusinessException{
		String endpointConExp ="";
		try{
		if (wsConExpedientes == null) {
			synchronized( lockWsDetExpedientes ) {
				if( wsConExpedientes == null ) {			
					endpointConExp = Configuracion.getPropertyAsString("url.bus.consultaExp");
					//endpointConExp = "http://prebussoa.oepm.local/busmule/WSBusConsultaExpedientes?wsdl";
					//endpointConExp = "http://localhost:8080/busmule/WSBusConsultaExpedientes?wsdl";
					
					int receiveTimeout = Configuracion.getPropertyAsInteger( "ws.default.recieveTimeout" );
					int connectionTimeout = Configuracion.getPropertyAsInteger( "ws.default.connectionTimeout" );
					
					if (!StringUtils.isEmptyOrNull(endpointConExp)) {
						WSBusConsultaExpedientes wsConExpedientesLocator = 
								new WSBusConsultaExpedientes(new URL(endpointConExp));
		
						wsConExpedientes = wsConExpedientesLocator.getWSBusConsultaExpedientes();
						
						// HCS: Quitamos la validacion de campos
						IWSBusConsultaExpedientes consultaExpediente = wsConExpedientesLocator.getWSBusConsultaExpedientes();
						BindingProvider bindingProvider = (BindingProvider) consultaExpediente;
	                    bindingProvider.getRequestContext().put("set-jaxb-validation-event-handler", Boolean.FALSE);
		
						WSUtils.setBindingEndpoint( wsConExpedientes, endpointConExp );
						WSUtils.setBindingTimeouts( wsConExpedientes, receiveTimeout, connectionTimeout );
					} else {
						FacesUtil.addErrorMessage("error.ws.consultaExpedientes");	
						OepmLogger.error("Error al instanciar el wsclient de consulta de expediente. No esta definida en la configuración la variable url.bus.consultaExp");
					}
				}
			}
		}
		} catch (Exception e) {
			wsConExpedientes=null;
			FacesUtil.addErrorMessage("error.ws.consultaExpedientes");	
			OepmLogger.error("No se puede instanciar el WS Consulta de Expediente de BUS para la dirección " + endpointConExp ,							e);
			throw new BusinessException(e, "");
		}
	}
	
	/**
	 * Crear el Cliente de WS de Detalle de Expediente
	 */
	private void comprobarClienteDetalleExpediente() throws BusinessException{
		String endpointDetExp ="";
		try{
			if (wsDetExpedientes == null) {
				synchronized( lockWsDetExpedientes ) {
					if( wsDetExpedientes == null ) {			
						endpointDetExp = Configuracion.getPropertyAsString("url.bus.detalleExp");
						//endpointDetExp = "http://localhost:8080/busmule/WSBusDetalleExpediente?wsdl";						
						
						int receiveTimeout = Configuracion.getPropertyAsInteger( "ws.default.recieveTimeout" );
						int connectionTimeout = Configuracion.getPropertyAsInteger( "ws.default.connectionTimeout" );
						
						if (!StringUtils.isEmptyOrNull(endpointDetExp)) {
							WSBusDetalleExpediente wsDetExpedientesLocator = new WSBusDetalleExpediente(
									new URL(endpointDetExp));
			
							wsDetExpedientes = wsDetExpedientesLocator.getWSBusDetalleExpediente();
							
							// HCS: Quitamos la validacion de campos
							IWSBusDetalleExpediente detExpediente = wsDetExpedientesLocator.getWSBusDetalleExpediente();
							BindingProvider bindingProvider = (BindingProvider) detExpediente;
		                    bindingProvider.getRequestContext().put("set-jaxb-validation-event-handler", Boolean.FALSE);
		
							WSUtils.setBindingEndpoint( wsDetExpedientes, endpointDetExp );
							WSUtils.setBindingTimeouts( wsDetExpedientes, receiveTimeout, connectionTimeout );
						} else {
							FacesUtil.addErrorMessage("error.ws.detalleExpedientes");	
							OepmLogger.error("Error al instanciar el wsclient de detalle de expediente. No esta definida en la configuracion la variable url.bus.detalleExp");
						}
					}
				}
			}
		} catch (Exception e) {
			wsDetExpedientes=null;
			FacesUtil.addErrorMessage("error.ws.detalleExpedientes");	
			OepmLogger.error("No se puede instanciar el WS Detalle de Expediente de BUS para la dirección "+ endpointDetExp,e);
			throw new BusinessException(e, "");
		}

	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.ExpedientesService#getEstadosAlfa()
	 */
	@Override
	public BusObtenerEstadosAlfaResponse getEstadosAlfa() throws BusinessException {
		BusObtenerEstadosAlfaResponse response = null;

		try {
			// CONSULTA DE EXPEDIENTES
			comprobarClienteConsultaExpediente();

			// Llamada al servicio web de CEO
//			response = wsConExpedientes.obtenerEstadosAlfa(Configuracion.getPropertyAsString("WSECURITY.WSUSER"),
//					Configuracion.getPropertyAsString("WSECURITY.WSPASSWORD"));
			
			response = wsConExpedientes.obtenerEstadosAlfa(null,null);

		} catch (Exception e) {
			OepmLogger.error(e);
			ExceptionUtil.throwBusinessException(e);
		}

		return response;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.ExpedientesService#consultarExpedientes(es.oepm.core.business.ceo.vo.FiltroBusquedaExpedienteVO)
	 */
	@Override
	public BusConsultarExpedientesResponse consultarExpedientes(FiltroBusquedaExpedienteVO filter, boolean isquick) throws BusinessException {
		BusConsultarExpedientesResponse resultado = null;
		
		try {
			// CONSULTA DE EXPEDIENTES
			comprobarClienteConsultaExpediente();
			comprobarClienteDetalleExpediente();

			// Insertamos en la traza
			if (TipoUsuario.GESTOR.toString().equals(
					SessionContext.getTipoUsuario())) {
				trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_BUSQ_EXP, filter.toTraze());
			}

			// Parseamos la entrada al parametro del servicio
			BusConsultarExpedientesRequest request = rellenarDatosConsulta(filter);

			if (wsConExpedientes != null) {
				// Obtenemos la lista de sistemas a los que tiene acceso el usuario
				List<BusSistemas> listaSistemas = AuthorityManager
						.obtenerSistemasUsuario((Collection<SimpleGrantedAuthority>) SessionContext
								.getUserDetails().getAuthorities());

				// Llamada al servicio web de CEO
				resultado = wsConExpedientes.consultarExpedientes(request, listaSistemas);
				/**
				 * MAO-315
				 */

				if ( (resultado == null) || (resultado != null && resultado.getResultado() == -1) )
				{
					// HCS: La llamada a consultaExpedientes ha fallado 
					OepmLogger.error("El WS de consultaExpedientes ha fallado");
					FacesUtil.addErrorMessage("busqueda.error");		
				}

				else if( CollectionUtils.isEmpty(resultado.getExpedientes())){
					Mensajes[] mensajes;
					mensajes=resultado.getMensajes();
					request.setCodigoAgente(null);
					request.setCodigoRepresentante(null);
					request.setCodigoTitular(null);
					resultado = wsConExpedientes.consultarExpedientes(request, listaSistemas);

					for(int i=0; i<resultado.getExpedientes().size(); i++ ) {							

						String modalidad = resultado.getExpedientes().get(i).getModalidad();
						
						if( StringUtils.isEmptyOrNull(resultado.getExpedientes().get(i).getPublico())) {
							
							if (Modalidad.find(modalidad).isAlfa()) {
							
								// Invenciones
								DetalleExpedienteRequest requestDet = new DetalleExpedienteRequest();
								DetalleExpedienteResponse responseDet = null;
								requestDet.setId(resultado.getExpedientes().get(i).getId());
								requestDet.setModalidad(resultado.getExpedientes().get(i).getModalidad());							
								responseDet = wsDetExpedientes.detalleExpediente(requestDet);
	
								if(responseDet.getDetalleInvencion()!=null){
									if( !responseDet.getDetalleInvencion().isPublicable()) {
										Object o= resultado.getExpedientes().get(i);
										resultado.getExpedientes().remove(o);
										resultado.setMensajes(mensajes);
										resultado.setResultado(3);
									}	
									else {
										resultado.setResultado(2);
									}
								}
							}
							else if (Modalidad.find(modalidad).isSitamar())   {							
								// Todas las marcas son publicas
								resultado.setResultado(2);
							}
							else if (Modalidad.find(modalidad).isSitamod())   {
								// Disenos...imagino que los disenos son todos publicos tambien
								resultado.setResultado(2);
							}
						}
						else if ("FALSE".equals(resultado.getExpedientes().get(i).getPublico()))
						{
							Object o= resultado.getExpedientes().get(i);
							resultado.getExpedientes().remove(o);
							resultado.setMensajes(mensajes);
							resultado.setResultado(3);
						}
						else {
							resultado.setResultado(2);
						}
					}

					if(resultado.getResultado()!=3 || resultado.getResultado()!=2){							
						if(resultado.getExpedientes().size() == 0){
							resultado.setMensajes(mensajes);
						}
					}
				} 
			} else {		
				// Fallo en el WS (no esta instanciado)
				FacesUtil.addErrorMessage("busqueda.error");
				OepmLogger.error("El WS de Sireco ha fallado.");
			}

		} catch (Exception e) {
			OepmLogger.error(e);
			ExceptionUtil.throwBusinessException(e);
		}
		
		return resultado;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.ExpedientesService#consultarUltimosExpedientesUsuario()
	 */
	@Override
	public BusConsultarExpedientesResponse consultarUltimosExpedientesUsuario() throws BusinessException {
		BusConsultarExpedientesResponse resultado = null;
		
		try {
			// CONSULTA DE EXPEDIENTES
			comprobarClienteConsultaExpediente();
			
			//Recuperamos los datos del usuario
			Integer numMaxResultados = Configuracion.getPropertyAsInteger("NUM_ULTIMOS_EXPEDIENTES_SOLICITADOS");
			String codigoRepresentante= SessionContext.getDocumentoRepresentante();
			String codigoTitular = SessionContext.getDocumentoTitular();
			////COMENTADO MAO-309
			/*if (!SessionContext.getUserDetails().getAuthorities().contains(new SimpleGrantedAuthority(Roles.ROLE_CUENTA_VERIFICADA))) {
				codigoRepresentante = SessionContext.getEmailRepresentante();
				codigoTitular = SessionContext.getEmailTitular();
			}*/
			
			// Lista de sistemas del usuario
			/*List<BusSistemas> listaSistemas = AuthorityManager
					.obtenerSistemasUsuario((Collection<SimpleGrantedAuthority>) SessionContext
							.getUserDetails().getAuthorities());*/
			
			// Llamada al servicio web de CEO
//			resultado = wsConExpedientes.consultarUltimosExpedientesUsuario(
//					SessionContext.getCodigoAgente(), codigoRepresentante,
//					codigoTitular, numMaxResultados,Configuracion.getPropertyAsString("WSECURITY.WSUSER"),
//					Configuracion.getPropertyAsString("WSECURITY.WSPASSWORD"), listaSistemas);
			
			/*MAO -270 SE CREA UNA LISTA UNICAMENTE CON EL SISTEMA ALFA
			 * PARA ESTA PRIMERA VERSION DE MAO. DE ESTA MANERA SOLO SACARA LOS EXPEDIENTES ALFA
			 */
			List<BusSistemas> listaSistemas = new ArrayList<BusSistemas>();
			
			if (!MAOConfiguracion.getAlfaIsDisabled()) {
				listaSistemas.add(BusSistemas.ALFA);
			}
			
			if (!MAOConfiguracion.getSitamarIsDisabled()) {
				listaSistemas.add(BusSistemas.SITAMAR);
			}
			
			if (!MAOConfiguracion.getSitamodIsDisabled()) {
				listaSistemas.add(BusSistemas.SITAMOD);
			}
			
			resultado = wsConExpedientes.consultarUltimosExpedientesUsuario(
			SessionContext.getCodigoAgente(), codigoRepresentante,
			codigoTitular, numMaxResultados,null,null, listaSistemas);

		} catch (Exception e) {
			OepmLogger.error(e);
			ExceptionUtil.throwBusinessException(e);
		}
		
		return resultado;
	}

	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.ExpedientesService#getExpedientesSolPub(es.oepm.mao.view.controller.util.ExpedientesFilter)
	 */
	@Override
	public BusConsultarExpedientesResponse getExpedientesSolPub(ExpedientesFilter filter) throws BusinessException {
		BusConsultarExpedientesResponse response = null;
		BusConsultarExpedientesRequest request = fillRequestFromFilterExpedientesSolPub(filter);
		/**
		 * MAO-331 + MAO-333
		 */
		//request.setUsuario(Configuracion.getPropertyAsString("WSECURITY.WSUSER"));
		//request.setPass(Configuracion.getPropertyAsString("WSECURITY.WSPASSWORD"));
		try{
			comprobarClienteConsultaExpediente();
			
			// Obtenemos la lista de sistemas y la seteamos
			List<BusSistemas> listaSistemas = AuthorityManager.obtenerSistemasUsuario(
					(Collection<SimpleGrantedAuthority>) SessionContext.getUserDetails().getAuthorities());

			response = wsConExpedientes.consultarExpedientes(request, listaSistemas);
		} catch (Exception e) {
			FacesUtil.addErrorMessage("error.ws.consultaExpedientes");	
			ExceptionUtil.throwBusinessException(e);
		}
		
		return response;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.ExpedientesService#getExpedientesConPagos(es.oepm.mao.view.controller.util.ExpedientesFilter)
	 */
	@Override
	public BusConsultarExpedientesResponse getExpedientesConPagos(ExpedientesFilter filter) throws BusinessException {
		BusConsultarExpedientesResponse response = null;
		ExpedientesFilter f = tratarFilterExpedientesWithPagos(filter);
				
		try {
			comprobarClienteConsultaExpediente();
			//MAO-331
			response = wsConExpedientes.consultarExpedientesDePagoUsuario( 
					f.codAgente, 
					f.emailAgente,
					f.codRepresentante, 
					f.emailRepresentante, 
					f.codTitular,
					f.emailTitular,
					f.modalidad, f.numeroSolicitud, f.numeroPublicacion, 
					f.calFecPresentExpDesde, f.calFecPresentExpHasta,
					null,
					null
			);
		} catch (Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}

		return response;
	}
	
	/**
	 * Método que trata el filtro para llamar a getExpedientesWithPagos en calidad de Agente, 
	 * Representante o Titular (estos dos últimos tengan o no cuenta verificada)
	 * @param filter
	 * @return
	 */
	private ExpedientesFilter tratarFilterExpedientesWithPagos(ExpedientesFilter filter) {
		ExpedientesFilter f = new ExpedientesFilter();
		
		f.modalidad = filter.modalidad;
		f.numeroSolicitud = filter.numeroSolicitud;
		f.numeroPublicacion = filter.numeroPublicacion;
		f.calFecPresentExpDesde= DateUtils.parseDateToXMLGregCal( filter.fhPresentacionInicio);
		f.calFecPresentExpHasta = DateUtils.parseDateToXMLGregCal( filter.fhPresentacionFin );
		
		switch (filter.tipoUsuario) {
		case AGENTE:
			f.codAgente = filter.codAgente;
			f.emailAgente = filter.emailAgente;
			
			break;
		case REPRESENTANTE:
			f.codRepresentante = filter.codRepresentante;
			f.emailRepresentante = filter.emailRepresentante;
			
			break;
		case TITULAR:
			f.codTitular = filter.codTitular;
			f.emailTitular = filter.emailTitular;
			
			break;

		default:
			f.codAgente = filter.codAgente;
			f.emailAgente = filter.emailAgente;
			f.codRepresentante = filter.codRepresentante;
			f.emailRepresentante = filter.emailRepresentante;
			f.codTitular = filter.codTitular;
			f.emailTitular = filter.emailTitular;
			return f;
		}
		
		return f;
	}

	/**
	 * Método que trata el filtro para llamar a getExpedientesSolPub en calidad de Agente, 
	 * Representante o Titular (estos dos últimos tengan o no cuenta verificada)
	 * @param filter
	 * @return
	 */
	private BusConsultarExpedientesRequest fillRequestFromFilterExpedientesSolPub(ExpedientesFilter filter) {
		BusConsultarExpedientesRequest request = new BusConsultarExpedientesRequest();

		request.setNombreApTitular(filter.nombre.trim());
		request.setRellenarDatos(false);
		request.setModalidad(filter.modalidad);
		request.setNumeroSolicitud(filter.numeroSolicitud);
		request.setNumeroPublicacion(filter.numeroPublicacion);
		/**
		 * MAO-333
		 */
		switch (filter.tipoUsuario) {
		case AGENTE:
			//request.setEmailAgente(filter.emailAgente);
			request.setCodigoAgente(filter.codAgente);
			request.setCodigoTitular(filter.codTitular);
			break;
			
		case REPRESENTANTE:
			//request.setEmailRepresentante(filter.emailRepresentante);
			request.setCodigoRepresentante(filter.codRepresentante);
			request.setCodigoTitular(filter.codTitular);
			break;
			
		case TITULAR:
			//request.setEmailAgente(filter.emailTitular);
			request.setCodigoTitular(filter.codTitular);
			break;

		default:
			//request.setEmailAgente(filter.emailAgente);
			request.setCodigoAgente(filter.codAgente);
			//request.setEmailRepresentante(filter.emailRepresentante);
			request.setCodigoRepresentante(filter.codRepresentante);
			//request.setEmailAgente(filter.emailTitular);
			request.setCodigoTitular(filter.codTitular);
			return request;
		}
		
		return request;
	}
	
	/**
	 * Realiza la llamada al servicio web de CEO para recuperar detalles de
	 * expedientes.
	 * 
	 * @param idExpediente
	 *            Id del expediente a recuperar.
	 * @param modalidad
	 *            Modalidad del expediente a recuperar.
	 * @return
	 * @throws BusinessException
	 */
	private DetalleExpedienteResponse doGetDetalleExpediente(String idExpediente,
			String modalidad) throws BusinessException {
		DetalleExpedienteResponse response = null;

		try {
			// Comprobamos el cliente del WS
			comprobarClienteDetalleExpediente();
			
			// Seteamos los parametros de busqueda
			DetalleExpedienteRequest request = new DetalleExpedienteRequest();
			request.setId(idExpediente);
			request.setModalidad(modalidad);
			/**
			 * CEO-861 + MAO-331
			 */
			//request.setUsuario(Configuracion.getPropertyAsString("WSECURITY.WSUSER"));
			//request.setPass(Configuracion.getPropertyAsString("WSECURITY.WSPASSWORD"));

			response = wsDetExpedientes.detalleExpediente(request);
			
			// Comprobamos la respuesta
			if (response == null
					|| (response.getResultado() != DetalleExpedienteResponse.RESULTADO_OK
							&& response.getMensajes() != null && response
							.getMensajes().length > 0)) {
				// Si no hay respuesta o no es correcta pero no hay mensajes
				// lanzamos excepción
				ExceptionUtil.throwBusinessException(ExceptionUtil.GENERIC_ERROR_KEY);
			}
			
			// Si el usuario es de tipo gestor insertamos la traza
			if (SessionContext.getTipoUsuario().equals(TipoUsuario.GESTOR.toString())) {
				insertGestorTraze(response, modalidad);
			}
			
		} catch (Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}
		
		return response;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.ExpedientesService#getDetalleExpediente(java.lang.String, java.lang.String)
	 */
	public DetalleExpedienteResponse getDetalleExpediente(String idExpediente,
			String modalidad) throws BusinessException {

		return doGetDetalleExpediente(idExpediente, modalidad);

	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.ExpedientesService#getDetalleExpedienteInvencion(java.lang.String, java.lang.String)
	 */
	public DetalleExpedienteResponseVO<CeAlrExpedientesMvVO> getDetalleExpedienteInvencion(String idExpediente,
			String modalidad) throws BusinessException {
		DetalleExpedienteResponseVO<CeAlrExpedientesMvVO> result = null;

		try {
			result = new DetalleExpedienteResponseVO<CeAlrExpedientesMvVO>();
			// Llamamos el metodo que realiza la llamada al servicio web
			DetalleExpedienteResponse response = doGetDetalleExpediente(idExpediente, modalidad);
			// Comprobamos si se ha devuelto un expediente de invenciones
			if (response.getDetalleInvencion() != null) {
				CeAlrExpedientesMvVO expediente = dozerBeanMapper.map(response.getDetalleInvencion(), CeAlrExpedientesMvVO.class);
				expediente.setTituloDetalle(eliminaEspacios(expediente.getTituloDetalle()));
				expediente.setTituloInvencion(eliminaEspacios(expediente.getTituloInvencion()));
				result.setDetalleExpediente(expediente);
			}
			result.setMensajes(response.getMensajes());
		} catch (Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}

		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.ExpedientesService#getDetalleExpedienteCesion(java.lang.String, java.lang.String)
	 */
	public DetalleExpedienteResponseVO<CeAlrCesionMvVO> getDetalleExpedienteCesion(String idExpediente,
			String modalidad) throws BusinessException {
		DetalleExpedienteResponseVO<CeAlrCesionMvVO> result = null;

		try {
			result = new DetalleExpedienteResponseVO<CeAlrCesionMvVO>();
			// Llamamos el metodo que realiza la llamada al servicio web
			DetalleExpedienteResponse response = doGetDetalleExpediente(idExpediente, modalidad);
			// Comprobamos si se ha devuelto un expediente de cesion invenciones
			if (response.getDetalleInvencionCesion() != null) {
				CeAlrCesionMvVO expediente = dozerBeanMapper.map(response.getDetalleInvencionCesion(), CeAlrCesionMvVO.class);
				result.setDetalleExpediente(expediente);
			}
			result.setMensajes(response.getMensajes());
		} catch (Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}

		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.ExpedientesService#getDetalleExpedienteLicencia(java.lang.String, java.lang.String)
	 */
	public DetalleExpedienteResponseVO<CeAlrLicenciasMvVO> getDetalleExpedienteLicencia(String idExpediente,
			String modalidad) throws BusinessException {
		DetalleExpedienteResponseVO<CeAlrLicenciasMvVO> result = null;

		try {
			result = new DetalleExpedienteResponseVO<CeAlrLicenciasMvVO>();
			// Llamamos el metodo que realiza la llamada al servicio web
			DetalleExpedienteResponse response = doGetDetalleExpediente(idExpediente, modalidad);
			// Comprobamos si se ha devuelto un expediente de invenciones
			if (response.getDetalleInvencionLicencia() != null) {
				CeAlrLicenciasMvVO expediente = dozerBeanMapper.map(response.getDetalleInvencionLicencia(), CeAlrLicenciasMvVO.class);
				result.setDetalleExpediente(expediente);
			}
			result.setMensajes(response.getMensajes());
		} catch (Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}

		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.ExpedientesService#getDetalleExpedienteSitamar(java.lang.String, java.lang.String)
	 */
	public DetalleExpedienteResponseVO<CeMnrExpedientesMvVO> getDetalleExpedienteSitamar(String idExpediente,
			String modalidad) throws BusinessException {
		DetalleExpedienteResponseVO<CeMnrExpedientesMvVO> result = null;

		try {
			result = new DetalleExpedienteResponseVO<CeMnrExpedientesMvVO>();
			// Llamamos el metodo que realiza la llamada al servicio web
			DetalleExpedienteResponse response = doGetDetalleExpediente(idExpediente, modalidad);
			// Comprobamos si se ha devuelto un expediente de marcas
			if (response.getDetalleMarca() != null) {
				CeMnrExpedientesMvVO expediente = dozerBeanMapper.map(response.getDetalleMarca(), CeMnrExpedientesMvVO.class);
				result.setDetalleExpediente(expediente);
			}
			result.setMensajes(response.getMensajes());
		} catch (Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}

		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.ExpedientesService#getDetalleExpedienteEspecialSitamar(java.lang.String, java.lang.String)
	 */
	public DetalleExpedienteResponseVO<CeMnrTransferCesioMvVO> getDetalleExpedienteEspecialSitamar(String idExpediente,
			String modalidad) throws BusinessException {
		DetalleExpedienteResponseVO<CeMnrTransferCesioMvVO> result = null;

		try {
			result = new DetalleExpedienteResponseVO<CeMnrTransferCesioMvVO>();
			// Llamamos el metodo que realiza la llamada al servicio web
			DetalleExpedienteResponse response = doGetDetalleExpediente(idExpediente, modalidad);
			// Comprobamos si se ha devuelto un expediente especial de marcas
			if (response.getDetalleMarcaEspecial() != null) {
				CeMnrTransferCesioMvVO expediente = dozerBeanMapper.map(response.getDetalleMarcaEspecial(), CeMnrTransferCesioMvVO.class);
				result.setDetalleExpediente(expediente);
			}
			result.setMensajes(response.getMensajes());
		} catch (Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}

		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.ExpedientesService#getDetalleExpedienteSitamod(java.lang.String, java.lang.String)
	 */
	public DetalleExpedienteResponseVO<CeDirExpedientesMvVO> getDetalleExpedienteSitamod(String idExpediente,
			String modalidad) throws BusinessException {
		DetalleExpedienteResponseVO<CeDirExpedientesMvVO> result = null;

		try {
			result = new DetalleExpedienteResponseVO<CeDirExpedientesMvVO>();
			// Llamamos el metodo que realiza la llamada al servicio web
			DetalleExpedienteResponse response = doGetDetalleExpediente(idExpediente, modalidad);
			// Comprobamos si se ha devuelto un expediente de modelos
			if (response.getDetalleModelo() != null) {
				CeDirExpedientesMvVO expediente = dozerBeanMapper.map(response.getDetalleModelo(), CeDirExpedientesMvVO.class);
				result.setDetalleExpediente(expediente);
			}
			result.setMensajes(response.getMensajes());
		} catch (Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}

		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.ExpedientesService#getDetalleExpedienteEspecialSitamod(java.lang.String, java.lang.String)
	 */
	public DetalleExpedienteResponseVO<CeDirTransferCesioMvVO> getDetalleExpedienteEspecialSitamod(String idExpediente,
			String modalidad) throws BusinessException {
		DetalleExpedienteResponseVO<CeDirTransferCesioMvVO> result = null;

		try {
			result = new DetalleExpedienteResponseVO<CeDirTransferCesioMvVO>();
			// Llamamos el metodo que realiza la llamada al servicio web
			DetalleExpedienteResponse response = doGetDetalleExpediente(idExpediente, modalidad);
			// Comprobamos si se ha devuelto un expediente especial de modelos
			if (response.getDetalleModeloEspecial() != null) {
				CeDirTransferCesioMvVO expediente = dozerBeanMapper.map(response.getDetalleModeloEspecial(), CeDirTransferCesioMvVO.class);
				result.setDetalleExpediente(expediente);
			}
			result.setMensajes(response.getMensajes());
		} catch (Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}

		return result;
	}

	/**
	 * Inserta en la traza del gestor el detalle del expediente consultado
	 * 
	 * @param response
	 *            Respuesta del WS con el expediente solicitado
	 * @throws BusinessException
	 */
	private void insertGestorTraze(DetalleExpedienteResponse response, String modalidad) throws BusinessException {
		// Seteamos los datos del expediente
		StringBuilder detalle = new StringBuilder();
		if (response.getDetalleInvencion() != null) {
			detalle.append("Expediente de invenciones: ");
			detalle.append(response.getDetalleInvencion().getModalidad());
			detalle.append(response.getDetalleInvencion().getNumeroSolicitud());
		} else if (response.getDetalleInvencionCesion() != null) {
			detalle.append("Expediente de invenciones: ");
			detalle.append(Modalidad.AL_TRANSMISION);
			detalle.append(response.getDetalleInvencionCesion().getNumeroCesion());
		} else if (response.getDetalleInvencionLicencia() != null) {
			detalle.append("Expediente de invenciones: ");
			detalle.append(Modalidad.AL_LICENCIA);
			detalle.append(response.getDetalleInvencionLicencia().getNumeroLicencia());
		} else if (response.getDetalleMarca() != null) {
			detalle.append("Expediente de signos distintivos: ");
			detalle.append(response.getDetalleMarca().getModalidad());
			detalle.append(response.getDetalleMarca().getNumero());
		} else if (response.getDetalleMarcaEspecial() != null) {
			detalle.append("Expediente de signos distintivos: ");
			detalle.append(modalidad);
			detalle.append(response.getDetalleMarcaEspecial().getNumTransfer());
		} else if (response.getDetalleModelo() != null) {
			detalle.append("Expediente de diseños: ");
			detalle.append(response.getDetalleModelo().getModalidad());
			detalle.append(response.getDetalleModelo().getNumero());
		} else if (response.getDetalleModeloEspecial() != null) {
			detalle.append("Expediente de diseños: ");
			detalle.append(modalidad);
			detalle.append(response.getDetalleModeloEspecial().getNumTransfer());
		}
		
		// Insertamos en la traza
		trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_DET_EXP, detalle.toString());
	}
	
	/**
	 * Método que elimina los saltos de línea y los espacios
	 * @param cadena
	 * @return
	 */
	private String eliminaEspacios (String cadena) {
		return !StringUtils.isEmptyOrNull(cadena)?cadena.trim().replaceAll("\n", ""):cadena;
	}
	
	/**
	 * Permite pasar los parametros de filtrado al WS de consulta de BUS.
	 * 
	 * @param filter
	 *            Filter Objeto con los parámetros para crear el objeto request.
	 * @return
	 * @throws Exception
	 */
	private BusConsultarExpedientesRequest rellenarDatosConsulta(FiltroBusquedaExpedienteVO filter) throws Exception {
		BusConsultarExpedientesRequest request = new BusConsultarExpedientesRequest();
		
		GregorianCalendar c = new GregorianCalendar();

		// Estado
		request.setEstado(filter.getEstado());

		// Fecha prioridad
		if (filter.getFechaPrioridad() != null) {
			c.setTime(filter.getFechaPrioridad());
			XMLGregorianCalendar fecha = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(c);
			request.setFechaPrioridad(fecha);
		}

		// Fecha publicacion desde
		if (filter.getFechaPublicacionDesde() != null) {
			c.setTime(filter.getFechaPublicacionDesde());
			XMLGregorianCalendar fecha = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(c);
			request.setFechaPublicacionDesde(fecha);
		}

		// Fecha publicacion desde
		if (filter.getFechaPublicacionHasta() != null) {
			c.setTime(filter.getFechaPublicacionHasta());
			XMLGregorianCalendar fecha = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(c);
			request.setFechaPublicacionHasta(fecha);
		}

		// Fecha solicitud desde
		if (filter.getFechaSolicitudDesde() != null) {
			c.setTime(filter.getFechaSolicitudDesde());
			XMLGregorianCalendar fecha = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(c);
			request.setFechaSolicitudDesde(fecha);
		}

		// Fecha solicitud hasta
		if (filter.getFechaSolicitudHasta() != null) {
			c.setTime(filter.getFechaSolicitudHasta());
			XMLGregorianCalendar fecha = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(c);
			request.setFechaSolicitudHasta(fecha);
		}

		request.setModalidad(filter.getModalidad());
		
		//request.setModalidadPublicacion(filter.getModalidadPublicacion());
		
		request.setNumeroPublicacion(filter.getNumeroPublicacion());
		
		if (!StringUtils.isEmptyOrNull(filter.getNumeroPublicacion())) {
			if ("EP".equalsIgnoreCase(filter.getPais()) || "ES".equalsIgnoreCase(filter.getPais())) {
				request.setNumeroPublicacion(filter.getPais()+filter.getNumeroPublicacion());		
			}
		}
		
		request.setModalidadSolicitud(filter.getModalidadSolicitud());
		request.setNombreApAgente(filter.getNombreApAgente());
		request.setNombreApInventor(filter.getNombreApInventor());
		request.setNombreApTitular(filter.getNombreApTitular());
		request.setNumero(filter.getNumero());
		
		request.setNumeroSolicitud(filter.getNumeroSolicitud());
		request.setNumPrioridad(filter.getNumPrioridad());
		request.setTituloInvencion(filter.getTituloInvencion());
		request.setDocTitular(filter.getDocTitular());
		/**
		 * CEO-861 + MAO-333
		 */
		//request.setUsuario(Configuracion.getPropertyAsString("WSECURITY.WSUSER"));
		
		//request.setPass(Configuracion.getPropertyAsString("WSECURITY.WSPASSWORD"));
		// Separa el nº de solicitud
		if(request.getNumeroSolicitud() != null){
			NumSolicitudVO numeroSolicitudASeparar = ExpedienteUtils.separaNumeroSolicitud(request.getNumeroSolicitud(), request.getModalidad()); 
			if(numeroSolicitudASeparar != null){
				if(numeroSolicitudASeparar.getNumeroSolicitud() != null){
					request.setNumeroSolicitud(numeroSolicitudASeparar.getNumeroSolicitud());	
				}
				if(numeroSolicitudASeparar.getModalidad() != null){
					request.setModalidadSolicitud(numeroSolicitudASeparar.getModalidad());	
				}
			}
		}

		// Fecha acto desde
		if (filter.getFechaActoDesde() != null) {
			c.setTime(filter.getFechaActoDesde());
			XMLGregorianCalendar fecha = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(c);
			request.setFechaActoDesde(fecha);
		}

		// Fecha acto hasta
		if (filter.getFechaActoHasta() != null) {
			c.setTime(filter.getFechaActoHasta());
			XMLGregorianCalendar fecha = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(c);
			request.setFechaActoHasta(fecha);
		}

		// Comprobación del usuario
		if (SessionContext.getTipoUsuario() == null) {
			ExceptionUtil.throwBusinessException(new Exception(
					"No hay usuario en la sesión"), "-1",
					"No hay usuario en la sesión");
		} else {
			if (TipoUsuario.AGENTE.toString().equals(
					SessionContext.getTipoUsuario())) {
				
				request.setCodigoAgente(SessionContext.getCodigoAgente());
				
				/**
				 *  MAO-309 : Quitar acceso a expediente por coincidencia de email
				 */
				if (StringUtils.isEmptyOrNull(request.getCodigoAgente())
						) {
					ExceptionUtil.throwBusinessException(new Exception(
							"Sin permiso"), "-1",
							"No hay agente o asociado en la sesión");
				}
			} else if (TipoUsuario.REPRESENTANTE.toString().equals(
					SessionContext.getTipoUsuario())) {
				
				//Si tiene cuenta verificada se envia correo y codigo
				////COMENTADO MAO-309
				/*if (SessionContext.getUserDetails().getAuthorities().contains(new SimpleGrantedAuthority(Roles.ROLE_CUENTA_VERIFICADA))) {
					request.setCodigoRepresentante(SessionContext.getDocumento());
					//request.setEmailRepresentante(SessionContext.getEmailRepresentante());
				} else { //Cuenta no verificada: solo envia el correo
					request.setEmailRepresentante(SessionContext.getEmailRepresentante());
				}*/
				request.setCodigoRepresentante(SessionContext.getDocumento());
				//&& StringUtils.isEmptyOrNull(request.getEmailRepresentante())
				if (StringUtils.isEmptyOrNull(request.getCodigoRepresentante())
						) {
					ExceptionUtil.throwBusinessException(new Exception(
							"Sin permiso"), "-1",
							"No hay agente o asociado en la sesión");
				}
			} else if (TipoUsuario.TITULAR.toString().equals(
					SessionContext.getTipoUsuario())) {
				//Si tiene cuenta verificada se envia correo y codigo
				//////SE COMENTA EL CODIGO PARA QUE NO FALLE CUANDO SE BUSCA UN EXPEDIENTE SI LA CUENTA NO ESTA VERIFICADA
				/*if (SessionContext.getUserDetails().getAuthorities().contains(new SimpleGrantedAuthority(Roles.ROLE_CUENTA_VERIFICADA))) {
					request.setCodigoTitular(SessionContext.getDocumento());
					//request.setEmailTitular(SessionContext.getEmailTitular());
				} else {//Cuenta no verificada: solo envia el correo
					request.setEmailTitular(SessionContext.getEmailTitular());
				}*/
				
				request.setCodigoTitular(SessionContext.getDocumento());
				//&& StringUtils.isEmptyOrNull(request.getEmailTitular())
				if (StringUtils.isEmptyOrNull(request.getCodigoTitular())
						) {
					ExceptionUtil
					.throwBusinessException(
							new Exception("Sin permiso"), "-1",
							"No hay titular en la sesión");
				}
			} else if (TipoUsuario.ASOCIADO.toString().equals(
					SessionContext.getTipoUsuario())) {
				String codAgente = SessionContext.getCodigoAgente();
				String documentoRepresentante = SessionContext
						.getDocumentoRepresentante();
				String documentoTitular = SessionContext.getDocumentoTitular();
				
				/*String emailAgente = SessionContext.getEmailAgente();
				String emailRepresentante = SessionContext.getEmailRepresentante();
				String emailTitular= SessionContext.getEmailTitular();*/
				
				if (!StringUtils.isEmptyOrNull(codAgente)) { //|| !StringUtils.isEmptyOrNull(emailAgente)
					request.setCodigoAgente(codAgente);
					//request.setEmailAgente(emailAgente);
				} else if (!StringUtils.isEmptyOrNull(documentoRepresentante)) {//|| !StringUtils.isEmptyOrNull(emailRepresentante)
					request.setCodigoRepresentante(documentoRepresentante);
					//request.setEmailRepresentante(emailRepresentante);
				} else if (!StringUtils.isEmptyOrNull(documentoTitular)) {// || !StringUtils.isEmptyOrNull(emailTitular)
					request.setCodigoTitular(documentoTitular);
					//request.setEmailTitular(emailTitular);
				} else {
					ExceptionUtil.throwBusinessException(new Exception(
							"Sin permiso"), "-1",
							"No hay asociado en la sesión");
				}
			} else if (!TipoUsuario.GESTOR.toString().equals(
					SessionContext.getTipoUsuario())) {
				// Si no es gestor lanzamos excepción
				ExceptionUtil.throwBusinessException(new Exception(
						"Sin permiso"), "-1", "No hay usuario en la sesión");
			}
		}
		
		return request;
	}
}