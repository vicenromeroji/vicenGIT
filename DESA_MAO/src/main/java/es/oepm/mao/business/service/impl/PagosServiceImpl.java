package es.oepm.mao.business.service.impl;

import java.math.BigDecimal;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.cxf.common.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.alfa.interfaces.sireco.DatosExpedienteVO;
import es.oepm.alfa.interfaces.sireco.ResultConsultaDatosExpedienteVO;
import es.oepm.alfa.interfaces.sireco.ws.PagosWebServices;
import es.oepm.alfa.interfaces.sireco.ws.PagosWebServicesService;
import es.oepm.alfa.interfaces.sireco.ws.PagosWebServicesServiceLocator;
import es.oepm.busmule.ws.client.ceo.BusExpediente;
import es.oepm.ceo.ws.detalleexp.parameters.DetalleExpedienteResponse;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.util.DateUtils;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.core.view.faces.MessagesUtil;
import es.oepm.mao.business.service.ExpedientesService;
import es.oepm.mao.business.service.PagosService;
import es.oepm.mao.business.vo.CabeceraPasarelaVO;
import es.oepm.mao.business.vo.PagoEsperadoVO;
import es.oepm.mao.business.vo.PagoPrevioVO;
import es.oepm.mao.business.vo.PagoRealizadoVO;
import es.oepm.mao.constants.MaoPropiedadesConf;
import es.oepm.pasarela2WS.ws.client.Pasarela2WS;
import es.oepm.pasarela2WS.ws.client.Pasarela2WSService;
import es.oepm.pasarela2WS.ws.client.Pasarela2WSServiceLocator;
import es.oepm.pasarela2WS.ws.client.entity.InfoPagoBean;
import es.oepm.pasarela2WS.ws.client.entity.InfoPagosPostBean;
import es.oepm.pasarela2WS.ws.client.entity.ResultEncriptaTasasPost;
import es.oepm.pasarela2WS.ws.client.entity.ResultGetIdSesion;
import es.oepm.sireco.webservice.DatosPagoTotalBean;
import es.oepm.sireco.webservice.FiltroPagosExpedientes;
import es.oepm.sireco.webservice.OrdenConsulta;
import es.oepm.sireco.webservice.PagoLibreMao;
import es.oepm.sireco.webservice.ServiceSireco;
import es.oepm.sireco.webservice.ServiceSirecoService;
import es.oepm.sireco.webservice.ServiceSirecoServiceLocator;
import es.oepm.sireco.webservice.TasaMaoBean;
import es.oepm.sireco.ws.SirecoService.beans.RespuestaConsultaTasasPasarela;
import es.oepm.sireco.ws.SirecoService.beans.RespuestaDatosPagoTotal;
import es.oepm.sireco.ws.SirecoService.beans.RespuestaPagosLibresMao;
import es.oepm.sireco.ws.SirecoService.beans.RespuestaTasasMao;

@Service( value = "pagosService" )
@Transactional( propagation = Propagation.SUPPORTS )
public class PagosServiceImpl implements PagosService {
	
	private static final long serialVersionUID = -6458946106736948208L;

	private ServiceSireco wsSireco;
	private ServiceSireco wsSirecoInter;
	private Pasarela2WS wsPasarela2ws;
	private PagosWebServices wsPagosAlfa;

	private Object lockWsSireco = new Object();
	private Object lockWsPagosAlfa = new Object();
	private Object lockPasarela2ws = new Object();
	
	@Autowired
	private ExpedientesService expedientesService;
	
	public void setExpedientesService( ExpedientesService expedientesService ) {
		this.expedientesService = expedientesService;
	}
	
	public PagosServiceImpl() {
//		try {
//			comprobarClienteSireco();
//			comprobarClienteAlfa();			
//		} catch (Exception e) {
//			
//		}
	}
	
	private void comprobarClienteSireco() throws BusinessException {
		String urlWSSireco="";
		try {
			if (wsSireco == null) {
				synchronized( lockWsSireco ) {
					if( wsSireco == null ) {				
						urlWSSireco=Configuracion.getPropertyAsString("url.bus.sireco");
						//urlWSSireco="http://prebussoa2.oepm.local/busmule/SirecoWS";
						
						if(!StringUtils.isEmptyOrNull(urlWSSireco)){
							ServiceSirecoService wsSirecoLocator = new ServiceSirecoServiceLocator();
							wsSireco = wsSirecoLocator.getServiceSirecoPort( new URL(urlWSSireco));
						}else
							OepmLogger.error("Error al instanciar el wsclient de SIRECO. No esta definida en la configuracion la variable url.bus.sireco");
					}
				}
			}
		} catch( Exception e ) {
			wsSireco=null;
			FacesUtil.addErrorMessage( "error.ws.sireco" );
			OepmLogger.error("Error al instanciar el wsclient de SIRECO para la direccion " + urlWSSireco, e);
			throw new BusinessException(e,"");

		}
	}
	//MAO-335
	private void comprobarClienteSirecoInter() throws BusinessException {
		String urlWSSirecoInter="";
		try {
			if(wsSirecoInter==null){							
				urlWSSirecoInter=Configuracion.getPropertyAsString("url.bus.sirecoInter");
					
				if(!StringUtils.isEmptyOrNull(urlWSSirecoInter)){
					ServiceSirecoService wsSirecoLocator = new ServiceSirecoServiceLocator();
					wsSirecoInter = wsSirecoLocator.getServiceSirecoPort( new URL(urlWSSirecoInter));
					
				}else
					OepmLogger.error("Error al instanciar el wsclient de SIRECO interno. No esta definida en la configuracion la variable url.bus.sirecoInter");
				}
		} catch( Exception e ) {
			wsSirecoInter=null;
			FacesUtil.addErrorMessage( "error.ws.sireco" );
			OepmLogger.error("Error al instanciar el wsclient de SIRECO interno para la direccion " + urlWSSirecoInter, e);
			throw new BusinessException(e,"");

		}
	}
	
	private void comprobarClientePasarela2ws() throws BusinessException {
		String urlPasarela2ws="";
		try {
			if (wsPasarela2ws == null) {
				synchronized( lockPasarela2ws ) {
					if( wsPasarela2ws == null ) {				
						urlPasarela2ws=Configuracion.getPropertyAsString(MaoPropiedadesConf.PASARELA2_URL_SERVICIOWEB);
						if(!StringUtils.isEmptyOrNull(urlPasarela2ws)){
							Pasarela2WSService pasarela2wsLocator = new Pasarela2WSServiceLocator();
							wsPasarela2ws = pasarela2wsLocator.getPasarela2WSPort(new URL(urlPasarela2ws));
							ResultGetIdSesion result = wsPasarela2ws.getIdSesion(MaoPropiedadesConf.APLICATION_NAME, MaoPropiedadesConf.PASARELA2_URL_RETORNO);
							result.getIdSesion();
						}else
							OepmLogger.error("Error al instanciar el wsclient de SIRECO. No esta definida en la configuracion la variable url.bus.sireco");
					}
				}
			}
		} catch( Exception e ) {
			wsPasarela2ws=null;
			OepmLogger.error("Error al instanciar el wsclient de Pasarela2ws para la direccion " + urlPasarela2ws, e);
			throw new BusinessException(e,"");
		}
	}
	
	private void comprobarClienteAlfa() throws BusinessException{
		String urlWSPagosAlfa="";
		try {
			if (wsPagosAlfa== null) {
				synchronized( lockWsPagosAlfa ) {
					if( wsPagosAlfa == null ) {
						//urlWSPagosAlfa=Configuracion.getPropertyAsString("url.bus.pagosAlfa");
						urlWSPagosAlfa="http://prealfa.oepm.local/alfa_interfaces_jb_war/PagosWebServices?wsdl";
						//urlWSPagosAlfa="http://localhost:9080/busmule/PagosAlfaWS"//Configuracion.getPropertyAsString("url.bus.pagosAlfa");
						if(!StringUtils.isEmptyOrNull(urlWSPagosAlfa)){
							PagosWebServicesService wsPagosAlfaLocator = new PagosWebServicesServiceLocator();
							wsPagosAlfa = wsPagosAlfaLocator.getPagosWebServicesImplPort( new URL(urlWSPagosAlfa));
						}else {
							OepmLogger.error("Error al instanciar el wsclient de Pagos Alfa. No esta definida en la configuracion la variable url.bus.pagosAlfa");
							FacesUtil.addErrorMessage("error.ws.pagosAlfa");
						}
					}
				}
			}	
		} catch( Exception e ) {
			wsPagosAlfa=null;
			FacesUtil.addErrorMessage( "error.ws.pagosAlfa" );
			OepmLogger.error("Error al instanciar el wsclient de pagos de Alfa para la direccion " + urlWSPagosAlfa, e);
			throw new BusinessException(e,"");

		}
		
	}

	public String parseModalidad( Modalidad modalidad ) {
		String oldModalidad = modalidad.getModalidad();
		
		if( modalidad == Modalidad.DI_DIBUJO ) {
			oldModalidad = "D";
		}
		
		return oldModalidad;
	}
	
	public String parseModalidad( String modalidad ) {
		String oldModalidad = modalidad;
		
		if( modalidad.equals( Modalidad.DI_DIBUJO.getModalidad() ) ) {
			oldModalidad = "D";
		}
		
		return oldModalidad;
	}

	public RespuestaConsultaTasasPasarela getTasas() throws BusinessException {
		RespuestaConsultaTasasPasarela tasas = null;
		
		try {
			comprobarClienteSirecoInter();
			//tasas = wsSireco.consultaTasas();
			//MAO-335
			tasas = wsSirecoInter.consultaTasas();
		} catch (RemoteException re) {
			OepmLogger.error(re);

			ExceptionUtil.throwBusinessException(re);
		}
		
		return tasas;
	}
	
	//TODO: REFACTOR REQUIRED: Create new method to retunt View objects instead to force view to call 2 differente methods (getPagosRealizados and afterwards, parsePagosRealizados)
	public RespuestaDatosPagoTotal getPagosRealizados( String[] expedientes, int pagina, int resultsPorPagina, FiltroPagosExpedientes filtro, OrdenConsulta orden ) throws BusinessException {
		RespuestaDatosPagoTotal pagosRealizados = null;
		
		try {
			comprobarClienteSireco();
			pagosRealizados = wsSireco.pagosMisExpedientesExpes(expedientes,
					String.valueOf(pagina), String.valueOf(resultsPorPagina),
					filtro, orden);
		} catch (RemoteException re) {
			OepmLogger.error(re);

			ExceptionUtil.throwBusinessException(re);
		}
		
		return pagosRealizados;
	}
	
	//TODO: REFACTOR REQUIRED: Create new method to retunt View objects instead to force view to call 2 differente methods (getPagosPrevios and afterwards, parsePagosLibresMao)
	public RespuestaPagosLibresMao getPagosPrevios( String codigoAgente, String nifTitular, String loginMao, int pagina, int resultsPorPagina ) throws BusinessException {
		RespuestaPagosLibresMao pagosPrevios = null;
		
		try {
			comprobarClienteSireco();
			pagosPrevios = wsSireco.pagosLibresMao(
				codigoAgente != null ? codigoAgente : "",
				nifTitular != null ? nifTitular : "", "",
				loginMao != null ? loginMao : "", String.valueOf(pagina),
				String.valueOf(resultsPorPagina));
		} catch( RemoteException re ) {
			OepmLogger.error( re );
			FacesUtil.addErrorMessage( "error.ws.sireco" );
			ExceptionUtil.throwBusinessException( re );
		}
		
		return pagosPrevios;
	}
	
	public RespuestaTasasMao getTasa( Date fecha, String codigo, String descripcion, Modalidad modalidad ) throws BusinessException {
		RespuestaTasasMao tasa = null;
		
		try {
			comprobarClienteSireco();
			tasa = wsSireco.consultaTasasMao(
					fecha != null ? DateUtils.formatFecha(fecha) : "",
					descripcion != null ? descripcion : "",
					codigo != null ? codigo : "",
					modalidad != null ? modalidad.getModalidad() : "");
		} catch( RemoteException re ) {
			OepmLogger.error( re );
			
			ExceptionUtil.throwBusinessException( re );
		}
		
		return tasa;
	}
	
	public List<PagoPrevioVO> parsePagosLibresMao( RespuestaPagosLibresMao resPagosLibre ) {
		List<PagoPrevioVO> pagosPrevios = new ArrayList<PagoPrevioVO>();
		
		if( resPagosLibre != null && resPagosLibre.getPagoLibre() != null ) {
			for( PagoLibreMao pagoLibreMao : resPagosLibre.getPagoLibre() ) {
				try {
					PagoPrevioVO pagoPrevio = new PagoPrevioVO();
					
					pagoPrevio.setCodigoBarras( pagoLibreMao.getCodBarras() );
					pagoPrevio.setFechaPago( pagoLibreMao.getFechaPago() != null ? pagoLibreMao.getFechaPago().getTime() : null );
					pagoPrevio.setImporte( pagoLibreMao.getImporte() );
					
//					if( pagoLibreMao.getFechaPago() != null && !StringUtils.isEmptyOrNull( pagoLibreMao.getCodigoTasa() ) ) {
//						RespuestaTasasMao resTasasMao = getTasa( pagoLibreMao.getFechaPago().getTime(), pagoLibreMao.getCodigoTasa(), null, null );
//						
//						if( resTasasMao != null ) {
//							pagoPrevio.setTasa( resTasasMao.getTasasMao() != null ? resTasasMao.getTasasMao()[0] : null );
//						}
//					}
					
					// La asignacion de la tasa se hace asi porque el webservice esta devolviendo la descripcion de la tasa en vez del codigo  
					pagoPrevio.setTasa( new TasaMaoBean( 0, "", 0F, pagoLibreMao.getCodigoTasa() ) );
					
					pagosPrevios.add( pagoPrevio );
				} catch( Exception e ) {
					OepmLogger.error( e );
				}
			}
		}
		
		// HCS: MAO-433
		pagosPrevios = filtrarPagosUltimoAnyo(pagosPrevios);
		
		return pagosPrevios;
	}
	
//	public List<PagoPrevioVO> parsePagosLibresMao( RespuestaPagosLibresMao resPagosLibre, boolean useCache ) {
//		List<PagoPrevioVO> pagosPrevios = new ArrayList<PagoPrevioVO>();
//		
//		if( resPagosLibre != null && resPagosLibre.getPagoLibre() != null ) {
//			Map<Entry<Date, String>, TasaMaoBean> cacheTasas = null;
//			
//			if( useCache ) {
//				cacheTasas = new HashMap<Entry<Date, String>, TasaMaoBean>( resPagosLibre.getPagoLibre().length );
//			}
//			
//			for( PagoLibreMao pagoLibreMao : resPagosLibre.getPagoLibre() ) {
//				try {
//					PagoPrevioVO pagoPrevio = new PagoPrevioVO();
//					
//					pagoPrevio.setCodigoBarras( pagoLibreMao.getCodBarras() );
//					pagoPrevio.setFechaPago( pagoLibreMao.getFechaPago() != null ? pagoLibreMao.getFechaPago().getTime() : null );
//					pagoPrevio.setImporte( pagoLibreMao.getImporte() );
//					
//					if( pagoLibreMao.getFechaPago() != null && !StringUtils.isEmptyOrNull( pagoLibreMao.getCodigoTasa() ) ) {
//						TasaMaoBean tasa = useCache ? cacheTasas.get( new SimpleEntry<Date, String>( pagoLibreMao.getFechaPago().getTime(), pagoLibreMao.getCodigoTasa() ) ) : null;
//						
//						if( tasa == null ) {
//							RespuestaTasasMao resTasasMao = getTasa( pagoLibreMao.getFechaPago().getTime(), pagoLibreMao.getCodigoTasa(), null, null );
//							
//							if( resTasasMao != null ) {
//								tasa = resTasasMao.getTasasMao() != null ? resTasasMao.getTasasMao()[0] : null;
//								
//								if( useCache ) {
//									cacheTasas.put( new SimpleEntry<Date, String>( pagoLibreMao.getFechaPago().getTime(), pagoLibreMao.getCodigoTasa() ), tasa );
//								}
//							}
//						}
//						
//						pagoPrevio.setTasa( tasa );
//					}
//	
//					pagosPrevios.add( pagoPrevio );
//				} catch( Exception e ) {
//					log.error( e );
//				}
//			}
//		}
//		
//		return pagosPrevios;
//	}
	
	public List<PagoRealizadoVO> parsePagosRealizados( final RespuestaDatosPagoTotal respuestaDatosPagoTotal ) {
		List<PagoRealizadoVO> pagosRealizados = new ArrayList<PagoRealizadoVO>();
		
		if( respuestaDatosPagoTotal != null && respuestaDatosPagoTotal.getPagos() != null ) {
			for( DatosPagoTotalBean datosPagototal : respuestaDatosPagoTotal.getPagos() ) {
				pagosRealizados.add( parsePagoRealizado( datosPagototal ) );
			}
		}
		
		return pagosRealizados;
	}
	
	private PagoRealizadoVO parsePagoRealizado( final DatosPagoTotalBean datosPagototal ) {
		PagoRealizadoVO pagoRealizado = new PagoRealizadoVO();
		
		try {
			if( datosPagototal != null ) {
				pagoRealizado.setCodigoBarras( datosPagototal.getCodBarras() );
				pagoRealizado.setTasaCodigo( datosPagototal.getTasa() );
				
				if( datosPagototal.getTasa() != null ) {
					RespuestaTasasMao resTasasMao = null;
					
					try {
						resTasasMao = getTasa( datosPagototal.getFechaPago().getTime(), datosPagototal.getTasa(), null, null );
					} catch( BusinessException be ) {
						OepmLogger.error( be );
					}
					
					if( resTasasMao != null && resTasasMao.getTasasMao() != null && resTasasMao.getTasasMao().length > 0 ) {
						TasaMaoBean tasaMao = resTasasMao.getTasasMao( 0 );
						
						if( tasaMao != null ) {
							pagoRealizado.setTasaDescripcion( tasaMao.getLiteral() );
						}
					}
				}
				
				pagoRealizado.setSolicitudNumero( datosPagototal.getExpediente() );
				pagoRealizado.setFechaPago( datosPagototal.getFechaPago().getTime() );
				pagoRealizado.setImportePagado( datosPagototal.getImportePagado() );
				pagoRealizado.setImporteDevuelto( datosPagototal.getImporteDevuelto() );
				pagoRealizado.setEstado( datosPagototal.isEstado() ? MessagesUtil.getMessage( "pagos.realizados.estado.bloqueado" ) : MessagesUtil.getMessage( "pagos.realizados.estado.NoBloqueado" ) );
			}
		} catch( Exception e ) {
			OepmLogger.error( e );
		}
		
		return pagoRealizado;
	}
	
//	public List<PagoEsperadoVO> getPagosEsperados( String modalidad, String solicitud, String publicacion ) throws BusinessException {
//		List<PagoEsperadoVO> pagosEsperados = new ArrayList<PagoEsperadoVO>();
//		
//		Modalidad eModalidad = Modalidad.find( modalidad );
//		
//		if( eModalidad.isAlfa() ) {
//			ResultConsultaDatosExpedienteVO resPagosEsperados = null;
//			
//			if( !StringUtils.isEmptyOrNull( modalidad ) && !StringUtils.isEmptyOrNull( solicitud ) ) {
//				resPagosEsperados = getPagosEsperadosBySolicitud( modalidad, solicitud );
//			} else if( !StringUtils.isEmptyOrNull( modalidad ) && !StringUtils.isEmptyOrNull( publicacion ) ) {
//				resPagosEsperados = getPagosEsperadosByPublicacion( modalidad, publicacion );
//			}
//			
//			// Agregar a la lista pagosEsperados los datos recuperados
//			if( resPagosEsperados != null && resPagosEsperados.getResultado() == 0 ) {
//				DatosExpedienteVO datosExpediente = resPagosEsperados.getDatosExpediente();
//				
//				if( datosExpediente != null ) {
//					pagosEsperados.addAll( processPagoEsperado( datosExpediente ) );
//				}
//			}
//		} else {
//			DetalleExpedienteResponse detExpResponse = expedientesService.getDetalleExpediente( modalidad, solicitud );
//			
//			if( detExpResponse != null && detExpResponse.getDetalleMarca() != null ) {
//				CeMnrExpedientesMvVO detExpediente = detExpResponse.getDetalleMarca();
//				
//				PagoEsperadoVO pagoEsperado = new PagoEsperadoVO();
//				
//				try {
//					pagoEsperado.setFechaFinPago( DateUtils.parseFecha( detExpediente.getFechaProxRenova() ) );
//				} catch( ParseException pe ) {
//					log.error( pe );
//				}
//				
//				pagosEsperados.add( pagoEsperado );
//			}
//		}
//		
//		return pagosEsperados;
//	}
	
	public List<PagoEsperadoVO> getPagosEsperados( List<BusExpediente> busExpedientes ) throws BusinessException {
		List<PagoEsperadoVO> pagosEsperados = new ArrayList<PagoEsperadoVO>();
		
		for( BusExpediente busExpediente : busExpedientes ) {
			pagosEsperados.addAll( getPagosEsperados( busExpediente ) );
		}
		
		return pagosEsperados;
	}
	
	public List<PagoEsperadoVO> getPagosEsperados( BusExpediente busExpediente ) throws BusinessException {
		List<PagoEsperadoVO> pagosEsperados = new ArrayList<PagoEsperadoVO>();
		
		Modalidad eModalidad = Modalidad.find( busExpediente.getModalidad() );
		
		if( eModalidad.isAlfa() ) {
			DatosExpedienteVO datosExpediente = getPagoEsperadoDatosExpediente( busExpediente.getModalidad(), busExpediente.getNumeroSolicitud(), busExpediente.getNumeroPublicacion() );
			
			if( datosExpediente != null ) {
				
				List<PagoEsperadoVO> aux = processPagoEsperado( datosExpediente, busExpediente );
				
				if (aux!=null){ 
					pagosEsperados.addAll(aux);
				}
			}
		} else {
			Date now = new Date();
			Calendar calFecProxRenova = busExpediente.getFechaProxRenova().toGregorianCalendar();
			Calendar calcFecIniPago = Calendar.getInstance();
			calcFecIniPago.setTime( calFecProxRenova.getTime() );
			calcFecIniPago.add( Calendar.DATE, -180 );
			
			// HCS: Revisada logica para que solo muestren pagos esperados cuya fecha de inicio sea
			// posterior a hoy.
			if( busExpediente.getFechaProxRenova() != null && calcFecIniPago.before( now )) {
				PagoEsperadoVO pagoEsperado = new PagoEsperadoVO();
			
				TasaMaoBean tasa = getTasa( now, "ME04" );
				
				pagoEsperado.setTasaPagar( tasa != null ? tasa.getCodClave() : "" );
				pagoEsperado.setUnidades( busExpediente.getClasesVigentes() != null ? busExpediente.getClasesVigentes().intValue() : 0 );
				pagoEsperado.setModalidad( busExpediente.getModalidad() );
				pagoEsperado.setSolicitud( busExpediente.getNumeroSolicitud() );
				pagoEsperado.setPublicacion( busExpediente.getNumeroPublicacion() );
				pagoEsperado.setId(busExpediente.getId());
				
				pagoEsperado.setFechaInicioPago( calcFecIniPago.getTime() );
				
				pagoEsperado.setFechaFinPago( calFecProxRenova.getTime() );
				
				Calendar calcFecRec25 = Calendar.getInstance();
				calcFecRec25.setTime( calFecProxRenova.getTime() );
				calcFecRec25.add( Calendar.DATE, 90 );
				
				pagoEsperado.setFechaRecargo25( calcFecRec25.getTime() );
				
				Calendar calcFecRec50 = Calendar.getInstance();
				calcFecRec50.setTime( calFecProxRenova.getTime() );
				calcFecRec50.add( Calendar.DATE, 180 );
				
				pagoEsperado.setFechaRecargo50( calcFecRec50.getTime() );
				
				pagosEsperados.add( pagoEsperado );
			}
		}
		
		return pagosEsperados;
	}
	
	private DatosExpedienteVO getPagoEsperadoDatosExpediente( String modalidad, String solicitud, String publicacion ) throws BusinessException {
		DatosExpedienteVO pagoEsperado = null;
		
		ResultConsultaDatosExpedienteVO resPagosEsperados = null;
			
		if (!StringUtils.isEmptyOrNull( modalidad ) && !StringUtils.isEmptyOrNull( solicitud )) {
			resPagosEsperados = getPagoEsperadoBySolicitud( modalidad, solicitud );
		} else if (!StringUtils.isEmptyOrNull( modalidad ) && !StringUtils.isEmptyOrNull( publicacion )) {
			resPagosEsperados = getPagoEsperadoByPublicacion( modalidad, publicacion );
		}
			
		if (resPagosEsperados != null && resPagosEsperados.getResultado() == 0) {
			pagoEsperado = resPagosEsperados.getDatosExpediente();
		}
		
		return pagoEsperado;
	}
	

	
	private ResultConsultaDatosExpedienteVO getPagoEsperadoBySolicitud( String modalidad, String solicitud ) throws BusinessException {
		ResultConsultaDatosExpedienteVO resDatosExp = null;
		
		if (modalidad.equals(Modalidad.AL_PATENTE_EUROPEA.getModalidad())) {
			resDatosExp = getPagoEsperado( "S", solicitud );
		} else {
			resDatosExp = getPagoEsperado( modalidad, solicitud );
		}
		
		return resDatosExp;
	}
	
	private ResultConsultaDatosExpedienteVO getPagoEsperadoByPublicacion( String modalidad, String publicacion ) throws BusinessException {
		return getPagoEsperado(modalidad, publicacion);
	}
	
	private ResultConsultaDatosExpedienteVO getPagoEsperado( String modalidad, String numSolicitudPublicacion ) throws BusinessException {
		ResultConsultaDatosExpedienteVO resDatosExp = null;
		
		try {
			comprobarClienteAlfa();
			resDatosExp = wsPagosAlfa.consultaDatosPagosExpediente( numSolicitudPublicacion, modalidad, "0" );
			
			if( resDatosExp != null && resDatosExp.getDatosExpediente() != null && resDatosExp.getDatosExpediente().getAnualidadEsperada() != null) {
				resDatosExp = wsPagosAlfa.consultaDatosPagosExpediente( numSolicitudPublicacion, modalidad, String.valueOf( resDatosExp.getDatosExpediente().getAnualidadEsperada() ) );
			}
			
			if( resDatosExp != null && resDatosExp.getDatosExpediente() == null ) {
				OepmLogger.info("Fallo al consultar WS ALfa:"+resDatosExp.getDescripcion());
			} else if (resDatosExp == null) {
				OepmLogger.error("Fallo al consultar WS Alfa");
			}

		} catch (RemoteException re) {
			
			OepmLogger.error(re);		
			ExceptionUtil.throwBusinessException(re);
		}
		
		return resDatosExp;
	}
	
	private List<PagoEsperadoVO> processPagoEsperado( DatosExpedienteVO datosExpediente, BusExpediente busExpediente ) throws BusinessException {
		List<PagoEsperadoVO> pagosEsperados = new ArrayList<PagoEsperadoVO>();
		
		Date now = new Date();
		Date fechaDevengo = datosExpediente.getPlazosHabiles( 0 ).getTime();
		
		if (!estaPagoEsperadoFueraFecha(now, datosExpediente) && datosExpediente.getConcedido()) {
			
			// HCS: Solo podremos pagar anualidades cuya fecha de devengo sea anterior a hoy.
			if (fechaDevengo.before(now))
			{
				PagoEsperadoVO auxPagoEsperado = parsePagoEsperado( now, datosExpediente, busExpediente );
				
				if (auxPagoEsperado != null) {
					pagosEsperados.add(auxPagoEsperado);
				}
				
				boolean equalsC = datosExpediente.getModalidad().equals(Modalidad.AL_PATENTE_NACIONAL.getModalidad());
				boolean firstPartC = !datosExpediente.getPagadoIET() && !datosExpediente.getExencionIBIPrevio() && datosExpediente.getExencion() != 1;
				boolean secondPartC = !datosExpediente.getPagadoIET() && datosExpediente.getAplazamientoTasas();
				if (equalsC && (firstPartC || secondPartC)) {
					
					PagoEsperadoVO auxPagoEsperadoAdic = createPagoEsperadoAdic(now, datosExpediente, busExpediente);
					if (auxPagoEsperadoAdic != null) {
						pagosEsperados.add(auxPagoEsperadoAdic);
					}
				}
			}
		}
		
		return pagosEsperados;
	}
	
	/**
	 * Filtra los pagos del ultimo año y devuelve unicamente
	 * los pagos previos con fecha del ultimo anyo
	 */
	private List<PagoPrevioVO> filtrarPagosUltimoAnyo(List<PagoPrevioVO> pagos) 
	{
		List<PagoPrevioVO> result = null;
		
		if (!CollectionUtils.isEmpty(pagos)) 
		{	
			result = new ArrayList<PagoPrevioVO>();

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.YEAR, -1); 
			Date lastYear = cal.getTime();
				
			for (PagoPrevioVO pago : pagos) {
				if (pago.getFechaPago().after(lastYear)) {
					result.add(pago);
				}	
			}
		}
		return result;
	}
	
	private PagoEsperadoVO createPagoEsperadoAdic( Date fecha, DatosExpedienteVO datosExpediente, BusExpediente busExpediente ) throws BusinessException {
		PagoEsperadoVO pagoEsperadoAdic = new PagoEsperadoVO();

		Date fecIniPago = null;
		Date fecFinPago = null;
		
		if( !datosExpediente.getPagadoIET() && !datosExpediente.getExencionIBIPrevio() && datosExpediente.getExencion() != 1 ) {
			Calendar fecPresentacion = null;
			Calendar fecPrioridad = null;
			Calendar fecPubContinuacion = null;
			
			if( busExpediente.getFechaPresentacion() != null ) {
				fecPresentacion = busExpediente.getFechaPresentacion().toGregorianCalendar();
				
				fecPresentacion.add( Calendar.MONTH, 15 );
			}
			
			if( busExpediente.getFechaPrioridad() != null ) {
				fecPrioridad = busExpediente.getFechaPrioridad().toGregorianCalendar();
				
				fecPrioridad.add( Calendar.MONTH, 15 );
			}
			
			if( busExpediente.getFechaAnotacionPublicacionContinuacion() != null ) {
				fecPubContinuacion = busExpediente.getFechaAnotacionPublicacionContinuacion().toGregorianCalendar();
				
				fecPubContinuacion.add( Calendar.MONTH, 1 );
			}
			
			fecFinPago = calcPagoEsperadoAdicFechaFinPagoSinAplazamiento( fecPresentacion, fecPrioridad, fecPubContinuacion );
		} else if( !datosExpediente.getPagadoIET() && datosExpediente.getAplazamientoTasas() ) {
			Calendar fecPresentacion = null;
			
			if( busExpediente.getFechaPresentacion() != null ) {
				fecPresentacion = busExpediente.getFechaPresentacion().toGregorianCalendar();
				
				fecPresentacion.add( Calendar.YEAR, 3 );
				
				fecPresentacion.set( Calendar.DAY_OF_MONTH, 1 );
				fecPresentacion.add( Calendar.MONTH, -2 );
				
				fecIniPago = fecPresentacion.getTime();
				
				fecPresentacion.add( Calendar.MONTH, 3 );
				fecPresentacion.set( Calendar.DAY_OF_MONTH, fecPresentacion.getActualMaximum( Calendar.DAY_OF_MONTH ) );
				
				fecFinPago = fecPresentacion.getTime();
			}
		}
		
		TasaMaoBean tasa = getTasa( fecha, calcPagoEsperadoTasaCodigo( true, fecha, datosExpediente ) );
		
		pagoEsperadoAdic.setTasaPagar( tasa != null ? tasa.getCodClave() : "" );
		pagoEsperadoAdic.setUnidades( busExpediente.getClasesVigentes() != null ? busExpediente.getClasesVigentes().intValue() : 0 );
		pagoEsperadoAdic.setAplazamientoTasas( datosExpediente.getAplazamientoTasas() );
		pagoEsperadoAdic.setConcedido( datosExpediente.getConcedido() );
		pagoEsperadoAdic.setModalidad( datosExpediente.getModalidad() );
		pagoEsperadoAdic.setSolicitud( datosExpediente.getNumSolicitud() );
		pagoEsperadoAdic.setPublicacion( datosExpediente.getNumPublicacion() );
		pagoEsperadoAdic.setFechaInicioPago( fecIniPago );
		pagoEsperadoAdic.setFechaFinPago( fecFinPago );
		pagoEsperadoAdic.setId(busExpediente.getId());
		
		return pagoEsperadoAdic;
	}
	
	private Date calcPagoEsperadoAdicFechaFinPagoSinAplazamiento( Calendar fecPresentacion, Calendar fecPrioridad, Calendar fecPubContinuacion ) {
		Date fecFinPago = null;
		
		if( fecPubContinuacion != null ) {
			if( fecPresentacion != null ) {
				if( fecPrioridad != null ) {
					if( fecPresentacion.after( fecPrioridad ) ) {
						if( fecPresentacion.after( fecPubContinuacion ) ) {
							fecFinPago = fecPresentacion.getTime();
						} else {
							fecFinPago = fecPubContinuacion.getTime();
						}
					} else {
						if( fecPrioridad.after( fecPubContinuacion ) ) {
							fecFinPago = fecPrioridad.getTime();
						} else {
							fecFinPago = fecPubContinuacion.getTime();
						}
					}
				} else {
					if( fecPresentacion.after( fecPubContinuacion ) ) {
						fecFinPago = fecPresentacion.getTime();
					} else {
						fecFinPago = fecPubContinuacion.getTime();
					}
				}
			} else if( fecPrioridad != null ) {
				if( fecPrioridad.after( fecPubContinuacion ) ) {
					fecFinPago = fecPrioridad.getTime();
				} else {
					fecFinPago = fecPubContinuacion.getTime();
				}
			} else {
				fecFinPago = fecPubContinuacion.getTime();
			}
		} else {
			if( fecPresentacion != null ) {
				if( fecPrioridad != null ) {
					if( fecPresentacion.after( fecPrioridad ) ) {
						fecFinPago = fecPresentacion.getTime();
					} else {
						fecFinPago = fecPrioridad.getTime();
					}
				} else {
					fecFinPago = fecPresentacion.getTime();
				}
			} else if( fecPrioridad != null ) {
				fecFinPago = fecPrioridad.getTime();
			}
		}
		
		return fecFinPago;
	}
	
	public PagoEsperadoVO parsePagoEsperado( Date fecha, DatosExpedienteVO datosExpediente, BusExpediente busExpediente ) throws BusinessException {
		PagoEsperadoVO pagoEsperado = new PagoEsperadoVO();
		
		TasaMaoBean tasa = getTasa( fecha, calcPagoEsperadoTasaCodigo( false, fecha, datosExpediente ) );
		
		pagoEsperado.setTasaPagar( tasa != null ? tasa.getCodClave() : null );
		pagoEsperado.setUnidades( busExpediente.getClasesVigentes() != null ? busExpediente.getClasesVigentes().intValue() : 0 );
		pagoEsperado.setAplazamientoTasas( datosExpediente.getAplazamientoTasas() );
		pagoEsperado.setConcedido( datosExpediente.getConcedido() );
		pagoEsperado.setModalidad( datosExpediente.getModalidad() );
		pagoEsperado.setSolicitud( datosExpediente.getNumSolicitud() );
		pagoEsperado.setPublicacion( datosExpediente.getNumPublicacion() );
		pagoEsperado.setFechaInicioPago( datosExpediente.getPlazosHabiles( 0 ).getTime() );
		pagoEsperado.setFechaFinPago( datosExpediente.getPlazosHabiles( 3 ).getTime() );
		pagoEsperado.setFechaRecargo25( datosExpediente.getPlazosHabiles( 4 ).getTime() );
		pagoEsperado.setFechaRecargo50( datosExpediente.getPlazosHabiles( 5 ).getTime() );
		pagoEsperado.setFechaRehabilitacion( datosExpediente.getPlazosHabiles( 6 ).getTime() );
		pagoEsperado.setId(busExpediente.getId());
		
		return pagoEsperado;
	}
	
	private boolean estaPagoEsperadoFueraFecha( Date fecha, DatosExpedienteVO datosExpediente ) {
		boolean estaFueraFecha = true;
		
		if( fecha != null && datosExpediente != null ) {
			
			// HCS: Se resuelve un null pointer exception y además se simplifica el codigo
			// de la aplicación.
			for(int i = 3; i<=6; i++)
			{
				if (datosExpediente.getPlazosHabiles(i) != null) 
				{
					if (estaFueraFecha) {
						estaFueraFecha = datosExpediente.getPlazosHabiles(i).getTime().before( fecha );
					}
				}
			}			
		}
		
		return estaFueraFecha;
	}
	
	private String calcPagoEsperadoTasaCodigo( boolean esPagoEsperadoAdic, Date fecha, DatosExpedienteVO datosExpediente ) {
		String tasaCodigo = null;
		
		if( fecha != null && datosExpediente != null ) {
			if( !esPagoEsperadoAdic ) {
				if( datosExpediente.getReducida() == 1 ) {
					if( datosExpediente.getPlazosHabiles( 3 ).getTime().after( fecha ) ) {
						tasaCodigo = String.format( "IR%02d", datosExpediente.getAnualidadEsperada() );
					} else if( datosExpediente.getPlazosHabiles( 4 ).getTime().after( fecha ) ) {
						tasaCodigo = String.format( "2R%02d", datosExpediente.getAnualidadEsperada() );
					} else if( datosExpediente.getPlazosHabiles( 5 ).getTime().after( fecha ) ) {
						tasaCodigo = String.format( "5R%02d", datosExpediente.getAnualidadEsperada() );
					} else if( datosExpediente.getPlazosHabiles( 6 ).getTime().after( fecha ) ) {
						if( datosExpediente.getModalidad().equals( Modalidad.AL_MODELO_UTILIDAD.getModalidad() ) ) {
							tasaCodigo = "IR10";
						} else if( datosExpediente.getModalidad().equals( Modalidad.AL_PATENTE_INVENCION.getModalidad() ) ||
								   datosExpediente.getModalidad().equals( Modalidad.AL_PATENTE_NACIONAL.getModalidad() ) ||
								   datosExpediente.getModalidad().equals( Modalidad.AL_PATENTE_EUROPEA.getModalidad() ) ) {
							tasaCodigo = "IR20";
						}
					}
				} else {
					if( datosExpediente.getPlazosHabiles( 3 ).getTime().after( fecha ) ) {
						tasaCodigo = String.format( "IP%02d", datosExpediente.getAnualidadEsperada() );
					} else if( datosExpediente.getPlazosHabiles( 4 ).getTime().after( fecha ) ) {
						tasaCodigo = String.format( "2P%02d", datosExpediente.getAnualidadEsperada() );
					} else if( datosExpediente.getPlazosHabiles( 5 ).getTime().after( fecha ) ) {
						tasaCodigo = String.format( "5P%02d", datosExpediente.getAnualidadEsperada() );
					} else if( datosExpediente.getPlazosHabiles( 6 ).getTime().after( fecha ) ) {
						if( datosExpediente.getModalidad().equals( Modalidad.AL_MODELO_UTILIDAD.getModalidad() ) ) {
							tasaCodigo = "IP10";
						} else if( datosExpediente.getModalidad().equals( Modalidad.AL_PATENTE_INVENCION.getModalidad() ) ||
								   datosExpediente.getModalidad().equals( Modalidad.AL_PATENTE_NACIONAL.getModalidad() ) ||
								   datosExpediente.getModalidad().equals( Modalidad.AL_PATENTE_EUROPEA.getModalidad() ) ) {
							tasaCodigo = "IP20";
						}
					}
				}
			} else {
				tasaCodigo = String.format( "IE%02d", datosExpediente.getAnualidadEsperada() );
			}
		}
		
		return tasaCodigo;
	}
	
	private TasaMaoBean getTasa(Date fecha, String tasaCodigo) {
		TasaMaoBean tasa = null;
		
		if( tasaCodigo != null ) {
			RespuestaTasasMao resTasa = null;
			
			try {
				resTasa = getTasa( fecha, tasaCodigo, null, null );
			} catch (BusinessException be) {
				OepmLogger.error(be);
			}
			
			if (resTasa != null && resTasa.getResultado() == 0
					&& resTasa.getTasasMao() != null
					&& resTasa.getTasasMao().length > 0) {
				tasa = resTasa.getTasasMao( 0 );
			}
		}
		
		return tasa;
	}
	
		
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.PagosService#obtenerIdSesionPasarelaDePago()
	 */
	public ResultGetIdSesion obtenerIdSesionPasarelaDePago() throws BusinessException{
		ResultGetIdSesion resultGetIdSesion = null;

		comprobarClientePasarela2ws();
		// tasas = wsSireco.consultaTasas();
		try {
			resultGetIdSesion = wsPasarela2ws
					.getIdSesion(
							Configuracion
									.getPropertyAsString(MaoPropiedadesConf.APLICATION_NAME),
							Configuracion
									.getPropertyAsString(MaoPropiedadesConf.PASARELA2_URL_RETORNO));
		} catch (RemoteException re) {
			OepmLogger.error(re);
			ExceptionUtil.throwBusinessException(re);
		}

		return resultGetIdSesion;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.PagosService#obtenerEncriptacionTasas()
	 */
	public ResultEncriptaTasasPost obtenerEncriptacionTasas(CabeceraPasarelaVO cabeceraPasarelaVO, PagoEsperadoVO pagoEsperadoVO) throws BusinessException {
		ResultEncriptaTasasPost resulEncriptaTasasPost = null;
		
		InfoPagosPostBean infoPagosPostBean = crearObjetoInfopagos(cabeceraPasarelaVO, pagoEsperadoVO);
		
		comprobarClientePasarela2ws();
		// tasas = wsSireco.consultaTasas();
		try {
			resulEncriptaTasasPost = wsPasarela2ws.encriptaTasasPost(infoPagosPostBean);
		} catch (RemoteException re) {
			OepmLogger.error(re);

			ExceptionUtil.throwBusinessException(re);
		}
		
		return resulEncriptaTasasPost;
	}
	
	/**
	 * Crea un objeto de tipo InfoPagosPostBean con los datos necesarios para llamar al servicio
	 * @return
	 * @throws BusinessException 
	 */
	private InfoPagosPostBean crearObjetoInfopagos(CabeceraPasarelaVO cabeceraPasarelaVO, PagoEsperadoVO pagoEsperadoVO) throws BusinessException {
		// Cabecera del objeto
		InfoPagosPostBean infoPagosPostBean = new InfoPagosPostBean();
		infoPagosPostBean.setAplicacionOrigen(Configuracion.getPropertyAsString(MaoPropiedadesConf.APLICATION_NAME));
		infoPagosPostBean.setCodRepresentante(cabeceraPasarelaVO.getCodRepresentante());
		infoPagosPostBean.setEmail(cabeceraPasarelaVO.getEmail());
		infoPagosPostBean.setIdentificador(new BigDecimal(cabeceraPasarelaVO.getIdSesion()));
		infoPagosPostBean.setNif(cabeceraPasarelaVO.getNif());
		infoPagosPostBean.setNomApe(cabeceraPasarelaVO.getNomApe());
		infoPagosPostBean.setTipoIdentificacion(cabeceraPasarelaVO.getTipoIdentificacion());
		infoPagosPostBean.setTipoPasarela(cabeceraPasarelaVO.getTipoPasarela());
		
		if(infoPagosPostBean.getCodRepresentante() != null) {
			// Recuperamos el expediente completo para recuperar el titular del expediente
			DetalleExpedienteResponse result = expedientesService.getDetalleExpediente(pagoEsperadoVO.getId(), pagoEsperadoVO.getModalidad());
			if (result != null && result.getResultado() == DetalleExpedienteResponse.RESULTADO_OK) {
				// Comprobamos de que tipo de detalle se trata
				if(result.getDetalleInvencion() != null) {
					if (!StringUtils.isEmptyOrNull(result.getDetalleInvencion().getTitular().getNombreApellidos())) {
						infoPagosPostBean.setNomApeTitExp(result.getDetalleInvencion().getTitular().getNombreApellidos());
					} else {
						infoPagosPostBean.setNomApeTitExp(result.getDetalleInvencion().getTitular().getRazonSocial());
					}
				} else if (result.getDetalleInvencionCesion() != null){
					if (!StringUtils.isEmptyOrNull(result.getDetalleInvencionCesion().getCesionarios().get(0).getNombreApellidos())) {
						infoPagosPostBean.setNomApeTitExp(result.getDetalleInvencion().getTitular().getNombreApellidos());
					} else {
						infoPagosPostBean.setNomApeTitExp(result.getDetalleInvencionCesion().getCesionarios().get(0).getNombreApellidos());
					}
				} else if (result.getDetalleInvencionLicencia() != null){
					if (!StringUtils.isEmptyOrNull(result.getDetalleInvencionLicencia().getLicenciatarios().get(0).getNombreApellidos())) {
						infoPagosPostBean.setNomApeTitExp(result.getDetalleInvencionLicencia().getLicenciatarios().get(0).getNombreApellidos());
					} else {
						infoPagosPostBean.setNomApeTitExp(result.getDetalleInvencionLicencia().getLicenciatarios().get(0).getNombreApellidos());
					}
					infoPagosPostBean.setNomApeTitExp(result.getDetalleModelo().getTitular().getNombreApellidos());
				} else if (result.getDetalleMarca() != null){
					infoPagosPostBean.setNomApeTitExp(result.getDetalleMarca().getTitular().getNombreApellidos());
				} else if (result.getDetalleMarcaEspecial() != null){
					infoPagosPostBean.setNomApeTitExp(result.getDetalleMarcaEspecial().getNombreApellidos());
				} else if (result.getDetalleModelo() != null){
					infoPagosPostBean.setNomApeTitExp(result.getDetalleModelo().getTitular().getNombreApellidos());
				} else if (result.getDetalleModeloEspecial() != null){
					infoPagosPostBean.setNomApeTitExp(result.getDetalleModeloEspecial().getNombreApellidos());
				} else {
					ExceptionUtil.throwBusinessException(ExceptionUtil.GENERIC_ERROR_KEY);
				}
			} else {
				ExceptionUtil.throwBusinessException(ExceptionUtil.GENERIC_ERROR_KEY);
			}
		}
		
		// Informacion del pago
		InfoPagoBean infoPagoBean = new InfoPagoBean();
		infoPagoBean.setAnyoPago(Calendar.getInstance().get(Calendar.YEAR));
		infoPagoBean.setClave(pagoEsperadoVO.getTasaPagar());
		infoPagoBean.setModalidadExp(pagoEsperadoVO.getModalidad());
		infoPagoBean.setNumExpediente(pagoEsperadoVO.getSolicitud());
		infoPagoBean.setNumUnidades(pagoEsperadoVO.getUnidades());
		infoPagosPostBean.setListaPagos(new InfoPagoBean[] {infoPagoBean ,});
		
		return infoPagosPostBean;
	}
	
}