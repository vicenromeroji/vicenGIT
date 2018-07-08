package es.oepm.mao.ws.params;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import es.oepm.wservices.core.BaseWSRequest;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConsultaAgenteRequest", namespace = "http://params.usuariosws.ws.mao.oepm.es/")
public class ConsultaAgenteRequest extends BaseWSRequest {

	private static final long serialVersionUID = 7358255549346206503L;
	
	private String codigo;

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo( String codigo ) {
		this.codigo = codigo;
	}

}