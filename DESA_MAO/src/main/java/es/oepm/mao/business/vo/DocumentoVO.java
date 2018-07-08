package es.oepm.mao.business.vo;

import java.io.Serializable;

import es.oepm.busmule.ws.client.ceo.documentos.BusDocumento;

/**
 * DocumentoVO para Documentos resultados de bus
 * @author AYESA AT
 *
 */
public class DocumentoVO extends BusDocumento implements Serializable {

	private static final long serialVersionUID = 2545065428662552320L;

	public DocumentoVO(BusDocumento doc) {
		this.setAnonimizado(doc.getAnonimizado());
		this.setContenido(doc.getContenido());
		this.setFecha(doc.getFecha());
		this.setIdCoddoc(doc.getIdCoddoc());
		this.setIdContenido(doc.getIdContenido());
		this.setMetadatos(doc.getMetadatos());
		this.setMimetipe(doc.getMimetipe());
		this.setNombre(doc.getNombre());
		this.setNumeroPaginas(doc.getNumeroPaginas());
	}
}
