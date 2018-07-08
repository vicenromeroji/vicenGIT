package es.oepm.mao.business.service.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.dxd.oepm.repositorio.webservicesfg.DatosDocumentoFG;
import es.dxd.oepm.repositorio.webservicesfg.MetadatoEspecificoFG;
import es.dxd.oepm.repositorio.webservicesfg.OutputBeanFG;
import es.oepm.busmule.ws.client.ceo.documentos.BusDocumento;
import es.oepm.busmule.ws.client.ceo.documentos.IWSBusConsultaDocumentosExpediente;
import es.oepm.busmule.ws.client.ceo.documentos.WSBusConsultaDocumentosExpediente;
import es.oepm.busmule.ws.client.ceo.documentos.parameters.BusConsultarDocumentosExpedienteRequest;
import es.oepm.busmule.ws.client.ceo.documentos.parameters.BusConsultarDocumentosExpedienteResponse;
import es.oepm.ceo.ws.detalleexp.parameters.DetalleExpedienteResponse;
import es.oepm.core.business.vo.DocumentoExpedienteVO;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.core.util.DateUtils;
import es.oepm.core.util.StringUtils;
import es.oepm.mao.business.service.DocumentosService;
import es.oepm.mao.business.service.TrazaGestorService;
import es.oepm.mao.business.vo.BusquedaDocumentosExpedienteResponseVO;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.constants.MaoTrazaGestor;
import es.oepm.wsclient.ucmFGMTOM.DocumentosFGMTOMExpediente;
import es.oepm.wservices.core.util.WSUtils;

@Service(value = "documentosService")
@Transactional(propagation = Propagation.SUPPORTS)
public class DocumentosServiceImpl implements DocumentosService {
	
	private static final long serialVersionUID = 5584311333369974356L;

	@Autowired
	private TrazaGestorService trazaGestorService;

	private final String METADATO_ESPECIFICO_FG_LABEL_MIMETYPE = "dFormat";
	private final String METADATO_ESPECIFICO_FG_LABEL_FECHADOCUMENTO = "xFECDOC";
	
	/**
	 * Bean del mapeador de objetos.
	 */
	@Autowired
	private transient DozerBeanMapper dozerBeanMapper;

	//private static Logger log = Logger.getLogger(ExpedientesServiceImpl.class);

	private transient IWSBusConsultaDocumentosExpediente clienteWS = null;

	private transient Object lockWsConsultaDocExp = new Object();

	public DocumentosServiceImpl() {
	}

	/**
	 * Crear el Cliente de WS de Consulta de documentos expediente.
	 * 
	 * @throws BusinessException
	 */
	private void comprobarClienteConsultaDocumentosExpediente() throws BusinessException {
		String endpoint = "";
		try {
			if (clienteWS == null) {
				synchronized (lockWsConsultaDocExp) {
					if (clienteWS == null) {
						endpoint = Configuracion
								.getPropertyAsString("url.bus.consultaDocsExp");
						//endpoint = "http://localhost:8081/busmule/WSBusConsultaDocumentosExpediente?wsdl";
						
						int receiveTimeout = Configuracion.getPropertyAsInteger( "ws.ucm.recieveTimeout" );
						int connectionTimeout = Configuracion.getPropertyAsInteger( "ws.ucm.connectionTimeout" );
						
						if (!StringUtils.isEmptyOrNull(endpoint)) {
							WSBusConsultaDocumentosExpediente ss = new WSBusConsultaDocumentosExpediente(
									new URL(endpoint));
							clienteWS = ss.getWSBusConsultaDocumentosExpediente();

							WSUtils.setBindingEndpoint( clienteWS, endpoint );
							WSUtils.setBindingTimeouts( clienteWS, receiveTimeout, connectionTimeout );
						} else
							OepmLogger
									.error("Error al instanciar el wsclient de Documentos. No esta definida en la configuración la variable url.bus.consultaDocsExp");
					}
				}
			}
		} catch (MalformedURLException e) {
			clienteWS = null;
			OepmLogger.error(
					"Error al instanciar el wsclient de Documentos para la dirección "
							+ endpoint, e);
		}
	}
	
	/*
	 * (non-Javadoc) 
	 * @see es.oepm.mao.business.service.DocumentosService#buscarDocumentosExpediente
	 * (java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.util.Date)
	 */
	@Override
	public BusquedaDocumentosExpedienteResponseVO buscarDocumentosExpediente(
			String modalidad, String numeroSolicitud, String tipoPersona,
			Boolean usuarioAnonimo, Date fechaPublicacion) throws BusinessException {
		BusquedaDocumentosExpedienteResponseVO result = null;
		
		try {
			// Comprobamos el cliente del WS
			comprobarClienteConsultaDocumentosExpediente();

			// Seteamos los parametros de busqueda
			BusConsultarDocumentosExpedienteRequest request = new BusConsultarDocumentosExpedienteRequest();
			request.setFechaPublicacion(DateUtils.parseDateToXMLGregCal(fechaPublicacion));
			//Pasamos la modalidad DA a D siempre porque en BD solo existen D
			String modalidadParaUCM = modalidad.replaceAll("DA", "D");
			request.setModalidad(modalidadParaUCM);
			request.setNumeroSolicitud(numeroSolicitud);
			request.setTipoPersona(tipoPersona);
			request.setUsuarioAnonimo(false);
			
			/**
			 * CEO-861
			 */
			//request.setUsuario(Configuracion.getPropertyAsString("WSECURITY.WSUSER"));
			//request.setPass(Configuracion.getPropertyAsString("WSECURITY.WSPASSWORD"));
			
			// Escribimos la traza del usuario gestor
			if (SessionContext.getTipoUsuario().equals(TipoUsuario.GESTOR.toString())) {
				// Seteamos los datos del expediente
				StringBuilder detalle = new StringBuilder();
				detalle.append("Expediente: ");
				detalle.append(modalidad).append(numeroSolicitud);
				trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_DET_EXP_DOC, detalle.toString());
			}
			
			BusConsultarDocumentosExpedienteResponse response = clienteWS.consultarDocumentosExpediente(request);
			
			// Comprobamos la respuesta
			if (response == null || response.getResultado() != DetalleExpedienteResponse.RESULTADO_OK) {
				ExceptionUtil.throwBusinessException(ExceptionUtil.GENERIC_ERROR_KEY);
			}
			
			// Mapeamos el resultado
			result = new BusquedaDocumentosExpedienteResponseVO();
			List<DocumentoExpedienteVO> documentos = null;
			if (response != null) {
				result.setMensajes(response.getMensajes());
				if(response.getDocumentos() != null) {
					// Inicializamos la lista
					documentos = new ArrayList<DocumentoExpedienteVO>();
					// Recoremos la lista de respuesta y mapeamos los objetos
					for (BusDocumento documento : response.getDocumentos()) {
						DocumentoExpedienteVO documentoExpedienteVO = dozerBeanMapper
								.map(documento, DocumentoExpedienteVO.class);
						documentos.add(documentoExpedienteVO);
					}
					result.setDocumentos(documentos);
				}
			}
			
		} catch (Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.DocumentosService#recuperarDocumentoEspecificoFG(java.lang.String)
	 */
	@Override
	public DocumentoExpedienteVO recuperarDocumentoEspecificoFG(String idContenido) throws BusinessException {
		DocumentoExpedienteVO documento = null;
		OutputBeanFG respuesta = null;

		try {
			// Escribimos la traza del usuario gestor
			if (SessionContext.getTipoUsuario() != null
					&& SessionContext.getTipoUsuario().equals(TipoUsuario.GESTOR.toString())) {
				// Seteamos los datos del expediente
				StringBuilder detalle = new StringBuilder();
				detalle.append("IdContenido : ");
				detalle.append(idContenido);
				trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_DET_EXP_DESC_DOC, detalle.toString());
			}
			DocumentosFGMTOMExpediente wsclient = new DocumentosFGMTOMExpediente();
			wsclient.setUrlWSUCMFGMTOM(Configuracion
					.getPropertyAsString("URL_WS_WUCMFGMTOM"));
			//wsclient.setUrlWSUCMFGMTOM("http://localhost:8091/busmule/UCMFGMTOMWS?wsdl");
			String receiveTimeout = Configuracion.getPropertyAsString( "ws.ucmFGMTOM.recieveTimeout" );
			String connectionTimeout = Configuracion.getPropertyAsString( "ws.ucmFGMTOM.connectionTimeout" );
			wsclient.setReceiveTimeout(receiveTimeout);
			wsclient.setConnectionTimeout(connectionTimeout);
			wsclient.setUser(Configuracion
					.getPropertyAsString("USER_WS_WUCMFGMTOM"));
			//wsclient.setUser("App_CEO_GD_DES");
			respuesta = wsclient.recuperarDocumentoEspecificoFG(idContenido);
		} catch (final Exception e) {
			ExceptionUtil.throwBusinessException(e);
		}

		if (respuesta == null) {
			ExceptionUtil.throwBusinessException("Error al invocar el WS de UCM");
		}
		else if (respuesta !=null && "1".equals(respuesta.getResultado())) {
			tratamientoExcepcionWSFG(respuesta);
		} else {
			try {
				if (respuesta.getListaDatosDoc() != null) {
					documento = parsearDocumentoFG(respuesta.getListaDatosDoc()[0]);
				} else {
					tratamientoExcepcionWSFG(respuesta);
				}

			} catch (final Exception e) {
				ExceptionUtil.throwBusinessException(e);
			}

		}
		return documento;
		
	}
	
	private DocumentoExpedienteVO parsearDocumentoFG(DatosDocumentoFG datosDocumento) {
		Pattern p = Pattern.compile("\\S\\d+_FIG_\\d{4}[-,_]\\d{1,2}[-,_]\\d{1,2}_(\\d{2}|A-J|X)\\+(\\d{2})\\.\\S+");
		Matcher m;
		
		MetadatoEspecificoFG[] metadatosEspecificos = datosDocumento.getMetaEsp();
		DocumentoExpedienteVO documento = new DocumentoExpedienteVO();
		String idContenido = datosDocumento.getMetaGen().getIdContenido();
		documento.setIdContenido(idContenido);
		HashMap<String, String> metadatos = new HashMap<String, String>();
		String title = "";
		String desc = "";		
		for (int j = 0; j < metadatosEspecificos.length; j++) {
			MetadatoEspecificoFG meta = metadatosEspecificos[j];
			metadatos.put(meta.getNombre(), meta.getValor());
			// Guardamos el title
			if (meta.getNombre().equalsIgnoreCase("dDocTitle")) {
				title = meta.getValor();
				m = p.matcher(title);
				// Si el title se adapta al pattern lo modificamos
				if (m.matches()) {
					title = "Diseño " + m.group(1) + " / " + m.group(2);
					// Aprovecho para guardar el COD_VARIANTE en los metadatos del documento
					// para usarlo en filtrarDocumentosSinPublicacionConcesion
					documento.setNumeroDiseno(m.group(1));
				}
			} else if (meta.getNombre().equalsIgnoreCase("xDESCDOC")){
				desc = meta.getValor();
			} else if (meta.getNombre().equalsIgnoreCase(METADATO_ESPECIFICO_FG_LABEL_FECHADOCUMENTO)){
				if(!StringUtils.isEmptyOrNull(meta.getValor())){
						documento.setFecha(meta.getValor());
				}
			} else if (meta.getNombre().equalsIgnoreCase("xNUMPAGS")){
				documento.setNumeroPaginas(meta.getValor());
			} else if (meta.getNombre().equalsIgnoreCase("xCODDOC")){
				documento.setIdCoddoc(meta.getValor());
			} else if (meta.getNombre().equalsIgnoreCase("xANONIMIZADO")){
				documento.setAnonimizado(meta.getValor());
			} else if (meta.getNombre().equalsIgnoreCase("xMODALIDAD")){
				documento.setModalidad(meta.getValor());
			} else if (meta.getNombre().equalsIgnoreCase("xEXPMAESTRO")){
				documento.setNumero(meta.getValor());
			}
		}
		
		// Comprobamos si es figura para asignar el nombre
		if ("FIG".equals(documento.getIdCoddoc()))
			documento.setNombre("- " + desc + ": " + title);
		else
			documento.setNombre(desc);
		
		// Datos específicos del documento
		DataHandler dataHandler = datosDocumento.getDocumento().getContenido();
		documento.setContenido(dataHandler);
		documento.setNombre(datosDocumento.getDocumento().getNombre());
		
		documento.setMimetipe(getMetadatoEspecificoFG(metadatosEspecificos, METADATO_ESPECIFICO_FG_LABEL_MIMETYPE));
		documento.setFecha(getMetadatoEspecificoFG(metadatosEspecificos, METADATO_ESPECIFICO_FG_LABEL_FECHADOCUMENTO));
		
		documento.setMetadatos(metadatos);
		return documento;
	}
	
	private void tratamientoExcepcionWSFG(OutputBeanFG respuesta) throws BusinessException{
		Exception e = new Exception(
				"Excepcion: "
						+ respuesta.getListaDatosDoc()[0]
								.getMetaEsp()[0]
								.getNombre()
						+ ": "
						+ respuesta.getListaDatosDoc()[0]
								.getMetaEsp()[0]
								.getValor());
		
		ExceptionUtil.throwBusinessException(e);
	}
	
	private String getMetadatoEspecificoFG(MetadatoEspecificoFG[] lista, String campo){
		for (MetadatoEspecificoFG metadatoEspecificoFG : lista) {
			if(metadatoEspecificoFG.getNombre().compareTo(campo) == 0)
				return metadatoEspecificoFG.getValor();
		}
		return null;
	}
	
}