package es.oepm.mao.ws.results;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


import es.oepm.mao.ws.beans.Titular;
import es.oepm.wservices.core.BaseWSResponse;
import es.oepm.wservices.core.mensajes.Mensajes;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConsultaTitularResponse", namespace = "http://results.usuariosws.ws.mao.oepm.es/")
public class ConsultaTitularResponse extends BaseWSResponse {

	private static final long serialVersionUID = 2221413007207139615L;
	
	private Titular titular;
	
	public ConsultaTitularResponse() {
		super();
	}
	
	public ConsultaTitularResponse( int resultado, Mensajes[] mensajes ) {
		super( resultado, mensajes );
	}

	public Titular getTitular() {
		return titular;
	}

	public void setTitular( Titular titular ) {
		this.titular = titular;
	}

}