package es.oepm.mao.view.controller;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.core.business.mao.vo.UsuPdteConfirmacionVO;
import es.oepm.core.business.mao.vo.UsuariosAgenteVO;
import es.oepm.core.business.mao.vo.UsuariosTituRepreVO;
import es.oepm.core.logger.OepmLogger;
import es.oepm.mao.comun.business.service.UsuPdteConfirmacionService;
import es.oepm.mao.comun.business.service.UsuariosAgenteService;
import es.oepm.mao.comun.business.service.UsuariosTituRepreService;


@ManagedBean(name = "verificarEmailController")
@ViewScoped
public class VerificarEmailController implements Serializable {
	
	private static final long serialVersionUID = -8178372103968524652L;
	
	@ManagedProperty(name = "usuariosAgenteService", value = "#{usuariosAgenteService}")	
	private UsuariosAgenteService usuariosAgenteService;
	@ManagedProperty(name = "usuariosTituRepreService", value = "#{usuariosTituRepreService}")	
	private UsuariosTituRepreService usuariosTituRepreService;
	@ManagedProperty(name = "usuPdteConfirmacionService", value = "#{usuPdteConfirmacionService}")	
	private UsuPdteConfirmacionService usuPdteConfirmacionService;
	
	String codVerificacion="";
	boolean resultadoConfirmacion= false;

	/*
	 * Constructor por defecto de la clase.
	 */
	public VerificarEmailController() {
		super();		
	}
	
	
	/**************************MeTODOS AUXILIARES****************************************/
	
	/*
	 * Metodo que devuelve el resultado de confirmar el usuario su email.
	 * */
	public boolean getResultado(){
		boolean res= false;
		UsuariosAgenteVO usuAgente= null;
		UsuariosTituRepreVO usuTituRepre= null;
		UsuPdteConfirmacionVO usuPdte= null;		
		try { 
			usuPdte= usuPdteConfirmacionService.getById(codVerificacion);
			if(usuPdte!= null){
				if (usuPdte.getTipoUsuario()=='A'){
					usuAgente= usuariosAgenteService.getById(usuPdte.getCodAgente());
					if(usuAgente!= null) {
						usuAgente.setBoMverificado('S');
						usuAgente.setBoBloqueocuenta(false);
						usuariosAgenteService.update(usuAgente);
						usuPdteConfirmacionService.delete(usuPdte);
						res= true;
					}
				}else{
					usuTituRepre= usuariosTituRepreService.getById(usuPdte.getIdTitrep());
					if(usuTituRepre!= null){
						usuTituRepre.setBoMverificado('S');
						usuTituRepre.setBoBloqueocuenta(false);
						usuariosTituRepreService.update(usuTituRepre);
						usuPdteConfirmacionService.delete(usuPdte);
						res= true;
					}
				}				
			}								
		} catch (Exception e) {
			res= false;			
			OepmLogger.error(e);	
		}
		return res;
	}
	
	public String getUrlMao(){
		return Configuracion.getPropertyAsString("url.mao.home");
	}
	
	
	/*****************************METODOS GET/SET******************************/

	public UsuariosAgenteService getUsuariosAgenteService() {
		return usuariosAgenteService;
	}

	public void setUsuariosAgenteService(UsuariosAgenteService usuariosAgenteService) {
		this.usuariosAgenteService = usuariosAgenteService;
	}

	public UsuariosTituRepreService getUsuariosTituRepreService() {
		return usuariosTituRepreService;
	}

	public void setUsuariosTituRepreService(
			UsuariosTituRepreService usuariosTituRepreService) {
		this.usuariosTituRepreService = usuariosTituRepreService;
	}

	public UsuPdteConfirmacionService getUsuPdteConfirmacionService() {
		return usuPdteConfirmacionService;
	}

	public void setUsuPdteConfirmacionService(
			UsuPdteConfirmacionService usuPdteConfirmacionService) {
		this.usuPdteConfirmacionService = usuPdteConfirmacionService;
	}

	public String getCodVerificacion() {
		return codVerificacion;		
	}

	public void setCodVerificacion(String codVerificacion) {
		this.codVerificacion = codVerificacion;
		resultadoConfirmacion= getResultado();
	}

	public boolean getResultadoConfirmacion() {
		return resultadoConfirmacion;
	}

	public void setResultadoConfirmacion(boolean resultadoConfirmacion) {
		this.resultadoConfirmacion = resultadoConfirmacion;
	}	
	
	
	
}
