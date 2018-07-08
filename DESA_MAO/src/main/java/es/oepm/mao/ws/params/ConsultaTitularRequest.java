package es.oepm.mao.ws.params;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import es.oepm.wservices.core.BaseWSRequest;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConsultaTitularRequest", namespace = "http://params.usuariosws.ws.mao.oepm.es/")
public class ConsultaTitularRequest extends BaseWSRequest {

	private static final long serialVersionUID = -4147461004283234122L;
	
	private String documento;

	public String getDocumento() {
		return documento;
	}

	public void setDocumento( String documento ) {
		this.documento = documento;
	}

}