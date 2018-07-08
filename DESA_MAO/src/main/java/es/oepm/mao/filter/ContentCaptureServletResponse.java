package es.oepm.mao.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ContentCaptureServletResponse extends HttpServletResponseWrapper {
	
	private ByteArrayOutputStream contentBuffer;
	private PrintWriter writer;

	public ContentCaptureServletResponse(HttpServletResponse originalResponse) {
		super(originalResponse);
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (writer == null) {
			contentBuffer = new ByteArrayOutputStream();
			writer = new PrintWriter(contentBuffer);
		}
		return writer;
	}

	public String getContent() {
		writer.flush();
		String xhtmlContent = new String(contentBuffer.toByteArray());
		xhtmlContent = xhtmlContent.replaceAll(
				"<thead.*?\\>|</thead>|\\<tbody.*?\\>|</tbody>", "");
		// Para eliminar los campos hidden
		xhtmlContent = xhtmlContent.replaceAll("<input type=\"hidden\".*?/\\>",
				"");
	
		System.out.println("Contenido despues " + xhtmlContent);

		xhtmlContent =  eliminaNonBreakingSpace(xhtmlContent);
		
		System.out.println("Contenido despues  " + xhtmlContent);
		
		return xhtmlContent;
	}

	private String eliminaNonBreakingSpace(String xhtmlContent) {
		xhtmlContent = xhtmlContent.replaceAll("&"+"nbsp;", " "); 
		xhtmlContent = xhtmlContent.replaceAll(String.valueOf((char) 160), " ");
		xhtmlContent = xhtmlContent.replaceAll("&nbsp;"," ");
		
		return xhtmlContent;
	}
}
