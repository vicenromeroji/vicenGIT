package es.oepm.mao.view.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

import org.springframework.web.jsf.FacesContextUtils;

import es.oepm.core.business.ceo.vo.ActosTramitacionDTO;
import es.oepm.core.business.ceo.vo.CeAlrAnotacionesMvVO;
import es.oepm.core.business.ceo.vo.CeAlrCesionExpedienteMvVO;
import es.oepm.core.business.ceo.vo.CeAlrCesionMvVO;
import es.oepm.core.business.ceo.vo.CeAlrDepositoBioMvVO;
import es.oepm.core.business.ceo.vo.CeAlrExhibicionMvVO;
import es.oepm.core.business.ceo.vo.CeAlrExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeAlrInventorMvVO;
import es.oepm.core.business.ceo.vo.CeAlrLicenciasExpMvVO;
import es.oepm.core.business.ceo.vo.CeAlrLicenciasMvVO;
import es.oepm.core.business.ceo.vo.CeAlrPrioridadMvVO;
import es.oepm.core.business.ceo.vo.CeAlrPublicacionesBepDTO;
import es.oepm.core.business.ceo.vo.CeAlrPublicacionesMvVO;
import es.oepm.core.business.ceo.vo.CeAlrRelExpMvVO;
import es.oepm.core.business.ceo.vo.CeAlrRepresentacionMvVO;
import es.oepm.core.business.ceo.vo.CeAlrSenalControlMvVO;
import es.oepm.core.business.ceo.vo.CeAlrTitularMvVO;
import es.oepm.core.business.vo.DetalleExpedienteResponseVO;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.constants.Roles;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.util.ExpedienteUtils;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.controller.TituloDetalleExp;
import es.oepm.core.view.controller.TituloDetalleExpGral;
import es.oepm.core.view.controller.TituloDetalleExpW;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.core.view.faces.MessagesUtil;
import es.oepm.mao.application.TrazaOpsMAO;
import es.oepm.mao.business.service.ExpedientesService;
import es.oepm.mao.business.vo.ExpedienteVO;
import es.oepm.mao.view.controller.util.JSFPages;
import es.oepm.maoceo.comun.view.controller.IDetalleExpedienteAlfaController;

@ManagedBean(name = "detalleAlfaController")
@ViewScoped
public class DetalleExpedienteAlfaController extends DetalleExpedienteController implements IDetalleExpedienteAlfaController {

	private static final long serialVersionUID = 2877994163890499477L;
	
	@ManagedProperty( "#{expedientesService}" )
	private ExpedientesService expedientesService;
	
	public void setExpedientesService(ExpedientesService expedientesService) {
		this.expedientesService = expedientesService;
	}
	
	// ---------------
	private CeAlrExpedientesMvVO expediente = null;
	private CeAlrLicenciasMvVO licencia = null;
	private CeAlrCesionMvVO cesion = null;

	// ---------------
	private String idExpediente = null;

	/**
	 * Constructor por defecto de la clase.
	 */
	public DetalleExpedienteAlfaController() {
		ExpedienteVO exp = (ExpedienteVO)SessionUtil.getFromSession("expedienteSeleccionado");
		idExpediente = exp.getId();
		modalidad = exp.getModalidad().trim();
		expediente = null;
	}
	
	@PostConstruct
	public void init() {
		Object exp = SessionUtil.getFromSession("detalleExpediente");
		
		if(exp != null) {
			if(exp instanceof CeAlrExpedientesMvVO) {
				this.expediente = (CeAlrExpedientesMvVO)exp;
			} else if(exp instanceof CeAlrLicenciasMvVO) {
				this.licencia = (CeAlrLicenciasMvVO)exp;
			 }else if(exp instanceof CeAlrCesionMvVO) {
				this.cesion = (CeAlrCesionMvVO) exp;
			}
		} else {
			try {
				// Recuperamos el expediente
				DetalleExpedienteResponseVO<CeAlrExpedientesMvVO> response = expedientesService
						.getDetalleExpedienteInvencion(idExpediente, modalidad);
				expediente = response.getDetalleExpediente();
				procesarMensajesDetalleExpediente(response.getMensajes());
				
				if (expediente != null) {
					// Lo añadimos a sesion
					SessionUtil.addToSession("detalleExpediente", expediente);
					SessionUtil.addToSession("tituloDetalle", getTituloDetalle());
					
					// Escribimos la traza
					TrazaOpsMAO.trazaOperacionConsultaExp(expediente.getModalidad(),
														  expediente.getNumeroSolicitud());		
					
					// MAO-458
					if (SessionContext.hasUserRole(Roles.ROLE_MAO_GESTOR)) {
						if ("3".equalsIgnoreCase(expediente.getSecreta())) 
						{
							// Tenemos que hacer una pequeña chapa para lograr sacar el mensaje de error
							SessionUtil.addToSession("expedienteSecreto", true);
							String context= FacesContext.getCurrentInstance().getExternalContext().getContextName();
							FacesContext.getCurrentInstance().getExternalContext().redirect("/"+context+ JSFPages.INICIO_PRIVADO + ".xhtml");
						}
					}
					
						
				}
				
			} catch (final Exception e) {
				FacesUtil.addErrorMessage("busqueda.error");
			}
		}
	}

	public CeAlrExpedientesMvVO getExpediente() {
		return this.expediente;
	}

	public CeAlrLicenciasMvVO getLicencia() {
		if (licencia == null) {
			try {
				// Recuperamos el expediente
				DetalleExpedienteResponseVO<CeAlrLicenciasMvVO> response = expedientesService.getDetalleExpedienteLicencia(idExpediente, modalidad);
				licencia = response.getDetalleExpediente();
				procesarMensajesDetalleExpediente(response.getMensajes());
				
				// Lo añadimos a sesion
				SessionUtil.addToSession("detalleExpediente", licencia);
				SessionUtil
						.addToSession("tituloDetalle", getTituloDetalleLic());

				// Escribimos la traza
				TrazaOpsMAO.trazaOperacionConsultaExp(modalidad,
						licencia.getNumeroLicencia());
				
			} catch (final Exception e) {
				FacesUtil.addErrorMessage(e.getMessage());
			}
		}
		

		return licencia;
	}

	public CeAlrCesionMvVO getCesion() {
		if (cesion == null) {
			try {
				// Recuperamos el expediente
				DetalleExpedienteResponseVO<CeAlrCesionMvVO> response = expedientesService
						.getDetalleExpedienteCesion(idExpediente, modalidad);
				cesion = response.getDetalleExpediente();
				procesarMensajesDetalleExpediente(response.getMensajes());

				// Lo añadimos a sesion
				SessionUtil.addToSession("detalleExpediente", cesion);
				SessionUtil.addToSession("tituloDetalle",
						getTituloDetalleCesion());
				
				// Escribimos la traza
				TrazaOpsMAO.trazaOperacionConsultaExp(modalidad,
						cesion.getNumeroCesion());
			} catch (final Exception e) {
				FacesUtil.addErrorMessage(e.getMessage());
			}
		}

		return cesion;
	}

	/*private BigDecimal getIdCesion() {
		return this.getCesion().getId();
	}*/

	public boolean isExpedientePublicable() {
		return this.getExpediente().isPublicable();
	}

	public CeAlrRepresentacionMvVO getAgente() {
		return this.expediente.getAgente();
	}

	public CeAlrRepresentacionMvVO getRepresentante() {
		return this.expediente.getRepresentante();
	}

	public CeAlrRepresentacionMvVO getAgenteLic() {
		return this.licencia.getAgente();
	}

	public CeAlrRepresentacionMvVO getRepresentanteLic() {
		return this.licencia.getRepresentante();
	}

	public CeAlrRepresentacionMvVO getAgenteCesion() {
		return this.cesion.getAgente();
	}

	public CeAlrRepresentacionMvVO getRepresentanteCesion() {
		return this.cesion.getRepresentante();
	}
	
	public boolean isRenderInvenes () {
		boolean render = false;
		
		if (Modalidad.AL_MODELO_UTILIDAD.is(this.getExpediente().getModalidad())
				|| Modalidad.AL_PATENTE_NACIONAL.is(this.getExpediente().getModalidad())) {
			render = true;
		} else if (Modalidad.AL_PATENTE_EUROPEA.is(this.getExpediente().getModalidad())
				|| Modalidad.AL_PATENTE_PCT.is(this.getExpediente().getModalidad())) {
				if (this.getExpediente().getPatenteBase()!=null
						&& !StringUtils.isEmptyOrNull(this.getExpediente().getPatenteBase().getNumeroSolicitud())
						&& this.getExpediente().getPatenteBase().getNumerosPublicacion()!=null
						&& this.getExpediente().getPatenteBase().getNumerosPublicacion().contains("ES")) {
					render = true;
				} else if (this.getExpediente().getNumeroSolicitud()!=null
						&& this.getExpediente().getNumeroSolicitud().contains("ES")) {
					render = true;
				} else if (this.getExpediente().getBis()!=null
						&& this.getExpediente().getBis().contains("ES")) {
					render = true;
				} else if (this.getExpediente().getNumeroPublicacion()!=null
						&& this.getExpediente().getNumeroPublicacion().contains("ES")) {
					render = true;
				} else if (this.getExpediente().getNumeroPublicacionCabecera()!=null
						&& this.getExpediente().getNumeroPublicacionCabecera().contains("ES")) {
					render = true;
				} else if (this.getExpediente().getPublicacionOEPM()!=null
						&& this.getExpediente().getPublicacionOEPM().getNumeroPublicacion()!=null
						&& this.getExpediente().getPublicacionOEPM().getNumeroPublicacion().contains("ES")) {
					render = true;
				} else if (this.getExpediente().getPublicacionOEPM()!=null
						&& this.getExpediente().getPublicacionOEPM().getNumeroPublicacionCabecera()!=null
						&& this.getExpediente().getPublicacionOEPM().getNumeroPublicacionCabecera().contains("ES")) {
					render = true;
				} 
		}
		
		return render;
	}

	public String getReferenciaInvenes() {
		if(licencia != null){
			return licencia.getNumeroLicencia() ;
		}else if(cesion != null){
			return cesion.getNumeroCesion();
		} else {
			return ExpedienteUtils.getReferenciaInvenes(this.getExpediente());
		}
	}
	
	@Override
	public String getReferenciaPestanas() {
		if(licencia != null){
			return licencia.getNumeroLicencia() ;
		}else if(cesion != null){
			return cesion.getNumeroCesion();
		} else {
			return expediente.getReferenciaInvenes();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.oepm.maoceo.comun.view.controller.IDetalleExpedienteAlfaController#
	 * getUriConsultaExternaCesionExp
	 * (es.oepm.core.business.ceo.vo.CeAlrCesionExpedienteMvVO)
	 */
	@Override
	public String getUriConsultaExternaCesionExp(CeAlrCesionExpedienteMvVO cesionExp) {
		// El numero de solicitud incluye el bis
		return ExpedienteUtils.componerUriConsultaExternaGeneral(
				cesionExp.getModalidad(), cesionExp.getNumeroSolicitud(), "");
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.oepm.maoceo.comun.view.controller.IDetalleExpedienteAlfaController
	 * #getUriConsultaExternaCesion
	 * (es.oepm.core.business.ceo.vo.CeAlrCesionMvVO)
	 */
	@Override
	public String getUriConsultaExternaCesion(CeAlrCesionMvVO cesion) {
		String uri = "";
		
		if (cesion != null) {
			uri = ExpedienteUtils.componerUriConsultaExternaGeneral(
					Modalidad.AL_TRANSMISION.getModalidad(), cesion.getNumeroCesion(), "");
		}
		
		return uri;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.oepm.maoceo.comun.view.controller.IDetalleExpedienteAlfaController
	 * #getUriConsultaExternaLicenciaExp
	 * (es.oepm.core.business.ceo.vo.CeAlrLicenciasExpMvVO)
	 */
	@Override
	public String getUriConsultaExternaLicenciaExp(CeAlrLicenciasExpMvVO licenciaExp) {
		// El numero de solicitud incluye el bis
		return ExpedienteUtils.componerUriConsultaExternaGeneral(
				licenciaExp.getModalidad(), licenciaExp.getNumeroSolicitud(), "");
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.oepm.maoceo.comun.view.controller.IDetalleExpedienteAlfaController
	 * #getUriConsultaExternaLicencia
	 * (es.oepm.core.business.ceo.vo.CeAlrLicenciasMvVO)
	 */
	@Override
	public String getUriConsultaExternaLicencia(CeAlrLicenciasMvVO licencia) {
		String uri = "";

		if (licencia != null) {
			uri = ExpedienteUtils.componerUriConsultaExternaGeneral(
					Modalidad.AL_LICENCIA.getModalidad(), licencia.getNumeroLicencia(), "");
		}

		return uri;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.oepm.maoceo.comun.view.controller.IDetalleExpedienteAlfaController
	 * #getUriConsultaExternaPatenteBase
	 * (es.oepm.core.business.ceo.vo.CeAlrRelExpMvVO)
	 */
	@Override
	public String getUriConsultaExternaPatenteBase(CeAlrRelExpMvVO expBase) {
		String uri = "";
		
		// Si se trata de un expediente de tipo W lo tratamos
		if (expBase.getModalidadPadre().equals(Modalidad.AL_PATENTE_PCT.getModalidad())) {
			uri = ExpedienteUtils.componerUriConsultaExternaPCT(
					ExpedienteUtils.componerNumeroSolicitudOMPIPatenteBase(expBase));
		} else {
			// El numero de solicitud incluye el bis
			uri = ExpedienteUtils.componerUriConsultaExternaGeneral(
					expBase.getModalidadPadre(), expBase.getNumeroSolicitudPadre(), "");
		}
		
		return uri;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.oepm.maoceo.comun.view.controller.IDetalleExpedienteAlfaController
	 * #getUriConsultaExternaPrioridad
	 * (es.oepm.core.business.ceo.vo.CeAlrPrioridadMvVO)
	 */
	@Override
	public String getUriConsultaExternaPrioridad(CeAlrPrioridadMvVO prioridad) {
		String uri = "";
		// Solo se trata en caso de que el país sea ESPAÑA
		if ("ESPAÑA".equals(prioridad.getNomPais())) {
			// Si tiene modalidad y número lo tratamos por si es pct
			if (!StringUtils.isEmptyOrNull(prioridad.getModalidad())
					&& !StringUtils.isEmptyOrNull(prioridad.getNumero())) {
				String numeroSolicitud = ExpedienteUtils.formatNumeroSolicitud(
						prioridad.getModalidad().trim(), prioridad.getNumero().trim());
				// Componemos la URL
				uri = ExpedienteUtils
						.componerUriConsultaExternaPCT(numeroSolicitud);
			}
		}
		
		return uri;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.maoceo.comun.view.controller.IDetalleExpedienteAlfaController#getTargetConsultaExterna()
	 */
	@Override
	public String getTargetConsultaExterna() {
		return "_blank";
	}
	
	/**
	 * Devuelve la referencia a espacenet
	 * 
	 * @return 
	 */
	public String getReferenciaEspacenet () {
		return ExpedienteUtils.getReferenciaEspacenet(this.getExpediente());
	}
	

	/**
	 * Devuelve la referencia a Patentscope
	 * 
	 * @return  numPub
	 */
	public String getReferenciaPatentscope () {
		return ExpedienteUtils.getReferenciaPatentscope(this.getExpediente());
	}
	
	/**
	 * Referencia para Register
	 * @return referenciaRegister
	 */ 
	public String getReferenciaRegister () {
		return ExpedienteUtils.getReferenciaRegister(this.getExpediente());
	}
	
	public String getReferenciaLatipat () {
		return getNumeroPublicacionExpediente();
	}

	public String getTituloInvencion(){
		return this.getExpediente().getTituloInvencion();
	}
	
	public String getTituloDetalle() {
		return getTituloDetalleExp();
	}

	private String getTituloDetalleExp() {
		final String mod = this.getExpediente().getModalidad();
		final String modPct = Modalidad.AL_PATENTE_PCT.getModalidad();
		
		return (modPct.equals(mod))? getTituloDetalleExpW() : getTituloDetalleExpOtros();
	}
	
	private String getTituloDetalleExpW() {
		/*final CeAlrExpedientesMvVO exp = this.getExpediente();
		final String modalidad = this.getTextoModalidad(exp);
		String numSol = exp.getNumeroSolicitud().split(" ")[0];
		final String ano = "20" + numSol.substring(0, 2);
		numSol = numSol.substring(2);
		final String bis = exp.getBis();
		String titulo = exp.getTituloInvencion();
		titulo = (titulo!=null)? " - " + titulo : "";
		
		return modalidad + "/" + bis + ano +"/"+ numSol + titulo;*/
		final CeAlrExpedientesMvVO exp = this.getExpediente();		
		final TituloDetalleExp titulo = new TituloDetalleExpW();
		
		titulo.setTextoModalidad(this.getTextoModalidad(exp));
		titulo.setBis(exp.getBis());
		titulo.setNumero(exp.getNumeroSolicitud());
		titulo.setTitulo(exp.getTituloInvencion());		
		
		return titulo.toString();
	}
	
	private String getTituloDetalleExpOtros() {
		final CeAlrExpedientesMvVO exp = this.getExpediente();		
		final TituloDetalleExp titulo = new TituloDetalleExpGral();
		
		titulo.setTextoModalidad(this.getTextoModalidad(exp));
		titulo.setLetraModalidad(exp.getModalidad());
		titulo.setNumero(exp.getNumeroSolicitud());
		titulo.setTitulo(exp.getTituloInvencion());		
		
		return titulo.toString();
	}

	public String getTituloDetalleLic() {
		final CeAlrLicenciasMvVO lic = this.getLicencia();
		final String modalidad = this.getTextoModalidadLic();
		final String letra = Modalidad.AL_LICENCIA.getModalidad();
		final String numLic = lic.getNumeroLicencia();

		return new TituloDetalleExpGral(modalidad, letra, numLic).toString();
	}

	public String getTituloDetalleCesion() {
		final CeAlrCesionMvVO ces = this.getCesion();
		final String modalidad = this.getTextoModalidadCes();
		final String letra = Modalidad.AL_TRANSMISION.getModalidad();
		final String numCes = ces.getNumeroCesion();

		return new TituloDetalleExpGral(modalidad, letra, numCes).toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.maoceo.comun.view.controller.IDetalleExpedienteAlfaController#
	 * componerUriConsultaPublicacion(es.oepm.core.business.ceo.vo.CeAlrPublicacionesMvVO)
	 */
	@Override
	public String componerUriConsultaPublicacion(CeAlrPublicacionesMvVO publicacion) {
		return ExpedienteUtils.componerUriConsultaPublicacion(publicacion);
	}

	private String getTextoModalidad(CeAlrExpedientesMvVO exp) {
		return Modalidad.find(exp.getModalidad()).getDescModalidad();
	}

	private String getTextoModalidadLic() {
		return Modalidad.AL_LICENCIA.getDescModalidad();
	}

	private String getTextoModalidadCes() {
		return Modalidad.AL_TRANSMISION.getDescModalidad();
	}

	public Date getFechaPrioridad() {
		if(null!=this.getExpediente()){
			return this.expediente.getFechaPrioridad();
		}
		
		return null;
	}

	public String getNumPublicacionOEPM() {
		return StringUtils.defaultSiNulo(this.expediente
				.getNumPublicacionOEPM());
	}

	public CeAlrAnotacionesMvVO getAnotacionRegistro() {
		return this.expediente.getAnotacionRegistro();
	}

	public CeAlrPublicacionesMvVO getPublicacionOEPM() {
		return this.getExpediente().getPublicacionOEPM();
	}

	public String getNumeroPublicacionExpediente() {
		return this.getExpediente().getNumeroPublicacionCabecera();
	}
	
	public String getNumeroSolicitudExpediente() {
		return ExpedienteUtils.formatNumeroSolicitud( this.expediente.getModalidad(), this.expediente.getNumeroSolicitud() );
	}
	
	public String getNumeroSolicitudCesion() {
		return ExpedienteUtils.formatNumeroSolicitud( Modalidad.AL_TRANSMISION.getModalidad(), this.getCesion().getNumeroCesion() );
	}
	
	public String getNumeroSolicitudLicencia() {
		return ExpedienteUtils.formatNumeroSolicitud( Modalidad.AL_LICENCIA.getModalidad(), this.getLicencia().getNumeroLicencia() );
	}
	
	public boolean isCesionVisible () {
		boolean publico = false;
		
		
		if (getCesion() != null) {
			List<CeAlrAnotacionesMvVO> anotaciones = cesion.getAnotaciones();
			if (anotaciones != null) {
				for (CeAlrAnotacionesMvVO anotacion : anotaciones) {
					if ("PUBLICACION_RESOLUCION_CAMBIO_DE_NOMBRE".equals(anotacion.getTipoAnotacionFk())
							|| "PUBLICACION_RESOLUCION_CESION".equals(anotacion.getTipoAnotacionFk())) {
						publico = true;
						break;
					}
				}
			}
		}
		
		return publico;
	}
	
	public boolean isLicenciaVisible () {
		boolean publico = false;
		
		if (getLicencia() != null) {
			List<CeAlrAnotacionesMvVO> anotaciones = licencia.getAnotaciones();
			if (anotaciones != null) {
				for (CeAlrAnotacionesMvVO anotacion : anotaciones) {
					if ("PUBLICACION_CONCESION_LICENCIA_OBLIGATORIA".equals(anotacion.getTipoAnotacionFk())
							|| "PUBLICACION_RESOLUCION_INSCRIPCION_LICENCIA".equals(anotacion.getTipoAnotacionFk())
							|| "PUBLICACION_RESOLUCION_LICENCIA_PLENO_DERECHO".equals(anotacion.getTipoAnotacionFk())) {
						publico = true;
						break;
					}
				}
			}
		}
		
		return publico;
	}

	// -----------------------

	public boolean isPatenteNacional() {
		return isModalidad(Modalidad.AL_PATENTE_NACIONAL);
	}

	public boolean isPatenteEuropea() {
		return isModalidad(Modalidad.AL_PATENTE_EUROPEA);
	}

	public boolean isPatenteSemiconductor() {
		return isModalidad(Modalidad.AL_SEMICONDUCTOR);
	}

	public boolean isPatenteInvencion() {
		return isModalidad(Modalidad.AL_PATENTE_INVENCION);
	}

	public boolean isModeloUtilidad() {
		return isModalidad(Modalidad.AL_MODELO_UTILIDAD);
	}

	public boolean isPatentePCT() {
		return isModalidad(Modalidad.AL_PATENTE_PCT);
	}

	protected boolean isModalidad(Modalidad mod) {
		return mod.is(this.getExpediente().getModalidad());
	}

	// -------------

	public CeAlrPublicacionesBepDTO getSolicitudBEP() {
		return this.expediente.getSolicitudBEP();
	}

	public CeAlrPublicacionesBepDTO getConcesionBEP() {
		return this.expediente.getConcesionBEP();
	}

	public String getFechaTradProteccionProvisional() {
		return this.expediente.getFechaTradProteccionProvisional();
	}

	public CeAlrPublicacionesMvVO getPublicacionProteccionProvisional() {
		return this.expediente.getPublicacionProteccionProvisional();
	}

	public String getFechaPubProteccionDefinitiva() {
		return this.expediente.getFechaPubProteccionDefinitiva();
	}

	public CeAlrPublicacionesMvVO getPublicacionProteccionDefinitiva() {
		return this.expediente.getPublicacionProteccionDefinitiva();
	}

	public String getEstadoExpediente() {
		return this.expediente.getEstado() != null ?  MessagesUtil.getMessage(this.expediente.getEstado()) : null;
	}
	
	public String getEstadoLicencia() {
		return this.licencia.getEstado() != null ?  MessagesUtil.getMessage(this.licencia.getEstado()) : null;
	}
	
	public String getEstadoCesion() {
		return this.cesion.getEstado() != null ?  MessagesUtil.getMessage(this.cesion.getEstado()) : null;
	}
	
	public Date getFechaEstadoExpediente() {
		return this.expediente.getFechaEstado() == null ? null : this.expediente.getFechaEstado();
	}

	// --------------------------------------------------------------
	public CeAlrTitularMvVO getTitular() {
		return this.expediente.getTitular();
	}

	public List<CeAlrTitularMvVO> getOtrosTitulares() {
		return this.expediente.getOtrosTitulares();
	}

	public CeAlrRelExpMvVO getPatenteBase() {
		return this.expediente.getPatenteBase();
	}

	public String getNumerosPubPatenteBase() {
		return this.expediente.getPatenteBase().getNumerosPublicacion();
	}

	public String getTitularPatenteBase() {
		return this.expediente.getPatenteBase().getTitular();
	}

	public List<CeAlrPrioridadMvVO> getDeclaracionesPrioridad() {
		return this.expediente.getPrioridades();
	}

	public List<CeAlrDepositoBioMvVO> getDepositosBio() {
		return this.expediente.getDepositosBio();
	}

	public List<CeAlrExhibicionMvVO> getExhibicionesExpediente() {
		return this.expediente.getExhibiciones();
	}

	public List<CeAlrInventorMvVO> getInventores() {
		return this.expediente.getInventores();
	}

	// *****************************************************

	public String getCipInvencionPub() {
		return this.expediente.getCipInvencionPub();
	}

	public String getCipAdicionalPub() {
		return this.expediente.getCipAdicionalPub();
	}

	public String getCipInvencionConc() {
		return this.expediente.getCipInvencionConc();
	}

	public String getCipAdicionalConc() {
		return this.expediente.getCipAdicionalConc();
	}

	public String getCip7() {
		return this.expediente.getCip7();
	}

	public boolean isTieneClasif() {
		final String cipInvPub = this.getCipInvencionPub();
		final String cipAdicPub = this.getCipAdicionalPub();
		final String cipInvConc = this.getCipInvencionConc();
		final String cipAdicConc = this.getCipAdicionalConc();
		final String cip7 = this.getCip7();

		return (cipInvPub != null || cipAdicPub != null || cipInvConc != null
				|| cipAdicConc != null || cip7 != null);
	}
	
	public String solicitudOMPI(CeAlrRelExpMvVO patenteBase) {
		String solicitud = patenteBase.getSolicitud();
		
		if (Modalidad.find(patenteBase.getModalidad()) == Modalidad.AL_PATENTE_PCT) {
			int longitudNumeroSolicitud;
			// OBTENER OMPI SI CORRESPONDE
			try {
				String year = solicitud.substring(1, 3);
				if (Integer.parseInt(year) < 86) {
					year = "20" + year;
				} else {
					year = "19" + year;
				}
				if (Integer.parseInt(year) < 2004) {
					longitudNumeroSolicitud = 5;
				} else {
					longitudNumeroSolicitud = 6;
				}
				solicitud = "PCT/"
						+ (patenteBase.getBis() != null ? patenteBase.getBis()
								: "")
						+ year
						+ "/"
						+ StringUtils.lcouch(solicitud.substring(3, 9).trim(),
								longitudNumeroSolicitud, '0');
			} catch (Exception ignored) {
			}
		}

		return solicitud;
	}

	public String getLeyTramitacion() { 
		return "1".equals(expediente.getLeyTramitacion()) ? 
					MessagesUtil.getMessage("alfa.leyTramitacion", MessagesUtil.getMessage("alfa.ley2015")) 
					:  null;
	}
	
	// *******************************************************************

	public List<ActosTramitacionDTO> getActosTramitacion() {
		return this.expediente.getActosTramitacion();
	}

	public List<CeAlrAnotacionesMvVO> getAnotacionesPagos() {
		return this.expediente.getAnotacionesPagos();
	}

	
	
	// *************************
	// LICENCIAS

	public List<CeAlrLicenciasExpMvVO> getLicenciasExpediente()
			throws BusinessException {
		return this.expediente.getLicencias();
	}

	// **************************************
	// CESIONES
	public List<CeAlrCesionExpedienteMvVO> getCesionesExpediente()
			throws BusinessException {
		return this.expediente.getCesiones();
	}
	
	// Metodos implementados por la interfaz
	@Override
	public String getModalidad() {
		if (licencia != null) {
			return "L";
		} else if (cesion != null) {
			return "F";
		} else {
			return expediente.getModalidad();
		}
	}

	@Override
	public String getNumeroSolicitud() {
		if(licencia != null){
			return licencia.getNumeroLicencia() ;
		}else if(cesion != null){
			return cesion.getNumeroCesion();
		} else {
			return expediente.getNumeroSolicitud();
		}
	}

	@Override
	public String getTipoPersona() {
		if(licencia != null || cesion != null){
			return "PF";
		}
		return null;
	}

	@Override
	public String getSelectedExpId() {
		if(licencia != null){
			return licencia.getId().toString();
		}else if(cesion != null){
			return cesion.getId().toString();
		} else {
			return expediente.getId().toString();
		}
	}

	@Override
	public String getTitDetalle() {
		if(licencia != null){
			return getTituloDetalleLic();
		}else if(cesion != null){
			return getTituloDetalleCesion();
		} else {
			return getTituloDetalle();
		}
	}

	@Override
	public boolean isVisible() {
		return true;
	}
	
	@Override
	public boolean isLicenciaPublica() {
		return true;
	}

	@Override
	public boolean isCesionPublica() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see es.oepm.maoceo.comun.view.controller.IDetalleExpedienteAlfaController#obtenerImagenTribunales(java.lang.String)
	 */
	@Override
	public String obtenerImagenTribunales(String tipoTribunal) {
		if(expediente != null){
			return ExpedienteUtils.obtenerImagenTribunalesExpediente(tipoTribunal, expediente);
		}else if (licencia != null){
			return ExpedienteUtils.obtenerImagenTribunalesLicencia(tipoTribunal, licencia);
		}else if (cesion != null){
			return ExpedienteUtils.obtenerImagenTribunalesCesion(tipoTribunal, cesion);
		}
		return IMAGEN_TRIBUNALES_KO;
	}

	/*
	 * (non-Javadoc)
	 * @see es.oepm.maoceo.comun.view.controller.IDetalleExpedienteAlfaController#getSenalesControlVigentes()
	 */
	@Override
	public List<CeAlrSenalControlMvVO> getSenalesControlVigentes(){
		if(expediente != null){
			return expediente.getSenalesControlVigentes();
		}else if (licencia != null){
			return licencia.getSenalesControlVigentes();
		}else if (cesion != null){
			return cesion.getSenalesControlVigentes();
		}
		return null;
	}

	@Override
	public void setExpediente(CeAlrExpedientesMvVO exp) {
		expediente = exp;
	}
}
