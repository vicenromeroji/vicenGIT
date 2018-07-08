package es.oepm.mao.view.controller;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.core.business.ceo.vo.CeMnrClasesConcedidasMvVO;
import es.oepm.core.business.ceo.vo.CeMnrClasesDesistMvVO;
import es.oepm.core.business.ceo.vo.CeMnrClasesSoliciMvVO;
import es.oepm.core.business.ceo.vo.CeMnrClasesVigentesMvVO;
import es.oepm.core.business.ceo.vo.CeMnrDenominacionMvVO;
import es.oepm.core.business.ceo.vo.CeMnrDerivadasHijosDTO;
import es.oepm.core.business.ceo.vo.CeMnrDerivadasMvVO;
import es.oepm.core.business.ceo.vo.CeMnrDirecCorresponMvVO;
import es.oepm.core.business.ceo.vo.CeMnrEstadosMvVO;
import es.oepm.core.business.ceo.vo.CeMnrExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeMnrExposicionesMvVO;
import es.oepm.core.business.ceo.vo.CeMnrHistoTitularesMvVO;
import es.oepm.core.business.ceo.vo.CeMnrMunicipiosMvVO;
import es.oepm.core.business.ceo.vo.CeMnrPagosMvVO;
import es.oepm.core.business.ceo.vo.CeMnrPaisesDesigMvVO;
import es.oepm.core.business.ceo.vo.CeMnrPrioridadMvVO;
import es.oepm.core.business.ceo.vo.CeMnrProcedenciaMvVO;
import es.oepm.core.business.ceo.vo.CeMnrSituTramitesMvVO;
import es.oepm.core.business.ceo.vo.CeMnrSituVienaMvVO;
import es.oepm.core.business.ceo.vo.CeMnrTitularMvVO;
import es.oepm.core.business.vo.DetalleExpedienteResponseVO;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.constants.Roles;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.session.SessionContext;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.util.ExpedienteUtils;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.controller.TituloDetalleExp;
import es.oepm.core.view.controller.TituloDetalleExpMarcas;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.core.view.faces.MessagesUtil;
import es.oepm.mao.application.TrazaOpsMAO;
import es.oepm.mao.business.service.ExpedientesService;
import es.oepm.mao.business.vo.ExpedienteVO;
import es.oepm.mao.constants.MaoPropiedadesConf;
import es.oepm.maoceo.comun.view.controller.IDetalleExpedienteSitamarController;


@ManagedBean(name = "detalleSitamarController")
@ViewScoped
public class DetalleExpedienteSitamarController extends DetalleExpedienteController implements IDetalleExpedienteSitamarController {

	private static final long serialVersionUID = 2183919135994084457L;
	
	@ManagedProperty( "#{expedientesService}" )
	private ExpedientesService expedientesService;

	// ---------------
	private CeMnrExpedientesMvVO expediente = null;
	private Modalidad modExpediente = null;
	private String urlBusquedaOami;
	
	/**
	 * Constructor por defecto de la clase.
	 */
	public DetalleExpedienteSitamarController() {
		ExpedienteVO exp = (ExpedienteVO)SessionUtil.getFromSession("expedienteSeleccionado");
		idExpediente = exp.getId();
		modalidad = exp.getModalidad().trim();
		modExpediente = Modalidad.find(exp.getModalidad().trim());
		expediente = null;
	}

	@PostConstruct
	public void init() {
		// Cargamos la url de búsqueda de la Oami de la configuración
		urlBusquedaOami = Configuracion.getPropertyAsString(MaoPropiedadesConf.URL_BUSQUEDA_OAMI);
				
		this.expediente = (CeMnrExpedientesMvVO)SessionUtil.getFromSession("detalleExpediente");
		if (expediente == null) {
			if (!SessionContext.getUserDetails().getAuthorities().contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "S"))) {
				FacesUtil.addErrorMessage("busquedaRapidaPorNumero.error.sitamar");
			} else {
				try {
					// Recuperamos el expediente
					DetalleExpedienteResponseVO<CeMnrExpedientesMvVO> response = expedientesService
							.getDetalleExpedienteSitamar(idExpediente, modalidad);
					expediente = response.getDetalleExpediente();
					
					procesarMensajesDetalleExpediente(response.getMensajes());
					// Lo añadimos a sesion
					SessionUtil.addToSession("detalleExpediente",expediente );
					modExpediente = Modalidad.find(expediente.getModalidad().trim());
					SessionUtil.addToSession("tituloDetalle",getTituloDetalle());
					
					// Escribimos la traza
					TrazaOpsMAO.trazaOperacionConsultaExp( expediente.getModalidad(), expediente.getNumero() );
				} catch (final Exception e) {
					FacesUtil.addErrorMessage("busqueda.error");
				}
			}
		}
	}
		
	public void setExpedientesService(ExpedientesService expedientesService) {
		this.expedientesService = expedientesService;
	}

	public CeMnrExpedientesMvVO getExpediente() {
		return this.expediente;
	}

	public String getReferenciaTmview() {
		/*String MM = "";
		String numero = expediente.getNumero();
		if (expediente.getModalidad().trim().equalsIgnoreCase("M"))
			MM = "ES50";
		else if (expediente.getModalidad().trim().equalsIgnoreCase("N"))
			MM = "ES67";
		else if (expediente.getModalidad().trim().equalsIgnoreCase("R"))
			MM = "ES68";
		else if (expediente.getModalidad().trim().equalsIgnoreCase("H"))
			MM = "WO50";

		numero = numero + expediente.getBis();
		numero = numero.trim();

		numero = completarConCeros(numero);

		//String referencia = MM + "0000" + numero;*/
		String referencia = expediente.getModalidad() + expediente.getNumero();

		return referencia;
	}

//	private String completarConCeros(String numero) {
//		while (numero.length() < 9)
//			numero = "0" + numero;
//		return numero;
//	}

	public String getTituloDetalle() {
		CeMnrExpedientesMvVO exp = this.expediente;
		final String mod = modExpediente.getModalidad();
		final String descMod = Modalidad.find(mod).getDescModalidad();

		final TituloDetalleExp titulo = createTituloExp();		
		titulo.setTextoModalidad(descMod);
		titulo.setLetraModalidad(mod);
		titulo.setNumero(exp.getNumero());
		titulo.setDigito(exp.getDigito());
		titulo.setBis(exp.getBis());
		titulo.setTitulo(exp.getDenominacion());
		
		return titulo.toString();
	}

	private TituloDetalleExp createTituloExp(){
		/*final String m = this.expediente.getModalidad();
		final Modalidad mod = Modalidad.MN_MARCA_INTERNACIONAL;
		return (mod.is(m))? new TituloDetalleExpH() : new TituloDetalleExpGral();*/
		return new TituloDetalleExpMarcas();
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.maoceo.comun.view.controller.IDetalleExpedienteSitamarController#getNumeroDivisionPrincipal()
	 */
	@Override
	public String getNumeroDivisionPrincipal() {
		String numExp = "";
		
		// Comprobamos si viene informado el expediente derivado
		if (StringUtils.isEmptyOrNull(expediente.getModExpDeriva()) && StringUtils.isEmptyOrNull(expediente.getNumExpDeriva())) {
			Boolean existeEnLista = Boolean.FALSE;
			// Comprobamos si está en la lista de expedientes
			if (expediente.getProcedenciasPorDivision() != null) {
				for (CeMnrDerivadasMvVO division : expediente.getProcedenciasPorDivision()) {
					if (expediente.getModExpDivi().equals(division.getModHijo())
							&& expediente.getNumExpDivi().equals(division.getNumHijo())) {
						existeEnLista = Boolean.TRUE;
					}
				}
			}
			
			// Si no está en la lista lo mostramos
			if (existeEnLista.equals(Boolean.FALSE)) {
				
				// HCS: Arreglado fallo que sacaba el mensaje EXPEDIENTE DIVISIONAL DE: nullnull
				if (!StringUtils.isEmptyOrNull(expediente.getModExpDivi())) {
					numExp += expediente.getModExpDivi();
				}
				
				if (!StringUtils.isEmptyOrNull(expediente.getNumExpDivi())) {
					numExp +=  expediente.getNumExpDivi();
				}

			}
		}
		
		return numExp.trim();
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.maoceo.comun.view.controller.IDetalleExpedienteSitamarController#
	 * getUriConsultaExternaDerivada(es.oepm.core.business.ceo.vo.CeMnrDerivadasMvVO)
	 */
	@Override
	public String getUriConsultaExternaDerivada(CeMnrDerivadasMvVO derivada) {
		return ExpedienteUtils.componerUriConsultaExternaGeneral(
				derivada.getModalidad(), derivada.getNumero(),
				derivada.getBis());
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.maoceo.comun.view.controller.IDetalleExpedienteAlfaController#getTargetConsultaExterna()
	 */
	@Override
	public String getTargetConsultaExterna() {
		return "_blank";
	}
	
	public boolean isCodStatus160() {
		return StringUtils.isCode(this.expediente.getCodStatusAct(), "160");
		// this.expediente.getCodStatusAct().equals("160   ");
	}

	public String getFechaPublicacion() {
		return this.expediente.getFechaPublicacion();
	}
	
	public boolean isVisible(){
		return this.expediente != null ? 
				this.expediente.isVisible() 
				: false;	
	}

	public boolean isPublicable(){	
		return this.expediente != null ? 
				this.expediente.isPublicable() 
				: false;				
	}
	
	public boolean isMarcaNacional() {
		return Modalidad.MN_MARCA_NACIONAL == modExpediente;
	}

	public boolean isMarcaInternacional() {
		return Modalidad.MN_MARCA_INTERNACIONAL == modExpediente;
	}

	public boolean isNombreComercial() {
		return Modalidad.MN_NOMBRE_COMERCIAL == modExpediente;
	}

	public boolean isRotulo() {
		return Modalidad.MN_ROTULO == modExpediente;
	}

	public boolean isExentoTasas() {
		return expediente.getCodExencion().trim().equals("E");
	}

	public boolean isDesglosada() {
		return expediente.getIndDesglose().trim().equals("T");
	}

	public String getFechaPresentSolic() {
		String res = "";
		try {
			if (!StringUtils.isEmptyOrNull(this.expediente.getFechaSolici())
					&& !StringUtils.isEmptyOrNull(this.expediente
							.getFechaPresentacion())) {
				Date fechaSol = null;
				fechaSol = new SimpleDateFormat("dd/MM/yyyy")
						.parse(this.expediente.getFechaSolici());
				Date fechaPre = null;
				fechaPre = new SimpleDateFormat("dd/MM/yyyy")
						.parse(this.expediente.getFechaPresentacion());

				if (fechaPre.compareTo(fechaSol) < 0) {
					res = this.expediente.getFechaPresentacion();
					if (!"00:00".equals(this.expediente.getHoraSolici())) {
						res += " A LAS " + this.expediente.getHoraSolici();
					}
				} else {
					res = this.expediente.getFechaSolici() + " A LAS "
							+ this.expediente.getHoraSolici();
					final String lugar = this.expediente.getLugarPresenta();
					if(!StringUtils.isEmptyOrNull(lugar)){
						res+= " EN " + lugar;
					}
				}
			}
		} catch (ParseException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		return res;
	}

	// --------------

	public CeMnrTitularMvVO getTitular() {
		return this.expediente.getTitular();
	}

	public List<CeMnrTitularMvVO> getOtrosTitulares() {
		return this.expediente.getOtrosTitulares();
	}

	// --------------

	public List<CeMnrDenominacionMvVO> getDenominaciones() {
		return this.expediente.getDenominaciones();
	}

	public boolean hayColores() {
		return 0 < getDenominaciones().size() && hayDescColor();
	}

	public boolean hayDistintivo() {
		return 0 < getDenominaciones().size() && hayDescripcion();
	}

	public boolean hayDistintivoNoReivind() {
		return 0 < getDenominaciones().size() && hayDescNoReivind();
	}

	// --

	private boolean hayDescColor() {
		final String d = getDenominaciones().get(0).getDescripColor();
		return d != null && !d.trim().isEmpty();
	}

	private boolean hayDescripcion() {
		final String d = getDenominaciones().get(0).getDescripcion();
		return d != null && !d.trim().isEmpty();
	}

	private boolean hayDescNoReivind() {
		final String d = getDenominaciones().get(0).getDescNoReivind();
		return d != null && !d.trim().isEmpty();
	}

	// ------------

	public List<CeMnrClasesSoliciMvVO> getClasesSolicitadas() {
		return this.expediente.getClasesSolic();
	}

	public List<CeMnrClasesSoliciMvVO> getClasesRenovadas() {
		return this.expediente.getClasesRenovadas();
	}

	public List<CeMnrClasesConcedidasMvVO> getClasesConcedidas() {
		return this.expediente.getClasesConc();
	}

	public String getTituloClasesAmpliadas() {
		final String num = this.expediente.getNumAmpliacion();
		final String msg = MessagesUtil.getMessage("mn_clases_amp.titulo");
		return MessageFormat.format(msg, num);
	}

	public List<String> getClasesAmpliadas() {
		return this.expediente.getClasesAmpliadas();
	}

	// ----------------

	public List<CeMnrSituTramitesMvVO> getTramites() {
		return this.expediente.getTramites();
	}

	// ----------------------
	public List<CeMnrDirecCorresponMvVO> getDirecCorrespon() {
		return this.expediente.getDirecCorrespon();
	}

	public List<CeMnrSituTramitesMvVO> getLicencias() throws BusinessException {
		return this.expediente.getLicencias();
	}

	// ----------------------
	public List<CeMnrHistoTitularesMvVO> getTitularesAnteriores() {
		return this.expediente.getTitularesAnteriores();
	}

	// ----------------------
	public List<CeMnrMunicipiosMvVO> getMunicipiosSolicitados() {
		return this.expediente.getMunicipiosSolicitados();
	}

	public List<CeMnrMunicipiosMvVO> getMunicipiosConcedidos() {
		return this.expediente.getMunicipiosConcedidos();
	}

	public List<CeMnrMunicipiosMvVO> getMunicipiosRenovados() {
		return this.expediente.getMunicipiosRenovados();
	}

	// ----------------------

	public List<CeMnrPrioridadMvVO> getPrioridadesTotales() {
		return this.expediente.getPrioridadesTotales();
	}

	public List<CeMnrPrioridadMvVO> getPrioridadesParciales() {
		return this.expediente.getPrioridadesParciales();
	}

	// ----------------------

	public List<CeMnrExposicionesMvVO> getExposicionesTotales() {
		return this.expediente.getExposicionesTotales();
	}

	public List<CeMnrExposicionesMvVO> getExposicionesParciales() {
		return this.expediente.getExposicionesParciales();
	}

	// ----------------------

	public List<CeMnrSituVienaMvVO> getClasifViena() {
		return this.expediente.getClasifViena();
	}

	// ----------------------

	public List<CeMnrDerivadasHijosDTO> getMarcasDerivadas() {
		return this.expediente.getMarcasDerivadas();
	}

	// ----------------------
	public String getPaisesDesignados() {
		List<CeMnrPaisesDesigMvVO> paises = this.expediente
				.getPaisesDesignados();
		StringBuilder sb = new StringBuilder();
		for (CeMnrPaisesDesigMvVO pais : paises) {
			sb.append(pais.getPais()).append(" ");
		}

		return sb.toString();
	}

	// ----------------------

	public List<CeMnrProcedenciaMvVO> getPaisesProcedencia() {
		return this.expediente.getPaisesProcedencia();
	}

	// ----------------------

	public List<CeMnrClasesVigentesMvVO> getProductosLimitados() {
		return this.expediente.getProductosLimitados();
	}

	public List<CeMnrClasesVigentesMvVO> getProductosSolicExten() {
		return this.expediente.getProductosSolicExten();
	}

	public List<CeMnrClasesVigentesMvVO> getProductosRenovados() {
		return this.expediente.getProductosRenovados();
	}

	// ----------------------

	public List<CeMnrClasesDesistMvVO> getClasesDesistidas() {
		return this.expediente.getClasesDesistidas();
	}

	// ----------------------

	public List<CeMnrPagosMvVO> getAnotacionesPagos() {
		return this.expediente.getAnotacionesPagos();
	}

	public String getUrlCEO(){
		return Configuracion.getPropertyAsString("url.ceo.home");
	}

	@Override
	public boolean isCaducado() {
		return StringUtils.isCode(expediente.getCodStatusAct(), "160");
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.maoceo.comun.view.controller.IDetalleExpedienteSitamarController#getUrlBusquedaOami()
	 */
	@Override
	public String getUrlBusquedaOami() {
		return urlBusquedaOami;
	}

	@Override
	public void setExpediente(CeMnrExpedientesMvVO exp) {
		expediente = exp;
	}

	public CeMnrEstadosMvVO getEstado() {
		return expediente.getEstado();
	}

	public void setEstado(CeMnrEstadosMvVO estado) {
		this.expediente.setEstado(estado);
	}
}
