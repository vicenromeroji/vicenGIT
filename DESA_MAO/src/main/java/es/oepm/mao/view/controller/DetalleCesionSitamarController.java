package es.oepm.mao.view.controller;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import es.oepm.core.business.ceo.vo.CeMnrCedenteCesionarioMvVO;
import es.oepm.core.business.ceo.vo.CeMnrTransferCesioMvVO;
import es.oepm.core.business.ceo.vo.CeMnrTransferExpeMvVO;
import es.oepm.core.business.ceo.vo.CeMnrTransferRepreMvVO;
import es.oepm.core.business.vo.DetalleExpedienteResponseVO;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.view.controller.TituloDetalleExpGral;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.application.TrazaOpsMAO;
import es.oepm.mao.business.service.ExpedientesService;
import es.oepm.mao.business.vo.ExpedienteVO;
import es.oepm.maoceo.comun.view.controller.IDetalleCesionSitamarController;


@ManagedBean(name = "detalleCesionSitamarController")
@ViewScoped
public class DetalleCesionSitamarController extends DetalleExpedienteController implements IDetalleCesionSitamarController{

	private static final long serialVersionUID = -5405843658545574528L;
	
	@ManagedProperty( "#{expedientesService}" )
	private ExpedientesService expedientesService;
	
	public void setExpedientesService(ExpedientesService expedientesService) {
		this.expedientesService = expedientesService;
	}

	// ---------------
	private CeMnrTransferCesioMvVO transferCesio = null;
	
	
	
	/**
	 * Constructor por defecto de la clase.
	 */
	public DetalleCesionSitamarController() {
		ExpedienteVO exp = (ExpedienteVO)SessionUtil.getFromSession("expedienteSeleccionado");
		idExpediente = exp.getId();
		modalidad = exp.getModalidad();
		transferCesio = null;
	}

	@PostConstruct
	public void init() {
		getTransferCesio();		
	}

	public CeMnrTransferCesioMvVO getTransferCesio() {
		if (transferCesio == null) {
			try {
				// Recuperamos el expediente
				DetalleExpedienteResponseVO<CeMnrTransferCesioMvVO> response = expedientesService
						.getDetalleExpedienteEspecialSitamar(idExpediente,
								modalidad);
				transferCesio = response.getDetalleExpediente();
				procesarMensajesDetalleExpediente(response.getMensajes());
				
				// Lo añadimos a sesion
				SessionUtil.addToSession("detalleExpediente",transferCesio);
				SessionUtil.addToSession("tituloDetalle",getTituloDetalle());
				
				// Escribimos la traza
				TrazaOpsMAO.trazaOperacionConsultaExp( modalidad, (transferCesio.getAnoTransfer() + transferCesio.getNumTransfer()) );
			} catch (final Exception e) {
				FacesUtil.addErrorMessage(e.getMessage());
			}
		}

		return transferCesio;
	}

	
	public String getTituloDetalle() {
		final String num = this.transferCesio.getAnoTransfer() +  this.transferCesio.getNumTransfer();
		Modalidad cesion = Modalidad.find(this.transferCesio.getIndCesion(), "SITAMAR");
		final String txtMod = cesion.getDescModalidad();
		final String mod = cesion.getModalidad();
		
		return new TituloDetalleExpGral(txtMod, mod, num).toString();
	}
	
		

	//--------------
	
	public List<CeMnrCedenteCesionarioMvVO> getCedente() {
		return this.transferCesio.getCedente();
	}
	
	public List<CeMnrCedenteCesionarioMvVO> getCesionario() {
		return this.transferCesio.getCesionario();
	}
	
	public CeMnrTransferRepreMvVO getRepresentante() {
		return this.transferCesio.getRepresentante();
	}
	
	public List<CeMnrTransferExpeMvVO> getExpedientesAfectados() {
		return this.transferCesio.getExpedientesAfectados();
	}
	
	public boolean tieneAgente(){
		if(null == this.transferCesio){
			return false;
		}
		
		CeMnrTransferRepreMvVO repre = getRepresentante();
		if(null == repre){
			return false;
		}
		
		return hayCodAg(repre.getCodAgente());
	}
	
	public boolean tieneRepresentante(){
		if(null == this.transferCesio){
			return false;
		}
		
		CeMnrTransferRepreMvVO repre = getRepresentante();
		if(null == repre){
			return false;
		}		
		
		return !hayCodAg(repre.getCodAgente()) && null!=repre.getNombreApellidos() ;
	}
	
	private boolean hayCodAg(String codAg){
		return codAg!=null && !codAg.trim().isEmpty() && !codAg.equals("0000");		
	}
	public void setTransferCesio(CeMnrTransferCesioMvVO transferCesio) {
		this.transferCesio = transferCesio;
	}
}
