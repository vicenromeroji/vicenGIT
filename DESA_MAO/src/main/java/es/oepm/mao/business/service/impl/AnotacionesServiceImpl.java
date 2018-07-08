package es.oepm.mao.business.service.impl;

import java.math.BigDecimal;
import java.net.URL;

import javax.xml.ws.BindingProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.busmule.ws.ceo.anotaciones.IWSBusAnotaciones;
import es.oepm.busmule.ws.ceo.anotaciones.WSBusAnotaciones;
import es.oepm.ceo.ws.anotacionesexp.parameters.ConsultaAnotacionBonoAlfaRequest;
import es.oepm.ceo.ws.anotacionesexp.parameters.ConsultaAnotacionBonoAlfaResponse;
import es.oepm.ceo.ws.anotacionesexp.parameters.ConsultaAnotacionesBonoAlfaAgenteRequest;
import es.oepm.ceo.ws.anotacionesexp.parameters.ConsultaAnotacionesBonoAlfaRepresentanteRequest;
import es.oepm.ceo.ws.anotacionesexp.parameters.ConsultaAnotacionesBonoAlfaResponse;
import es.oepm.ceo.ws.anotacionesexp.parameters.ConsultaAnotacionesBonoAlfaTitularRequest;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.util.DateUtils;
import es.oepm.core.util.StringUtils;
import es.oepm.mao.business.service.AnotacionesService;
import es.oepm.mao.business.service.TrazaGestorService;
import es.oepm.mao.business.vo.ConsultaBonosFilterVO;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.constants.MaoTrazaGestor;
import es.oepm.wservices.core.util.WSUtils;

/**
 * Implementación del servicio para la consulta de anotaciones de bonos.
 * 
 * @author jugonzalez
 *
 */
@Service( value = "anotacionesService" )
@Transactional( propagation = Propagation.SUPPORTS )
public class AnotacionesServiceImpl implements AnotacionesService {
	
	private static final long serialVersionUID = 6976308953702125391L;

	@Autowired
	private TrazaGestorService trazaGestorService;
	
	
	//private static Logger log = Logger.getLogger( AnotacionesServiceImpl.class );
	
//	private static AnotacionesWS wsAnotaciones;
//	
//	static {
//		try {
//			String endpoint = rb.getString( "wsAnotaciones.url" );
//			
//			AnotacionesWService anotacionesWService = new AnotacionesWService( new URL( endpoint ) );
//			
//			wsAnotaciones = anotacionesWService.getAnotacionesWSPort();
//			
//			BindingProvider bp = ( BindingProvider )wsAnotaciones;
//			bp.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint );
//		} catch( MalformedURLException e ) {
//			log.error( "Error al instanciar el wsclient de ANOTACIONES" );
//		}
//	}

	public AnotacionesServiceImpl() {
		super();
	}

	private transient Object lockWsAnotaciones = new Object();
	
	private transient IWSBusAnotaciones wsAnotaciones;
	
	/**
	 * Comprueba el cliente del servicio web
	 */
	private void comprobarClienteAnotaciones() {
		String endpoint="";
		try {
			if (wsAnotaciones == null) {
				synchronized( lockWsAnotaciones ) {
					if( wsAnotaciones == null ) {
						endpoint=Configuracion.getPropertyAsString("url.bus.anotaciones");
						//endpoint = "http://localhost:8081/busmule/WSBusAnotaciones?wsdl";
						
						int receiveTimeout = Configuracion.getPropertyAsInteger( "ws.default.recieveTimeout" );
						int connectionTimeout = Configuracion.getPropertyAsInteger( "ws.default.connectionTimeout" );
						
						if(!StringUtils.isEmptyOrNull(endpoint)){
							WSBusAnotaciones anotacionesWService = new WSBusAnotaciones( new URL( endpoint ) );
							wsAnotaciones = anotacionesWService.getWSBusAnotacionesPort();
							BindingProvider bp = ( BindingProvider )wsAnotaciones;
							bp.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint );
							
							WSUtils.setBindingEndpoint( wsAnotaciones, endpoint );
							WSUtils.setBindingTimeouts( wsAnotaciones, receiveTimeout, connectionTimeout );
						}else
							OepmLogger.error("Error al instanciar el wsclient de Anotaciones. No esta definida en la configuracion la variable url.bus.anotaciones");
					}
				}	
			}
		} catch( Exception e ) {
			wsAnotaciones=null;
			OepmLogger.error("Error al instanciar el wsclient de Anotaciones para la direccion " + endpoint, e);

		}
	}
	
	private IWSBusAnotaciones getWsAnotaciones() {
		comprobarClienteAnotaciones();
		
		return wsAnotaciones;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.AnotacionesService#getAnotacionBono(java.math.BigDecimal)
	 */
	@Override
	public ConsultaAnotacionBonoAlfaResponse getAnotacionBono( BigDecimal idAnotacion ) throws BusinessException {
		ConsultaAnotacionBonoAlfaResponse response = null;
		
		try {
			// Si se trata de un usuario gestor insertamos la traza
			if (SessionContext.getTipoUsuario().equals(TipoUsuario.GESTOR.name())) {
				trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_CNTA_BONO, "IdAnotacion: " + idAnotacion);
			}
			
			ConsultaAnotacionBonoAlfaRequest request = new ConsultaAnotacionBonoAlfaRequest();
			
			request.setIdAnotacion( idAnotacion );
//			response = wsAnotaciones.consultarAnotacionBonoAlfa( request );
			response = getWsAnotaciones().consultarAnotacionBonoAlfa( request );
		} catch( Exception e ) {
			ExceptionUtil.throwBusinessException( e );
		}
		
		return response;
	}

	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.AnotacionesService#getAnotacionesBonoAlfaAgente(es.oepm.mao.business.vo.ConsultaBonosFilterVO)
	 */
	@Override
	public ConsultaAnotacionesBonoAlfaResponse getAnotacionesBonoAlfaAgente(ConsultaBonosFilterVO filter) throws BusinessException {
		ConsultaAnotacionesBonoAlfaResponse response = null;

		try {
			// Si se trata de un usuario gestor insertamos la traza
			if (SessionContext.getTipoUsuario().equals(TipoUsuario.GESTOR.name())) {
				trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_CNTA_BONOS, filter.toTraze());
			}
						
			ConsultaAnotacionesBonoAlfaAgenteRequest requestAgente = new ConsultaAnotacionesBonoAlfaAgenteRequest();

			requestAgente.setAgenteEmail(filter.getAgeEmail());
			requestAgente.setAgenteCodigo(filter.getAgeCodigo());
			requestAgente.setTitularApellidosRazonSocial((filter.getTitularApellido1() + " " + filter.getTitularApellido2()).trim());
			requestAgente.setTitularDocumento(filter.getTitularDocumento());
			requestAgente.setFechaBonoDesde(DateUtils.parseDateToXMLGregCal(filter.getFechaBonoDesde()));
			requestAgente.setFechaBonoHasta(DateUtils.parseDateToXMLGregCal(filter.getFechaBonoHasta()));
			requestAgente.setModalidad(filter.getModalidad());
			requestAgente.setNumSolicitud(filter.getSolicitud());
			requestAgente.setNumPublicacion(filter.getPublicacion());
			
			response = getWsAnotaciones().consultarAnotacionesBonoAlfaAgente( requestAgente );
		} catch( Exception e ) {
			ExceptionUtil.throwBusinessException( e );
		}
		
		return response;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.AnotacionesService#getAnotacionesBonoAlfaRepresentante(es.oepm.mao.business.vo.ConsultaBonosFilterVO)
	 */
	@Override
	public ConsultaAnotacionesBonoAlfaResponse getAnotacionesBonoAlfaRepresentante(ConsultaBonosFilterVO filter) throws BusinessException {
		ConsultaAnotacionesBonoAlfaResponse response = null;
		
		try {
			// Si se trata de un usuario gestor insertamos la traza
			if (SessionContext.getTipoUsuario().equals(TipoUsuario.GESTOR.name())) {
				trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_CNTA_BONOS, filter.toTraze());
			}
			
			ConsultaAnotacionesBonoAlfaRepresentanteRequest requestRepresentante = new ConsultaAnotacionesBonoAlfaRepresentanteRequest();

			requestRepresentante.setRepresentanteEmail(filter.getRepEmail());
			requestRepresentante.setRepresentanteDocumento(filter.getRepDocumento());
			requestRepresentante.setTitularApellidosRazonSocial((filter.getTitularApellido1() + " " + filter.getTitularApellido2()).trim());
			requestRepresentante.setTitularDocumento(filter.getTitularDocumento());
			requestRepresentante.setFechaBonoDesde(DateUtils.parseDateToXMLGregCal(filter.getFechaBonoDesde()));
			requestRepresentante.setFechaBonoHasta(DateUtils.parseDateToXMLGregCal(filter.getFechaBonoHasta()));
			requestRepresentante.setModalidad(filter.getModalidad());
			requestRepresentante.setNumSolicitud(filter.getSolicitud());
			requestRepresentante.setNumPublicacion(filter.getPublicacion());
			
			response = getWsAnotaciones().consultarAnotacionesBonoAlfaRepresentante( requestRepresentante );
		} catch( Exception e ) {
			ExceptionUtil.throwBusinessException( e );
		}
		
		return response;
	}

	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.AnotacionesService#getAnotacionesBonoAlfaTitular(es.oepm.mao.business.vo.ConsultaBonosFilterVO)
	 */
	@Override
	public ConsultaAnotacionesBonoAlfaResponse getAnotacionesBonoAlfaTitular(ConsultaBonosFilterVO filter) throws BusinessException {
		ConsultaAnotacionesBonoAlfaResponse response = null;
		
		try {
			// Si se trata de un usuario gestor insertamos la traza
			if (SessionContext.getTipoUsuario().equals(TipoUsuario.GESTOR.name())) {
				trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_CNTA_BONOS, filter.toTraze());
			}
			
			ConsultaAnotacionesBonoAlfaTitularRequest requestTitular = new ConsultaAnotacionesBonoAlfaTitularRequest();

			requestTitular.setTitularEmail(filter.getTitEmail());
			requestTitular.setTitularDocumento(filter.getTitularDocumento());
			requestTitular.setFechaBonoDesde(DateUtils.parseDateToXMLGregCal(filter.getFechaBonoDesde()));
			requestTitular.setFechaBonoHasta(DateUtils.parseDateToXMLGregCal(filter.getFechaBonoHasta()));
			requestTitular.setModalidad(filter.getModalidad());
			requestTitular.setNumSolicitud(filter.getSolicitud());
			requestTitular.setNumPublicacion(filter.getPublicacion());
			
			response = getWsAnotaciones().consultarAnotacionesBonoAlfaTitular( requestTitular );
		} catch( Exception e ) {
			ExceptionUtil.throwBusinessException( e );
		}
		
		return response;
	}

}