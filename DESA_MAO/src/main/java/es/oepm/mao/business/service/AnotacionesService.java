package es.oepm.mao.business.service;

import java.io.Serializable;
import java.math.BigDecimal;

import es.oepm.ceo.ws.anotacionesexp.parameters.ConsultaAnotacionBonoAlfaResponse;
import es.oepm.ceo.ws.anotacionesexp.parameters.ConsultaAnotacionesBonoAlfaResponse;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.mao.business.vo.ConsultaBonosFilterVO;

/**
 * Servicio para la consulta de anotaciones de bonos.
 * 
 * @author jugonzalez
 *
 */
public interface AnotacionesService extends Serializable {

	/**
	 * Consulta una anotacion de bono de alfa.
	 * 
	 * @param idAnotacion
	 *            Id de la anotación de bono.
	 * @return
	 * @mensaje WS_ERROR_INVALID_PARAMS
	 * @mensaje WS_ERROR_GENERICO
	 */
	public ConsultaAnotacionBonoAlfaResponse getAnotacionBono(
			BigDecimal idAnotacion) throws BusinessException;

	/**
	 * Consulta anotaciones de bono de alfa para los agentes.
	 * 
	 * @param filter
	 *            Filtro para la búsqueda de bonos.
	 * @return
	 * @mensaje WS_ERROR_INVALID_PARAMS
	 * @mensaje WS_ERROR_GENERICO
	 * @throws BusinessException
	 */
	public ConsultaAnotacionesBonoAlfaResponse getAnotacionesBonoAlfaAgente(
			ConsultaBonosFilterVO filter) throws BusinessException;

	/**
	 * Consulta anotaciones de bono de alfa para los representantes.
	 * 
	 * @param filter
	 *            Filtro para la búsqueda de bonos.
	 * @return
	 * @mensaje WS_ERROR_INVALID_PARAMS
	 * @mensaje WS_ERROR_GENERICO
	 * @throws BusinessException
	 */
	public ConsultaAnotacionesBonoAlfaResponse getAnotacionesBonoAlfaRepresentante(
			ConsultaBonosFilterVO filter) throws BusinessException;

	/**
	 * Consulta anotaciones de bono de alfa para los titulares.
	 * 
	 * @param filter
	 *            Filtro para la búsqueda de bonos.
	 * @return
	 * @mensaje WS_ERROR_INVALID_PARAMS
	 * @mensaje WS_ERROR_GENERICO
	 * @throws BusinessException
	 */
	public ConsultaAnotacionesBonoAlfaResponse getAnotacionesBonoAlfaTitular(
			ConsultaBonosFilterVO filter) throws BusinessException;

}