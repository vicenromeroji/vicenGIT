package es.oepm.mao.view.controller;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import es.oepm.mao.constants.MaoPropiedadesConf;
import es.oepm.mao.view.controller.util.JSFPages;


@ManagedBean(name = "verificarCuentaController")
@ViewScoped
public class VerificarCuentaController implements Serializable {

	private static final long serialVersionUID = 6051761069109224348L;

	public VerificarCuentaController() {
		super();		
	}
	
	/**
	 * Redirige al usuario al login con clave
	 * @return
	 */
	public String redireccionarAClave() {
		// Seteamos los atributos en la sesion de JSF ya que no hay todavia usuario logado
		HttpServletRequest httpServletRequest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        
		// Seteamos el atributo de sesion para que se realice la redirecion a clave
		httpServletRequest.getSession().setAttribute(MaoPropiedadesConf.CLAVE_REALIZAR_LLAMADA, Boolean.TRUE.toString());
		
		// Seteamos el atributo que indica que realizamos la llamada desde la pantalla de login
		httpServletRequest.getSession().setAttribute(MaoPropiedadesConf.CLAVE_URL_LLAMADA, MaoPropiedadesConf.CLAVE_URL_LLAMADA_VERIFICAR);
		
		// Redirigimos a la pantalla que realiza la integracion con la pasarela de Cl@ve
		return JSFPages.PASARELA_CLAVE;
	}
}
