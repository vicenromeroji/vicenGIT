package es.oepm.mao.view.controller;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import es.oepm.core.business.ceo.vo.CeDirCedenteCesionarioMvVO;
import es.oepm.core.business.ceo.vo.CeDirTransferCesioMvVO;
import es.oepm.core.business.ceo.vo.CeDirTransferRegisMvVO;
import es.oepm.core.business.vo.DetalleExpedienteResponseVO;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.view.controller.TituloDetalleExpGral;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.application.TrazaOpsMAO;
import es.oepm.mao.business.service.ExpedientesService;
import es.oepm.mao.business.vo.ExpedienteVO;
import es.oepm.maoceo.comun.view.controller.IDetalleCesionSitamodController;



@ManagedBean(name = "detalleCesionSitamodController")
@ViewScoped
public class DetalleCesionSitamodController extends DetalleExpedienteController implements IDetalleCesionSitamodController {

	private static final long serialVersionUID = -1169702842719651070L;
	
	@ManagedProperty( "#{expedientesService}" )
	private ExpedientesService expedientesService;
	
	public void setExpedientesService(ExpedientesService expedientesService) {
		this.expedientesService = expedientesService;
	}
	
	private CeDirTransferCesioMvVO transferCesio = null;		
	
	/**
	 * Constructor por defecto de la clase.
	 */
	public DetalleCesionSitamodController() {
		ExpedienteVO exp = (ExpedienteVO)SessionUtil.getFromSession("expedienteSeleccionado");
		idExpediente = exp.getId();
		modalidad = exp.getModalidad();
		transferCesio = null;
	}

	@PostConstruct
	public void init() {
		getTransferCesio();		
	}

	public CeDirTransferCesioMvVO getTransferCesio() {
		if (transferCesio == null) {
			try {
				DetalleExpedienteResponseVO<CeDirTransferCesioMvVO> response = expedientesService
						.getDetalleExpedienteEspecialSitamod(idExpediente,
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
		Modalidad cesion = Modalidad.find(this.transferCesio.getIndCesion(), "SITAMOD");
		final String txtMod = cesion.getDescModalidad();
		final String mod = cesion.getModalidad();
		
		return new TituloDetalleExpGral(txtMod, mod, num).toString();
	}
	
	//--------------
	
	public List<CeDirCedenteCesionarioMvVO> getCedente() {
		return this.transferCesio.getCedente(); 
	}
	
	public List<CeDirCedenteCesionarioMvVO> getCesionario() {
		return this.transferCesio.getCesionario();
	}	
	
	public List<CeDirTransferRegisMvVO> getExpedientesAfectados() {
		return this.transferCesio.getExpedientesAfectados();
	}
		
	public boolean tieneAgente(){
		if(null == this.transferCesio){
			return false;
		}
		
		return hayCodAg(this.transferCesio.getCodAgente());
	}
	
	public boolean tieneRepresentante(){
		if(null == this.transferCesio){
			return false;
		}

		String codAg = this.transferCesio.getCodAgente();
		String nomAp = this.transferCesio.getNombreApellidosRepre();
		
		return !hayCodAg(codAg) && null!=nomAp ;
	}
	
	private boolean hayCodAg(String codAg){
		return codAg!=null && !codAg.trim().isEmpty() && !codAg.equals("0000");		
	}	
	

	public void setTransferCesio(CeDirTransferCesioMvVO transferCesio) {
		this.transferCesio = transferCesio;
	}
		
}
