package es.oepm.mao.business.service;

import java.io.Serializable;
import java.util.Date;

import es.oepm.core.business.vo.DocumentoExpedienteVO;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.mao.business.vo.BusquedaDocumentosExpedienteResponseVO;

public interface DocumentosService extends Serializable {

	/**
	 * Obtiene los documentos de un expediente.
	 * 
	 * @param modalidad
	 *            Modalidad del expediente.
	 * @param numeroSolicitud
	 *            Nº de solicitud del expediente.
	 * @param tipoPersona
	 *            Tipo de persona que realiza la consulta.
	 * @param usuarioAnonimo
	 *            Usario anonimo
	 * @param fechaPublicacion
	 *            Fecha de publicacion del expediente.
	 * @return
	 * @throws BusinessException
	 */
	public BusquedaDocumentosExpedienteResponseVO buscarDocumentosExpediente(
			String modalidad, String numeroSolicitud, String tipoPersona,
			Boolean usuarioAnonimo, Date fechaPublicacion) throws BusinessException;

	/**
	 * Obtiene un documento especifico.
	 * 
	 * @param idContenido
	 * @return
	 * @throws BusinessException
	 */
	public DocumentoExpedienteVO recuperarDocumentoEspecificoFG(String idContenido)
			throws BusinessException;
}
