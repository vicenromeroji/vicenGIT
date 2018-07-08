package es.oepm.mao.ws.results;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


import es.oepm.mao.ws.beans.Agente;
import es.oepm.wservices.core.BaseWSResponse;
import es.oepm.wservices.core.mensajes.Mensajes;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConsultaAgenteResponse", namespace = "http://results.usuariosws.ws.mao.oepm.es/")
public class ConsultaAgenteResponse extends BaseWSResponse {

	private static final long serialVersionUID = -7839735461801241343L;
	
	private Agente agente;
	
	public ConsultaAgenteResponse() {
		super();
	}
	
	public ConsultaAgenteResponse( int resultado, Mensajes[] mensajes ) {
		super( resultado, mensajes );
	}

	public Agente getAgente() {
		return agente;
	}

	public void setAgente( Agente agente ) {
		this.agente = agente;
	}

}