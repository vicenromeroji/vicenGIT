package es.oepm.mao.view.controller;

import java.util.Collection;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.apache.cxf.common.util.CollectionUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import es.oepm.ceo.ws.anotacionesexp.AnotacionBonoDTO;
import es.oepm.ceo.ws.anotacionesexp.parameters.ConsultaAnotacionesBonoAlfaResponse;
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
import es.oepm.mao.business.service.AnotacionesService;
import es.oepm.mao.business.service.ExpedientesService;
import es.oepm.mao.business.vo.ConsultaBonosFilterVO;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.comun.business.service.UsuariosAgenteService;
import es.oepm.mao.view.controller.util.GeneradorValoresCombo;
import es.oepm.mao.view.controller.util.JSFPages;
import es.oepm.wservices.core.mensajes.Mensajes;

@ManagedBean( name = "consultaBonosController" )
@ViewScoped
public class ConsultaBonosController extends BaseController implements ListController {
		
	private static final long serialVersionUID = 6075113316353218897L;
	
	private boolean mustLoadList;
	private ConsultaBonosFilterVO filter;
	private List<AnotacionBonoDTO> anotacionesBono;
	
	private List<SelectItem> modalidades;
	
	// Al entrar con ususario Gestor
	public String documentoUsuario;
	public String emailUsuario;
	public List<String> tiposUsuario = obtenerListaUsuariosSinTitular();
	public String usuarioSeleccionado;
	public String emailTitular;
	
	@ManagedProperty( "#{anotacionesService}" )
	private AnotacionesService anotacionesService;
	
	@ManagedProperty( "#{expedientesService}" )
	private ExpedientesService expedientesService;
	
	@ManagedProperty( "#{usuariosAgenteService}" )
	private UsuariosAgenteService usuariosAgenteService;
	
	public ConsultaBonosController() {
		super();
		
		BaseVO sessionValue = SessionUtil.getSearchFilter( true );
		
		if( ( sessionValue != null ) && ( sessionValue instanceof ConsultaBonosFilterVO ) ) {
			setFilter( sessionValue );

			mustLoadList = true;
		} else {			
			SessionUtil.clearSearchFilter();
		}
		
		List<Modalidad> listModalidades = GeneradorValoresCombo
				.getListaModalidadesConsultaBonos((Collection<SimpleGrantedAuthority>) SessionContext
						.getUserDetails().getAuthorities());
		
		modalidades = HTMLUtil.getSelectItemListByGroup( SelectItemList.createDefault(),
														 listModalidades,
														 Modalidad.class, "textoModalidad", "modalidad", "" );
	}
	
	public List<SelectItem> getModalidades() {
		return modalidades;
	}
	
	public List<AnotacionBonoDTO> getAnotacionesBono() {
		return anotacionesBono;
	}
	
	public String actionConsultar( String idAnotacion ) {
		saveFilterInSession( filter );
		
		return String.format( "%s?faces-redirect=true&idAnotacion=%s", JSFPages.PAGOS_BONOS_VIEW, idAnotacion );
	}

	@Override
	public String actionSearch() {
		boolean validFilter = false;
		
		if( filter != null ) {
			if( StringUtils.isEmptyOrNull( filter.getModalidad() ) && ( !StringUtils.isEmptyOrNull( filter.getSolicitud() ) || !StringUtils.isEmptyOrNull( filter.getPublicacion() ) ) ) {
				FacesUtil.addErrorMessage( "pagos.bonos.error.criterioBusqueda.modalidad" );
			} else {
				validFilter = true;
			}
		}
		
		if( validFilter ) {
			ConsultaAnotacionesBonoAlfaResponse response = null;
			
			try {
				if( SessionContext.getTipoUsuario().equals( TipoUsuario.AGENTE.name() ) ||
					( SessionContext.getTipoUsuario().equals( TipoUsuario.ASOCIADO.name() ) && 
					  !StringUtils.isEmptyOrNull( SessionContext.getCodigoAgente() ) ) ) {
					filter.setAgeEmail(SessionContext.getEmailAgente());
					filter.setAgeCodigo(SessionContext.getCodigoAgente());
					
					response = anotacionesService.getAnotacionesBonoAlfaAgente(filter);
				} else if( SessionContext.getTipoUsuario().equals( TipoUsuario.REPRESENTANTE.name() ) ||
						   ( SessionContext.getTipoUsuario().equals( TipoUsuario.ASOCIADO.name() ) &&
							 !StringUtils.isEmptyOrNull( SessionContext.getDocumentoRepresentante() ) ) ) {
					filter.setRepEmail(SessionContext.getEmailRepresentante());
					filter.setRepDocumento(SessionContext.getDocumentoRepresentante());
					
					response = anotacionesService.getAnotacionesBonoAlfaRepresentante(filter);
				} else if( SessionContext.getTipoUsuario().equals( TipoUsuario.TITULAR.name() ) ) {
					filter.setTitEmail(SessionContext.getEmailTitular());
					filter.setTitularDocumento(SessionContext.getDocumentoTitular());
					
					response = anotacionesService.getAnotacionesBonoAlfaTitular(filter);
				} else if (TipoUsuario.GESTOR.name().equals(SessionContext.getTipoUsuario())) {
					// Comprobamos que se informa el usuario
					if (StringUtils.isEmptyOrNull(documentoUsuario)
							&& StringUtils.isEmptyOrNull(emailUsuario)
							&& StringUtils.isEmptyOrNull(emailTitular)
							&& StringUtils.isEmptyOrNull(filter
									.getTitularDocumento())) {
						FacesUtil
								.addErrorMessage("usuarioGestor.error.datosUsuario");
						return null;
					}
					switch (StringUtils.isEmptyOrNull(usuarioSeleccionado) ? TipoUsuario.TITULAR : TipoUsuario.valueOf(usuarioSeleccionado)) {
					case AGENTE:
						// Dado que el WS utiliza el código del Agente en lugar de su documento para 
						// recuperar los expedientes con pagos, debemos recuperar el codAgente de BD
						UsuariosAgenteVO usuarioAgenteVo  = usuariosAgenteService.findByNumeroDocumento(documentoUsuario);
						
						// Una vez verificado el documento rellenamos los campos restantes del Agente
						if (usuarioAgenteVo != null) {
							filter.setAgeEmail(usuarioAgenteVo.getEmail());
							filter.setAgeCodigo(usuarioAgenteVo.getCodAgente());
							
							response = anotacionesService.getAnotacionesBonoAlfaAgente(filter);
						} else {
							FacesUtil.addErrorMessage( "bopi.error.documentoNoEncontrado" );
							return null;
						}
						
						break;

					case REPRESENTANTE:
						filter.setRepEmail(emailUsuario);
						filter.setRepDocumento(documentoUsuario);
						
						response = anotacionesService.getAnotacionesBonoAlfaRepresentante(filter);
						
						break;
						
					case TITULAR:
						filter.setTitEmail(emailTitular);
						response = anotacionesService.getAnotacionesBonoAlfaTitular(filter);
						
						break;
					
					default:
						break;
					}
					
				}
				
				if (response != null) {
					if (response.getAnotacionesBono() != null) {
						anotacionesBono = response.getAnotacionesBono();
					}
					procesarMensajesConsultaAnotacionesBonoAlfa(response.getMensajes());
				}
				
				
				// Sin resultados.
				if (CollectionUtils.isEmpty(anotacionesBono)) {
					FacesUtil.addOKMessage("busqueda.sin.resultados");
				}
				
			} catch( BusinessException be ) {
				OepmLogger.error(be);
				
				FacesUtil.addErrorMessage( "pagos.bonos.error.recuperarDatos" );
			}
			
		}
		
		return null;
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
	
	/**
	 * Procesa los mensajes como mensajes faces de la llamada a consulta
	 * anotaciones bono alfa
	 * 
	 * @param mensajes
	 */
	private void procesarMensajesConsultaAnotacionesBonoAlfa(Mensajes[] mensajes) {
		if (mensajes != null) {
			for (Mensajes mensaje : mensajes) {
				switch (mensaje.getCodigo()) {
				case Mensaje.COD_WS_ERROR_GENERICO:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"pagos.bonos.error.recuperarDatos");
					break;
				case Mensaje.COD_WS_ERROR_INVALID_PARAMS:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"pagos.bonos.error.recuperarDatos");
					break;
				}
			}
		}
	}

	@Override
	public ConsultaBonosFilterVO getFilter() {
		try {
			if( filter == null ) {
				filter = new ConsultaBonosFilterVO();
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
		this.filter = ( ConsultaBonosFilterVO )filter;
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
	 * @param anotacionesService the anotacionesService to set
	 */
	public void setAnotacionesService(AnotacionesService anotacionesService) {
		this.anotacionesService = anotacionesService;
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

	/**
	 * @return the tiposUsuario
	 */
	public List<String> getTiposUsuario() {
		return tiposUsuario;
	}

}