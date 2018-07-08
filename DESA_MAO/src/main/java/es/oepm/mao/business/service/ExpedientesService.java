package es.oepm.mao.business.service;

import java.io.Serializable;

import es.oepm.busmule.ws.client.ceo.parameters.BusConsultarExpedientesResponse;
import es.oepm.busmule.ws.client.ceo.parameters.BusObtenerEstadosAlfaResponse;
import es.oepm.ceo.ws.detalleexp.parameters.DetalleExpedienteResponse;
import es.oepm.core.business.ceo.vo.CeAlrCesionMvVO;
import es.oepm.core.business.ceo.vo.CeAlrExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeAlrLicenciasMvVO;
import es.oepm.core.business.ceo.vo.CeDirExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeDirTransferCesioMvVO;
import es.oepm.core.business.ceo.vo.CeMnrExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeMnrTransferCesioMvVO;
import es.oepm.core.business.ceo.vo.FiltroBusquedaExpedienteVO;
import es.oepm.core.business.vo.DetalleExpedienteResponseVO;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.mao.view.controller.util.ExpedientesFilter;

public interface ExpedientesService extends Serializable {

	//public CeAlrAnotacionesMvVO findDetExpAnotacionByTipoFk( List<CeAlrAnotacionesMvVO> detExpAnotaciones, String tipoAnotacionFk );
	
	/**
	 * Obtiene la lista de estados de alfa.
	 * 
	 * @return
	 * @throws BusinessException
	 */
	public BusObtenerEstadosAlfaResponse getEstadosAlfa() throws BusinessException;
	
	/**
	 * Realiza una busqueda de expedientes en CEO.
	 * 
	 * @param filter
	 *            Datos con los parámetros de la búsqueda.
	 * @return
	 * @throws BusinessException
	 */
	public BusConsultarExpedientesResponse consultarExpedientes(FiltroBusquedaExpedienteVO filter, boolean isquick) throws BusinessException;
	
	/**
	 * Realiza la búsqueda de los últimos expedientes del usuario.
	 * 
	 * @return
	 * @throws BusinessException
	 */
	public BusConsultarExpedientesResponse consultarUltimosExpedientesUsuario() throws BusinessException;
	
	public BusConsultarExpedientesResponse getExpedientesSolPub(ExpedientesFilter filter) throws BusinessException;
	
	public BusConsultarExpedientesResponse getExpedientesConPagos(ExpedientesFilter filter) throws BusinessException;
	
	public DetalleExpedienteResponse getDetalleExpediente( String idExpediente, String modalidad) throws BusinessException;
	
	/**
	 * Obtiene el detalle de un expediente de invenciones.
	 * 
	 * @param idExpediente
	 *            Id del expediente.
	 * @param modalidad
	 *            Modalidad del expediente.
	 * @return Detalle del expediente.
	 * @throws BusinessException
	 */
	public DetalleExpedienteResponseVO<CeAlrExpedientesMvVO> getDetalleExpedienteInvencion(String idExpediente, String modalidad) throws BusinessException;
	
	/**
	 * Obtiene el detalle de un expediente de cesion invenciones.
	 * 
	 * @param idExpediente
	 *            Id del expediente.
	 * @param modalidad
	 *            Modalidad del expediente.
	 * @return Detalle del expediente.
	 * @throws BusinessException
	 */
	public DetalleExpedienteResponseVO<CeAlrCesionMvVO> getDetalleExpedienteCesion(String idExpediente, String modalidad) throws BusinessException;
	
	/**
	 * Obtiene el detalle de un expediente de licencia invenciones.
	 * 
	 * @param idExpediente
	 *            Id del expediente.
	 * @param modalidad
	 *            Modalidad del expediente.
	 * @return Detalle del expediente.
	 * @throws BusinessException
	 */
	public DetalleExpedienteResponseVO<CeAlrLicenciasMvVO> getDetalleExpedienteLicencia(String idExpediente, String modalidad) throws BusinessException;
	
	/**
	 * Obtiene el detalle de un expediente de marcas.
	 * 
	 * @param idExpediente
	 *            Id del expediente.
	 * @param modalidad
	 *            Modalidad del expediente.
	 * @return Detalle del expediente.
	 * @throws BusinessException
	 */
	public DetalleExpedienteResponseVO<CeMnrExpedientesMvVO> getDetalleExpedienteSitamar(String idExpediente, String modalidad) throws BusinessException;
	
	/**
	 * Obtiene el detalle de un expediente especial de marcas.
	 * 
	 * @param idExpediente
	 *            Id del expediente.
	 * @param modalidad
	 *            Modalidad del expediente.
	 * @return Detalle del expediente.
	 * @throws BusinessException
	 */
	public DetalleExpedienteResponseVO<CeMnrTransferCesioMvVO> getDetalleExpedienteEspecialSitamar(String idExpediente, String modalidad) throws BusinessException;
	
	/**
	 * Obtiene el detalle de un expediente de modelos.
	 * 
	 * @param idExpediente
	 *            Id del expediente.
	 * @param modalidad
	 *            Modalidad del expediente.
	 * @return Detalle del expediente.
	 * @throws BusinessException
	 */
	public DetalleExpedienteResponseVO<CeDirExpedientesMvVO> getDetalleExpedienteSitamod(String idExpediente,String modalidad) throws BusinessException;
	
	/**
	 * Obtiene el detalle de un expediente especial de modelos.
	 * 
	 * @param idExpediente
	 *            Id del expediente.
	 * @param modalidad
	 *            Modalidad del expediente.
	 * @return Detalle del expediente.
	 * @throws BusinessException
	 */
	public DetalleExpedienteResponseVO<CeDirTransferCesioMvVO> getDetalleExpedienteEspecialSitamod(String idExpediente, String modalidad) throws BusinessException; 
}
