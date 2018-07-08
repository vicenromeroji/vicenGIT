package es.oepm.mao.view.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.ayesa.utilities.iconfiguration.Configuracion;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import es.oepm.busmule.ws.client.ceo.BusExpediente;
import es.oepm.busmule.ws.client.ceo.parameters.BusConsultarExpedientesResponse;
import es.oepm.core.business.mao.vo.AyudasVO;
import es.oepm.core.business.mao.vo.EnlacesVO;
import es.oepm.core.business.mao.vo.FamiliasVO;
import es.oepm.core.business.mao.vo.MensajesDiaVO;
import es.oepm.core.business.mao.vo.TramiteDocumentosVO;
import es.oepm.core.business.mao.vo.TramitesVO;
import es.oepm.core.business.vo.MensajeVO;
import es.oepm.core.constants.Mensaje;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.service.MensajesService;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.business.service.ExpedientesService;
import es.oepm.mao.business.vo.ExpedienteVO;
import es.oepm.mao.constants.MaoPropiedadesConf;
import es.oepm.mao.contenido.business.service.AyudasService;
import es.oepm.mao.contenido.business.service.EnlacesService;
import es.oepm.mao.contenido.business.service.FamiliasService;
import es.oepm.mao.contenido.business.service.MensajesDiaService;
import es.oepm.mao.contenido.business.service.TramitesService;
import es.oepm.mao.view.controller.util.HtmlStringUtils;
import es.oepm.mao.view.controller.util.JSFPages;
import es.oepm.mao.view.controller.util.MAOConfiguracion;
import es.oepm.platanot.vo.NotificacionVO;
import es.oepm.wservices.core.mensajes.Mensajes;


/**
 * Controlador de la pagina principal
 * 
 * @author AYESA AT
 */
@ManagedBean(name = "principalController")
@SessionScoped
public class PrincipalController implements Serializable {
	
	private static final long serialVersionUID = 607153240457874181L;

	@ManagedProperty(name = "tramitesService", value = "#{tramitesService}")
	private TramitesService tramitesService;

	@ManagedProperty(name = "familiasService", value = "#{familiasService}")
	private FamiliasService familiasService;
	
	@ManagedProperty(name = "enlacesService", value = "#{enlacesService}")
	private EnlacesService enlacesService;
	
	@ManagedProperty(name = "mensajesService", value = "#{mensajesService}")
	private MensajesService mensajesService;
	
	@ManagedProperty(name = "mensajesDiaService", value = "#{mensajesDiaService}")
	private MensajesDiaService mensajesDiaService;
	
	@ManagedProperty(name = "busquedaExpController", value = "#{busquedaExpController}")
	private BusquedaExpedientesController busquedaExpController;

	@ManagedProperty(name = "ayudasService", value = "#{ayudasService}")
	private AyudasService ayudasService;
	
	@ManagedProperty(name = "expedientesService", value = "#{expedientesService}")
	private ExpedientesService expedientesService;
	
	/**
	 * TODO eliminar cuando se considere oportuno
	private List<FamiliasVO> familias = null;*/
	private List<EnlacesVO> enlaces = null;
	private ArrayList<List<EnlacesVO>> listaEnlaces = null;
	private List<SyndEntry> ultimasNoticias = null;
	private List<SyndEntry> noticias = null;
	private List<MensajeVO> comunicaciones = null;
	private List<NotificacionVO> notificaciones = null;
	private MensajesDiaVO mensajeDia = null;
	private List<ExpedienteVO> expedientes = null;
	
	//Seleccion
	private FamiliasVO familiaSeleccionada;
	private List<TramitesVO> tramites; 
	private TramitesVO tramiteSeleccionado;
	
	private String logoMinisterio;
	private String logoOEPM;
	
	private String logoMinisterioReport;
	private String logoOEPMReport;
	
	//Cambio del logo LOGO_MIN_OEPM
	private String logoMINOEPM;
	private String logoSede;
	private String enlaceSede;
	private String mensajeEnlace;
	
	// Interruptor para comunicaciones y notificaciones
	private Boolean comNotifIsDisabled;
	
	// Interruptor para seccion de bonos
	private Boolean bonosIsDisabled;
	
	// Interruptores para sitamod,sitamar y alfa
	private Boolean sitamodIsDisabled;
	private Boolean sitamarIsDisabled; 
	private Boolean alfaIsDisabled;
	
	/**
	 *  xteve019 - [MAO-501] - v1.8.4 - Desactivar pestaña de pagos en Mao
	 **/	
	//Interruptores para pagos
	private Boolean pagosIsDisabled;
	
	public PrincipalController () {
	}
	
	@PostConstruct
	public void init () {
		comNotifIsDisabled = MAOConfiguracion.getComNotifIsDisabled();
		bonosIsDisabled = MAOConfiguracion.getBonosIsDisabled();
		sitamodIsDisabled = MAOConfiguracion.getSitamodIsDisabled();
		sitamarIsDisabled = MAOConfiguracion.getSitamarIsDisabled();	
		alfaIsDisabled = MAOConfiguracion.getAlfaIsDisabled();
		setPagosIsDisabled(MAOConfiguracion.getPagosIsDisabled());
	}

	/**
	 * @return the tramitesService
	 */
	public TramitesService getTramitesService() {
		return tramitesService;
	}
	
	public void esExpedienteSecreto() {
		
		Boolean expedienteSecreto = (Boolean) SessionUtil.getFromSession("expedienteSecreto");
			
		if (Boolean.TRUE.equals(expedienteSecreto)) {
			SessionUtil.addToSession("expedienteSecreto", false);
			FacesUtil.addWarningMessage("error.expedienteSecreto");
		}
		
	}
	
	/**
	 * Carga familias de tramites
	 * TODO eliminar cuando se considere oportuno
	private void cargarTramites () {
		try {
			if (familias == null) {
				familias = familiasService.search(new FamiliasVO());
			}
			eliminarFamiliasSinAcceso();
		} catch (Exception e) {
			familias = null;
			FacesUtil.addErrorMessage("principal.error.familias", e);
		}
	}*/
	
	/**
	 * Elimina de la lista de familias las familias a las que el usuario no tiene acceso
	@SuppressWarnings("unchecked")
	private void eliminarFamiliasSinAcceso() {
		// Iterador para recorrer los roles
		Collection<GrantedAuthority> authList = (Collection<GrantedAuthority>) SessionContext.getUserDetails().getAuthorities();
		// Iterador para recorrer las familias
		Iterator<FamiliasVO> it = familias.iterator();
		while (it.hasNext()) {
			FamiliasVO familiActual = it.next();
			
			// Si es la familia de signos comprobamos si tiene el rol ROLE_MAO_MODALIDAD_S
			if(familiActual.getTxDescripcion().toLowerCase().indexOf("signo")>=0){
				Boolean rolModalidadSignos = Boolean.FALSE;
				for (GrantedAuthority grantedAuthority : authList) {
					if (grantedAuthority.getAuthority().equals(Roles.ROLE_MAO_MODALIDAD_ + "S")) {
						rolModalidadSignos = Boolean.TRUE;
					}
				}
				
				// Si no existe eliminamos la familia de la lista
				if(!rolModalidadSignos) {
					it.remove();
				}
			}
			
			// Si es la familia de diseños comprobamos si tiene el rol ROLE_MAO_MODALIDAD_D
			if(familiActual.getTxDescripcion().toLowerCase().indexOf("diseñ")>=0){
				Boolean rolModalidadDisenos = Boolean.FALSE;
				for (GrantedAuthority grantedAuthority : authList) {
					if (grantedAuthority.getAuthority().equals(Roles.ROLE_MAO_MODALIDAD_ + "D")) {
						rolModalidadDisenos = Boolean.TRUE;
					}
				}
				
				// Si no existe eliminamos la familia de la lista
				if(!rolModalidadDisenos) {
					it.remove();
				}
			}
		}
	}*/
	
	/**
	 * Carga de enlaces BD
	 */
	private void cargarEnlaces() {
		try {
			if (enlaces == null) {
				enlaces = enlacesService.search(new EnlacesVO());
				ordenaListaEnlaces();
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("principal.error.enlaces", e);
		}
	}
	
	/**
	 * Rellena la lista con las 3 listas de los enlaces
	 */
	private void ordenaListaEnlaces() {
		if (enlaces != null) {
			listaEnlaces = new ArrayList<List<EnlacesVO>>();
			// La primera columna de enlaces tendra tramo1 enlaces
			int tramo1 = enlaces.size()/3;
			int tramo2 = tramo1;
			if (enlaces.size()%3 != 0){
				tramo1 += 1;
				// La segunda columna de enlaces tendra tramo2 enlaces
				if (enlaces.size()%3 == 2) {
					tramo2 -= 1;
				}
			}
			listaEnlaces.add(enlaces.subList(0, tramo1));
			listaEnlaces.add(enlaces.subList(tramo1, tramo1 + tramo2));
			// El numero de enlaces de las columnas 2 y 3 diferiran como mucho en 1 enlace
			listaEnlaces.add(enlaces.subList(tramo1 + tramo2, enlaces.size()));
		}
	}
	
	@SuppressWarnings("unchecked")
	private void cargarNoticias () throws Exception {
	    XmlReader reader = null;
	    
		try {
			// RSS Reader con Rome
			if (ultimasNoticias==null || noticias==null
					|| ultimasNoticias.isEmpty() || noticias.isEmpty()) {
				// Creación de la conexión a través de un proxy
				String urlRSS = Configuracion.getPropertyAsString(MaoPropiedadesConf.URL_RSS);
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.oepm.local", 8080));
                reader = new XmlReader(new URL(urlRSS).openConnection(proxy));

				SyndFeedInput input = new SyndFeedInput();
				SyndFeed feed = input.build (reader);
				
				ultimasNoticias = new ArrayList<SyndEntry>();
				noticias = new ArrayList<SyndEntry>();
				
				Integer numMaxResultados = Configuracion.getPropertyAsInteger("NUM_ULTIMAS_NOTICIAS");
				
				for (Iterator<SyndEntry> i = feed.getEntries().iterator(); i.hasNext();) {
			        SyndEntry entry = i.next();
			        
			        String title = entry.getTitle();
			        String parsedTitle = HtmlStringUtils.getContentBetweenHtmlTags(title);
			        entry.setTitle(parsedTitle);
			        if (ultimasNoticias.size() < numMaxResultados) {
			        	ultimasNoticias.add(entry);
			        }
			        noticias.add(entry);
			    }
			}
		}catch (Exception e){
			FacesUtil.addErrorMessage("principal.error.enlaces", e);
		} finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception ingnored) {}
            }
        }
	}
	
	/**
	 * Carga de ultimas comunicaciones
	 */
	private void cargarComunicaciones () throws Exception {
		if (comunicaciones == null) {
			Integer numMaxResultados = Configuracion.getPropertyAsInteger("NUM_ULTIMAS_NOTIFICACIONES_COMUNICACIONES");
			comunicaciones = mensajesService.recuperarUltComunicaciones(numMaxResultados);
			clearComunicaciones();
		}
	}
	
	private void clearComunicaciones(){
		if(comunicaciones != null){
			for (MensajeVO mensajeVO : comunicaciones) {
				String parsedTitle = HtmlStringUtils.getContentBetweenHtmlTags(mensajeVO.getAsunto());
				String parsedText = HtmlStringUtils.getContentBetweenHtmlTags(mensajeVO.getTexto());
				mensajeVO.setAsunto(parsedTitle);
				mensajeVO.setTexto(parsedText);
			}
		}
	}
	
	/**
	 * Carga de ultimas notificaciones
	 */
	private void cargarNotificaciones () throws Exception {
		if (notificaciones == null) {
			Integer numMaxResultados = Configuracion.getPropertyAsInteger("NUM_ULTIMAS_NOTIFICACIONES_COMUNICACIONES");
			notificaciones = mensajesService.recuperarUltNotificaciones(numMaxResultados);
			clearNotificaciones();
		}
	}

	
	private void clearNotificaciones(){
		if(notificaciones != null){
			for (NotificacionVO notificacionVO : notificaciones) {
				String parsedTitle = HtmlStringUtils.getContentBetweenHtmlTags(notificacionVO.getAsunto());
				String parsedText = HtmlStringUtils.getContentBetweenHtmlTags(notificacionVO.getTexto());
				notificacionVO.setAsunto(parsedTitle);
				notificacionVO.setTexto(parsedText);
			}			
		}
	}
	
	private void cargarMensajeDia () {
		try {
			if (mensajeDia == null) {
				mensajeDia = mensajesDiaService.getUltimoMensaje();
				
				if (mensajeDia==null) {
					mensajeDia = new MensajesDiaVO();
				}
			}
		} catch (Exception e) {
			// No genera error visible
			OepmLogger.error("principal.error.mensajeDia", e);
		}
	}
	
	/**
	 * Permite consultar los tramites de una familia
	 * 
	 * @param familia
	 * 
	 * @return String
	 */
	public String consultaCatalogoTramites(FamiliasVO familia) {
		try {
			familiaSeleccionada = familia;
			TramitesVO filter = new TramitesVO();
			filter.setFamiliaVO(familiaSeleccionada);
			tramites = tramitesService.search(filter);
		} catch (Exception e) {
			FacesUtil.addErrorMessage("principal.error.catalogo", e);
			return null;
		}
		
		return JSFPages.PRINCIPAL_CATALOGO;
	}
	
	public String consultaTramite(TramitesVO tramite) {
		try {
			tramiteSeleccionado = tramite;
		} catch (Exception e) {
			FacesUtil.addErrorMessage("principal.error.tramite", e);
			return null;
		}
		
		return JSFPages.PRINCIPAL_TRAMITE;
	}
	/**
	 * 
	 * @param tramite Tramite que se quiere comprobar
	 * @return true si hay que ver el detalle del tramite en una pagina externa y false si se tiene que ver el detalle del tramite en MAO
	 */
	public boolean redirigirPaginaTramiteExterna(TramitesVO tramite){
		if(StringUtils.isEmptyOrNull(tramite.getTxInformacion()) && StringUtils.isEmptyOrNull(tramite.getTxPresentacion()) && StringUtils.isEmptyOrNull(tramite.getTxProcedimientos()))
			return true;
		else
			return false;
			
	}
	
	private void cargarExpedientes () throws Exception {
		try {
			if (expedientes == null) {
				BusConsultarExpedientesResponse response = expedientesService.consultarUltimosExpedientesUsuario();
				List<BusExpediente> expedientesBus = response.getExpedientes();
				expedientes = toExpList(expedientesBus);
				procesarMensajesBusquedaUltimosExpedientes(response.getMensajes());
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("principal.error.ultExpedientes", e);
			throw e;
		}
	}
	
	private List<ExpedienteVO> toExpList(List<BusExpediente> expedientesBus) {
		List <ExpedienteVO> expedientes = new ArrayList<ExpedienteVO>();
		
		for (BusExpediente exp : expedientesBus) {
			expedientes.add(new ExpedienteVO(exp));
		}
		
		return expedientes;
	}
	
	/**
	 * Procesa los mensajes como mensajes faces para la busqueda de últimos expedientes
	 * 
	 * @param mensajes
	 */
	private void procesarMensajesBusquedaUltimosExpedientes(Mensajes[] mensajes) {
		if (mensajes != null) {
			for (Mensajes mensaje : mensajes) {
				switch (mensaje.getCodigo()) {
				case Mensaje.COD_ERROR_BUSQUEDA_RAPIDA_NUMERO_ALFA:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busquedaRapidaPorNumero.error.alfa");
					break;
				case Mensaje.COD_ERROR_BUSQUEDA_RAPIDA_NUMERO_SITAMOD:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busquedaRapidaPorNumero.error.sitamod");
					break;
				case Mensaje.COD_ERROR_BUSQUEDA_RAPIDA_NUMERO_SITAMAR:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busquedaRapidaPorNumero.error.sitamar");
					break;
				case Mensaje.COD_WS_ERROR_GENERICO:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busqueda.error");
					break;
				}
			}
		}
	}

	/**
	 * Permite descargar el fichero seleccionado
	 * 
	 * @param doc
	 * @return StreamedContent
	 */
	public StreamedContent descargarFichero (TramiteDocumentosVO doc) {
		 InputStream stream = new ByteArrayInputStream (doc.getBlDocumento());
		 StreamedContent file = new DefaultStreamedContent(stream, doc.getTxMime(), doc.getTxNombredoc());
		 return file;
	}
	
	/**
	 * @param tramitesService the tramitesService to set
	 */
	public void setExpedientesService(ExpedientesService expedientesService) {
		this.expedientesService = expedientesService;
	}


	/**
	 * @param tramitesService the tramitesService to set
	 */
	public void setTramitesService(TramitesService tramitesService) {
		this.tramitesService = tramitesService;
	}

	/**
	 * @return the familiasService
	 */
	public FamiliasService getFamiliasService() {
		return familiasService;
	}

	/**
	 * @param familiasService the familiasService to set
	 */
	public void setFamiliasService(FamiliasService familiasService) {
		this.familiasService = familiasService;
	}

	/**
	 * @return the familias
	TODO eliminar cuando se considere oportuno
	public List<FamiliasVO> getFamilias() {
		cargarTramites();
		return familias;
	}
	 */

	/**
	 * @param familias the familias to set
	 * TODO eliminar cuando se considere oportuno
	public void setFamilias(List<FamiliasVO> familias) {
		this.familias = familias;
	}*/

	/**
	 * @return the enlaces
	 */
	public List<EnlacesVO> getEnlaces() {
		return enlaces;
	}

	/**
	 * @param enlaces the enlaces to set
	 */
	public void setEnlaces(List<EnlacesVO> enlaces) {
		this.enlaces = enlaces;
	}

	/**
	 * @return the enlacesService
	 */
	public EnlacesService getEnlacesService() {
		return enlacesService;
	}

	/**
	 * @param enlacesService the enlacesService to set
	 */
	public void setEnlacesService(EnlacesService enlacesService) {
		this.enlacesService = enlacesService;
	}

	/**
	 * @return the ultimasNoticias
	 */
	public List<SyndEntry> getUltimasNoticias() throws Exception {
		cargarNoticias ();
		return ultimasNoticias;
	}

	/**
	 * @param ultimasNoticias the ultimasNoticias to set
	 */
	public void setUltimasNoticias(List<SyndEntry> ultimasNoticias) {
		this.ultimasNoticias = ultimasNoticias;
	}

	/**
	 * @return the noticias
	 */
	public List<SyndEntry> getNoticias() throws Exception {
		cargarNoticias ();
		return noticias;
	}

	/**
	 * @param noticias the noticias to set
	 */
	public void setNoticias(List<SyndEntry> noticias) {
		this.noticias = noticias;
	}

	/**
	 * @return the mensajesService
	 */
	public MensajesService getMensajesService() {
		return mensajesService;
	}

	/**
	 * @param mensajesService the mensajesService to set
	 */
	public void setMensajesService(MensajesService mensajesService) {
		this.mensajesService = mensajesService;
	}

	/**
	 * @return the comunicaciones
	 */
	public List<MensajeVO> getComunicaciones() throws Exception {
		cargarComunicaciones ();
		return comunicaciones;
	}

	/**
	 * @param comunicaciones the comunicaciones to set
	 */
	public void setComunicaciones(List<MensajeVO> comunicaciones) {
		this.comunicaciones = comunicaciones;
	}

	/**
	 * @return the notificaciones
	 */
	public List<NotificacionVO> getNotificaciones() throws Exception {
		cargarNotificaciones ();
		return notificaciones;
	}

	/**
	 * @param notificaciones the notificaciones to set
	 */
	public void setNotificaciones(List<NotificacionVO> notificaciones) {
		this.notificaciones = notificaciones;
	}

	/**
	 * @return the mensajeDia
	 */
	public MensajesDiaVO getMensajeDia() {
		cargarMensajeDia();
		return mensajeDia;
	}

	/**
	 * @param mensajeDia the mensajeDia to set
	 */
	public void setMensajeDia(MensajesDiaVO mensajeDia) {
		this.mensajeDia = mensajeDia;
	}

	/**
	 * @return the mensajesDiaService
	 */
	public MensajesDiaService getMensajesDiaService() {
		return mensajesDiaService;
	}

	/**
	 * @param mensajesDiaService the mensajesDiaService to set
	 */
	public void setMensajesDiaService(MensajesDiaService mensajesDiaService) {
		this.mensajesDiaService = mensajesDiaService;
	}

	/**
	 * @return the familiaSeleccionada
	 */
	public FamiliasVO getFamiliaSeleccionada() {
		return familiaSeleccionada;
	}

	/**
	 * @param familiaSeleccionada the familiaSeleccionada to set
	 */
	public void setFamiliaSeleccionada(FamiliasVO familiaSeleccionada) {
		this.familiaSeleccionada = familiaSeleccionada;
	}

	/**
	 * @return the tramites
	 */
	public List<TramitesVO> getTramites() {
		return tramites;
	}

	/**
	 * @param tramites the tramites to set
	 */
	public void setTramites(List<TramitesVO> tramites) {
		this.tramites = tramites;
	}

	/**
	 * @return the tramiteSeleccionado
	 */
	public TramitesVO getTramiteSeleccionado() {
		return tramiteSeleccionado;
	}

	/**
	 * @param tramiteSeleccionado the tramiteSeleccionado to set
	 */
	public void setTramiteSeleccionado(TramitesVO tramiteSeleccionado) {
		this.tramiteSeleccionado = tramiteSeleccionado;
	}
	
	/**
	 * @return the expedientes
	 */
	public List<ExpedienteVO> getExpedientes() throws Exception {
		cargarExpedientes ();
		return expedientes;
	}

	/**
	 * @param expedientes the expedientes to set
	 */
	public void setExpedientes(List<ExpedienteVO> expedientes) {
		this.expedientes = expedientes;
	}

	/**
	 * @return the busquedaExpController
	 */
	public BusquedaExpedientesController getBusquedaExpController() {
		return busquedaExpController;
	}

	/**
	 * @param busquedaExpController the busquedaExpController to set
	 */
	public void setBusquedaExpController(
			BusquedaExpedientesController busquedaExpController) {
		this.busquedaExpController = busquedaExpController;
	}
	
	public String getPaginaActual(){
		
		return FacesUtil.getCurrentViewId();
	}
	/**
	 * 
	 * @return true si la pagina tiene que tener ayua y false en acaso contrario
	 */
	@Deprecated
	public  boolean  isPaginaActualAyuda(){
		String paginaActual = FacesUtil.getCurrentViewId();
		if(paginaActual.indexOf("ayuda")!=-1 || paginaActual.indexOf("verificar")!=-1)
			return true;
		else
			return false;
	}
	
	/**
	 * 
	 * @return true si la pagina tiene que tener ayuda y false en acaso contrario
	 */
	public boolean isTienePaginaActualAyuda(){
		try {
			String paginaActual = FacesUtil.getCurrentViewId();
			int finNombrePagina = paginaActual.indexOf(".xhtml");
			if(finNombrePagina==-1)
				finNombrePagina= paginaActual.length();
			int inicioNombrePagina = paginaActual.lastIndexOf("/");
			String pagina = paginaActual.substring(inicioNombrePagina +1, finNombrePagina);
			AyudasVO filter = new AyudasVO();
			filter.setPagina(pagina);
			List <AyudasVO> ayudas = ayudasService.search(filter);
			if(ayudas!=null && !ayudas.isEmpty())
				return true;
			else
				return false;
		} catch (Exception exception) {
			String errorMsg = ExceptionUtil.getMessage(exception);
			FacesUtil.addErrorMessage(errorMsg);
		}
		return false;
	}
	
	public void redirigirURLTramite(){
		RequestContext.getCurrentInstance().execute("parametros.submit();");
		
	}

	public AyudasService getAyudasService() {
		return ayudasService;
	}

	public void setAyudasService(AyudasService ayudasService) {
		this.ayudasService = ayudasService;
	}
	
	/**
	 * 
	 * @return
	 * TODO eliminar cuando se considere oportuno
	public String getTramitesWidth() {
		String resultado;
		
		// Devolvemos el tamaño del div que contiene cada tramite en funcion de
		// los tramites obtenidos
		if (familias != null) {
			if (familias.size() > 4 ) {
				resultado = "33%";
			} else {
				resultado = "50%";
			}
		} else {
			resultado = "100%";
		}
		
		return resultado;
	}*/

	public ArrayList<List<EnlacesVO>> getListaEnlaces() {
		cargarEnlaces();
		
		return listaEnlaces;
	}

	public void setListaEnlaces(ArrayList<List<EnlacesVO>> listaEnlaces) {
		this.listaEnlaces = listaEnlaces;
	}
	
	public String getLogoMinisterio() {
		if(logoMinisterio == null){
			logoMinisterio = Configuracion.getPropertyAsString(MaoPropiedadesConf.LOGO_MINISTERIO);
		}
		return logoMinisterio;
	}

	public String getLogoOEPM() {
		if(logoOEPM == null){
			logoOEPM = Configuracion.getPropertyAsString(MaoPropiedadesConf.LOGO_OEPM);
		}
		return logoOEPM;
	}
	
	public String getLogoMinisterioReport() {
		if(logoMinisterioReport == null){
			logoMinisterioReport = Configuracion.getPropertyAsString(MaoPropiedadesConf.LOGO_MINISTERIO_REPORT);
		}
		return logoMinisterioReport;
	}

	public String getLogoOEPMReport() {
		if(logoOEPMReport == null){
			logoOEPMReport = Configuracion.getPropertyAsString(MaoPropiedadesConf.LOGO_OEPM_REPORT);
		}
		return logoOEPMReport;
	}
	
	
	//Cambio del logo 
	public String getLogoMINOEPM() {
		if(logoMINOEPM == null){
			logoMINOEPM = Configuracion.getPropertyAsString(MaoPropiedadesConf.LOGO_MIN_OEPM);
		}
		return logoMINOEPM;
	}
	
	public String getLogoSede() {
		if(logoSede == null){
			logoSede = Configuracion.getPropertyAsString(MaoPropiedadesConf.LOGO_SEDE);
		}
		return logoSede;
	}

	/**
	 * @return the enlaceSede
	 */
	public String getEnlaceSede() {
		if(enlaceSede == null){
			enlaceSede = Configuracion.getPropertyAsString(MaoPropiedadesConf.ENLACE_SEDE);
		}
		return enlaceSede;
	}

	/**
	 * @return the mensajeEnlace
	 */
	public String getMensajeEnlace() {
		if(mensajeEnlace == null){
			mensajeEnlace = Configuracion.getPropertyAsString(MaoPropiedadesConf.MENSAJE_ENLACE);
		}
		return mensajeEnlace;
	}
	
	public boolean getBonosIsDisabled() {
		return bonosIsDisabled;
	}

	public Boolean getSitamodIsDisabled() {
		return sitamodIsDisabled;
	}

	public Boolean getSitamarIsDisabled() {
		return sitamarIsDisabled;
	}

	public Boolean getAlfaIsDisabled() {
		return alfaIsDisabled;
	}

	public Boolean getComNotifIsDisabled() {
		return comNotifIsDisabled;
	}

	public void setComNotifIsDisabled(Boolean comNotifIsDisabled) {
		this.comNotifIsDisabled = comNotifIsDisabled;
	}

	public Boolean getPagosIsDisabled() {
		return pagosIsDisabled;
	}

	public void setPagosIsDisabled(Boolean pagosIsDisabled) {
		this.pagosIsDisabled = pagosIsDisabled;
	}
	
}
