package es.oepm.mao.view.controller;

import java.util.Calendar;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.primefaces.event.data.PageEvent;
import org.primefaces.event.data.SortEvent;
import org.primefaces.model.LazyDataModel;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.core.business.BaseVO;
import es.oepm.core.business.ceo.vo.CeAlrCesionMvVO;
import es.oepm.core.business.ceo.vo.CeAlrExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeAlrLicenciasMvVO;
import es.oepm.core.business.ceo.vo.CeDirExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeDirTransferCesioMvVO;
import es.oepm.core.business.ceo.vo.CeMnrExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeMnrTransferCesioMvVO;
import es.oepm.core.business.vo.EstadoPLATANOT;
import es.oepm.core.business.vo.MensajeVO;
import es.oepm.core.business.vo.OrigenVO;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.constants.Roles;
import es.oepm.core.constants.Sistemas;
import es.oepm.core.constants.TipoUsuariosMAO;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.service.MensajesService;
import es.oepm.core.session.SessionContext;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.controller.ListController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.core.view.faces.SelectItemList;
import es.oepm.core.view.util.HTMLUtil;
import es.oepm.mao.business.service.TrazaGestorService;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.constants.MaoTrazaGestor;
import es.oepm.mao.contenido.business.service.OriPlatanotService;
import es.oepm.mao.view.MensajesLazyDataModel;
import es.oepm.mao.view.controller.util.JSFPages;
import es.oepm.wservices.core.util.WSUtils;


@ManagedBean(name = "comunicacionesController")
@ViewScoped
public class ComunicacionesController extends BaseController implements ListController {
	
	private static final long serialVersionUID = -9071818761782051919L;

	private static final String MENSAJE_WARN_FROMDETEXP = "detalleExp.comunicaciones.gestor.warning";
	
	private String solModalidad;
	private String solNumero;
	private boolean fromDetExp;
	private String tituloDetalle;
	
	private boolean mustLoadList;
	private MensajeVO filter;
	private LazyDataModel<MensajeVO> comunicaciones;
	
	private List<SelectItem> origenes;
	
	private MensajeVO selectedComunicacion;
	
	// Documento para buscar con usuario Gestor
	public String documentoBusquedaGestor;
	
	@ManagedProperty( "#{mensajesService}" )
	private MensajesService mensajesService;

	@ManagedProperty( "#{oriPlatanotService}" )
	private OriPlatanotService oriPlatanotService;
	
	@ManagedProperty( "#{trazaGestorService}" )
	private TrazaGestorService trazaGestorService;
	
	public ComunicacionesController() {
		super();
		Object exp = SessionUtil.getFromSession("detalleExpediente");
		if(exp instanceof CeMnrExpedientesMvVO ){
			solModalidad = ((CeMnrExpedientesMvVO) exp).getModalidad();
			solNumero = ((CeMnrExpedientesMvVO)exp).getNumero();		
		}else if(exp instanceof CeAlrExpedientesMvVO ){
			solModalidad = ((CeAlrExpedientesMvVO) exp).getModalidad();
			solNumero = ((CeAlrExpedientesMvVO)exp).getNumeroSolicitud();
		}else if(exp instanceof CeDirExpedientesMvVO ){
			solModalidad = ((CeDirExpedientesMvVO) exp).getModalidad();
			solNumero = ((CeDirExpedientesMvVO)exp).getNumero();
		}else if(exp instanceof CeAlrCesionMvVO ){
			solModalidad  = Modalidad.AL_TRANSMISION.getModalidad();
			solNumero= ((CeAlrCesionMvVO)exp).getNumeroCesion();
		}else if(exp instanceof CeAlrLicenciasMvVO){
			solModalidad  = Modalidad.AL_LICENCIA.getModalidad();
			solNumero= ((CeAlrLicenciasMvVO)exp).getNumeroLicencia();
		}else if(exp instanceof CeDirTransferCesioMvVO ){			
			solModalidad  = Modalidad.find(((CeDirTransferCesioMvVO) exp).getIndCesion(), Sistemas.SITAMOD.name()).getModalidad();
			solNumero= ((CeDirTransferCesioMvVO)exp).getAnoTransfer() +((CeDirTransferCesioMvVO)exp).getNumTransfer();
		}else if(exp instanceof CeMnrTransferCesioMvVO){
			solModalidad  = Modalidad.find(((CeMnrTransferCesioMvVO) exp).getIndCesion(), Sistemas.SITAMAR.name()).getModalidad();
			solNumero= ((CeMnrTransferCesioMvVO)exp).getAnoTransfer() +((CeMnrTransferCesioMvVO)exp).getNumTransfer();
		}	

		this.tituloDetalle = (String)SessionUtil.getFromSession("tituloDetalle");
				
		// Indica si la peticion proviene del detalle de un expediente
		fromDetExp = Boolean.valueOf( FacesUtil.getParameter( "fromDetExp" ) );
		
		if( !fromDetExp ) {
			BaseVO sessionValue = SessionUtil.getSearchFilter( true );
			
			if( ( sessionValue != null ) && ( sessionValue instanceof MensajeVO ) ) {
				setFilter( sessionValue );
	
				mustLoadList = true;
			} else {			
				SessionUtil.clearSearchFilter();
			}
		} else {
			filter = getFilter();
		}
	}
	
	public String getSolModalidad() {
		return solModalidad;
	}
	
	public String getSolNumero() {
		return solNumero;
	}
	
	public boolean getFromDetExp() {
		return fromDetExp;
	}
	
	public LazyDataModel<MensajeVO> getComunicaciones() {
		return comunicaciones;
	}
	
	public List<SelectItem> getOrigenes() {
		if( origenes == null ) {
			try {
				List<OrigenVO> auxOrigenes = oriPlatanotService.search(new OrigenVO());
				
				if( auxOrigenes != null ) {
					origenes = HTMLUtil.getSelectItemList( SelectItemList.createDefault(), 
							   auxOrigenes , 
							   OrigenVO.class, "nombre", "nombre" );
				}
			} catch( BusinessException e ) {
				FacesUtil.addErrorMessage( "comynot.error.recuperarOrigenes" );
			}
		}
		
		return origenes;
	}
	
	public MensajeVO getSelectedComunicacion() {
		return selectedComunicacion;
	}
	
	public void setSelectedComunicacion( MensajeVO selectedComunicacion ) {
		this.selectedComunicacion = selectedComunicacion;
	}
	
	@Override
	public String actionSearch() {
		if( !WSUtils.isAvailable( Configuracion.getPropertyAsString( "url.bus.platanot" ) + "?wsdl" ) ) {
			FacesUtil.addErrorMessage( "comynot.comunicaciones.error.recuperarDatos" );
		} else {
			if (TipoUsuariosMAO.GESTOR.name().equals(SessionContext.getTipoUsuario())) {
				this.searchGestor();
			} else {
				comunicaciones = new MensajesLazyDataModel( mensajesService, false, filter );
			}
		}
		
		return null;
	}
	
	/**
	 * Realiza la búsqueda para los usuarios gestores
	 */
	private void searchGestor() {
		// Seteamos el documento al filtro
		filter.getDestinatario().setDocumento(documentoBusquedaGestor);
		
		try {
			if (fromDetExp) {
				// Mostramos el warning indicando que la consulta se ha de
				// realizar en la pantalla de comunicaicones
				FacesUtil.addWarningMessage(MENSAJE_WARN_FROMDETEXP);
			} else {
				// Insertamos la traza
				trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_COMUNICACIONES, filter.toTraze());
				comunicaciones = new MensajesLazyDataModel( mensajesService, false, filter );
			}
		
		} catch (BusinessException e) {
			OepmLogger.error(e);
		}
	}
	
	@Override
	public String actionEdit() {
		return null;
	}
	
	/**
	 * Consulta una comunicación.
	 * 
	 * @param idComunicacion
	 *            Id de la comunicación.
	 * @return
	 */
	public String actionConsultar(String idComunicacion) {
		String forward = null;
		saveFilterInSession( filter );
		
		try {
			// Actualizamos la última lectura
			actualizarLecturaComunicacion();
			
			forward = String.format( "%s?faces-redirect=true&idComunicacion=%s&solModalidad=%s&solNumero=%s&fromDetExp=%b", 
									 JSFPages.COMYNOT_COM_VIEW, 
									 idComunicacion,
									 solModalidad == null ? "" : solModalidad,
									 solNumero == null ? "" : solNumero,
									 fromDetExp );
		} catch( BusinessException be ) {
			OepmLogger.error(be);
			FacesUtil.addErrorMessage( "comynot.comunicaciones.error.actualizarComunicacion" );
		}
		
		return forward;
	}
	
	/**
	 * Actualiza la lectura de una comunicación
	 * 
	 * @throws BusinessException
	 */
	private void actualizarLecturaComunicacion() throws BusinessException {
		// Fecha actural
		Calendar now = Calendar.getInstance();
		
		// Si es de solo lectura no la marcamos como leida
		if(!SessionContext.hasUserRole(Roles.ROLE_MAO_SOLOLECTURA)) {
			if(!selectedComunicacion.isLeido()) {
				if( mensajesService.actualizarComunicacion( selectedComunicacion.getId(), true, now, now ) ) {
					selectedComunicacion.setLeido( true );
				}
			} else {
				mensajesService.actualizarUltimaLecturaComunicacion( selectedComunicacion.getId(), now );
			}
		}
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
	public MensajeVO getFilter() {
		try {
			if( filter == null ) {
				String documento = null;
				
				if( SessionContext.getTipoUsuario().equals( TipoUsuario.ASOCIADO.name() ) ) {
					if( !StringUtils.isEmptyOrNull( SessionContext.getDocumentoAgente() ) ) {
						documento = SessionContext.getDocumentoAgente();
					} else if( !StringUtils.isEmptyOrNull( SessionContext.getDocumentoRepresentante() ) ) {
						documento = SessionContext.getDocumentoRepresentante();
					} else {
						documento = SessionContext.getDocumentoTitular();
					}
				} else {
					documento = SessionContext.getDocumento();
				}
				
				if( !fromDetExp ) {
					filter = new MensajeVO( documento );
				} else {
					filter = new MensajeVO( documento, solNumero );
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
		this.filter = ( MensajeVO )filter;
	}

	public String getMensajeEstadoAceptada() {
		return EstadoPLATANOT.ACEPTADA.getEstadoValue();
	}
	
	public String getMensajeEstadoPendiente() {
		return EstadoPLATANOT.PENDIENTE.getEstadoValue();
	}

	public String getTituloDetalle() {
		return tituloDetalle;
	}

	public void setTituloDetalle(String tituloDetalle) {
		this.tituloDetalle = tituloDetalle;
	}

	public void paginateComunicaciones( PageEvent event ) {
		if( !WSUtils.isAvailable( Configuracion.getPropertyAsString( "url.bus.platanot" ) + "?wsdl" ) ) {
			FacesUtil.addErrorMessage( "comynot.comunicaciones.error.recuperarDatos" );
		}
	}
	
	public void sortComunicaciones( SortEvent event ) {
		if( !WSUtils.isAvailable( Configuracion.getPropertyAsString( "url.bus.platanot" ) + "?wsdl" ) ) {
			FacesUtil.addErrorMessage( "comynot.comunicaciones.error.recuperarDatos" );
		}
	}
	
	public void setMensajesService(MensajesService mensajesService) {
		this.mensajesService = mensajesService;
	}
	
	public void setOriPlatanotService(OriPlatanotService oriPlatanotService) {
		this.oriPlatanotService = oriPlatanotService;
	}
	
	public void setTrazaGestorService(TrazaGestorService trazaGestorService) {
		this.trazaGestorService = trazaGestorService;
	}
	
	public Boolean getModalidadSitamod() {
		if(StringUtils.isEmptyOrNull(solModalidad))
			return false;
		
		Modalidad m = Modalidad.find(solModalidad);
		return m.isSitamod();
	}

	/**
	 * @return the documentoBusquedaGestor
	 */
	public String getDocumentoBusquedaGestor() {
		return documentoBusquedaGestor;
	}

	/**
	 * @param documentoBusquedaGestor the documentoBusquedaGestor to set
	 */
	public void setDocumentoBusquedaGestor(String documentoBusquedaGestor) {
		this.documentoBusquedaGestor = documentoBusquedaGestor;
	}
}