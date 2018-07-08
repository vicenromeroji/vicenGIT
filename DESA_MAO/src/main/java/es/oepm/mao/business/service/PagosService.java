package es.oepm.mao.business.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import es.oepm.busmule.ws.client.ceo.BusExpediente;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.mao.business.vo.CabeceraPasarelaVO;
import es.oepm.mao.business.vo.PagoEsperadoVO;
import es.oepm.mao.business.vo.PagoPrevioVO;
import es.oepm.mao.business.vo.PagoRealizadoVO;
import es.oepm.pasarela2WS.ws.client.entity.ResultEncriptaTasasPost;
import es.oepm.pasarela2WS.ws.client.entity.ResultGetIdSesion;
import es.oepm.sireco.webservice.FiltroPagosExpedientes;
import es.oepm.sireco.webservice.OrdenConsulta;
import es.oepm.sireco.ws.SirecoService.beans.RespuestaConsultaTasasPasarela;
import es.oepm.sireco.ws.SirecoService.beans.RespuestaDatosPagoTotal;
import es.oepm.sireco.ws.SirecoService.beans.RespuestaPagosLibresMao;
import es.oepm.sireco.ws.SirecoService.beans.RespuestaTasasMao;

public interface PagosService extends Serializable {

	public RespuestaDatosPagoTotal getPagosRealizados( String[] expedientes, int pagina, int resultsPorPagina, FiltroPagosExpedientes filtro, OrdenConsulta orden ) throws BusinessException;
	public RespuestaPagosLibresMao getPagosPrevios( String codigoAgente, String nifTitular, String loginMao, int pagina, int resultsPorPagina ) throws BusinessException;
	public RespuestaTasasMao getTasa( Date fecha, String codigo, String descripcion, Modalidad modalidad ) throws BusinessException;
	public RespuestaConsultaTasasPasarela getTasas() throws BusinessException;
	public List<PagoPrevioVO> parsePagosLibresMao( RespuestaPagosLibresMao resPagosLibre );
//	public List<PagoPrevioVO> parsePagosLibresMao( RespuestaPagosLibresMao resPagosLibre, boolean useCache );
	public List<PagoRealizadoVO> parsePagosRealizados( final RespuestaDatosPagoTotal respuestaDatosPagoTotal );
//	public List<PagoEsperadoVO> getPagosEsperados( String modalidad, String solicitud, String publicacion ) throws BusinessException;
	public List<PagoEsperadoVO> getPagosEsperados( BusExpediente busExpediente ) throws BusinessException;
//	public ResultConsultaDatosExpedienteVO getPagosEsperadosSolicitud( String modalidad, String solicitud ) throws BusinessException;
//	public ResultConsultaDatosExpedienteVO getPagosEsperadosPublicacion( String modalidad, String publicacion ) throws BusinessException;
//	public List<PagoEsperadoVO> processPagoEsperado( DatosExpedienteVO datosExpediente );
//	public String getSessionCodeUrlPasarelaAEAT();
	
	public String parseModalidad( Modalidad modalidad );
	public String parseModalidad( String modalidad );
	
	/**
	 * Obtiene el id de sesion necesario para realizar los pagos mediante la pasarela de pagos.
	 * 
	 * @return
	 * @throws BusinessException
	 */
	public ResultGetIdSesion obtenerIdSesionPasarelaDePago() throws BusinessException;
	
	/**
	 * Obtiene el string codificado de los pagos para enviarselo a la pasarela.
	 * 
	 * @param cabeceraPasarelaVO Objeto con los datos de la cabecera para la llamada al servicio
	 * @param pagoEsperadoVO Objeto con los datos del pago
	 * @return
	 * @throws BusinessException
	 */
	public ResultEncriptaTasasPost obtenerEncriptacionTasas(CabeceraPasarelaVO cabeceraPasarelaVO, PagoEsperadoVO pagoEsperadoVO) throws BusinessException;
}