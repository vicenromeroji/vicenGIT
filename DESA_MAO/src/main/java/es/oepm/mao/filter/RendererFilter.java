package es.oepm.mao.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.XMLResource;

import com.itextpdf.text.DocumentException;

import es.oepm.core.logger.OepmLogger;

public class RendererFilter implements Filter, Serializable {
	

	private static final long serialVersionUID = -1089793962812363804L;
	
	private FilterConfig config;

	public void init(FilterConfig config) throws ServletException {
			this.config = config;
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain filterChain) throws IOException, ServletException {
		OepmLogger.debug("Entrada RendererFilter.");
		req.setCharacterEncoding("UTF-8");
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		// Check to see if this filter should apply.
		String renderType = request.getParameter("RenderOutputType");
		if (renderType != null) {
			// Capture the content for this request
			ContentCaptureServletResponse capContent = new ContentCaptureServletResponse(
					response);
			capContent.getWriter();
			filterChain.doFilter(request, capContent);

			try {
				// Parse the XHTML content to a document that is readable by the
				// XHTML renderer.		
				StringReader contentReader = new StringReader(capContent.getContent());
				Document xhtmlContent = XMLResource.load(contentReader).getDocument();

				if (renderType.equals("pdf")) {
					ITextRenderer renderer = new ITextRenderer();

					String contexto = request.getContextPath();
					StringBuffer url = request.getRequestURL();
					String urlString = url.substring(0, url.indexOf(contexto)
							+ contexto.length());
					renderer.setDocument(xhtmlContent, urlString);

					renderer.layout();

					response.setContentType("application/pdf");

					String fileName;

					String modalidad = request.getParameter("modalidad");
					String numeroSolictud = request
							.getParameter("numeroSolictud");
					fileName = modalidad + numeroSolictud;

					response.setHeader("Content-Disposition",
							"inline; filename=" + fileName + ".pdf");

					OutputStream browserStream = response.getOutputStream();
					renderer.createPDF(browserStream);
					return;
				}

			} catch (DocumentException e) {
				throw new ServletException(e);
			}

		} else {
			// Normal processing //request.getCharacterEncoding();
			filterChain.doFilter(request, response);
		}
	}

	public void destroy() {
	}
}
