package es.oepm.mao.view.controller;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


import es.oepm.core.logger.OepmLogger;
import es.oepm.core.util.DateUtils;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.comun.business.service.UsuPdteConfirmacionService;
import es.oepm.mao.comun.business.service.UsuariosAgenteService;
import es.oepm.mao.comun.business.service.UsuariosTituRepreService;
import es.oepm.persistencia.mao.MaoUsuPdteConfirmacion;

/**Clase que elimina todos los usuarios que se encuentran pendientes de confirmar desde hace mas de un mes.*/

/*@ManagedBean(name = "usuPdteCaducadosController")
@ViewScoped*/
public class UsuPdteCaducadosController implements Serializable {	
	
	private static final long serialVersionUID = 5784536997034620295L;
	
	//@ManagedProperty(name = "usuPdteConfirmacionService", value = "#{usuPdteConfirmacionService}")	
	private UsuPdteConfirmacionService usuPdteConfirmacionService;
	//@ManagedProperty(name = "usuariosAgenteService", value = "#{usuariosAgenteService}")	
	private UsuariosAgenteService usuariosAgenteService;
	//@ManagedProperty(name = "usuariosTituRepreService", value = "#{usuariosTituRepreService}")	
	private UsuariosTituRepreService usuariosTituRepreService;
	
	/*
	 * Constructor por defecto de la clase.
	 */
	public UsuPdteCaducadosController() {
		super();		
	}
	
	
	/**Borra todos los usuarios que no han confirmado su registro desde hace mas de un mes.*/

	public void deleteUsuPdteCaducados(){
		try{
			Date fechaActual= new Date();
			fechaActual= DateUtils.restarFechaDias(fechaActual, 30);
			
			//Elimina el usuario de la tabla Agentes o Titulares/Representantes segun corresponda.
			List<MaoUsuPdteConfirmacion> listCaducados= usuPdteConfirmacionService.searchCaducados(fechaActual);		
			Iterator<MaoUsuPdteConfirmacion > it= listCaducados.iterator();
			while(it.hasNext()){
				MaoUsuPdteConfirmacion elem= it.next();
				if(elem.getTipoUsuario()== 'A'){
					usuariosAgenteService.delete(usuariosAgenteService.getById(elem.getCodAgente()));
				}else{
					usuariosTituRepreService.delete(usuariosTituRepreService.getById(elem.getIdTitrep()));
				}
				//Elimina el usuario de la tabla de Usuarios Pendientes de Confirmar.
				usuPdteConfirmacionService.delete(usuPdteConfirmacionService.getById(elem.getCodVerificacion()));
			}			
		}catch(final Exception e){
			FacesUtil.addErrorMessage();
			OepmLogger.error(e);		
		}
	}

	
	/*********************METODOS GET / SET********************************************/
	public UsuPdteConfirmacionService getUsuPdteConfirmacionService() {
		return usuPdteConfirmacionService;
	}

	public void setUsuPdteConfirmacionService(
			UsuPdteConfirmacionService usuPdteConfirmacionService) {
		this.usuPdteConfirmacionService = usuPdteConfirmacionService;
	}


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
	
}
