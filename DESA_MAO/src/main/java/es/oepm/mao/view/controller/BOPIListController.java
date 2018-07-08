package es.oepm.mao.view.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.apache.cxf.common.util.CollectionUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.bopiws.beans.ResultadoBopiWS;
import es.oepm.bopiws.beans.ResultadoSumario;
import es.oepm.bopiws.beans.ResultadoSumarioWS;
import es.oepm.core.business.BaseVO;
import es.oepm.core.business.ceo.vo.CeAlrCesionMvVO;
import es.oepm.core.business.ceo.vo.CeAlrExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeAlrLicenciasMvVO;
import es.oepm.core.business.ceo.vo.CeDirExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeDirTransferCesioMvVO;
import es.oepm.core.business.ceo.vo.CeMnrExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeMnrTransferCesioMvVO;
import es.oepm.core.business.mao.vo.SesionesVO;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.constants.Roles;
import es.oepm.core.constants.Sistemas;
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
import es.oepm.mao.business.service.BOPIService;
import es.oepm.mao.business.service.TrazaGestorService;
import es.oepm.mao.business.vo.BOPIFilterVO;
import es.oepm.mao.business.vo.BOPIVO;
import es.oepm.mao.business.vo.TomoBOPI;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.comun.business.service.SesionesService;
import es.oepm.mao.comun.business.service.UsuariosAgenteService;
import es.oepm.mao.comun.business.service.UsuariosTituRepreService;
import es.oepm.mao.constants.MaoTrazaGestor;
import es.oepm.mao.view.controller.util.GeneradorValoresCombo;
import es.oepm.mao.view.controller.util.HtmlStringUtils;
import es.oepm.mao.view.controller.util.JSFPages;

@ManagedBean(name = "bopiListController")
@ViewScoped
@Secured({ Roles.ROLE_MAO_PERMISO_+"B" })
public class BOPIListController extends BaseController implements ListController {
	
	private static final long serialVersionUID = 3649906212302697235L;
	
	private String solModalidad;
	private String solNumero;
	private boolean fromDetExp;
	private String tituloDetalle;
	
	private boolean mustLoadList;
	private BOPIFilterVO filter;
	private List<BOPIVO> anotaciones;
	
	private List<SelectItem> modalidadesTomo1, modalidadesTomo2, modalidadesTomo3;
	private List<SelectItem> tomos;
	private List<SelectItem> sumarios;
	
	private Date ultimoAccesoBopi;
	
	public String documentoAgenteRepresentante;
	public String nombreAgenteRepresentante;
	public String documentoTitular;
	public String nombreTitular;
	boolean agenteSucio = false;
	boolean titularSucio = false;
	
	@ManagedProperty( "#{bopiService}" )
	private BOPIService bopiService;
	
	public void setBopiService( BOPIService bopiService ) {
		this.bopiService = bopiService;
	}
	
	@ManagedProperty( "#{trazaGestorService}" )
	private TrazaGestorService trazaGestorService;
	
	public void setTrazaGestorService(TrazaGestorService trazaGestorService) {
		this.trazaGestorService = trazaGestorService;
	}
	
	@ManagedProperty( "#{usuariosAgenteService}" )
	private UsuariosAgenteService usuariosAgenteService;
	
	public void setUsuariosAgenteService( UsuariosAgenteService usuariosAgenteService ) {
		this.usuariosAgenteService = usuariosAgenteService;
	}
	
	@ManagedProperty( "#{usuariosTituRepreService}" )
	private UsuariosTituRepreService usuariosTituRepreService;
	
	public void setUsuariosTituRepreService( UsuariosTituRepreService usuariosTituRepreService ) {
		this.usuariosTituRepreService = usuariosTituRepreService;
	}
	
	@ManagedProperty( "#{sesionesService}" )
	private SesionesService sesionesService;
	
	public void setSesionesService( SesionesService sesionesService ) {
		this.sesionesService = sesionesService;
	}
	
	public BOPIListController() {
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
		
		fromDetExp = Boolean.valueOf( FacesUtil.getParameter( "fromDetExp" ) );
		
		if( !fromDetExp ) {
			BaseVO sessionValue = SessionUtil.getSearchFilter( true );
			
			if( ( sessionValue != null ) && ( sessionValue instanceof BOPIFilterVO ) ) {
				setFilter( sessionValue );
				mustLoadList = true;
			} else {			
				SessionUtil.clearSearchFilter();
			}
			
			modalidadesTomo1 = HTMLUtil.getSelectItemList( SelectItemList.createDefault(), 
					   createListModalidades( new String[] { "H", "M", "N", "R" } ), 
					   Modalidad.class, "textoModalidad", "modalidad" );
			
			modalidadesTomo2 = HTMLUtil.getSelectItemList( SelectItemList.createDefault(), 
					   createListModalidades( new String[] { "C", "E", "F", "L", "P", "T", "U", "W" } ), 
					   Modalidad.class, "textoModalidad", "modalidad" );
			
			// K significa DiseÑo Comunitario (no hay, pero debe estar previsto)
			modalidadesTomo3 = HTMLUtil.getSelectItemList( SelectItemList.createDefault(), 
					createListModalidades( new String[] { "D", "DS", "DI", "DT", "G", "I"} ), 
					   Modalidad.class, "textoModalidad", "modalidad" );
			
						
			tomos = HTMLUtil.getSelectItemList( null, 
					   							Arrays.asList( TomoBOPI.values() ), 
					   							TomoBOPI.class, "tomoValue", "tomoKey" );
			
			tomos = GeneradorValoresCombo
			.getListaTomosCompleta((Collection<SimpleGrantedAuthority>) SessionContext
					.getUserDetails().getAuthorities(), tomos);
						
			
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
	
	private List<Modalidad> createListModalidades( String[] keysModalidades ) {
		List<Modalidad> modalidades = new ArrayList<Modalidad>( keysModalidades.length );
		
		for( String keyModalidad : keysModalidades ) {
			Modalidad modalidad = Modalidad.find( keyModalidad );
			
			if( modalidad != null ) {
				modalidades.add( modalidad );
			}
		}
		
		return modalidades;
	}
	
	public List<SelectItem> getModalidades() {
		List<SelectItem> modalidades = new ArrayList<SelectItem>();
		
		if( filter != null && !StringUtils.isEmptyOrNull( filter.getTomo() ) ) {
			switch( Integer.parseInt( filter.getTomo() ) ) {
				case 1:
					modalidades = modalidadesTomo1;
					break;
				case 2:
					modalidades = modalidadesTomo2;
					break;
				case 3:
					modalidades = modalidadesTomo3;
					break;
			}
		}
		
		return modalidades;
	}
	
	public List<SelectItem> getTomos() {
		return tomos;
	}
	
	public List<SelectItem> getSumarios() {

		if( filter != null && !StringUtils.isEmptyOrNull( filter.getTomo() ) ) {
			ResultadoSumarioWS resSumarioWS = null;
			
			try {
				resSumarioWS = bopiService.getSumario( Integer.parseInt( filter.getTomo() ) );
			} catch( NumberFormatException nfe ) {
				FacesUtil.addErrorMessage( "bopi.error.tomo" );
			} catch( BusinessException e ) {
				FacesUtil.addErrorMessage( "bopi.error.recuperarSumario" );
			}
			
			if( resSumarioWS != null) {
				if (resSumarioWS.getSumario() != null ) {		
					this.sumarios = getSelectBOPISumarios(SelectItemList.createDefault(), 
														  Arrays.asList( resSumarioWS.getSumario() ));
				}
				
				if (!StringUtils.isEmptyOrNull(resSumarioWS.getInfo())) {
					if (resSumarioWS.getInfo().contains("error")) {
						// HCS: Error en la llamada al WS
						FacesUtil.addErrorMessage("error.ws.bopi");
						OepmLogger.error("Error al invocar el WS de BOPI");
					}
				}
				
			}
		}
		
		return this.sumarios;
	}
	
	/**
	 * Crea una lista con los SelectItem para el combobox de sumarios de BOPI
	 * @param defaultItem
	 * @param listadoVO
	 * @return
	 */
	private List<SelectItem> getSelectBOPISumarios(SelectItem defaultItem, final List<ResultadoSumario> listadoVO) {
		
		List<SelectItem> result = new ArrayList<SelectItem>();
		
		// El primer elemento de nuestro combo
		if( defaultItem != null ) {
			result.add( defaultItem );
		}
		
		for (ResultadoSumario sumario : procesarListadoVO(listadoVO)) {
							
			if (sumario.getId_subapartado() == null) {
				SelectItemGroup item = new SelectItemGroup(sumario.getNombre());						
				item.setSelectItems(new SelectItem[0]);
				result.add(item);							
			}
			else {
				SelectItem item = new SelectItem();
				item.setLabel( sumario.getNombre() == null ? "" : sumario.getNombre() );
				item.setValue( sumario.getId_subapartado() == null ? "" : sumario.getId_subapartado() );
				
				result.add(item);
			}
		}
		
		return result;
	}
	
	/**
	 * Procesa los nombres de los items del lisado de sumarios para que se
	 * vean mejor en MIO
	 * @param listadoVO
	 * @return
	 */
	private List<ResultadoSumario> procesarListadoVO(List<ResultadoSumario> listadoVO) {
		
		int curGroup = 0;
		int curSubGroup = 0;
		int curSubSubGroup = 0;
		int curItem = 0;
		String nombre = "";
		
		for (ResultadoSumario item : listadoVO) {

			if (item.getId_subapartado() == null) {			
				
				if (StringUtils.isNumeric( item.getNombre().substring(0, 1))) {
					// Cambio de primer nivel de categoria... No es necesario tratar el nombre, ya está numerado
					curGroup++;
					curSubGroup = 0;
					curItem = 0;
				} else {			
					if (item.getOrden().toString().length() >= 6) {
						// Cambio de tercer nivel de categoria
						curSubSubGroup++;
					} else {
						// Cambio de segundo nivel de categoria		
						curSubSubGroup = 0;
						curSubGroup++;
					}
					
					curItem = 0;
					nombre = "  "+curGroup+"."+curSubGroup+". "+item.getNombre();
					
					if (curSubSubGroup > 0) {
						nombre = "    "+curGroup+"."+curSubGroup+"."+curSubSubGroup+". "+item.getNombre();	
					}
					
					item.setNombre(nombre);
				}
			} else if (curGroup > 0) {
				// Introduccion de los items seleccionables
				curItem++;
				nombre = curGroup+"."+curItem+". "+item.getNombre();
				
				if (curSubSubGroup > 0) {
					nombre = "    "+curGroup+"."+curSubGroup+"."+curSubSubGroup+"."+curItem+". "+item.getNombre();	
				} else if (curSubGroup > 0) {
					nombre = "  "+curGroup+"."+curSubGroup+"."+curItem+". "+item.getNombre(); 
				}
				
				item.setNombre(nombre);				
			}
		}
		return listadoVO;	
	}
	
	
	public List<BOPIVO> getAnotaciones() {
		return anotaciones;
	}
	
	@Override
	@Secured({ Roles.ROLE_MAO_PERMISO_+"B" })
	public String actionSearch() {
		int minfilter = 0;
		
		if( filter != null ) {
			if( filter.getFechaPublicacionDesde() != null && filter.getFechaPublicacionHasta() != null ) {
				minfilter++;
			}
			
			if( !StringUtils.isEmptyOrNull( filter.getModalidad() ) ) {
				minfilter++;
			}
			
			if( !StringUtils.isEmptyOrNull( filter.getSolicitud() ) ) {
				minfilter++;
			}
			
			if( !StringUtils.isEmptyOrNull( filter.getPublicacion() ) ) {
				minfilter++;
			}
			
			if( !StringUtils.isEmptyOrNull( filter.getTomo() ) ) {
				minfilter++;
			}
			
			if( !StringUtils.isEmptyOrNull( filter.getSumario() ) ) {
				minfilter++;
			}
			
			if( !StringUtils.isEmptyOrNull( filter.getTitular() ) ) {
				minfilter++;
			}
			
//			if( !StringUtils.isEmptyOrNull( filter.getAgente() ) ) {
//				minfilter++;
//			}
		}
		
		if( minfilter > 1 ) {
			anotaciones = null;
			
			try {
				String titular = null;
				String agente = null;
				
				if (!fromDetExp) {
					titular = filter.getTitular();
					if( SessionContext.getTipoUsuario().equals( TipoUsuario.AGENTE.name() ) ||
						( SessionContext.getTipoUsuario().equals( TipoUsuario.ASOCIADO.name() ) &&
						  !StringUtils.isEmptyOrNull( SessionContext.getCodigoAgente() ) ) ) {
						agente = bopiService.formatNombreCompletoAgenteByCodigo( Integer.parseInt( filter.getTomo() ), SessionContext.getCodigoAgente() );
						if(agente == null){
							FacesUtil.addErrorMessage( "bopi.error.documentoNoEncontrado" );
							return null;
						}
					} else if( SessionContext.getTipoUsuario().equals( TipoUsuario.REPRESENTANTE.name() ) ||
							   ( SessionContext.getTipoUsuario().equals( TipoUsuario.ASOCIADO.name() ) &&
								 !StringUtils.isEmptyOrNull( SessionContext.getDocumentoRepresentante() ) ) ) {
						agente = bopiService.formatNombreCompletoRepresentanteTitular( Integer.parseInt( filter.getTomo() ), SessionContext.getDocumentoRepresentante(), true );
						if(agente == null){
							FacesUtil.addErrorMessage( "bopi.error.documentoNoEncontrado" );
							return null;
						}
					} else if( SessionContext.getTipoUsuario().equals( TipoUsuario.TITULAR.name() ) ) {
						titular = bopiService.formatNombreCompletoRepresentanteTitular( Integer.parseInt( filter.getTomo() ), SessionContext.getDocumentoTitular(), false );
						if(titular == null){
							FacesUtil.addErrorMessage( "bopi.error.documentoNoEncontrado" );
							return null;
						}
					} else if (TipoUsuario.GESTOR.name().equals(SessionContext.getTipoUsuario())) {
						titular = nombreTitular;
						agente = nombreAgenteRepresentante;
						
						// Registramos la traza
						StringBuilder detalle = new StringBuilder();
						detalle.append(filter.toTraze());
						detalle.append("Titular: ").append(nombreTitular).append(" ,");
						detalle.append("Agente/Representante: ").append( nombreAgenteRepresentante);
						trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_BOPI, detalle.toString());
					}
				} else {
					if (TipoUsuario.GESTOR.name().equals(SessionContext.getTipoUsuario())) {
						// Registramos la traza
						StringBuilder detalle = new StringBuilder();
						detalle.append("Expediente: ").append(solModalidad).append(solNumero).append(" ,");
						detalle.append("Tomo: ").append(filter.getTomo());
						trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_DET_EXP_BOPI, detalle.toString());
					}
				}
				
				// HCS: Optimizada recupetación de datos del WS para la ordenación.
				int maxPagos = Configuracion.getPropertyAsInteger("MAX_RESULTADOS_PAGOS");
				
				ResultadoBopiWS resultadoBopiWS = bopiService.getAnotaciones(
						filter.getFechaPublicacionDesde(),
						filter.getFechaPublicacionHasta(), filter.getModalidad(),
						filter.getSolicitud(), filter.getPublicacion(),
						Integer.parseInt(filter.getTomo()), filter.getSumario(),
						titular, agente, 1, maxPagos);
				
				if ( (resultadoBopiWS == null) || 
				     (resultadoBopiWS != null && "-1".equals(resultadoBopiWS.getNumTotal())) ||
				     (resultadoBopiWS != null && resultadoBopiWS.getNumTotal() == null) ) {
					// HCS: Error en la llamada al WS
					FacesUtil.addErrorMessage("error.ws.bopi");
					OepmLogger.error("Error al invocar el WS de BOPI");
				} 
				else if (resultadoBopiWS != null && resultadoBopiWS.getResultado() != null) {
					anotaciones = bopiService.parseBOPIList(resultadoBopiWS);
					Integer rowCount = resultadoBopiWS.getResultado().length;
					
					if (rowCount >= maxPagos) {
						FacesUtil.addWarningMessage("bopi.warn.maxResultados");
					}
					else if (CollectionUtils.isEmpty(anotaciones)) {
						// HCS: Busqueda sin resultados
						FacesUtil.addOKMessage("busqueda.sin.resultados");		
					}		
					
					SessionContext.setUltimaBusquedaBopi(new Date());	
				}
				else {
					// HCS: Busqueda sin resultados
					FacesUtil.addOKMessage("busqueda.sin.resultados");		
				}
				
			} catch( NumberFormatException nfe ) {
				FacesUtil.addErrorMessage( "bopi.error.tomo" );
			} catch( BusinessException be ) {
				FacesUtil.addErrorMessage( "bopi.error.recuperarAnotaciones" );
			}
		} else {
			FacesUtil.addErrorMessage( "bopi.error.criterioBusqueda" );
		}	
		return null;
	}
	
	/**
	 * Action para el boton de limpiar en busqueda BOPI. 
	 * Creado para MAO-459
	 * @param to Parametro opcional para especificar una jsp de destino 
	 * @return
	 */
	public String actionLimpiar(String to) {
		filter = new BOPIFilterVO(tomos.get(0).getValue().toString());
		//saveFilterInSession(filter);
		this.anotaciones = new ArrayList<BOPIVO>();	
		// This rebuild the page and shows everything clean
		if(StringUtils.isEmptyOrNull(to)){
			return JSFPages.BUSQUEDA_BOPI;
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
	public BOPIFilterVO getFilter() {
		try {
			if( filter == null ) {
				if( !fromDetExp ) {
					filter = new BOPIFilterVO( tomos.get(0).getValue().toString() );
				} else {
					TomoBOPI tomoBOPI = null;
					Modalidad modalidad = Modalidad.find( solModalidad );
					
					if( modalidad.isSitamar() ) {
						tomoBOPI = TomoBOPI.TOMO1;
					} else if( modalidad.isAlfa() ) {
						tomoBOPI = TomoBOPI.TOMO2;
					} else {
						tomoBOPI = TomoBOPI.TOMO3;
					}
					
					filter = new BOPIFilterVO( tomoBOPI.getTomoKey(), solModalidad, solNumero );
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
		this.filter = ( BOPIFilterVO )filter;
	}

	public String getTituloDetalle() {
		return tituloDetalle;
	}

	public void setTituloDetalle(String tituloDetalle) {
		this.tituloDetalle = tituloDetalle;
	}

	public Date getUltimoAccesoBopi() {
		if( ultimoAccesoBopi == null ) {
			try {
				SesionesVO sesion = sesionesService.getById( SessionContext.getLoginUsuario() );
				
				if( sesion != null ) {
					ultimoAccesoBopi = sesion.getUltimoAccesoBopi();
				}
			} catch( BusinessException e ) {
				OepmLogger.debug( "No se ha podido recuperar la sesion del usuario" );
			}
		}
		
		return ultimoAccesoBopi;
	}

	public boolean esAnotacionNueva( BOPIVO resultadoBusqueda ) {
		boolean esNueva = false;
		
		if( getUltimoAccesoBopi() == null ) {
			esNueva = true;
		} else {
			esNueva = getUltimoAccesoBopi().before( resultadoBusqueda.getAnotacion().getFechaPublicacion().getTime() );
		}
		
		return esNueva;
	}

	public Boolean getModalidadSitamod() {
		if(StringUtils.isEmptyOrNull(solModalidad))
			return false;
		
		Modalidad m = Modalidad.find(solModalidad);
		return m.isSitamod();
	}
	/**
	 * @return the documentoAgenteRepresentante
	 */
	public String getDocumentoAgenteRepresentante() {
		return documentoAgenteRepresentante;
	}

	/**
	 * @param documentoAgenteRepresentante the documentoAgenteRepresentante to set
	 */
	public void setDocumentoAgenteRepresentante(String documentoAgenteRepresentante) {
		this.documentoAgenteRepresentante = documentoAgenteRepresentante;
		agenteSucio = true;
		nombreAgenteRepresentante = "";
		getNombreAgenteRepresentante();
	}

	/**
	 * @return the nombreAgenteRepresentante
	 */
	public String getNombreAgenteRepresentante() {
		if (agenteSucio || StringUtils.isEmptyOrNull(nombreAgenteRepresentante)) {
			if(!StringUtils.isEmptyOrNull(documentoAgenteRepresentante)){
				//Se setea el nombre del Agente/Representante al introducir su documento
				nombreAgenteRepresentante = bopiService.formatNombreCompletoAgenteByDocumento( Integer.parseInt( filter.getTomo() ), documentoAgenteRepresentante );
				if (StringUtils.isEmptyOrNull(nombreAgenteRepresentante)){
					nombreAgenteRepresentante = bopiService.formatNombreCompletoRepresentanteTitular( Integer.parseInt( filter.getTomo() ),
							documentoAgenteRepresentante, true );
				}
				if(StringUtils.isEmptyOrNull(nombreAgenteRepresentante)){
					FacesUtil.addErrorMessage( "bopi.error.documentoNoEncontrado" );				
				}
			}
			agenteSucio = false;
		}
		return nombreAgenteRepresentante;
	}

	/**
	 * @param nombreAgenteRepresentante the nombreAgenteRepresentante to set
	 */
	public void setNombreAgenteRepresentante(String nombreAgenteRepresentante) {
		this.nombreAgenteRepresentante = nombreAgenteRepresentante;
	}

	/**
	 * @return the documentoTitular
	 */
	public String getDocumentoTitular() {
		return documentoTitular;
	}

	/**
	 * @param documentoTitular the documentoTitular to set
	 */
	public void setDocumentoTitular(String documentoTitular) {
		this.documentoTitular = documentoTitular;
		titularSucio = true;
		nombreTitular = "";
		getNombreTitular();
	}

	/**
	 * @return the nombreTitular
	 */
	public String getNombreTitular() {
		if (titularSucio || StringUtils.isEmptyOrNull(nombreTitular)) {
			if(!StringUtils.isEmptyOrNull(documentoTitular)){
				//Se setea el nombre del Titular al introducir su documento
				nombreTitular = bopiService.formatNombreCompletoRepresentanteTitular( Integer.parseInt( filter.getTomo() ),
							documentoTitular, false );
				
				if(StringUtils.isEmptyOrNull(nombreTitular)){
					FacesUtil.addErrorMessage( "bopi.error.documentoNoEncontrado" );
				}
			}
			titularSucio = false;
		}
		return nombreTitular;
	}

	/**
	 * @param nombreTitular the nombreTitular to set
	 */
	public void setNombreTitular(String nombreTitular) {
		this.nombreTitular = nombreTitular;
	}
}