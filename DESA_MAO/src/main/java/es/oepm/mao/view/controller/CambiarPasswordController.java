package es.oepm.mao.view.controller;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.springframework.security.authentication.encoding.Md5PasswordEncoder;


import es.oepm.core.business.BaseVO;
import es.oepm.core.business.mao.vo.CambioPasswordVO;
import es.oepm.core.business.mao.vo.MaoUsuAsociadosVO;
import es.oepm.core.business.mao.vo.UsuariosAgenteVO;
import es.oepm.core.business.mao.vo.UsuariosTituRepreVO;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.comun.business.service.UsuAsociadosService;
import es.oepm.mao.comun.business.service.UsuariosAgenteService;
import es.oepm.mao.comun.business.service.UsuariosTituRepreService;

/**
 * The Class BaseController
 * 
 * @author AYESA AT
 */
@ManagedBean(name = "cambiarPasswordController")	
@ViewScoped
public class CambiarPasswordController extends BaseController {

	private static final long serialVersionUID = -2067750498759422502L;
	
	private Md5PasswordEncoder passwordEncoder= null;
	@ManagedProperty(name = "usuariosAgenteService", value = "#{usuariosAgenteService}")	
	private UsuariosAgenteService usuariosAgenteService;
	@ManagedProperty(name = "usuariosTituRepreService", value = "#{usuariosTituRepreService}")	
	private UsuariosTituRepreService usuariosTituRepreService;
	@ManagedProperty(name = "usuAsociadosService", value = "#{usuAsociadosService}")
	private UsuAsociadosService usuAsociadosService;

	private CambioPasswordVO filter = new CambioPasswordVO();	

	/**
	 * Constructor por defecto de la clase.
	 */
	public CambiarPasswordController() {
		super();
		if (passwordEncoder == null) {
			passwordEncoder = new Md5PasswordEncoder();
		}
	}	

	public String cambiarPassword(){
		String res= "";
		UsuariosAgenteVO voAgente= null;
		UsuariosTituRepreVO voTituRepre= null;
		MaoUsuAsociadosVO voAsociado= null;	
		
		if(!filter.getClaveNueva().equals(filter.getClaveNuevaRepetida())){
			FacesUtil.addErrorMessage("cambiarPassword.error.claveNueva");
		}else{			
			try {
				if (passwordEncoder == null) {
					passwordEncoder = new Md5PasswordEncoder();
				}
				String idUsuario= SessionContext.getIdUsuario();	
				if(isAgente()){
					if(idUsuario.length()<4){
						idUsuario= idUsuario + " ";
					}
					voAgente = usuariosAgenteService.getById(idUsuario);
					//Se comprueba que la clave actual coincide con la clave del usuario logado.
					if(!passwordEncoder.encodePassword(filter.getClaveActual(), null).equals(voAgente.getClave())){
						FacesUtil.addErrorMessage("cambiarPassword.error.claveActual");
					}else{					
						voAgente.setClave(passwordEncoder.encodePassword(filter.getClaveNueva(), null));
						usuariosAgenteService.update(voAgente);	
						FacesUtil.addOKMessage("cambiarPassword.ok");
					}				
				}else if(isTituRepre()){
					voTituRepre = usuariosTituRepreService.getById(Long.valueOf(idUsuario));
					//Se comprueba que la clave actual coincide con la clave del usuario logado.
					if(! passwordEncoder.encodePassword(filter.getClaveActual(), null).equals(voTituRepre.getTxClave())){
						FacesUtil.addErrorMessage("cambiarPassword.error.claveActual");
					}else{
						voTituRepre.setTxClave(passwordEncoder.encodePassword(filter.getClaveNueva(), null));					
						usuariosTituRepreService.update(voTituRepre);
						FacesUtil.addOKMessage("cambiarPassword.ok");
					}
				}else if(isAsociado()){
					voAsociado = usuAsociadosService.getById(Long.valueOf(idUsuario));
					//Se comprueba que la clave actual coincide con la clave del usuario logado.
					if(! passwordEncoder.encodePassword(filter.getClaveActual(), null).equals(voAsociado.getTxClave())){
						FacesUtil.addErrorMessage("cambiarPassword.error.claveActual");
					}else{
						voAsociado.setTxClave(passwordEncoder.encodePassword(filter.getClaveNueva(), null));					
						usuAsociadosService.update(voAsociado);
						FacesUtil.addOKMessage("cambiarPassword.ok");
					}
				}
				
			} catch (final Exception e) {
				FacesUtil.addErrorMessage("cambiarPassword.error.update");
				OepmLogger.error(e);
			}		
		}
		return res;
	}
	
	public void setPasswordEncoder(Md5PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public CambioPasswordVO getFilter() {		
		return filter;
	}

	@Override
	public void setFilter(BaseVO filter) {
		this.filter = (CambioPasswordVO)filter;
	}
	
	//Indica si el usuario logado es un agente.
	private boolean isAgente(){
		boolean res= false;
		if(SessionContext.getTipoUsuario().equals( TipoUsuario.AGENTE.name() ) ) {
			res= true;
		}
		return res;
	}
	
	//Indica si el usuario logado es un titular o un representante.
	private boolean isTituRepre(){
		boolean res= false;
		if(SessionContext.getTipoUsuario().equals( TipoUsuario.REPRESENTANTE.name()) || SessionContext.getTipoUsuario().equals( TipoUsuario.TITULAR.name()) ) {
			res= true;
		}
		return res;
	}
	
	//Indica si el usuario logado es un asociado.
	private boolean isAsociado(){
		boolean res= false;
		if(SessionContext.getTipoUsuario().equals( TipoUsuario.ASOCIADO.name() ) ) {
			res= true;
		}
		return res;
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

	public UsuAsociadosService getUsuAsociadosService() {
		return usuAsociadosService;
	}

	public void setUsuAsociadosService(UsuAsociadosService usuAsociadosService) {
		this.usuAsociadosService = usuAsociadosService;
	}

}
