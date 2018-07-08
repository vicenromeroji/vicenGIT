package es.oepm.mao.view.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.primefaces.context.RequestContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import es.oepm.busmule.ws.client.ceo.BusExpediente;
import es.oepm.busmule.ws.client.ceo.parameters.BusConsultarExpedientesResponse;
import es.oepm.core.business.BaseVO;
import es.oepm.core.business.mao.vo.UsuariosAgenteVO;
import es.oepm.core.constants.Mensaje;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.controller.ListController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.core.view.faces.SelectItemList;
import es.oepm.core.view.util.HTMLUtil;
import es.oepm.mao.business.service.ExpedientesService;
import es.oepm.mao.business.service.PagosService;
import es.oepm.mao.business.service.TrazaGestorService;
import es.oepm.mao.business.vo.CabeceraPasarelaVO;
import es.oepm.mao.business.vo.PagoEsperadoVO;
import es.oepm.mao.business.vo.PagosEsperadosFilterVO;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.comun.business.service.UsuariosAgenteService;
import es.oepm.mao.constants.MaoPropiedadesConf;
import es.oepm.mao.constants.MaoTrazaGestor;
import es.oepm.mao.view.controller.util.ExpedientesFilter;
import es.oepm.mao.view.controller.util.GeneradorValoresCombo;
import es.oepm.mao.view.controller.util.JSFPages;
import es.oepm.pasarela2WS.ws.client.entity.ResultEncriptaTasasPost;
import es.oepm.pasarela2WS.ws.client.entity.ResultGetIdSesion;
import es.oepm.wservices.core.mensajes.Mensajes;

@ManagedBean( name = "pagosEsperadosController" )
@ViewScoped
public class PagosEsperadosController extends BaseController implements ListController {
	
	private static final long serialVersionUID = 1290822362393319046L;
	
	private boolean mustLoadList;
	private PagosEsperadosFilterVO filter;
	private List<PagoEsperadoVO> pagosEsperados;
	private PagoEsperadoVO selectedPagoEsperado;
	
	private List<SelectItem> modalidades;
	
	private String tipoPasarela;
	
	// Al entrar con ususario Gestor
	public String documentoUsuario;
	public String emailUsuario;
	public List<String> tiposUsuario = GeneradorValoresCombo.getListaTipoUsuariosPrincipales();
	public String usuarioSeleccionado;
	
	@ManagedProperty( "#{pagosService}" )
	private PagosService pagosService;
	
	@ManagedProperty( "#{expedientesService}" )
	private ExpedientesService expedientesService;
	
	@ManagedProperty( "#{usuariosAgenteService}" )
	private UsuariosAgenteService usuariosAgenteService;
	
	@ManagedProperty( "#{trazaGestorService}" )
	private TrazaGestorService trazaGestorService;
	
	public PagosEsperadosController() {
		super();
		
		BaseVO sessionValue = SessionUtil.getSearchFilter( true );
		
		if( ( sessionValue != null ) && ( sessionValue instanceof PagosEsperadosFilterVO ) ) {
			setFilter( sessionValue );

			mustLoadList = true;
		} else {			
			SessionUtil.clearSearchFilter();
		}
		
		List<Modalidad> listModalidades = GeneradorValoresCombo
				.getListaModalidadesPagosEsperados((Collection<SimpleGrantedAuthority>) SessionContext
						.getUserDetails().getAuthorities());
		
		modalidades = HTMLUtil.getSelectItemListByGroup( SelectItemList.createDefault(), 
												 	    listModalidades, 
												 	    Modalidad.class, "textoModalidad", "modalidad", "" );
	}
	
	public List<SelectItem> getModalidades() {
		return modalidades;
	}
	
	public List<PagoEsperadoVO> getPagosEsperados() {
		return pagosEsperados;
	}
	
	public PagoEsperadoVO getSelectedPagoEsperado() {
		return selectedPagoEsperado;
	}
	
	public void setSelectedPagoEsperado( PagoEsperadoVO selectedPagoEsperado ) {
		this.selectedPagoEsperado = selectedPagoEsperado;
	}

	@Override
	public String actionSearch() {
		pagosEsperados = Collections.emptyList();
		
		try {
			BusConsultarExpedientesResponse expResponse = null;
			ExpedientesFilter f = new ExpedientesFilter();
			
			f.modalidad = filter.getModalidad();
			f.numeroSolicitud = filter.getSolicitud();
			f.numeroPublicacion = filter.getPublicacion();
			f.fhPresentacionInicio = filter.getFechaPresentExpDesde();
			f.fhPresentacionFin = filter.getFechaPresentExpHasta();
			
			if( SessionContext.getTipoUsuario().equals( TipoUsuario.AGENTE.name() ) ||
				( SessionContext.getTipoUsuario().equals( TipoUsuario.ASOCIADO.name() ) && 
				  !StringUtils.isEmptyOrNull( SessionContext.getCodigoAgente() ) ) ) {
				f.tipoUsuario = TipoUsuario.AGENTE;
				f.codAgente = SessionContext.getCodigoAgente();
				//f.emailAgente = SessionContext.getEmailAgente();
				
			} else if( SessionContext.getTipoUsuario().equals( TipoUsuario.REPRESENTANTE.name() ) ||
					   ( SessionContext.getTipoUsuario().equals( TipoUsuario.ASOCIADO.name() ) &&
						 !StringUtils.isEmptyOrNull( SessionContext.getDocumentoRepresentante() ) ) ) {
				
				f.tipoUsuario = TipoUsuario.REPRESENTANTE;
				
				////COMENTADO MAO-309
				/*if( SessionContext.hasUserRole( Roles.ROLE_CUENTA_VERIFICADA ) ) {
					f.codRepresentante = SessionContext.getDocumentoRepresentante();
					f.emailRepresentante = SessionContext.getEmailRepresentante();
					
				} else {
					f.emailRepresentante = SessionContext.getEmailRepresentante();
					
				}*/
			} else if( SessionContext.getTipoUsuario().equals( TipoUsuario.TITULAR.name() ) ) {
				
				f.tipoUsuario = TipoUsuario.TITULAR;
				
				////COMENTADO MAO-309
				/*if( SessionContext.hasUserRole( Roles.ROLE_CUENTA_VERIFICADA ) ) {
					f.codTitular = SessionContext.getDocumentoTitular();
					f.emailTitular = SessionContext.getEmailTitular();
					
				} else {
					f.emailTitular = SessionContext.getEmailTitular();
					
				}*/
			} else if (TipoUsuario.GESTOR.name().equals(SessionContext.getTipoUsuario())) {
				// Comprobamos que se han introducido datos
				if (StringUtils.isEmptyOrNull(documentoUsuario)
						&& StringUtils.isEmptyOrNull(emailUsuario)) {
					FacesUtil
							.addErrorMessage("usuarioGestor.error.datosUsuario");
					return null;
				}
				
				// Con el usuario Gestor buscamos los expedientes según el tipo de usuario seleccionado
				f.tipoUsuario = TipoUsuario.valueOf(usuarioSeleccionado);
				switch (f.tipoUsuario) {
				case AGENTE:
					// Dado que el WS utiliza el código del Agente en lugar de su documento para 
					// recuperar los expedientes con pagos, debemos recuperar el codAgente de BD
					UsuariosAgenteVO usuarioAgenteVo  = usuariosAgenteService.findByNumeroDocumento(documentoUsuario);
					
					// Una vez verificado el documento rellenamos los campos restantes del Agente
					if (usuarioAgenteVo != null) {
						f.codAgente = usuarioAgenteVo.getCodAgente();
						//f.emailAgente = usuarioAgenteVo.getEmail(); 
					} else {
						FacesUtil.addErrorMessage( "bopi.error.documentoNoEncontrado" );
						return null;
					}
					
					break;

				case REPRESENTANTE:
					f.codRepresentante = documentoUsuario;
					//f.emailRepresentante = emailUsuario;
					
					break;
					
				case TITULAR:
					f.codTitular = documentoUsuario;
					//f.emailTitular = emailUsuario;
					
					break;
					
				default:
					break;
				}
				
				// Escribimos la traza
				trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_HIS_PAGOS, f.toTraze());
			}
			// Finalmente llamamos al service con los datos rellenos según tipo de usuario
			expResponse = expedientesService.getExpedientesConPagos(f);
			
			if(expResponse != null) {
				procesarMensajesConsultarExpedientes(expResponse.getMensajes());
			}
			
			if(expResponse != null && expResponse.getResultado() == BusConsultarExpedientesResponse.RESULTADO_OK ) {
				if(!expResponse.getExpedientes().isEmpty() ) {
					pagosEsperados = new ArrayList<PagoEsperadoVO>();
					
					for( BusExpediente busExpediente : expResponse.getExpedientes() ) {
						pagosEsperados.addAll( pagosService.getPagosEsperados( busExpediente ) );
					}
					
					if(pagosEsperados.size() > 0){
					 Collections.sort(pagosEsperados, new Comparator<PagoEsperadoVO>() {
		                 @Override
		                 public int compare(PagoEsperadoVO s1, PagoEsperadoVO s2) {
		                     return s1.getFechaInicioPago().compareTo(s2.getFechaInicioPago());
		                 }
		             });
					}
					else {
						// Sin resultados.
						FacesUtil.addOKMessage("busqueda.sin.resultados");
					}
				}
				else {
					// Sin resultados.
					FacesUtil.addOKMessage("busqueda.sin.resultados");
				}
			} else {
				OepmLogger.error("El WS de consultaExpedientes ha fallado");
				FacesUtil.addErrorMessage("error.ws.consultaExpedientes");
			}
		} catch( BusinessException be ) {
			OepmLogger.error("El WS de pagosAlfa ha fallado");
			FacesUtil.addErrorMessage("error.ws.pagosAlfa");
		}
		
		return null;
	}
	
	/**
	 * Action para el boton de limpiar la busqueda de pagos esperados. 
	 * Creado para MAO-459
	 * @param to Parametro opcional para especificar una jsp de destino 
	 * @return
	 */
	public String actionLimpiar(String to) {
		filter = new PagosEsperadosFilterVO();
		//saveFilterInSession(filter);
		this.pagosEsperados = new ArrayList<PagoEsperadoVO>();
		if(StringUtils.isEmptyOrNull(to)){
			return JSFPages.PAGOS_ESPERADOS;
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
	public PagosEsperadosFilterVO getFilter() {
		try {
			if( filter == null ) {
				filter = new PagosEsperadosFilterVO();
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
		this.filter = ( PagosEsperadosFilterVO )filter;
	}
	
	
	public boolean isExpModMarca( PagoEsperadoVO pagoEsperado ) {
		Modalidad modalidad = Modalidad.find( pagoEsperado.getModalidad() );
		
		return modalidad.isSitamar();
	}
	
	public boolean esPagoExpModMarca( PagoEsperadoVO pagoEsperado ) {
		return Modalidad.find( pagoEsperado.getModalidad() ).isSitamar();
	}
	
	public void actionObtenerInfoPagosPostEncriptada() throws BusinessException {
		this.actionObtenerInfoPagosPostEncriptada(selectedPagoEsperado);
	}
	
	public void actionObtenerInfoPagosPostEncriptada(PagoEsperadoVO pagoEsperado) {
		// Obtenemos el id para realizar el pago
		ResultGetIdSesion resultGetIdSesion;
		try {
			resultGetIdSesion = pagosService.obtenerIdSesionPasarelaDePago();
			
			if(resultGetIdSesion.getCodigoResultado() == 0) {

				// Rellenamos los datos de la cabecera
				CabeceraPasarelaVO cabeceraPasarelaVO = new CabeceraPasarelaVO();
				cabeceraPasarelaVO.setIdSesion(resultGetIdSesion.getIdSesion());
				cabeceraPasarelaVO.setTipoPasarela(tipoPasarela);
				
				// Comprobamos de que tipo de identificacion se trata
				if (TipoUsuario.AGENTE.toString().equals(
						SessionContext.getTipoUsuario())
						|| TipoUsuario.REPRESENTANTE.toString().equals(
								SessionContext.getTipoUsuario())
						|| TipoUsuario.ASOCIADO.toString().equals(
								SessionContext.getTipoUsuario())) {
					// Identificacion para representantes
					cabeceraPasarelaVO.setTipoIdentificacion(MaoPropiedadesConf.PASARELA2_TIP_IDE_REPRESENTANTE_LEGAL);
					cabeceraPasarelaVO.setNomApe(SessionContext.getNombreUsuario());
					
					if (TipoUsuario.AGENTE.toString().equals(SessionContext.getTipoUsuario())) {
						// Tipo de usuario agente
						cabeceraPasarelaVO.setCodRepresentante(SessionContext.getCodigoAgente());
						cabeceraPasarelaVO.setEmail(SessionContext.getEmailAgente());
						cabeceraPasarelaVO.setNif(SessionContext.getDocumento());
					} else if (TipoUsuario.REPRESENTANTE.toString().equals(SessionContext.getTipoUsuario())) {
						// Tipo de usuario representante
						cabeceraPasarelaVO.setCodRepresentante(MaoPropiedadesConf.PASARELA2_COD_REPRESENTANTE);
						cabeceraPasarelaVO.setEmail(SessionContext.getEmailRepresentante());
						cabeceraPasarelaVO.setNif(SessionContext.getDocumentoRepresentante());
					} else if (TipoUsuario.ASOCIADO.toString().equals(SessionContext.getTipoUsuario())){
						// Si es de tipo asociado comprobamos el tipo del usuario padre
						if (SessionContext.getCodigoAgente() != null) {
							// Autorizado de agente
							cabeceraPasarelaVO.setCodRepresentante(SessionContext.getCodigoAgente());
							cabeceraPasarelaVO.setEmail(SessionContext.getEmailAgente());
							cabeceraPasarelaVO.setNif(SessionContext.getDocumentoAgente());
						} else if (SessionContext.getDocumentoRepresentante() != null) {
							// Autorizado de representante
							cabeceraPasarelaVO.setCodRepresentante(MaoPropiedadesConf.PASARELA2_COD_REPRESENTANTE);
							cabeceraPasarelaVO.setEmail(SessionContext.getEmailRepresentante());
							cabeceraPasarelaVO.setNif(SessionContext.getDocumentoRepresentante());
						} else if (SessionContext.getDocumentoTitular() != null) {
							// Autorizado de titular
							cabeceraPasarelaVO.setTipoIdentificacion(MaoPropiedadesConf.PASARELA2_TIP_IDE_SUJETO_PASIVO);
							cabeceraPasarelaVO.setEmail(SessionContext.getEmailTitular());
							cabeceraPasarelaVO.setNif(SessionContext.getDocumentoTitular());
						}
					}
				} else {
					// Identificacion por titular
					cabeceraPasarelaVO.setTipoIdentificacion(MaoPropiedadesConf.PASARELA2_TIP_IDE_SUJETO_PASIVO);
					cabeceraPasarelaVO.setEmail(SessionContext.getEmailTitular());
					cabeceraPasarelaVO.setNomApe(SessionContext.getNombreUsuario());
					cabeceraPasarelaVO.setNif(SessionContext.getDocumentoTitular());
				}
				
				ResultEncriptaTasasPost resultEncriptaTasasPost = pagosService.obtenerEncriptacionTasas(cabeceraPasarelaVO, pagoEsperado);
				if (resultEncriptaTasasPost.getCodigoResultado() == 0) {
					String tasasEncriptadas = resultEncriptaTasasPost.getTasasEncriptadas();
					SessionContext.setValue(MaoPropiedadesConf.PASARELA2_INFOPAGOS, tasasEncriptadas);
					
					RequestContext.getCurrentInstance().addCallbackParam("redirigirAPasarela", true);
				} else {
					FacesUtil.addErrorMessage( "GENERIC_ERROR" );
					RequestContext.getCurrentInstance().addCallbackParam("redirigirAPasarela", false);
				}
			} else {
				FacesUtil.addErrorMessage( "GENERIC_ERROR" );
				RequestContext.getCurrentInstance().addCallbackParam("redirigirAPasarela", false);
			}
		} catch (BusinessException e) {
			FacesUtil.addErrorMessage( "GENERIC_ERROR" );
			RequestContext.getCurrentInstance().addCallbackParam("redirigirAPasarela", false);
		}
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
//				case Mensaje.COD_ERROR_BUSQUEDA_RAPIDA_NUMERO_SITAMAR:
//				case Mensaje.COD_SITAMAR_MAX_SEARCH_RESULT:
				case Mensaje.COD_WS_ERROR_GENERICO:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busqueda.error");
					break;
				}
			}
		}
	}

	public String getTipoPasarelaCaixa(){
		return MaoPropiedadesConf.PASARELA2_TIP_PASARELA_CAIXA;
	}
	
	public String getTipoPasarelaAEAT(){
		return MaoPropiedadesConf.PASARELA2_TIP_PASARELA_AEAT;
	}

	public String getTipoPasarela() {
		return tipoPasarela;
	}
	
	public void setTipoPasarela(String tipoPasarela) {
		this.tipoPasarela = tipoPasarela;
	}

	public String getUrlRedireccionParasela() {
		return "/mio" + JSFPages.PAGOS_REDIRECCION_PASARELA + ".xhtml";
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
	 * @return the tiposUsuario
	 */
	public List<String> getTiposUsuario() {
		return tiposUsuario;
	}

	/**
	 * @param tiposUsuario the tiposUsuario to set
	 */
	public void setTiposUsuario(List<String> tiposUsuario) {
		this.tiposUsuario = tiposUsuario;
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
}