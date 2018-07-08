package es.oepm.mao.business.vo;

import java.util.List;

import es.oepm.core.business.BaseVO;
import es.oepm.core.business.vo.DocumentoExpedienteVO;
import es.oepm.wservices.core.mensajes.Mensajes;

public class BusquedaDocumentosExpedienteResponseVO extends BaseVO {

	private static final long serialVersionUID = 1955666579909407272L;

	private List<DocumentoExpedienteVO> documentos;

	private Mensajes[] mensajes;

	public BusquedaDocumentosExpedienteResponseVO() {
		super();
	}

	public List<DocumentoExpedienteVO> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<DocumentoExpedienteVO> documentos) {
		this.documentos = documentos;
	}

	public Mensajes[] getMensajes() {
		return mensajes;
	}

	public void setMensajes(Mensajes[] mensajes) {
		this.mensajes = mensajes;
	}

}