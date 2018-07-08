package es.oepm.mao.view.controller;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.busmule.ws.client.ceo.BusEstado;
import es.oepm.busmule.ws.client.ceo.BusExpediente;
import es.oepm.busmule.ws.client.ceo.parameters.BusConsultarExpedientesResponse;
import es.oepm.busmule.ws.client.ceo.parameters.BusObtenerEstadosAlfaResponse;
import es.oepm.core.business.BaseVO;
import es.oepm.core.business.ceo.vo.CeAlrExpedientesMvVO;
import es.oepm.core.business.ceo.vo.FiltroBusquedaExpedienteVO;
import es.oepm.core.business.mao.vo.NumSolicitudVO;
import es.oepm.core.business.vo.EstadosVO;
import es.oepm.core.constants.Mensaje;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.constants.Roles;
import es.oepm.core.constants.Sistemas;
import es.oepm.core.session.SessionContext;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.util.DateUtils;
import es.oepm.core.util.ExpedienteUtils;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.controller.ListController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.core.view.faces.MessagesUtil;
import es.oepm.core.view.faces.SelectItemList;
import es.oepm.core.view.util.HTMLUtil;
import es.oepm.mao.business.service.ExpedientesService;
import es.oepm.mao.business.service.TrazaGestorService;
import es.oepm.mao.business.vo.ExpedienteVO;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.constants.MaoPropiedadesConf;
import es.oepm.mao.view.controller.util.GeneradorValoresCombo;
import es.oepm.mao.view.controller.util.JSFPages;
import es.oepm.wservices.core.BaseWSResponse;
import es.oepm.wservices.core.mensajes.Mensajes;

/**
 * Constrolador para busqueda de expedientes simple o avanzada
 * 
 * @author AYESA AT
 */
@ManagedBean(name = "busquedaExpController")
@SessionScoped
@Secured({ Roles.ROLE_MAO_AGENTE, Roles.ROLE_MAO_ASOCIADO,
		Roles.ROLE_MAO_REPRESENTANTE, Roles.ROLE_MAO_TITULAR })
public class BusquedaExpedientesController extends BaseController implements
		ListController {

	private static final long serialVersionUID = -3662036264168254793L;

	@ManagedProperty( "#{trazaGestorService}" )
	private TrazaGestorService trazaGestorService;
	
	@ManagedProperty( "#{expedientesService}" )
	private ExpedientesService expedientesService;

	// Mensaje seleccionado en la lista.
	/**
	 * DTO de busqueda avanzada
	 */
	private FiltroBusquedaExpedienteVO busquedaAvanDTO = new FiltroBusquedaExpedienteVO();
	/**
	 * numeroSolicitud de busqueda rapida
	 */
	private String numeroSolicitud;
	/**
	 * numeroPublicacion de busqueda rapida
	 */
	private String numeroPublicacion;
	/**
	 * fechaSolicitudDesde de busqueda rapida
	 */
	private Date fechaSolicitudDesde;
	/**
	 * fechaSolicitudHasta de busqueda rapida
	 */
	private Date fechaSolicitudHasta;
		
	private CeAlrExpedientesMvVO selectedExp;

	private List<ExpedienteVO> expedientesList = new ArrayList<ExpedienteVO>();

	// Listados de estados
	List<EstadosVO> estadosAlfa = null;
	
	private static final Map<Modalidad, String> pagDetalleMap = new HashMap<Modalidad, String>();
	static {
		pagDetalleMap.put(Modalidad.AL_LICENCIA, "detalleLic");
		pagDetalleMap.put(Modalidad.AL_TRANSMISION, "detalleCesion");
		
		final String pag = "detalleLicenciaCesionNombre";
		pagDetalleMap.put(Modalidad.MN_LICENCIA, pag);
		pagDetalleMap.put(Modalidad.MN_CAMBIO_NOMBRE, pag);
		pagDetalleMap.put(Modalidad.MN_TRANSFERENCIA, pag);
		pagDetalleMap.put(Modalidad.DI_LICENCIA, pag);
		pagDetalleMap.put(Modalidad.DI_CAMBIO_NOMBRE, pag);
		pagDetalleMap.put(Modalidad.DI_CESION, pag);
	}

	/**
	 * Constructor por defecto de la clase.
	 */
	public BusquedaExpedientesController() {
		super();
	}

	/**
	 * 
	 * @param isAdvancedSearch to indicated if it is an advanced search or a basic one
	 */
	@Secured({ Roles.ROLE_MAO_AGENTE, Roles.ROLE_MAO_ASOCIADO,
		Roles.ROLE_MAO_REPRESENTANTE, Roles.ROLE_MAO_TITULAR })
	public String actionQuickSearch(){
		//Update search results view
		boolean isQuick=true;
		busquedaAvanDTO = new FiltroBusquedaExpedienteVO();
		numeroSolicitud = numeroSolicitud.toUpperCase();
		// Se comprueba que al menos se haya informado un campo de búsqueda
		if (StringUtils.isEmptyOrNull(numeroSolicitud) && StringUtils.isEmptyOrNull(numeroPublicacion)
				&& fechaSolicitudDesde == null && fechaSolicitudHasta == null) {
			FacesUtil.addErrorMessage("busqueda.error.parametrosIncorrectos");
			return null;
		}
		
		if (!StringUtils.isEmptyOrNull(numeroSolicitud)) {
			// Comprobamos el formato del nº de solicitud
			NumSolicitudVO nSolVO = ExpedienteUtils.separaNumeroSolicitudYOModalidad(numeroSolicitud);
			if(nSolVO == null){
				busquedaAvanDTO.setNumeroSolicitud(numeroSolicitud);
			} else {
				if(!StringUtils.isEmptyOrNull(nSolVO.getNumeroSolicitud())){
					busquedaAvanDTO.setNumeroSolicitud(numeroSolicitud.trim());
				}
				else{
					busquedaAvanDTO.setModalidadSolicitud(nSolVO.getModalidad());
				}
			}
		}
		
		if(!StringUtils.isEmptyOrNull(numeroPublicacion)){
			busquedaAvanDTO.setNumeroPublicacion(numeroPublicacion.trim());
			/**
			 * MAO-274
			*/
			if(StringUtils.isEmptyOrNull(numeroSolicitud)){
				busquedaAvanDTO.setNumeroSolicitud("");
				busquedaAvanDTO.setModalidadSolicitud("");
			} 
		}
		busquedaAvanDTO.setFechaSolicitudDesde(fechaSolicitudDesde);
		busquedaAvanDTO.setFechaSolicitudHasta(fechaSolicitudHasta);
		
		// Comprobamos el formato de las fechas
		if(!checkDates(busquedaAvanDTO)){
			return null;
		}

		return doSearch(isQuick);
	}
	
	@Secured({ Roles.ROLE_MAO_AGENTE, Roles.ROLE_MAO_ASOCIADO,
		Roles.ROLE_MAO_REPRESENTANTE, Roles.ROLE_MAO_TITULAR })
	public String actionAdvancedSearch(){
		// Calidamos los parámetros de búsqueda
		//numeroSolicitud = numeroSolicitud.toUpperCase();
		boolean isquick=false;
		busquedaAvanDTO.setNumeroSolicitud(busquedaAvanDTO.getNumeroSolicitud().toUpperCase());
		if (validarFormularioAvanzado()) {
			if(!checkDates(busquedaAvanDTO)){
				return null;
			}
			
			if(!StringUtils.isEmptyOrNull(busquedaAvanDTO.getNumeroSolicitud())){
				busquedaAvanDTO.setNumeroSolicitud(busquedaAvanDTO.getNumeroSolicitud().trim());
			}
			
			if(!StringUtils.isEmptyOrNull(busquedaAvanDTO.getNumeroPublicacion())){
				busquedaAvanDTO.setNumeroPublicacion(busquedaAvanDTO.getNumeroPublicacion().trim());
				/**
				 * MAO-274
				*/
				if(StringUtils.isEmptyOrNull(numeroSolicitud)){
					busquedaAvanDTO.setNumeroSolicitud("");
					busquedaAvanDTO.setModalidadSolicitud("");
				} 
			}
			
			return doSearch(isquick);
		}
		return null;
	}
	
	@Secured({ Roles.ROLE_MAO_AGENTE, Roles.ROLE_MAO_ASOCIADO,
		Roles.ROLE_MAO_REPRESENTANTE, Roles.ROLE_MAO_TITULAR })
	@Override
	public String actionSearch() {
		FacesUtil.addErrorMessage("busqueda.error");
		return JSFPages.BUSQ_EXP_RESULTADOS;
	}
	
	/**
	 * Busqueda avanzada
	 * 
	 * @return
	 */
	private String doSearch(boolean isquick) {
		
		SessionUtil.addToSession("paginaBusquedaExpediente",JSFPages.BUSQ_EXP_RESULTADOS);
		
		// Separamos la modalidad y el numero de solicitud
		String modalidadSeparada = busquedaAvanDTO.getModalidadSolicitud();
		String numeroSolicitudSeparado = busquedaAvanDTO.getNumeroSolicitud();
				
		if(!StringUtils.isEmptyOrNull(numeroSolicitudSeparado)){
			NumSolicitudVO numeroSolicitudASeparar = 
					ExpedienteUtils.separaNumeroSolicitud(numeroSolicitudSeparado, modalidadSeparada); 
			if(numeroSolicitudASeparar != null)
			{
				if(!StringUtils.isEmptyOrNull(numeroSolicitudASeparar.getModalidad())){
					modalidadSeparada = numeroSolicitudASeparar.getModalidad();
				}
				if (!StringUtils.isEmptyOrNull(numeroSolicitudASeparar.getNumeroSolicitud())) {
					numeroSolicitudSeparado = numeroSolicitudASeparar.getNumeroSolicitud();
				}
				
			}
		}
		
		if (!StringUtils.isEmptyOrNull(modalidadSeparada) && !StringUtils.isEmptyOrNull(numeroSolicitudSeparado))
		{
			busquedaAvanDTO.setModalidadSolicitud(modalidadSeparada);
			busquedaAvanDTO.setNumeroSolicitud(numeroSolicitudSeparado);
		}
		
		// Si se ha seteado modalidad comprobamos que sea accesible al usuario
		if(!StringUtils.isEmptyOrNull(modalidadSeparada)){
			Modalidad m = Modalidad.find(modalidadSeparada);
			if (!SessionContext.getUserDetails().getAuthorities()
					.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "I")) && m.getSistema() == Sistemas.ALFA) {
				FacesUtil.addErrorMessage("busquedaRapidaPorNumero.error.alfa");
				return JSFPages.BUSQ_EXP_RESULTADOS;
			} else if (!SessionContext.getUserDetails().getAuthorities()
					.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "S")) && m.getSistema() == Sistemas.SITAMAR) {
				FacesUtil.addErrorMessage("busquedaRapidaPorNumero.error.sitamar");
				return JSFPages.BUSQ_EXP_RESULTADOS;
			} else if (!SessionContext.getUserDetails().getAuthorities()
					.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "D")) && m.getSistema() == Sistemas.SITAMOD) {
				FacesUtil.addErrorMessage("busquedaRapidaPorNumero.error.sitamod");
				return JSFPages.BUSQ_EXP_RESULTADOS;
			}
		}
		
		try {
			// Inicializamos la lista de expedientes
			expedientesList = new ArrayList<ExpedienteVO>();
			
			// HCS: Webservice lanza excepcion si le pasamos el numero de publicacion a null
			// Lo ponemos a vacio para evitarlo.
			
			if (busquedaAvanDTO.getNumeroPublicacion() == null) {
				busquedaAvanDTO.setNumeroPublicacion("");
			}
			
			BusConsultarExpedientesResponse resultado = expedientesService
					.consultarExpedientes(busquedaAvanDTO, isquick);
			
			// HCS: MAO-337 
			// Filtramos los resultado para que solo saque aquellos cuyas modalidades
			// se usan actualmente en MAO
			resultado = this.filtrarResultado(resultado);
			
			List<BusExpediente> expedientesBus = resultado
					.getExpedientes();
			
			// Mapeamos el resultado
			this.expedientesList = toExpList(expedientesBus);				

			boolean isBusquedaAmplia = isBusquedaAmplia(busquedaAvanDTO);
			
			/**
			 * MAO-315
			 */
			// Procesamos los mensajes
			
			// Expediente no pertenece al usuario logado y es privado
			if(resultado.getResultado()==3){
				if (!isBusquedaAmplia && expedientesList.size() <= 1)
				{
					FacesUtil.addOKMessage("busqueda.resultados.noPublico");
				}
				else
				{
					FacesUtil.addOKMessage("busqueda.sin.resultados");	
				}
				this.expedientesList.clear();	
			}
			// Expediente no pertenece al usuario logado y es publico
			else if(resultado.getResultado()==2){	
				if (!isBusquedaAmplia && expedientesList.size() == 1)
				{
					FacesContext.getCurrentInstance()
					.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", FacesUtil.getMessage("busqueda.resultados.ceo")+" <a href="+
							Configuracion.getPropertyAsString("CONSULTA_EXTERNA")+
							this.expedientesList.get(0).getNumeroSolicitudFormateado()+" target = '_blank'> "+FacesUtil.getMessage("busqueda.aqui.ceo")+"</a>" ));		
				}
				else
				{
					FacesUtil.addOKMessage("busqueda.sin.resultados");
				}
				this.expedientesList.clear();
			}else{

				procesarMensajesConsultarExpedientes(resultado.getMensajes());
			}
			
			// Post-validaciones
			// Si solo hay un resultado nos vamos a su detalle
			if (this.expedientesList != null
					&& this.expedientesList.size() == 1) {
				ExpedienteVO exp = this.expedientesList.get(0);
				// FacesUtil.setParameter("selectedExpId", exp.getId());
				return actionDetalle(exp);
			}
		} catch (Exception exception) {
			FacesUtil.addErrorMessage("busqueda.error", exception);
		}

		// Vamos a la pagina de resultados
		return JSFPages.BUSQ_EXP_RESULTADOS;
	}
	
	/**
	 * Función creada para MAO-382. Determina si una busqueda es amplia o no.
	 * @param filtro
	 * @return
	 */
	private boolean isBusquedaAmplia (FiltroBusquedaExpedienteVO filtro)
	{
		boolean result = false;	
		
		if (filtro != null) {
			
			String modalidad = filtro.getModalidadSolicitud();
			String codPublicacion = filtro.getPais();
			String numeroSolicitud = filtro.getNumeroSolicitud();
			String numeroPublicacion = filtro.getNumeroPublicacion();
			
			int solSize = -1;
			int pubSize = -1;
			
			// Numero de publicacion
			if (StringUtils.isEmptyOrNull(codPublicacion) && !StringUtils.isEmptyOrNull(numeroPublicacion)) {
				codPublicacion = numeroPublicacion.substring(0, 2);
				pubSize = numeroPublicacion.length();
			}
				
			// Numero de solicitud 
			if (!StringUtils.isEmptyOrNull(numeroSolicitud)) {
				solSize = numeroSolicitud.length();
			}
			
			// Si el numero de solicitud es menor que la longitud estandar
			// entonces es que lo mas seguro es que se haya realizado una busqueda
			// amplia
			if (solSize < ExpedienteUtils.getSolicitudLength(modalidad) && 
				pubSize < ExpedienteUtils.getPublicacionLength(codPublicacion)) 
			{
				result = true;
			}				
		}

		return result;
	}
	
	
	private boolean checkDates(FiltroBusquedaExpedienteVO busquedaAvanDTO) {
		if(busquedaAvanDTO.getFechaSolicitudDesde() != null && busquedaAvanDTO.getFechaSolicitudHasta() != null){
			if(busquedaAvanDTO.getFechaSolicitudDesde().after(busquedaAvanDTO.getFechaSolicitudHasta())){
				FacesUtil.addErrorMessage("busqExp.fechaSolicitudDesdeHasta");
				return false;
			}
		}
		
		if(busquedaAvanDTO.getFechaPublicacionDesde() != null && busquedaAvanDTO.getFechaPublicacionHasta() != null){
			if(busquedaAvanDTO.getFechaPublicacionDesde().after(busquedaAvanDTO.getFechaPublicacionHasta())){
				FacesUtil.addErrorMessage("busqExp.fechaPublicacionDesdeHasta");
				return false;
			}
		}
		
		if(busquedaAvanDTO.getFechaActoDesde()!= null && busquedaAvanDTO.getFechaActoHasta() != null){
			if(busquedaAvanDTO.getFechaActoDesde().after(busquedaAvanDTO.getFechaActoHasta())){
				FacesUtil.addErrorMessage("busqExp.fechaActoDesdeHasta");
				return false;
			}
		}
		return true;
	}

	
	private List<ExpedienteVO> toExpList(List<BusExpediente> expedientesBus) {
		List<ExpedienteVO> expedientes = new ArrayList<ExpedienteVO>();

		for (BusExpediente exp : expedientesBus) {
			expedientes.add(new ExpedienteVO(exp));
		}

		return expedientes;
	}

	/**
	 * Validacion segun tipo de busqueda avanzada
	 * 
	 * @return boolean
	 */
	private boolean validarFormularioAvanzado() {
		boolean correcto = true;

		if (StringUtils.isEmptyOrNull(this.busquedaAvanDTO.getCodigoAgente())
				&& StringUtils.isEmptyOrNull(this.busquedaAvanDTO.getEstado())
				&& StringUtils.isEmptyOrNull(this.busquedaAvanDTO
						.getModalidadSolicitud())
				&& StringUtils.isEmptyOrNull(this.busquedaAvanDTO.getNombreApAgente())
				&& StringUtils.isEmptyOrNull(this.busquedaAvanDTO.getNombreApInventor())
				&& StringUtils.isEmptyOrNull(this.busquedaAvanDTO.getNombreApTitular())
				&& StringUtils.isEmptyOrNull(this.busquedaAvanDTO.getNumeroSolicitud())
				&& StringUtils
						.isEmptyOrNull(this.busquedaAvanDTO.getNumeroPublicacion())
				&& StringUtils.isEmptyOrNull(this.busquedaAvanDTO.getNumPrioridad())
				&& StringUtils.isEmptyOrNull(this.busquedaAvanDTO.getTituloInvencion())
				&& StringUtils.isEmptyOrNull(this.busquedaAvanDTO.getDocTitular())
				&& this.busquedaAvanDTO.getFechaPrioridad() == null
				&& this.busquedaAvanDTO.getFechaPublicacionDesde() == null
				&& this.busquedaAvanDTO.getFechaPublicacionHasta() == null
				&& this.busquedaAvanDTO.getFechaSolicitudDesde() == null
				&& this.busquedaAvanDTO.getFechaSolicitudHasta() == null
				&& this.busquedaAvanDTO.getFechaActoDesde() == null
				&& this.busquedaAvanDTO.getFechaActoHasta() == null) {
			FacesUtil.addErrorMessage("busqueda.error.parametrosIncorrectos");
			correcto = false;
		} else if (!StringUtils.isEmptyOrNull(this.busquedaAvanDTO.getModalidadSolicitud())) {
			/** Comprobar pemisos de modalidades de usuario */
			Modalidad modalidad = Modalidad.find(this.busquedaAvanDTO.getModalidadSolicitud());
			if (modalidad.isAlfa()) {
				// Permiso para moadlidad invenciones
				if (!SessionContext.getUserDetails().getAuthorities()
						.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "I"))) {
					FacesUtil.addErrorMessage("AbstractUserDetailsAuthenticationProvider.badCredentials");
					correcto = false;
				}
			} else if (modalidad.isSitamod()) {
				//Permiso para modalidad diseño
				if (!SessionContext.getUserDetails().getAuthorities()
						.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "D"))) {
					FacesUtil.addErrorMessage("AbstractUserDetailsAuthenticationProvider.badCredentials");
					correcto = false;
				}
			} else if (modalidad.isSitamar()) {
				//Permiso para modalidad marcas
				if (!SessionContext.getUserDetails().getAuthorities()
						.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "S"))) {
					FacesUtil.addErrorMessage("AbstractUserDetailsAuthenticationProvider.badCredentials");
					correcto = false;
				}
			}
		}

		return correcto;
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
				case Mensaje.COD_ERROR_BUSQUEDA_RAPIDA_NUMERO_ALFA:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busquedaRapidaPorNumero.error.alfa");
					break;
				case Mensaje.COD_ALFA_MAX_SEARCH_RESULT:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busquedaRapidaPorNumero.warn.alfaMax");
					break;
				case Mensaje.COD_ERROR_BUSQUEDA_RAPIDA_NUMERO_SITAMOD:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busquedaRapidaPorNumero.error.sitamod");
					break;
				case Mensaje.COD_SITAMOD_MAX_SEARCH_RESULT:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busquedaRapidaPorNumero.warn.sitamodMax");
					break;
				case Mensaje.COD_ERROR_BUSQUEDA_RAPIDA_NUMERO_SITAMAR:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busquedaRapidaPorNumero.error.sitamar");
					break;
				case Mensaje.COD_SITAMAR_MAX_SEARCH_RESULT:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busquedaRapidaPorNumero.warn.sitamarMax");
					break;
				case Mensaje.COD_WS_ERROR_GENERICO:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busqueda.error");
					break;
				case Mensaje.COD_BUSQUEDA_SIN_RESULTADOS:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busqueda.sin.resultados");
					break;
				}
			}
		}
	}

	/**
	 * Limpieza de formulario e ir
	 * 
	 * @return string
	 */
	public String actionLimpiar(String to) {
		busquedaAvanDTO = new FiltroBusquedaExpedienteVO();
		saveFilterInSession(busquedaAvanDTO);
		this.expedientesList = new ArrayList<ExpedienteVO>();
		// This rebuild the page and shows everything clean
		if(StringUtils.isEmptyOrNull(to)){
			return JSFPages.BUSQ_EXP_RESULTADOS;
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

	@Secured(Roles.ROLE_CUENTA_VERIFICADA)
	public String actionCrearSimilar(ExpedienteVO exp) {
		return null; // TODO pendiente de consulta a traves de WS de Tramites
	}

	public String actionDetallePpal(ExpedienteVO exp) {
		SessionUtil.addToSession("paginaBusquedaExpediente",
				JSFPages.INICIO_PRIVADO);
		return actionDetalle(exp);
	}

	public String actionDetalle(ExpedienteVO exp) {
		SessionUtil.addToSession("expedienteSeleccionado", exp);
		SessionUtil.addToSession("detalleExpediente", null);
		BusquedaDocumentosExpedienteController docsExpController = (BusquedaDocumentosExpedienteController) SessionUtil
				.getFromSession("busquedaDocsExpController");
		if (docsExpController != null)
			docsExpController.setDocumentosList(null);
			
		//return "/jsp/privado/contenido/expedientes/" + getSistema(exp) + "/detalleExp.xhtml";	
		final Modalidad mod = Modalidad.find(exp.getModalidad());
		final String sis = mod.getSistema().toString().toLowerCase();
		
		return "/comun/expedientes/" + sis + "/" + getDetalle(mod) + ".xhtml";
	}
	
	public String actionDetalle() {
		ExpedienteVO exp = (ExpedienteVO)SessionUtil.getFromSession("expedienteSeleccionado");
		final Modalidad mod = Modalidad.find(exp.getModalidad());
		final String sis = mod.getSistema().toString().toLowerCase();	
		return "/comun/expedientes/" + sis + "/" + getDetalle(mod) + ".xhtml";
	}	
	
	/**
	 * Segun la modalidad obtiene la pagina de detalle a mostrar (expediente,
	 * licencia o cesion)
	 * 
	 * @param exp
	 * @return String
	 */
	private String getDetalle(Modalidad mod) {
		final String pag = pagDetalleMap.get(mod);
		return (pag==null)? "detalleExp" : pag;
	}

	/**
	 * Permite devolver el numero de caracteres minimos para la busqueda por
	 * numero
	 * 
	 * @return Integer
	 */
	public int getMinimoCampoNumero() {
		return Configuracion
				.getPropertyAsInteger(MaoPropiedadesConf.MIN_CARACTERES_CAMPO_NUMERO);
	}

	/**
	 * Permite devolver el numero de caracteres minimos para la busqueda por una
	 * cadena
	 * 
	 * @return Integer
	 */
	public int getMinimoCampoCadena() {
		return Configuracion
				.getPropertyAsInteger(MaoPropiedadesConf.MIN_CARACTERES_CAMPO_CADENA);
	}

	/**
	 * Permite devolver el numero de caracteres minimos para la busqueda por
	 * numero
	 * 
	 * @return Integer
	 */
	public void validarCampoNumero(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		String valor = (String) value;

		if (!StringUtils.isEmptyOrNull(valor)) {
			Integer minimo = getMinimoCampoNumero();

			if (valor.length() < minimo) {
				throw new ValidatorException(new FacesMessage());
			}
		}
	}

	/**
	 * Permite devolver el numero de caracteres minimos para la busqueda por
	 * cadena
	 * 
	 * @return Integer
	 */
	public void validarCampoCadena(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		String valor = (String) value;

		if (!StringUtils.isEmptyOrNull(valor)) {
			Integer minimo = getMinimoCampoCadena();

			if (valor.length() < minimo) {
				throw new ValidatorException(new FacesMessage());
			}
		}
	}

	public String getFechaHoraConsulta() {
		return DateUtils.formatFecha(new Date()) + " "
				+ DateUtils.formatHoraLarga(new Date());
	}

	// *******************************************************************

	@SuppressWarnings("unchecked")
	private List<Modalidad> getModalidadesList() {
		
		List<Modalidad> result = GeneradorValoresCombo
				.getListaModalidadesCompleta((Collection<SimpleGrantedAuthority>) SessionContext
						.getUserDetails().getAuthorities());	
		
		return result;
	
	}

	/**
	 * Muestra/carga la lista de modalidades
	 * 
	 * @return List<SelectItem>
	 */
	public List<SelectItem> getModalidades() {
		return HTMLUtil.getSelectItemList(SelectItemList.createDefault(),
				getModalidadesList(), Modalidad.class, "textoModalidad",
				"modalidad");
	}

	/**
	 * Devuelve la lista de estados
	 * 
	 * @return List<SelectItem>
	 * @throws MalformedURLException
	 */
	@Secured({ Roles.ROLE_MAO_AGENTE, Roles.ROLE_MAO_ASOCIADO,
			Roles.ROLE_MAO_REPRESENTANTE, Roles.ROLE_MAO_TITULAR })
	public List<SelectItem> getEstadosAlfa() {

		try {
				estadosAlfa = new ArrayList<EstadosVO>();

				// Buscar la primera vez los estados de alfa
				BusObtenerEstadosAlfaResponse response = expedientesService.getEstadosAlfa();
				
				// Comprobamos la respuesta
				if (response != null
						&& response.getResultado() == BaseWSResponse.RESULTADO_OK) {
					// Mapeamos los estados al VO
					for (BusEstado busEst : response.getEstados()) {
						EstadosVO estadoVO = new EstadosVO(
								busEst.getCodigo(), MessagesUtil.getMessage(busEst.getDescripcion()));
						estadosAlfa.add(estadoVO);
					}
				}
		} catch (Exception e) {
			return null;
		}
		
		// Devolvemos la lista elementos del select
		return HTMLUtil.getSelectItemList(SelectItemList.createDefault(),
				estadosAlfa, EstadosVO.class, "descripcion", "codigo");
	}

	public boolean isUsuarioTitular(){		
		return TipoUsuario.TITULAR.toString().equals(
				SessionContext.getTipoUsuario());
	}
	
	/**
	 * Calcula si se debe mostrar o no la columna de numPublicacion
	 * @return
	 */
	public boolean isVerNumPublicacion () {
		boolean ver = true;
		
		if (this.busquedaAvanDTO!=null 
				&& (!StringUtils.isEmptyOrNull(this.busquedaAvanDTO.getModalidadSolicitud())
				|| !StringUtils.isEmptyOrNull(this.busquedaAvanDTO.getModalidad()))) {
			String modalidad = this.busquedaAvanDTO.getModalidadSolicitud();
			if (StringUtils.isEmptyOrNull(modalidad)) {
				modalidad = this.busquedaAvanDTO.getModalidad();
			}
			
			Modalidad mod = Modalidad.find(modalidad.trim());
			
			if (!mod.isAlfa() || mod.isCesion() || mod.isLicenciaAlfa()
					|| mod.is(Modalidad.AL_PATENTE_INVENCION.getModalidad())
					|| mod.is(Modalidad.AL_SEMICONDUCTOR.getModalidad())) {
				ver = false;
			}
		}
		
		return ver;
	}
	
	public int getExpedientesListSize() {
		return expedientesList == null ? 0 : expedientesList.size();
	}

	public CeAlrExpedientesMvVO getSelectedMensaje() {
		return selectedExp;
	}

	public void setSelectedMensaje(CeAlrExpedientesMvVO selectedExp) {
		this.selectedExp = selectedExp;
	}

	public List<ExpedienteVO> getExpedientesList() {
		return expedientesList;
	}
	
	public String getNumeroSolicitud() {
		return numeroSolicitud;
	}

	public void setNumeroSolicitud(String numeroSolicitud) {
		this.numeroSolicitud = numeroSolicitud;
	}

	public String getNumeroPublicacion() {
		return numeroPublicacion;
	}

	public void setNumeroPublicacion(String numeroPublicacion) {
		this.numeroPublicacion = numeroPublicacion;
	}

	public Date getFechaSolicitudDesde() {
		return fechaSolicitudDesde;
	}

	public void setFechaSolicitudDesde(Date fechaSolicitudDesde) {
		this.fechaSolicitudDesde = fechaSolicitudDesde;
	}

	public Date getFechaSolicitudHasta() {
		return fechaSolicitudHasta;
	}

	public void setFechaSolicitudHasta(Date fechaSolicitudHasta) {
		this.fechaSolicitudHasta = fechaSolicitudHasta;
	}

	public FiltroBusquedaExpedienteVO getBusquedaAvanDTO() {
		return busquedaAvanDTO;
	}

	public void setBusquedaAvanDTO(FiltroBusquedaExpedienteVO busquedaAvanDTO) {
		this.busquedaAvanDTO = busquedaAvanDTO;
	}

	@Override
	public BaseVO getFilter() {
		return busquedaAvanDTO;
	}

	@Override
	public void setFilter(final BaseVO filter) {
		this.busquedaAvanDTO = (FiltroBusquedaExpedienteVO) filter;
	}
	
	/**
	 * Añadido para la parte comun siempre es FALSE
	 * @return
	 */
	public boolean getEmbedded() {
		return Boolean.FALSE;
	}
	/**
	 * MAO-328
	 */
	public boolean getAccesoDocumentosMarcas() {
		return Configuracion.getPropertyAsBoolean("FUNCIONALIDAD_ACTIVA.ACCESO_DOC_MARCAS");
	}
	
	public void setTrazaGestorService(TrazaGestorService trazaGestorService) {
		this.trazaGestorService = trazaGestorService;
	}
	
	public void setExpedientesService(ExpedientesService expedientesService) {
		this.expedientesService = expedientesService;
	}

	/**
	 * MAO-337
	 * Función auxiliar para filtrar los resultados del webservice de CEO
	 * No queremos que MAO saque resultados de modalidades aun no oficialmente soportadas
	 * @param resultados
	 * @author hcanosor
	 * @return
	 */
	private BusConsultarExpedientesResponse filtrarResultado(BusConsultarExpedientesResponse resultados)
	{
		List<BusExpediente> expedientes = resultados.getExpedientes();
		
		// getModalidadesList devuelve una lista de modalidades filtrada (unicamente la que queremos muestre MAO)
		List<Modalidad> modalidades = this.getModalidadesList();		
		
		for (int i = 0; i<expedientes.size(); i++) {
		
			boolean ocultarExpediente = true;
			
			for (Modalidad modalidad : modalidades) {			
				if (modalidad.getModalidad().equalsIgnoreCase(expedientes.get(i).getModalidad())) {
					ocultarExpediente = false;
					break;
				}
			}
			
			
			// Sacamos del listado los expedientes que han de ocultarse
			if (ocultarExpediente==true) {
				expedientes.remove(i);
			}
		}
	
		// Al final, comprobamos si despues de filtrar quedan resultado.
		// Si no hay, ponemos el resultado a 0 para simular una consulta sin resultados.
		if (expedientes.size() <= 0 && resultados.getResultado() != 3)
		{
			resultados.setResultado(0);
		}
		
		return resultados;
	}
	
}
