package es.oepm.mao.business.service.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.bopiws.beans.ResultadoBopiWS;
import es.oepm.bopiws.beans.ResultadoBusqueda;
import es.oepm.bopiws.beans.ResultadoSumarioWS;
import es.oepm.bopiws.service.BopiWSInterface;
import es.oepm.bopiws.service.WsBopiLocator;
import es.oepm.core.business.mao.vo.UsuariosAgenteVO;
import es.oepm.core.business.mao.vo.UsuariosTituRepreVO;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.util.DateUtils;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.business.service.BOPIService;
import es.oepm.mao.business.vo.BOPIVO;
import es.oepm.mao.comun.business.service.UsuariosAgenteService;
import es.oepm.mao.comun.business.service.UsuariosTituRepreService;
import es.oepm.mao.ws.util.CifradoPassword;

@Service( value = "bopiService" )
@Transactional( propagation = Propagation.SUPPORTS )
public class BOPIServiceImpl implements BOPIService {
	
	private static final long serialVersionUID = -644024757253995687L;
	
	private static transient BopiWSInterface wsBopi = null;
	private transient Object lockWsBopi = new Object();
	
	@Autowired
	private UsuariosAgenteService usuariosAgenteService;
	@Autowired
	private UsuariosTituRepreService usuariosTituRepreService;
	
	public BOPIServiceImpl() {
		/*try {
			comprobarCliente();
		} catch (Exception e) {}*/
	}
	

	private void comprobarCliente() throws BusinessException {
		String urlWSBopi="";
		try {
			if (wsBopi == null) {
				synchronized( lockWsBopi ) {
					if( wsBopi == null ) {				
						urlWSBopi=Configuracion.getPropertyAsString("url.bus.bopi");
						//urlWSBopi = "http://preconsultas2.oepm.es/BopiWS/wsBopi?wsdl";
						
						if(!StringUtils.isEmptyOrNull(urlWSBopi)){
							WsBopiLocator wsBopiService = new WsBopiLocator();
							wsBopi = wsBopiService.getBopiWSPort(new URL(urlWSBopi));
							//wsBopi.getSumarioByTomo(1);
						}else {
							FacesUtil.addErrorMessage("error.ws.bopi");
							OepmLogger.error("Error al instanciar el wsclient de bopi. No esta definida en la configuración la variable url.bus.bopi");
						}
					}
				}
			}
		} catch( Exception e ) {
			wsBopi=null;
			FacesUtil.addErrorMessage("error.ws.bopi");
			OepmLogger.error("Error al instanciar el wsclient de bopi para la dirección " + urlWSBopi, e);
			ExceptionUtil.throwBusinessException( e );
		}
	
	}

	public ResultadoSumarioWS getSumario( final int tomo ) throws BusinessException {
		comprobarCliente();
		ResultadoSumarioWS sumario = null;
		
		try {
			sumario = wsBopi.getSumarioByTomo( tomo );
		} catch( Exception re ) {
			ExceptionUtil.throwBusinessException( re );
		}
		
		return sumario;
	}
	
	/* (non-Javadoc)
	 * @see es.oepm.mao.business.service.BOPIService#getAnotaciones(java.util.Date, java.util.Date, java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	public ResultadoBopiWS getAnotaciones( final Date fecPubDesde, final Date fecPubHasta, final String modalidad, final String solicitud, final String publicacion, final int tomo, final String sumario, final String titular, final String agente, final int page, final int pageSize ) throws BusinessException {
		comprobarCliente();
		ResultadoBopiWS anotaciones = null;
		
		try {
			String strFecPubDesde = fecPubDesde != null ? DateUtils.formatFecha( fecPubDesde ) : "";
			String strFecPubHasta = fecPubHasta != null ? DateUtils.formatFecha( fecPubHasta ) : "";
			
			String auxModalidad = modalidad != null ? modalidad : "";
			String auxSolicitud = solicitud != null ? solicitud : "";
			String auxPublicacion = publicacion != null ? publicacion : "";
			Integer auxSumario = sumario != null && !sumario.equals("") ? Integer.valueOf(sumario) : null;
			String auxTitular = titular != null ? titular : "";
			String auxAgente = agente != null ? agente : "";
			String pass = CifradoPassword.getInstance().getPasswordBOPI();
			
			switch( tomo ) {
				case 1:
					if( auxModalidad.equals( Modalidad.MN_LICENCIA.getModalidad() ) ) {
						auxModalidad = "L";
					} else if( auxModalidad.equals( Modalidad.MN_TRANSFERENCIA.getModalidad() ) || auxModalidad.equals( Modalidad.MN_CAMBIO_NOMBRE.getModalidad() ) ) {
						auxModalidad = "F";
					}
					
					anotaciones = wsBopi.getAnotacionAvanzadaTomo1( Configuracion.getPropertyAsString( "wsBopi.usuario" ), 
																	pass, 
																	strFecPubDesde, strFecPubHasta, 
																	auxModalidad, auxSolicitud, auxPublicacion, 
																	"", auxTitular, "", auxAgente,
																	"", "", "", null, null, null, null, auxSumario, page, pageSize );
					break;
				case 2:
					anotaciones = wsBopi.getAnotacionAvanzadaTomo2( Configuracion.getPropertyAsString( "wsBopi.usuario" ), 
																	pass, 
																	strFecPubDesde, strFecPubHasta, 
																	auxModalidad, auxSolicitud, auxPublicacion, 
																	"", auxTitular, "", auxAgente, 
																	"", null, null, null, null, auxSumario, page, pageSize );
					break;
				case 3:
					if( auxModalidad.equals( Modalidad.DI_DIBUJO.getModalidad() ) ) {
						auxModalidad = Modalidad.DI_DISENO.getModalidad();
					} else if( auxModalidad.equals( Modalidad.DI_LICENCIA.getModalidad() ) ) {
						auxModalidad = "L";
					} else if( auxModalidad.equals( Modalidad.DI_CESION.getModalidad() ) || auxModalidad.equals( Modalidad.DI_CAMBIO_NOMBRE.getModalidad() ) ) {
						auxModalidad = "F";
					}
					
					anotaciones = wsBopi.getAnotacionAvanzadaTomo3( Configuracion.getPropertyAsString( "wsBopi.usuario" ), 
																	pass, 
																	strFecPubDesde, strFecPubHasta, 
																	auxModalidad, auxSolicitud, auxPublicacion, 
																	"", auxTitular, "", auxAgente, 
																	"", null, null, null, null, auxSumario, page, pageSize );
					break;
			}
		} catch( Exception re ) {
			ExceptionUtil.throwBusinessException( re );
		}
		
		return anotaciones;
	}
	
	@Override
	public String formatNombreCompletoAgenteByCodigo( final int tomo, final String codigo ) {
		UsuariosAgenteVO filter = new UsuariosAgenteVO();
		filter.setCodAgente( codigo );
		return StringUtils.isEmptyOrNull(codigo) ? null : formatNombreCompletoAgente(tomo, filter);
	}
	
	@Override
	public String formatNombreCompletoAgenteByDocumento( final int tomo, final String documento ) {
		UsuariosAgenteVO filter = new UsuariosAgenteVO();
		filter.setDocumento(documento);
		return StringUtils.isEmptyOrNull(documento) ? null : formatNombreCompletoAgente(tomo, filter);
	}
	
	private String formatNombreCompletoAgente( final int tomo, UsuariosAgenteVO filter) {
		String nombreCompleto = "";
	
		List<UsuariosAgenteVO> usuariosAgente = null;
		
		try {
			usuariosAgente = usuariosAgenteService.search( filter );
		} catch( BusinessException be ) {
			OepmLogger.error( be );
		}
		
		if( usuariosAgente != null && !usuariosAgente.isEmpty() ) {
			UsuariosAgenteVO usuarioAgente = usuariosAgente.get( 0 );
			
			if( usuarioAgente != null ) {
				nombreCompleto = formatNombreCompletoUsuario( tomo, usuarioAgente.getNomAgente(), usuarioAgente.getApe1Agente(), usuarioAgente.getApe2Agente() );
			}
		}
		
		return nombreCompleto;
	}
	
	/**
	 * 
	 * @param tomo
	 * @param documento
	 * @param esRepresentante  true búsqueda por Representante, false búsqueda por Titular
	 * @return
	 */
	@Override
	public String formatNombreCompletoRepresentanteTitular( final int tomo, final String documento, boolean esRepresentante ) {
		String nombreCompleto = "";
		
		UsuariosTituRepreVO filter = new UsuariosTituRepreVO();
		filter.setTxDocumento( documento );
		if (esRepresentante) {
			filter.setBoRepresentante( 'S' );
		} else {
			filter.setBoRepresentante( 'N' );
		}

		List<UsuariosTituRepreVO> usuariosTituRepre = null;
		
		try {
			usuariosTituRepre = usuariosTituRepreService.search( filter );
		} catch( BusinessException be ) {
			OepmLogger.error( be );
		}
		
		if( usuariosTituRepre != null && !usuariosTituRepre.isEmpty() ) {
			UsuariosTituRepreVO usuarioTituRepre = usuariosTituRepre.get( 0 );
			
			if( usuarioTituRepre != null ) {
				nombreCompleto = formatNombreCompletoUsuario( tomo, usuarioTituRepre.getTxNombre(), usuarioTituRepre.getTxApellido1(), usuarioTituRepre.getTxApellido2() );
			}
		}
		
		return nombreCompleto;
	}
	
	@Override
	public String formatNombreCompletoUsuario( final int tomo, String nombre, String apellido1, String apellido2 ) {
		String nombreCompleto = "";
		
		switch( tomo ) {
			case 1:
			case 3:
				nombreCompleto = !StringUtils.isEmptyOrNull( nombre ) ? nombre.trim() : "";
				nombreCompleto += nombreCompleto.length() > 0 ? " " : "";
				nombreCompleto += !StringUtils.isEmptyOrNull( apellido1 ) ? apellido1.trim() : "";
				nombreCompleto += nombreCompleto.length() > 0 ? " " : "";
				nombreCompleto += !StringUtils.isEmptyOrNull( apellido2 ) ? apellido2.trim() : "";
			break;

			default:
				nombreCompleto = !StringUtils.isEmptyOrNull( apellido1 ) ? apellido1.trim() : "";
				nombreCompleto += nombreCompleto.length() > 0 ? " " : "";
				nombreCompleto += !StringUtils.isEmptyOrNull( apellido2 ) ? apellido2.trim() : "";
				nombreCompleto += nombreCompleto.length() > 0 ? ", " : "";
				nombreCompleto += !StringUtils.isEmptyOrNull( nombre ) ? nombre.trim() : "";
			break;
		}
		
		return nombreCompleto.trim();
	}


	

	/* (non-Javadoc)
	 * @see es.oepm.mao.business.service.BOPIService#parseBOPIList(es.oepm.bopiws.beans.ResultadoBopiWS)
	 */
	@Override
	public List<BOPIVO> parseBOPIList(ResultadoBopiWS resultadoBopiWS) {
		List<BOPIVO> bopiList = new ArrayList<BOPIVO>();
		
		if( resultadoBopiWS != null && resultadoBopiWS.getResultado() != null ) {
			for( ResultadoBusqueda resultadoBusqueda : resultadoBopiWS.getResultado() ) {
				BOPIVO bopiVO = new BOPIVO();
				bopiVO.setAnotacion(resultadoBusqueda.getAnotacion());
				bopiVO.setEnlace_ucm(resultadoBusqueda.getEnlace_ucm());
				bopiVO.setSubapartado(resultadoBusqueda.getSubapartado());
				bopiList.add(bopiVO);
			}
		}
		
		return bopiList;	
	}



	
	
}