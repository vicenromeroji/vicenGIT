package es.oepm.mao.view.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.security.access.annotation.Secured;

import es.oepm.core.business.BaseVO;
import es.oepm.core.business.ceo.vo.CeAlrCesionMvVO;
import es.oepm.core.business.ceo.vo.CeAlrExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeAlrLicenciasMvVO;
import es.oepm.core.business.ceo.vo.CeDirExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeMnrExpedientesMvVO;
import es.oepm.core.business.vo.DocumentoExpedienteVO;
import es.oepm.core.constants.Mensaje;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.constants.Roles;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.service.PdfUtilsService;
import es.oepm.core.service.ZipUtilsService;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.util.DateUtils;
import es.oepm.core.util.ExpedienteUtils;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.business.service.DocumentosService;
import es.oepm.mao.business.vo.BusquedaDocumentosExpedienteResponseVO;
import es.oepm.maoceo.comun.view.constatns.JSFComunPages;
import es.oepm.maoceo.comun.view.controller.IBusquedaDocumentosExpedienteController;
import es.oepm.wservices.core.mensajes.Mensajes;

@ManagedBean(name = "busquedaDocsExpController")
@SessionScoped
@Secured({ Roles.ROLE_MAO_AGENTE, Roles.ROLE_MAO_ASOCIADO,
		Roles.ROLE_MAO_REPRESENTANTE, Roles.ROLE_MAO_TITULAR })
public class BusquedaDocumentosExpedienteController extends BaseController
		implements IBusquedaDocumentosExpedienteController {

	private static final long serialVersionUID = -5904610678586507116L;

	@ManagedProperty( "#{documentosService}" )
	private DocumentosService documentosService;
	
	@ManagedProperty(name = "pdfUtilsService", value = "#{pdfUtilsService}")
	private PdfUtilsService pdfUtilsService;

	@ManagedProperty(name = "zipUtilsService", value = "#{zipUtilsService}")
	private ZipUtilsService zipUtilsService;
	
	public void setDocumentosService(DocumentosService documentosService) {
		this.documentosService = documentosService;
	}
	
	private List<DocumentoExpedienteVO> documentosList;
	private List<DocumentoExpedienteVO> selectedDocuments = new ArrayList<DocumentoExpedienteVO>();
	private String idExpediente;
	private String modalidad;
	private String numeroSolicitud;
	private String tituloDetalle;
	private String fechaPublicacion;
	private String tipoPersona;
	
	private Boolean concatenatePdf = false;

	public BusquedaDocumentosExpedienteController() {
		super();
	}

	public String actionSearch() {
		String target = FacesUtil.getParameter("target");
		boolean isFIG = target != null && "figuras".equals(target);
		documentosList = new ArrayList<DocumentoExpedienteVO>();
		boolean publicable = false;
		
		try {
			Object exp = SessionUtil.getFromSession("detalleExpediente");
		
			Date fechaPublicacionDate = null;
			if(exp instanceof CeMnrExpedientesMvVO ){
				idExpediente = ((CeMnrExpedientesMvVO) exp).getRowId();
				modalidad = ((CeMnrExpedientesMvVO) exp).getModalidad();
				numeroSolicitud = ((CeMnrExpedientesMvVO)exp).getNumero();
				tipoPersona = ((CeMnrExpedientesMvVO)exp).getTitular().getTipoPersona();
				if(((CeMnrExpedientesMvVO)exp).getFechaPublicacion() != null)
					fechaPublicacionDate = DateUtils.parseFecha(((CeMnrExpedientesMvVO)exp).getFechaPublicacion());
				fechaPublicacion = ((CeMnrExpedientesMvVO)exp).getFechaPublicacion();
			} else if(exp instanceof CeAlrExpedientesMvVO ){
				idExpediente = ((CeAlrExpedientesMvVO) exp).getId().toString();
				modalidad = ((CeAlrExpedientesMvVO) exp).getModalidad();
				numeroSolicitud = ((CeAlrExpedientesMvVO)exp).getNumeroSolicitud();
				tipoPersona = ((CeAlrExpedientesMvVO)exp).getTitular().getTipoPersona();
				if(((CeAlrExpedientesMvVO)exp).getFechaPublicacion() != null)
					fechaPublicacion = DateUtils.formatFecha(((CeAlrExpedientesMvVO)exp).getFechaPublicacion());
				fechaPublicacionDate = ((CeAlrExpedientesMvVO)exp).getFechaPublicacion();
			} else if(exp instanceof CeDirExpedientesMvVO ){
				idExpediente = ((CeDirExpedientesMvVO) exp).getId().toString();
				modalidad = ((CeDirExpedientesMvVO) exp).getModalidad();
				numeroSolicitud = ((CeDirExpedientesMvVO)exp).getNumero();
				tipoPersona = ((CeDirExpedientesMvVO)exp).getTitular().getTipoPersona();
				if(((CeDirExpedientesMvVO)exp).getFechaPublicacion() != null)
					fechaPublicacionDate = DateUtils.parseFecha(((CeDirExpedientesMvVO)exp).getFechaPublicacion());
				fechaPublicacion = ((CeDirExpedientesMvVO)exp).getFechaPublicacion();
				publicable = ((CeDirExpedientesMvVO) exp).isPublicable();
			} else if(exp instanceof CeAlrLicenciasMvVO ){
				idExpediente = ((CeAlrLicenciasMvVO) exp).getId().toString();
				modalidad = "L";
				numeroSolicitud = ((CeAlrLicenciasMvVO)exp).getNumeroLicencia();
				tipoPersona = "PF";
				fechaPublicacionDate = new Date();
				fechaPublicacion = DateUtils.formatFecha(fechaPublicacionDate);
			} else if(exp instanceof CeAlrCesionMvVO ){
				idExpediente = ((CeAlrCesionMvVO) exp).getId().toString();
				modalidad = "F";
				numeroSolicitud = ((CeAlrCesionMvVO)exp).getNumeroCesion();
				tipoPersona = "PF";
				fechaPublicacionDate = new Date();
				fechaPublicacion = DateUtils.formatFecha(fechaPublicacionDate);
			}		
			
			
			this.tituloDetalle = (String)SessionUtil.getFromSession("tituloDetalle");
			
			
			//Pasamos la modalidad DA a D siempre porque en BD solo existen D
			String modalidadParaUCM = modalidad.replaceAll("DA", "D");			
			boolean isModalidadDisenos = false;
			if(Modalidad.find(modalidadParaUCM).isSitamod()){
				isModalidadDisenos = true;
			}
			if(isFIG && isModalidadDisenos){
				if(!publicable){
					return JSFComunPages.FIGURAS_LIST;
				}
			}
			
			// Consultamos la lista de documentos del expediente
			BusquedaDocumentosExpedienteResponseVO resultado;
//			resultado = new BusquedaDocumentosExpedienteResponseVO();
			resultado = documentosService.buscarDocumentosExpediente(modalidad, 
					numeroSolicitud, tipoPersona, Boolean.FALSE, fechaPublicacionDate);
			
			// Seteamos los documentos
			documentosList = resultado.getDocumentos();
			if ((this.documentosList == null)
					|| (this.documentosList.isEmpty()))
				FacesUtil.addOKMessage("documento.noDocumentos");
			
			Modalidad mod = Modalidad.find(modalidad);
			if(mod.isSitamod() && !isFIG){
				this.documentosList = filtrarFiguras(this.documentosList);
			} else if (mod.isSitamod() && isFIG) {
				// HCS: MAO-491: Sacamos los documentos que no son figuras
				this.documentosList = soloFiguras(this.documentosList);
			}
			
			// Procesamos los mensajes
			procesarMensajes(resultado.getMensajes());
		} catch (Exception exception) {
			FacesUtil.addErrorMessage("busqueda.error", exception);
		}
		
		if (isFIG) {
			return JSFComunPages.FIGURAS_LIST;
		} else {
			Modalidad mod = Modalidad.find(this.modalidad);			
			if (mod.getSistema() == Modalidad.SITAMOD.getSistema()) {
				return JSFComunPages.PATH_COMUN_EXPEDIENTES + getSistema() + "/documentosExp.xhtml";
			} else if (mod == Modalidad.AL_LICENCIA || mod == Modalidad.AL_TRANSMISION) {
				return JSFComunPages.PATH_COMUN_EXPEDIENTES + getSistema() + "/documentosExpLicCesiones.xhtml";
			} else if (mod.getSistema() == Modalidad.ALFA.getSistema()) {
				return JSFComunPages.PATH_COMUN_EXPEDIENTES + getSistema() + "/documentos/documentosExp.xhtml";
			}
			else return JSFComunPages.DOCUMENTOS_LIST;
		}
	}
	
	/**
	 * Procesa los mensajes como mensajes faces
	 * 
	 * @param mensajes
	 */
	private void procesarMensajes(Mensajes[] mensajes) {
		if (mensajes != null) {
			for (Mensajes mensaje : mensajes) {
				switch (mensaje.getCodigo()) {
				case Mensaje.COD_WS_ERROR_GENERICO:
					FacesUtil.addMensajeMessage(mensaje.getCriticidad(),
							"busqueda.error");
					break;
				}
			}
		}
	}

	private List<DocumentoExpedienteVO> filtrarFiguras(List<DocumentoExpedienteVO> documentosList){
		List<DocumentoExpedienteVO> result = new ArrayList<DocumentoExpedienteVO>();
		for (DocumentoExpedienteVO documentoExpedienteVO : documentosList) {
			if(documentoExpedienteVO.getIdCoddoc().compareTo("FIG") == 0){
				continue;
			}
			result.add(documentoExpedienteVO);
		}
		return result;
	}
	
	/**
	 * Se queda unicamente con los documentos de diseños
	 * @param documentosList
	 * @return
	 */
	private List<DocumentoExpedienteVO> soloFiguras(List<DocumentoExpedienteVO> documentosList){
		List<DocumentoExpedienteVO> result = new ArrayList<DocumentoExpedienteVO>();
		for (DocumentoExpedienteVO documentoExpedienteVO : documentosList) {
			if(documentoExpedienteVO.getIdCoddoc().compareTo("FIG") == 0){
				result.add(documentoExpedienteVO);
			}
		}
		return result;
	}
	
	
	public String actionEdit() {
		return null;
	}

	public String actionDelete() {
		return null;
	}

	public String actionNew() {
		return null;
	}

	private String getSistema() {
		Modalidad mod = Modalidad.find(this.modalidad);
		return mod.getSistema().toString().toLowerCase();
	}

	@Override
	public List<DocumentoExpedienteVO> getDocumentosList() {
		return this.documentosList;
	}

	@Override
	public void setDocumentosList(List<DocumentoExpedienteVO> documentosList) {
		this.documentosList = documentosList;
	}

	public String getIdExpediente() {
		return this.idExpediente;
	}

	public void setIdExpediente(String idExpediente) {
		this.idExpediente = idExpediente;
	}

	@Override
	public StreamedContent getFile(DocumentoExpedienteVO documentoExpediente) {
		String idContenido = documentoExpediente.getIdContenido();
		StreamedContent file = null;
		try {
			DocumentoExpedienteVO documento = this.documentosService
					.recuperarDocumentoEspecificoFG(idContenido);
			file = new DefaultStreamedContent(documento.getContenido()
					.getInputStream(), documento.getMimetipe(),
					documento.getNombre());
		} catch (Exception exception) {
			FacesUtil.addErrorMessage("busqueda.error", exception);
		}

		return file;
	}

	public void setFile(StreamedContent file) {
		//this.file = file;
	}

	public String getTituloDetalle() {
		return tituloDetalle;
	}

	public void setTituloDetalle(String tituloDetalle) {
		this.tituloDetalle = tituloDetalle;
	}

	@Override
	public String getNumeroSolicitud() {
		return this.numeroSolicitud;
	}

	@Override
	public void setNumeroSolicitud(String numeroSolicitud) {
		this.numeroSolicitud = numeroSolicitud;
	}

	@Override
	public String getFechaPublicacion() {
		return fechaPublicacion;
	}

	@Override
	public void setFechaPublicacion(String fechaPublicacion) {
		this.fechaPublicacion = fechaPublicacion;
	}

	@Override
	public String getTipoPersona() {
		return tipoPersona;
	}

	@Override
	public void setTipoPersona(String tipoPersona) {
		this.tipoPersona = tipoPersona;
	}

	@Override
	public String getModalidad() {
		return modalidad;
	}

	@Override
	public void setModalidad(String modalidad) {
		this.modalidad = modalidad;
	}

	@Override
	public BaseVO getFilter() {
		return null;
	}

	@Override
	public void setFilter(BaseVO filter) {
	}
	
	public StreamedContent getFiles(){
		StreamedContent file = null;
		try {
			List<DocumentoExpedienteVO> docsRetrieved = retrieveRemoteFiles(selectedDocuments);
			
			List<DocumentoExpedienteVO> finalDocsToZip;
			finalDocsToZip = concatenatePdf ? concatenatePdfDocuments(docsRetrieved) : docsRetrieved;
			
			InputStream stream = zipUtilsService.zipDocumentoVOList(finalDocsToZip);
			String fileName = zipUtilsService.getFileNameByNSolicitud(modalidad, numeroSolicitud);
			file = new DefaultStreamedContent(stream, ZipUtilsService.APPLICATION_ZIP_MIME_TYPE, fileName, ZipUtilsService.ZIP_CONTENT_ENCODING);
			
		} catch (BusinessException be) {
			FacesUtil.addErrorMessage("GENERIC_ERROR", be);
		} catch (Exception exception) {
			FacesUtil.addErrorMessage("GENERIC_ERROR", exception);
		}

		return file;
	}

	private List<DocumentoExpedienteVO> retrieveRemoteFiles(List<DocumentoExpedienteVO> documentList){
		List<DocumentoExpedienteVO> docsRetrieved = new ArrayList<DocumentoExpedienteVO>();
		for (DocumentoExpedienteVO documentoExpedienteVO : documentList) {
			String idContenido = documentoExpedienteVO.getIdContenido();
			try {
				docsRetrieved.add(this.documentosService.recuperarDocumentoEspecificoFG(idContenido));
			} catch (Exception e) {
				OepmLogger.error("Imposible encontrar y obtener el documento con idContenido: " + idContenido);
			}
		}
		return docsRetrieved;
	}
	
	private List<DocumentoExpedienteVO> concatenatePdfDocuments(List<DocumentoExpedienteVO> documentList) throws BusinessException{
		List<DocumentoExpedienteVO> result = new ArrayList<DocumentoExpedienteVO>();
		List<DocumentoExpedienteVO> pDFDocuments = new ArrayList<DocumentoExpedienteVO>();
		
		//Split array, pdf & other mimeTypes
		for (DocumentoExpedienteVO documentoExpedienteVO : documentList) {
			if(documentoExpedienteVO.getMimetipe().compareTo(PdfUtilsService.APPLICATION_PDF_MIME_TYPE) == 0)
				pDFDocuments.add(documentoExpedienteVO);
			else
				result.add(documentoExpedienteVO);
		}
		
		//Concatenate PDF docs
		if(!pDFDocuments.isEmpty()){
			DocumentoExpedienteVO concatenatedDoc;
			
			concatenatedDoc = pdfUtilsService.concatenatePdfList(pDFDocuments, ExpedienteUtils.getNumeroSolicitudForFileName(modalidad, numeroSolicitud));
			result.add(concatenatedDoc);
		}
		
		return result;
	}

	public Boolean getConcatenatePdf() {
		return concatenatePdf;
	}

	public void setConcatenatePdf(Boolean concatenatePdf) {
		this.concatenatePdf = concatenatePdf;
	}

	public List<DocumentoExpedienteVO> getSelectedDocuments() {
		if(selectedDocuments == null)
			selectedDocuments = new ArrayList<DocumentoExpedienteVO>();
		return selectedDocuments;
	}

	public void setSelectedDocuments(List<DocumentoExpedienteVO> selectedDocuments) {
		this.selectedDocuments = selectedDocuments;
	}

	public PdfUtilsService getPdfUtilsService() {
		return pdfUtilsService;
	}

	public void setPdfUtilsService(PdfUtilsService pdfUtilsService) {
		this.pdfUtilsService = pdfUtilsService;
	}

	public ZipUtilsService getZipUtilsService() {
		return zipUtilsService;
	}

	public void setZipUtilsService(ZipUtilsService zipUtilsService) {
		this.zipUtilsService = zipUtilsService;
	}


}
