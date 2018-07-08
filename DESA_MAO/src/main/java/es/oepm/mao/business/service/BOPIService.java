package es.oepm.mao.business.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import es.oepm.bopiws.beans.ResultadoBopiWS;
import es.oepm.bopiws.beans.ResultadoSumarioWS;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.mao.business.vo.BOPIVO;

public interface BOPIService extends Serializable {

	public ResultadoBopiWS getAnotaciones( final Date fecPubDesde, final Date fecPubHasta, final String modalidad, final String solicitud, final String publicacion, final int tomo, final String sumario, final String titular, final String agente, final int page, final int pageSize ) throws BusinessException;
	public ResultadoSumarioWS getSumario( final int tomo ) throws BusinessException;
	public String formatNombreCompletoAgenteByCodigo(int tomo, String codigo);
	public String formatNombreCompletoUsuario(int tomo, String nombre, String apellido1, String apellido2);
	public String formatNombreCompletoRepresentanteTitular(int tomo, String documento, boolean esRepresentante);
	public String formatNombreCompletoAgenteByDocumento(int tomo, String documento);
	public List<BOPIVO> parseBOPIList(ResultadoBopiWS resultadoBopiWS);
	
}
