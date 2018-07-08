package es.oepm.mao.view.controller;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;




import es.oepm.core.constants.Modalidad;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.util.StringUtils;
import es.oepm.mao.business.vo.ExpedienteVO;



@ManagedBean(name = "enlacesExpedienteController")
@SessionScoped
public class EnlaceExpedienteController implements Serializable {
	
	private static final long serialVersionUID = 4201396428611752915L;

	public EnlaceExpedienteController () {
		OepmLogger.debug("EnlaceExpedienteController...");
	}

	public boolean isPatenteEuropea() {
		return compararModalidad("E");

	}
	
	public boolean isModeloUtilidad() {
		return compararModalidad("U");

	}
	
	public boolean isPatenteNacional() {
		return compararModalidad("P");
	}
	
	public boolean isPatentePCT() {
		return compararModalidad("W");
	}

	private boolean compararModalidad(String modalidad) {
		String modalidadExpSelecc = getModalidad();
		return modalidadExpSelecc.equalsIgnoreCase(modalidad);

	}
	
	public boolean isAlfa(){
		String modalidad = getModalidad();		
		Modalidad mod = Modalidad.find(modalidad);
		return mod.isAlfa();
	}
	
	public boolean isSitamar(){
		String modalidad = getModalidad();		
		Modalidad mod = Modalidad.find(modalidad);
		return mod.isSitamar();
	}	
	
	public boolean isSitamarValidos(){
		String modalidad = getModalidad();		 
		return Modalidad.MN_MARCA_NACIONAL.is(modalidad) 
				|| Modalidad.MN_NOMBRE_COMERCIAL.is(modalidad) 
				|| Modalidad.MN_ROTULO.is(modalidad);
	}
	
	public boolean isSitamarH(){
		String modalidad = getModalidad();		 
		return Modalidad.MN_MARCA_INTERNACIONAL.is(modalidad);
	}

	public boolean isSitamod(){
		String modalidad = getModalidad();		
		Modalidad mod = Modalidad.find(modalidad);
		return mod.isSitamod();
	}
	
	public boolean isSitamodValidos(){
		String modalidad = getModalidad();		
		return Modalidad.DI_DISENO.is(modalidad) 
				|| Modalidad.DI_DIBUJO.is(modalidad) 
				|| Modalidad.DI_MODELO.is(modalidad);
	}
	
	public boolean isSitamodG(){
		String modalidad = getModalidad();		
		return Modalidad.DI_DIBUJO_INT.is(modalidad);
	}
		
	public boolean isCesionSitamar(){
		String modalidad = getModalidad();		
		Modalidad mod = Modalidad.find(modalidad);
		return mod.isSitamar() && mod.isCesion();
	}
	
	public boolean isCesionSitamod(){
		String modalidad = getModalidad();		
		Modalidad mod = Modalidad.find(modalidad);
		return mod.isSitamod()&& mod.isCesion();
	}
	
	public boolean isCesionAlfa(){
		String modalidad = getModalidad();		
		Modalidad mod = Modalidad.find(modalidad);
		return mod.isAlfa()&& mod.isCesion();
	}
	
	public boolean isCesion(){
		String modalidad = getModalidad();		
		Modalidad mod = Modalidad.find(modalidad);
		return mod.isCesion();
	}
	
	public boolean isLicenciaAlfa(){
		String modalidad = getModalidad();	
		Modalidad mod = Modalidad.find(modalidad);
		return mod.isLicenciaAlfa();		
	}
	
	public String getNumeroSolicitud() {
		ExpedienteVO exp = (ExpedienteVO) SessionUtil
				.getFromSession("expedienteSeleccionado");
		return exp.getNumeroSolicitud();

	}
	
	private String getModalidad(){
		ExpedienteVO exp = (ExpedienteVO) SessionUtil
				.getFromSession("expedienteSeleccionado");
		return exp.getModalidad();				
	}
	
	public String getRomarinParam() {
		String num = "";
		ExpedienteVO exp = (ExpedienteVO) SessionUtil.
				getFromSession("expedienteSeleccionado");
		num = exp != null ? exp.getNumeroSolicitud() : num;
		num = StringUtils.isEmptyOrNull(num) ? num : num.replaceAll("^0*", "");
		num = "%28%2FMARKGR%2FINTREGN+contains+" + num + "%29";
		return num;
	}

}
