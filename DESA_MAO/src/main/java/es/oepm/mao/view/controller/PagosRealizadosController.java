package es.oepm.mao.view.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.primefaces.event.data.PageEvent;
import org.primefaces.event.data.SortEvent;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.busmule.ws.client.ceo.BusExpediente;
import es.oepm.busmule.ws.client.ceo.parameters.BusConsultarExpedientesResponse;
import es.oepm.core.business.BaseVO;
import es.oepm.core.business.ceo.vo.CeAlrCesionMvVO;
import es.oepm.core.business.ceo.vo.CeAlrExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeAlrLicenciasMvVO;
import es.oepm.core.business.ceo.vo.CeDirExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeMnrExpedientesMvVO;
import es.oepm.core.business.mao.vo.UsuariosAgenteVO;
import es.oepm.core.constants.Mensaje;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.constants.Roles;
import es.oepm.core.constants.Sistemas;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.util.DateUtils;
import es.oepm.core.util.ExpedienteUtils;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.controller.ListController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.core.view.faces.SelectItemList;
import es.oepm.core.view.util.HTMLUtil;
import es.oepm.mao.business.service.ExpedientesService;
import es.oepm.mao.business.service.PagosService;
import es.oepm.mao.business.service.TrazaGestorService;
import es.oepm.mao.business.vo.FormaPagoSIRECO;
import es.oepm.mao.business.vo.PagoRealizadoVO;
import es.oepm.mao.business.vo.PagosRealizadosFilterVO;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.comun.business.service.UsuariosAgenteService;
import es.oepm.mao.constants.MaoTrazaGestor;
import es.oepm.mao.view.controller.util.ExpedientesFilter;
import es.oepm.mao.view.controller.util.GeneradorValoresCombo;
import es.oepm.mao.view.controller.util.JSFPages;
import es.oepm.mao.view.controller.util.TasasListado;
import es.oepm.sireco.bl.beans.TasaPasarelaBean;
import es.oepm.sireco.webservice.FiltroPagosExpedientes;
import es.oepm.sireco.webservice.OrdenConsulta;
import es.oepm.sireco.ws.SirecoService.beans.RespuestaDatosPagoTotal;
import es.oepm.wservices.core.mensajes.Mensajes;
import es.oepm.wservices.core.util.WSUtils;

@ManagedBean( name = "pagosRealizadosController" )
@ViewScoped
public class PagosRealizadosController extends BaseController implements ListController {

	private static final long serialVersionUID = -1441913967806505014L;
	
	private String solModalidad;
	private String solNumero;
	//MAO-333
	private String solNumeroPublicacion;
	private boolean fromDetExp;
	private String tituloDetalle;
	
	private boolean mustLoadList;
	private PagosRealizadosFilterVO filter;	
	private List<PagoRealizadoVO> pagosRealizados;
	
	private List<SelectItem> formasPago;
	private List<SelectItem> tasas;
	private List<SelectItem> modalidades;
	
	// Al entrar con ususario Gestor
	public String documentoUsuario;
	public String emailUsuario;
	public List<String> tiposUsuario = obtenerListaUsuariosSinTitular();
	public String usuarioSeleccionado;
	public String emailTitular;
	
	@ManagedProperty( "#{pagosService}" )
	private PagosService pagosService;
	
	@ManagedProperty( "#{expedientesService}" )
	private ExpedientesService expedientesService;
	
	@ManagedProperty( "#{usuariosAgenteService}" )
	private UsuariosAgenteService usuariosAgenteService;
	
	@ManagedProperty( "#{trazaGestorService}" )
	private TrazaGestorService trazaGestorService;
	
	public PagosRealizadosController() {
		super();
		Object exp = SessionUtil.getFromSession("detalleExpediente");
		if(exp instanceof CeMnrExpedientesMvVO ){
			solModalidad = ((CeMnrExpedientesMvVO) exp).getModalidad();
			solNumero = ((CeMnrExpedientesMvVO)exp).getNumero();		
		}else if(exp instanceof CeAlrExpedientesMvVO ){
			solModalidad = ((CeAlrExpedientesMvVO) exp).getModalidad();
			solNumero = ((CeAlrExpedientesMvVO)exp).getNumeroSolicitud();			
			//MAO-333
			solNumeroPublicacion = ((CeAlrExpedientesMvVO) exp).getNumeroPublicacion();
		}else if(exp instanceof CeDirExpedientesMvVO ){
			solModalidad = ((CeDirExpedientesMvVO) exp).getModalidad();
			solNumero = ((CeDirExpedientesMvVO)exp).getNumero();
		}else if(exp instanceof CeAlrLicenciasMvVO) {
			solModalidad = Modalidad.AL_LICENCIA.getModalidad();
			solNumero = ((CeAlrLicenciasMvVO)exp).getNumeroLicencia();
		}else if(exp instanceof CeAlrCesionMvVO) {
			solModalidad = Modalidad.AL_TRANSMISION.getModalidad();
			solNumero = ((CeAlrCesionMvVO)exp).getNumeroCesion();
		}

		this.tituloDetalle = (String)SessionUtil.getFromSession("tituloDetalle");

		// Indica si la peticion proviene del detalle de un expediente
		fromDetExp = Boolean.valueOf( FacesUtil.getParameter( "fromDetExp" ) );
		
		if( !fromDetExp ) {
			BaseVO sessionValue = SessionUtil.getSearchFilter( true );
			
			if( ( sessionValue != null ) && ( sessionValue instanceof PagosRealizadosFilterVO ) ) {
				setFilter( sessionValue );
	
				mustLoadList = true;
			} else {			
				SessionUtil.clearSearchFilter();
			}
			
			formasPago = HTMLUtil.getSelectItemList( SelectItemList.createDefault(),
													 Arrays.asList( FormaPagoSIRECO.values() ), 
													 FormaPagoSIRECO.class, "formaPagoValue", "formaPagoKey" );
			
			List<Modalidad> modalidadesPagos = GeneradorValoresCombo
					.getListaModalidadesCompleta((Collection<SimpleGrantedAuthority>) SessionContext
							.getUserDetails().getAuthorities());
			
			modalidades = HTMLUtil.getSelectItemListByGroup( SelectItemList.createDefault(),
															 modalidadesPagos ,
															 Modalidad.class, "textoModalidad", "modalidad", "" );
		} else {			
			filter = getFilter();
		}
	}
	
	@PostConstruct
	public void init()
	{
	    if (fromDetExp && pagosRealizados==null) 
	    {
	    	this.actionSearch();
	    }
	}
	
	public String getSolModalidad() {
		return solModalidad;
	}
	
	public String getSolNumero() {
		return solNumero;
	}
	
	public String getSolNumeroPublicacion() {
		return solNumeroPublicacion;
	}
	
	public boolean getFromDetExp() {
		return fromDetExp;
	}
	
	public List<SelectItem> getFormasPago() {
		return formasPago;
	}
	
	public List<SelectItem> getTasas() {
		if( tasas == null )  {			
			tasas = getTasasByModalidad(null);
		}
		
		return tasas;
	}
	
	public List<SelectItem> getTasasByModalidad(String modalidad)
	{
		List<TasaPasarelaBean> listTasas;
		
		try {
			listTasas = TasasListado.getInstance().getTasas();
			
			List<TasaPasarelaBean> newListTasas = new ArrayList<TasaPasarelaBean>();			
			newListTasas = filtrarTasasPorModalidad(modalidad,listTasas);		
			
			if( listTasas != null && newListTasas != null ) {
				tasas = HTMLUtil.getSelectItemList( SelectItemList.createDefault(),newListTasas, 
													TasaPasarelaBean.class, "nombre", "codigo" );
				
				//HCS: (MAO-370) Anteponemos el codigo al texto de la descripcion de la tasa
				for (SelectItem tasaItem : tasas) {
					if ( tasaItem.getValue() != null)
					{
						String newLabel = tasaItem.getValue() +" - " + tasaItem.getLabel();
						tasaItem.setLabel(newLabel);
					}
				}
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		return tasas;
	}
	
	
	private List<TasaPasarelaBean> filtrarTasasPorModalidad (String modalidad, List<TasaPasarelaBean> listTasas) throws Exception
	{
		List<TasaPasarelaBean> newListTasas = new ArrayList<TasaPasarelaBean>();
		
		// Filtramos por modalidad
		if (!StringUtils.isEmptyOrNull(modalidad)) {
			newListTasas = TasasListado.getInstance().getTasaPorModalidad(modalidad);
		}
		
		return newListTasas;
	}
	
	public List<SelectItem> getModalidades() {
		return modalidades;
	}
	
	public List<PagoRealizadoVO> getPagosRealizados() {		
		return pagosRealizados;
	}

	/**
	 * Evento que se dispara al cambiar la modalidad de los
	 * pagos realizados.
	 * Modifica la lista de tasas en función de la modalidad
	 * seleccionada.
	 * @param event
	 */
	public void modalidadChangeListener()
	{
		String modalidad = null;
	
		if (filter != null) {
			modalidad = filter.getModalidad();
		}		
		tasas = getTasasByModalidad(modalidad);
	}
	
	@Override
	public String actionSearch() {
		List<String> expedientes = new ArrayList<String>();
		boolean error= false;
		/**
		 * MAO-333
		 */
		if( !WSUtils.isAvailable(  Configuracion.getPropertyAsString( "url.bus.sireco" )) || 
			!WSUtils.isAvailable(  Configuracion.getPropertyAsString( "url.bus.consultaExp" ) ) ) {
			FacesUtil.addErrorMessage( "pagos.realizados.error.recuperarDatos" );
		} else {
			if( !fromDetExp ) {
				try {
					// Si se ha seteado modalidad comprobamos que sea accesible
					// al usuario
					if (!StringUtils.isEmptyOrNull(filter.getModalidad())) {
						Modalidad m = Modalidad.find(filter.getModalidad());
						if (!SessionContext
								.getUserDetails()
								.getAuthorities()
								.contains(
										new SimpleGrantedAuthority(
												Roles.ROLE_MAO_MODALIDAD_ + "I"))
								&& m.getSistema() == Sistemas.ALFA) {
							FacesUtil
									.addErrorMessage("busquedaRapidaPorNumero.error.alfa");
							return null;
						} else if (!SessionContext
								.getUserDetails()
								.getAuthorities()
								.contains(
										new SimpleGrantedAuthority(
												Roles.ROLE_MAO_MODALIDAD_ + "S"))
								&& m.getSistema() == Sistemas.SITAMAR) {
							FacesUtil
									.addErrorMessage("busquedaRapidaPorNumero.error.sitamar");
							return null;
						} else if (!SessionContext
								.getUserDetails()
								.getAuthorities()
								.contains(
										new SimpleGrantedAuthority(
												Roles.ROLE_MAO_MODALIDAD_ + "D"))
								&& m.getSistema() == Sistemas.SITAMOD) {
							FacesUtil
									.addErrorMessage("busquedaRapidaPorNumero.error.sitamod");
							return null;
						}
					}
					
					BusConsultarExpedientesResponse expResponse = null;
					ExpedientesFilter f = new ExpedientesFilter();
					
					f.codTitular = filter.getDocumento(); //Documento obtenido dentro de la tabla de Titular de la vista
					f.modalidad = filter.getModalidad();
					f.numeroSolicitud = filter.getSolicitud();
					
					// HCS. MAO-393
					String numPublicacion = filter.getPublicacion();

					if ( !StringUtils.isEmptyOrNull(numPublicacion) &&
						("ES".equalsIgnoreCase(numPublicacion.substring(0,2)) 
					  || "EP".equalsIgnoreCase(numPublicacion.substring(0,2))) )
					{
						//Separamos la modalidad de la publicacion
						numPublicacion = numPublicacion.substring(2);
					}
					
					f.numeroPublicacion = numPublicacion;			
					f.nombre = filter.getApellido1() + " " + filter.getApellido2();
					f.nombre = f.nombre.trim();
					
					if( SessionContext.getTipoUsuario().equals( TipoUsuario.AGENTE.name() ) ||
						( SessionContext.getTipoUsuario().equals( TipoUsuario.ASOCIADO.name() ) && 
						  !StringUtils.isEmptyOrNull( SessionContext.getCodigoAgente() ) ) ) {
						f.tipoUsuario = TipoUsuario.AGENTE;
						//f.emailAgente = SessionContext.getEmailAgente();
						f.codAgente = SessionContext.getCodigoAgente();
					} else if( SessionContext.getTipoUsuario().equals( TipoUsuario.REPRESENTANTE.name() ) ||
							   ( SessionContext.getTipoUsuario().equals( TipoUsuario.ASOCIADO.name() ) &&
								 !StringUtils.isEmptyOrNull( SessionContext.getDocumentoRepresentante() ) ) ) {
						
						f.tipoUsuario = TipoUsuario.REPRESENTANTE;
						
						////COMENTADO MAO-309
						/*if( SessionContext.hasUserRole( Roles.ROLE_CUENTA_VERIFICADA ) ) {
							f.emailRepresentante = SessionContext.getEmailRepresentante();
							f.codRepresentante = SessionContext.getDocumentoRepresentante();
						} else {
							f.emailRepresentante = SessionContext.getEmailRepresentante();
						}*/
					} else if( SessionContext.getTipoUsuario().equals( TipoUsuario.TITULAR.name() ) ) {
						
						f.tipoUsuario = TipoUsuario.TITULAR;
						
						////COMENTADO MAO-309
						/*if( SessionContext.hasUserRole( Roles.ROLE_CUENTA_VERIFICADA ) ) {
							f.emailTitular = SessionContext.getEmailTitular();
							f.codTitular = SessionContext.getDocumentoTitular();
							f.nombre = SessionContext.getNombreUsuario();
						} else {
							f.emailTitular = SessionContext.getEmailTitular();
							f.nombre = SessionContext.getNombreUsuario();
						}*/
					} else if (TipoUsuario.GESTOR.name().equals(SessionContext.getTipoUsuario())) {
						// Con el usuario Gestor buscamos los expedientes según el tipo de usuario seleccionado
						f.tipoUsuario = StringUtils.isEmptyOrNull(usuarioSeleccionado) ? 
								TipoUsuario.TITULAR : TipoUsuario.valueOf(usuarioSeleccionado);
						
						switch (f.tipoUsuario) {
						case AGENTE:
							// Dado que el WS utiliza el código del Agente en lugar de su documento para 
							// recuperar los expedientes con pagos, debemos recuperar el codAgente de BD
							UsuariosAgenteVO usuarioAgenteVo  = usuariosAgenteService.findByNumeroDocumento(documentoUsuario);
							
							// Una vez verificado el documento rellenamos los campos restantes del Agente
							if (usuarioAgenteVo != null) {
								f.codAgente = usuarioAgenteVo.getCodAgente();
								f.emailAgente = usuarioAgenteVo.getEmail(); 
							} else {
								FacesUtil.addErrorMessage( "bopi.error.documentoNoEncontrado" );
								return null;
							}
							
							break;

						case REPRESENTANTE:
							f.codRepresentante = documentoUsuario;
							f.emailRepresentante = emailUsuario;
							
							break;
							
						default:
							//Este es el caso en que el usuario Gestor busca en calidad de Titular
							f.tipoUsuario = TipoUsuario.TITULAR;
							f.emailTitular = emailTitular;
							break;
						}
						
						// Escribimos la traza
						trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_HIS_PAGOS, f.toTraze());
					}
					// Finalmente llamamos al service con los datos rellenos según tipo de usuario
					expResponse = expedientesService.getExpedientesSolPub(f);
					
					if ( (expResponse == null) || (expResponse != null && expResponse.getResultado() == -1) )
					{
						// HCS: La llamada a consultaExpedientes ha fallado 
						FacesUtil.addErrorMessage("error.ws.consultaExpedientes");	
						OepmLogger.error("El WS de consultaExpedientes ha fallado");
						
					} else if(expResponse != null) {
						procesarMensajesConsultarExpedientes(expResponse.getMensajes());
						if (expResponse.getResultado() == BusConsultarExpedientesResponse.RESULTADO_OK) {
							for( BusExpediente busExpediente : expResponse.getExpedientes() ) {
								String oldModalidad = pagosService.parseModalidad( busExpediente.getModalidad() );

								if (Modalidad.AL_PATENTE_PCT.is(oldModalidad)) {
									busExpediente.setNumeroSolicitud(ExpedienteUtils.convertPctToW(busExpediente.getNumeroSolicitudFormateado()));
								}

								// HCS. MAO-393 y MAO-333 
								String auxNumPublicacion = busExpediente.getNumeroPublicacion();
								String auxNumSolicitud = busExpediente.getNumeroSolicitud();
								
								if(!StringUtils.isEmptyOrNull(auxNumPublicacion))
								{	
									expedientes.add( oldModalidad + auxNumPublicacion.substring(2) );			
								}
								
								if(!StringUtils.isEmptyOrNull(auxNumSolicitud)) {
									expedientes.add( oldModalidad + auxNumSolicitud );
								}
							}
						}
					}
				} catch( BusinessException e ) {
					FacesUtil.addErrorMessage( "pagos.realizados.error.recuperarDatos" );
					OepmLogger.error(e);
					error= true;
				}
			} else {
				String oldModalidad = pagosService.parseModalidad( solModalidad );
				if (Modalidad.AL_PATENTE_PCT.is(oldModalidad)) {
					Object exp = SessionUtil.getFromSession("detalleExpediente");
					String numeroSolicitudFormateado = ((CeAlrExpedientesMvVO) exp).getReferenciaInvenes();
					solNumero = ExpedienteUtils.convertPctToW(numeroSolicitudFormateado);
				}
				
				expedientes.add( oldModalidad + solNumero );
				/***
				 * MAO-333
				 */
				if((solNumeroPublicacion!=null)&&(Modalidad.AL_PATENTE_EUROPEA.is(oldModalidad))){						
					expedientes.add("E" + solNumeroPublicacion.substring(2));
				}
				if (SessionContext.getTipoUsuario().equals(TipoUsuario.GESTOR.name())) {
					try {
						trazaGestorService.insertTraze("Detalle expediente - Pagos", "Expediente: " + solModalidad + solNumero);
					} catch (BusinessException e) {
						OepmLogger.error(e);
						FacesUtil.addErrorMessage( "pagos.realizados.error.recuperarDatos" );
						error = true;
					}
				}
			}
			if(!error) {			
				try {
					// HCS: Optimización de la recuperación de los pagos
					FiltroPagosExpedientes filterAux = new FiltroPagosExpedientes();
					OrdenConsulta sort = new OrdenConsulta();
					sort.setCampoOrdenacion(null);
					sort.setTipoOrdenacion(null);
					
					if( filter != null ) {
						filterAux.setFormaPago( filter.getFormaPago() );
						filterAux.setCodClave( filter.getTasa() );
						filterAux.setFechaDesde( filter.getFechaPagoDesde() != null ? DateUtils.formatFechaGuion( filter.getFechaPagoDesde() ) : "" );
						filterAux.setFechaHasta( filter.getFechaPagoHasta() != null ? DateUtils.formatFechaGuion( filter.getFechaPagoHasta() ) : "" );
					}
					
					int maxPagos = Configuracion.getPropertyAsInteger("MAX_RESULTADOS_PAGOS");
					
					
					RespuestaDatosPagoTotal datosPagos;
					datosPagos = pagosService.getPagosRealizados (expedientes.toArray( new String[expedientes.size()] ), 1, maxPagos, filterAux, sort); 
					
					// HCS: El WS ha fallado
					if ( datosPagos == null || 
					(datosPagos != null && 
					(datosPagos.getPagos() == null || (datosPagos.getPagos()!=null && datosPagos.getPagos().length <=0) ) && 
					 datosPagos.getDescripcion() != null && datosPagos.getDescripcion().toUpperCase().contains("ERROR"))) {
						
						FacesUtil.addErrorMessage("error.ws.sireco");
						OepmLogger.error("El WS de Sireco ha fallado");
					}
					
					pagosRealizados = pagosService.parsePagosRealizados( datosPagos );
				
					if (pagosRealizados.size() >= maxPagos)
					{
						// Alcanzado maximo de resultados
						FacesUtil.addWarningMessage("bopi.warn.maxResultados");
					}
					else if (pagosRealizados.size() <= 0) {		
						// Busqueda sin resultados
						FacesUtil.addOKMessage("busqueda.sin.resultados");						
					}
					
					}
				catch( BusinessException be ) {
					OepmLogger.error(be);
					FacesUtil.addErrorMessage( "pagos.realizados.error.recuperarDatos" );
					pagosRealizados = null;	
				}
			
			}
		}
		
		return null;
	}
	
	/**
	 * Action para el boton de limpiar la busqueda de pagos realizados. 
	 * Creado para MAO-459
	 * @param to Parametro opcional para especificar una jsp de destino 
	 * @return
	 */
	public String actionLimpiar(String to) {
		filter = new PagosRealizadosFilterVO();
		//saveFilterInSession(filter);
		this.pagosRealizados = new ArrayList<PagoRealizadoVO>();
		if(StringUtils.isEmptyOrNull(to)){
			return JSFPages.PAGOS_REALIZADOS;
		}
		else {
			return to;
		}
	}
	
	public String actionLimpiar() {
		return actionLimpiar(null);
	}
	
	@Override
	public String actionEdit() {
		return null;
	}

	@Override
	public String actionDelete() {
		return null;
	}

	@Override
	public String actionNew() {
		return null;
	}

	@Override
	public PagosRealizadosFilterVO getFilter() {
		try {
			if( filter == null ) {
				if( !fromDetExp ) {
					filter = new PagosRealizadosFilterVO();
				} else {
					filter = new PagosRealizadosFilterVO( solModalidad, solNumero );
				}
			} else {
				if( mustLoadList ) {
					actionSearch();
					
					mustLoadList = false;
				}
			}
		} catch( Exception e ) {
			FacesUtil.addErrorMessage( ExceptionUtil.getMessage( e ) );
		}
		
		return filter;
	}

	@Override
	public void setFilter( BaseVO filter ) {
		this.filter = ( PagosRealizadosFilterVO )filter;
	}

	/**
	 * Procesa los mensajes como mensajes faces de llamada al servicio de
	 * consulta de expedientes
	 * 
	 * @param mensajes
	 */
	private void procesarMensajesConsultarExpedientes(Mensajes[] mensajes) {
		if (mensajes != null) {
			for (Mensajes mensaje : mensajes) {
				switch (mensaje.getCodigo()) {
//				case Mensaje.COD_ERROR_BUSQUEDA_RAPIDA_NUMERO_ALFA:
//				case Mensaje.COD_ALFA_MAX_SEARCH_RESULT:
//				case Mensaje.COD_ERROR_BUSQUEDA_RAPIDA_NUMERO_SITAMOD:
//				case Mensaje.COD_SITAMOD_MAX_SEARCH_RESULT:
//				case Mensaje.COD_ERROR_BUSQUEDA_RAPIDA_NUMERO_SITAMAR:
//				case Mensaje.COD_SITAMAR_MAX_SEARCH_RESULT:
//				case Mensaje.COD_BUSQUEDA_SIN_RESULTADOS:
//					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
//							"busqueda.sin.resultados");
//					break;
				case Mensaje.COD_WS_ERROR_GENERICO:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busqueda.error");
					break;
				}
			}
		}
	}
	
	/**
	 * Devuelve la lista de tipos de ususarios pero sin el elemento Titular
	 * @return
	 */
	private List<String> obtenerListaUsuariosSinTitular() {
		List<String> l = GeneradorValoresCombo.getListaTipoUsuariosPrincipales();
		l.remove(TipoUsuario.TITULAR.name());
		return l;
	}
	
	public String getTituloDetalle() {
		return tituloDetalle;
	}

	public void setTituloDetalle(String tituloDetalle) {
		this.tituloDetalle = tituloDetalle;
	}
	
	public void paginatePagosEsperados( PageEvent event ) {
		if( !WSUtils.isAvailable( Configuracion.getPropertyAsString( "url.bus.sireco" )) ) {
			FacesUtil.addErrorMessage( "pagos.realizados.error.recuperarDatos" );
		}
	}
	
	public void sortPagosEsperados( SortEvent event ) {
		if( !WSUtils.isAvailable( Configuracion.getPropertyAsString( "url.bus.sireco" )) ) {
			FacesUtil.addErrorMessage( "pagos.realizados.error.recuperarDatos" );
		}
	}
	
	public Boolean getModalidadSitamod() {
		if(StringUtils.isEmptyOrNull(solModalidad))
			return false;
		
		Modalidad m = Modalidad.find(solModalidad);
		return m.isSitamod();
	}
	/**
	 * MAO-328
	 */
	public Boolean getModalidadMarca() {
		if(StringUtils.isEmptyOrNull(solModalidad))
			return false;
		
		Modalidad m = Modalidad.find(solModalidad);
		return m.isSitamar();
	}


	/**
	 * @return the documentoUsuario
	 */
	public String getDocumentoUsuario() {
		return documentoUsuario;
	}

	/**
	 * @param documentoUsuario the documentoUsuario to set
	 */
	public void setDocumentoUsuario(String documentoUsuario) {
		this.documentoUsuario = documentoUsuario;
	}

	/**
	 * @return the emailUsuario
	 */
	public String getEmailUsuario() {
		return emailUsuario;
	}

	/**
	 * @param emailUsuario the emailUsuario to set
	 */
	public void setEmailUsuario(String emailUsuario) {
		this.emailUsuario = emailUsuario;
	}

	/**
	 * @return the usuarioSeleccionado
	 */
	public String getUsuarioSeleccionado() {
		return usuarioSeleccionado;
	}

	/**
	 * @param usuarioSeleccionado the usuarioSeleccionado to set
	 */
	public void setUsuarioSeleccionado(String usuarioSeleccionado) {
		this.usuarioSeleccionado = usuarioSeleccionado;
	}

	/**
	 * @param pagosService the pagosService to set
	 */
	public void setPagosService(PagosService pagosService) {
		this.pagosService = pagosService;
	}

	/**
	 * @param expedientesService the expedientesService to set
	 */
	public void setExpedientesService(ExpedientesService expedientesService) {
		this.expedientesService = expedientesService;
	}

	/**
	 * @param usuariosAgenteService the usuariosAgenteService to set
	 */
	public void setUsuariosAgenteService(UsuariosAgenteService usuariosAgenteService) {
		this.usuariosAgenteService = usuariosAgenteService;
	}
	
	/**
	 * @param trazaGestorService the trazaGestorService to set
	 */
	public void setTrazaGestorService(TrazaGestorService trazaGestorService) {
		this.trazaGestorService = trazaGestorService;
	}


	/**
	 * @return the tiposUsuario
	 */
	public List<String> getTiposUsuario() {
		return tiposUsuario;
	}

	/**
	 * @return the emailTitular
	 */
	public String getEmailTitular() {
		return emailTitular;
	}

	/**
	 * @param emailTitular the emailTitular to set
	 */
	public void setEmailTitular(String emailTitular) {
		this.emailTitular = emailTitular;
	}

}