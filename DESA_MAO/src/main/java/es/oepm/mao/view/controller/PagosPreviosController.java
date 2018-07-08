package es.oepm.mao.view.controller;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.cxf.common.util.CollectionUtils;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.core.business.BaseVO;
import es.oepm.core.business.mao.vo.UsuariosAgenteVO;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.controller.ListController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.business.service.PagosService;
import es.oepm.mao.business.service.TrazaGestorService;
import es.oepm.mao.business.vo.PagoPrevioVO;
import es.oepm.mao.business.vo.PagosPreviosFilterVO;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.comun.business.service.UsuariosAgenteService;
import es.oepm.mao.constants.MaoTrazaGestor;
import es.oepm.mao.view.controller.util.GeneradorValoresCombo;
import es.oepm.mao.view.controller.util.JSFPages;
import es.oepm.sireco.ws.SirecoService.beans.RespuestaPagosLibresMao;

@ManagedBean( name = "pagosPreviosController" )
@ViewScoped
public class PagosPreviosController extends BaseController implements ListController {

	private static final long serialVersionUID = -2089708192196125558L;
	
	private boolean mustLoadList;
	private PagosPreviosFilterVO filter;
	private List<PagoPrevioVO> pagosPrevios;
	
	// Al entrar con ususario Gestor 
	public String documentoUsuario;
	public String emailUsuario;
	public List<String> tiposUsuario = GeneradorValoresCombo.getListaTipoUsuariosPrincipales();
	public String usuarioSeleccionado;
	
	@ManagedProperty( "#{pagosService}" )
	private PagosService pagosService;
	@ManagedProperty( "#{trazaGestorService}" )
	private TrazaGestorService trazaGestorService;
	
	@ManagedProperty( "#{usuariosAgenteService}" )
	private UsuariosAgenteService usuariosAgenteService;
	
	public PagosPreviosController() {
		super();
		
		BaseVO sessionValue = SessionUtil.getSearchFilter( true );
		
		if( ( sessionValue != null ) && ( sessionValue instanceof PagosPreviosFilterVO ) ) {
			setFilter( sessionValue );

			mustLoadList = true;
		} else {			
			SessionUtil.clearSearchFilter();
		}
	}
	
	public List<PagoPrevioVO> getPagosPrevios() {
		return pagosPrevios;
	}

	@Override
	public String actionSearch() {
		try {
			int maxPagos = Configuracion.getPropertyAsInteger("MAX_RESULTADOS_PAGOS");
			RespuestaPagosLibresMao resPagosLibre = null;
			filter = getFilter();
			
			if (TipoUsuario.GESTOR.name().equals(SessionContext.getTipoUsuario())) {
				
				// Montamos el filtro
				if (!montarFiltroGestor()) {
					return null;
				}

				// Insertamos en la traza
				trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_PAGOS_PREV, filter.toTraze());

				// HCS: Optimización de recuperación de datos
				resPagosLibre = pagosService.getPagosPrevios(filter.getCodigoAgente(),
						filter.getDocumento(), filter.getLoginMAO(), 1, maxPagos);

				// HCS: El WS ha fallado
				if ( resPagosLibre == null || 
				(resPagosLibre != null && 
				(resPagosLibre.getPagoLibre() == null || (resPagosLibre.getPagoLibre() !=null && resPagosLibre.getPagoLibre().length <=0) ) && 
				resPagosLibre.getDescripcion() != null && resPagosLibre.getDescripcion().toUpperCase().contains("ERROR"))) {
					
					FacesUtil.addErrorMessage( "pagos.realizados.error.recuperarDatos" );
					OepmLogger.error("El WS de Sireco ha fallado");
					
				} else {			
					pagosPrevios = pagosService.parsePagosLibresMao(resPagosLibre);
				}

			} else {
		 
				resPagosLibre = pagosService.getPagosPrevios(filter.getCodigoAgente(),
						filter.getDocumento(), filter.getLoginMAO(), 1, maxPagos);
				
				// HCS: El WS ha fallado
				if ( resPagosLibre == null || 
				(resPagosLibre != null && 
				(resPagosLibre.getPagoLibre() == null || (resPagosLibre.getPagoLibre() !=null && resPagosLibre.getPagoLibre().length <=0) ) && 
				resPagosLibre.getDescripcion() != null && resPagosLibre.getDescripcion().toUpperCase().contains("ERROR"))) {
					FacesUtil.addErrorMessage( "error.ws.sireco" );
					OepmLogger.error("El WS de Sireco ha fallado");
					
				} else {
					pagosPrevios = pagosService.parsePagosLibresMao(resPagosLibre);
				}
				
			}				
			
			if (resPagosLibre != null && (resPagosLibre.getNumeroTotalPagos() >= maxPagos)) {
				FacesUtil.addWarningMessage("bopi.warn.maxResultados");
			}
			else if (CollectionUtils.isEmpty(pagosPrevios)) {
				FacesUtil.addOKMessage("busqueda.sin.resultados");
			}
			
		} catch (BusinessException e) {
			OepmLogger.error(e);
			FacesUtil.addErrorMessage("pagos.bonos.error.recuperarDatos");
		}
		return null;
	}
	
	/**
	 * Action para el boton de limpiar la busqueda de pagos previos. 
	 * Creado para MAO-459
	 * @param to Parametro opcional para especificar una jsp de destino 
	 * @return
	 */
	public String actionLimpiar(String to) {
		filter = new PagosPreviosFilterVO();
		//saveFilterInSession(filter);
		this.pagosPrevios = new ArrayList<PagoPrevioVO>();
		if(StringUtils.isEmptyOrNull(to)){
			return JSFPages.PAGOS_PREVIOS;
		}
		else {
			return to;
		}
	}
	
	public String actionLimpiar() {
		return actionLimpiar(null);
	}
	
	/**
	 * Monta el filtro para los usuarios gestores.
	 * 
	 * @return
	 * @throws BusinessException
	 */
	private Boolean montarFiltroGestor() throws BusinessException {
		Boolean resultado = Boolean.TRUE;
		
		// Comprobamos que se ha introducido un documento
		if (!StringUtils.isEmptyOrNull(documentoUsuario)) {
			// Si se trata de un agente buscamos el agente
			if (TipoUsuario.AGENTE.name().equals(usuarioSeleccionado)) {
				// Dado que el WS utiliza el código del Agente en lugar de su documento para 
				// recuperar los expedientes con pagos, debemos recuperar el codAgente de BD
				UsuariosAgenteVO usuarioAgenteVo  = usuariosAgenteService.findByNumeroDocumento(documentoUsuario);
				
				// Una vez verificado el documento rellenamos los campos restantes del Agente
				if (usuarioAgenteVo != null) {
					filter.setCodigoAgente(usuarioAgenteVo.getCodAgente());
				} else {
					FacesUtil.addErrorMessage( "bopi.error.documentoNoEncontrado" );
					return Boolean.FALSE;
				}
			} else {
				filter.setDocumento(documentoUsuario);
			}
		} else {
			FacesUtil.addErrorMessage( "usuarioGestor.error.datosUsuario" );
			resultado = Boolean.FALSE;
		}
		
		return resultado;
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
	public PagosPreviosFilterVO getFilter() {
		try {
			if( filter == null ) {
				filter = new PagosPreviosFilterVO();
				
				if( SessionContext.getTipoUsuario().equals( TipoUsuario.AGENTE.name() ) ||
					( SessionContext.getTipoUsuario().equals( TipoUsuario.ASOCIADO.name() ) &&
					  !StringUtils.isEmptyOrNull( SessionContext.getCodigoAgente() ) ) ) {
					filter.setCodigoAgente( SessionContext.getCodigoAgente() );
				} else if( SessionContext.getTipoUsuario().equals( TipoUsuario.REPRESENTANTE.name() ) ||
						   ( SessionContext.getTipoUsuario().equals( TipoUsuario.ASOCIADO.name() ) &&
							 !StringUtils.isEmptyOrNull( SessionContext.getDocumentoRepresentante() ) ) ) {
					String docRepresentante = SessionContext.getTipoUsuario().equals( TipoUsuario.REPRESENTANTE.name() ) ? SessionContext.getDocumento() : SessionContext.getDocumentoRepresentante();
					
					filter.setDocumento( docRepresentante );
				} else if( SessionContext.getTipoUsuario().equals( TipoUsuario.TITULAR.name() ) ) {
					filter.setDocumento( SessionContext.getDocumento() );
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
		this.filter = ( PagosPreviosFilterVO )filter;
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