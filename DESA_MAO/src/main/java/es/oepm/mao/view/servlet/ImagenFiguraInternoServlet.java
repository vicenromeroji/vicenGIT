package es.oepm.mao.view.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;

import es.oepm.core.business.vo.DocumentoExpedienteVO;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.util.CargaImagenUtils;
import es.oepm.mao.business.service.DocumentosService;
import es.oepm.mao.business.service.ExpedientesService;

/**
 * 
 * Servlet para servir las imágenes de figuras usado sólo por el servidor.
 *
 */
@Component("imagenFiguraInternoServlet") 
public class ImagenFiguraInternoServlet implements  HttpRequestHandler, Serializable {

	private static final long serialVersionUID = -1531670132438190342L;

	public static final String RUTA_SERVLET = "/jsp/privado/ImagenFiguraInternoServlet";
	
	private static final String RUTA_IMAGEN_ND = "/images/imageNotAvailable.png";
	private static final String RUTA_IMAGEN_ERROR = "/images/errorLoadImage.png";
	private static final String RUTA_IMAGEN_ND_THUMBNAIL = "/images/imageNotAvailableThumbnail.png";
	private static final String RUTA_IMAGEN_ERROR_THUMBNAIL = "/images/errorLoadImageThumbnail.png";
	private static final String NOMBRE_IMAGEN_ND = "imageNotAvailable.png";
	private static final String NOMBRE_IMAGEN_ERROR = "errorLoadImage.png";
	private static final String MIMETYPE_PNG = "image/png";
	private static final String EXTENSION_PNG = "image/png";
	
	@Autowired
	private DocumentosService documentosService;
	
	@Autowired
	private ExpedientesService expedientesService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.HttpRequestHandler#handleRequest(javax.servlet
	 * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		OepmLogger.info("Entada en el servlet ImagenFiguraInternoServlet");
        // Recogemos los parámetros de la llamada
        String idContenido = request.getParameter("idContenido");
        String modalidad = request.getParameter("modalidad");
        String numero = request.getParameter("numero");
        String numeroDiseno = request.getParameter("numeroDiseno");
        Boolean thumbnail = Boolean.valueOf(request.getParameter("thumbnail"));
		OepmLogger.debug("Parámetros: idContenido: " + idContenido + " modalidad : "
				+ modalidad + " numero: " + numero + " numeroDiseno: "
				+ numeroDiseno + " thumbnail: " + thumbnail);
		OepmLogger.info("Remote address: " + request.getRemoteAddr());
		// Recuperamos la figura para comprobar si es visible
		if (idContenido != null && !idContenido.trim().equals("")) {
			try {
				// Recuperamos la figura
				DocumentoExpedienteVO figura = documentosService
						.recuperarDocumentoEspecificoFG(idContenido);
				
				//Pasamos la modalidad DA a D siempre porque en BD solo existen D
				String modalidadParaUCM = modalidad.replaceAll("DA", "D");			
				// Comprobamos si pertenece al expediente
				if (figura.getModalidad().equals(modalidadParaUCM)
						&& figura.getNumero().equals(numero)
						&& figura.getNumeroDiseno().equals(numeroDiseno)
						&& figura.getIdCoddoc().equals("FIG")) {
					if (thumbnail) {
						// Devolvemos la miniatura
						InputStream imagenRedimensionadaIS = CargaImagenUtils
								.redimensionarImagenEnMemoria(figura.getContenido()
										.getInputStream());
						byte[] imageContent = IOUtils
								.toByteArray(imagenRedimensionadaIS);
						componerRespuesta(response, figura.getNombre() + EXTENSION_PNG,
								MIMETYPE_PNG, imageContent);
					} else {
						// Devolvemos la imagen completa
						byte[] imageContent = IOUtils.toByteArray(figura.getContenido()
								.getInputStream());
						componerRespuesta(response, figura.getNombre(),
								figura.getMimetipe(), imageContent);
					}
				} else {
					// Devolvemos la imagen no disponible
					cargarRespuestaImagenNoDisponible(response, thumbnail);
				}
			} catch (Exception e) {
				OepmLogger.error(e);
				cargarRespuestaImagenError(response, thumbnail);
			}
		}
		
		OepmLogger.info("Fin del servlet ImagenFiguraInternoServlet");
	}
	
	/**
	 * Carga la imagen no disponible y la setea en la respuesta.
	 * 
	 * @param response
	 *            Respuesta servlet.
	 * @param thumbnail
	 *            Booleano que indica si se ha de devolver en miniatura.
	 * @throws IOException
	 */
	private void cargarRespuestaImagenNoDisponible(HttpServletResponse response, Boolean thumbnail) throws IOException {
		if (thumbnail) {
			// Devolvemos imagen no disponible en miniatura
			byte [] imageContent = CargaImagenUtils.recuperarImagenInterna(RUTA_IMAGEN_ND_THUMBNAIL);
			componerRespuesta(response, NOMBRE_IMAGEN_ND, MIMETYPE_PNG, imageContent);
		} else {
			// Devolvemos imagen no disponible
			byte [] imageContent = CargaImagenUtils.recuperarImagenInterna(RUTA_IMAGEN_ND);
			componerRespuesta(response, NOMBRE_IMAGEN_ND, MIMETYPE_PNG, imageContent);
		}
	}
	
	/**
	 * Carga la imagen de error y la setea en la respuesta.
	 * 
	 * @param response
	 *            Respuesta servlet.
	 * @param thumbnail
	 *            Booleano que indica si se ha de devolver en miniatura.
	 * @throws IOException
	 */
	private void cargarRespuestaImagenError(HttpServletResponse response, Boolean thumbnail) throws IOException {
		if (thumbnail) {
			// Devolvemos imagen no disponible en miniatura
			byte [] imageContent = CargaImagenUtils.recuperarImagenInterna(RUTA_IMAGEN_ERROR_THUMBNAIL);
			componerRespuesta(response, NOMBRE_IMAGEN_ERROR, MIMETYPE_PNG, imageContent);
		} else {
			// Devolvemos imagen no disponible
			byte [] imageContent = CargaImagenUtils.recuperarImagenInterna(RUTA_IMAGEN_ERROR);
			componerRespuesta(response, NOMBRE_IMAGEN_ERROR, MIMETYPE_PNG, imageContent);
		}
	}
	
	/**
	 * Compone la respuesta del servlet
	 * 
	 * @param response
	 *            Response del servlet
	 * @param filename
	 *            Nombre del archivo
	 * @param mimetype
	 *            Tipo mime del archivo
	 * @param imageContent
	 *            Contenido de la imagen
	 * @throws IOException
	 */
	private void componerRespuesta(HttpServletResponse response, String filename,
			String mimetype, byte [] imageContent) throws IOException {
		response.setHeader("Content-Type", mimetype);
		response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
        response.setHeader("Content-Length", String.valueOf(imageContent.length));
        response.getOutputStream().write(imageContent);
	}

}
