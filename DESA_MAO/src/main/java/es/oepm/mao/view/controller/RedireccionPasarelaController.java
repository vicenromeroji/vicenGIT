package es.oepm.mao.view.controller;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.mao.constants.MaoPropiedadesConf;

@ManagedBean( name = "redireccionPasarelaController" )
@ViewScoped
public class RedireccionPasarelaController implements Serializable {
	
	private static final long serialVersionUID = -147848028219742543L;

	private String infoPagos;
	
	private String urlPasarelaPagos;
	
	public RedireccionPasarelaController() {
		super();
		// Recogemos el parametro con la url de la pasarela
		setUrlPasarelaPagos(Configuracion.getPropertyAsString(MaoPropiedadesConf.PASARELA2_URL_POSTACTION));
		// Recogemos el parametro de sesion y lo borramos
		infoPagos = SessionContext.getValue(MaoPropiedadesConf.PASARELA2_INFOPAGOS);
		SessionContext.setValue(MaoPropiedadesConf.PASARELA2_INFOPAGOS, null);
		OepmLogger.info("Enviamos al usuario a la pasarela de pago");
	}

	public String getInfoPagos() {
		return infoPagos;
	}

	public void setInfoPagos(String infoPagos) {
		this.infoPagos = infoPagos;
	}

	public String getUrlPasarelaPagos() {
		return urlPasarelaPagos;
	}

	public void setUrlPasarelaPagos(String urlPasarelaPagos) {
		this.urlPasarelaPagos = urlPasarelaPagos;
	}
	
}