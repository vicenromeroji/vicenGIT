package es.oepm.mao.view.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;








import es.oepm.core.business.BaseVO;
import es.oepm.core.business.vo.AdjuntoVO;
import es.oepm.core.business.vo.MensajeVO;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.service.MensajesService;
import es.oepm.core.session.SessionContext;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.business.service.TrazaGestorService;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;
import es.oepm.mao.constants.MaoTrazaGestor;
import es.oepm.mao.view.controller.util.JSFPages;

/**
 * 
 * Controller para consulta de una comunicación.
 *
 */
@ManagedBean(name = "comunicacionController")
@ViewScoped
public class ComunicacionController extends BaseController {
	
	private static final long serialVersionUID = -3768407138213520422L;
	
	private String solModalidad;
	private String solNumero;
	private boolean fromDetExp;
	
	private String idComunicacion;
	private MensajeVO filter;
	
	@ManagedProperty( "#{mensajesService}" )
	private MensajesService mensajesService;
	
	public void setMensajesService( MensajesService mensajesService ) {
		this.mensajesService = mensajesService;
	}
	
	@ManagedProperty( "#{trazaGestorService}" )
	private TrazaGestorService trazaGestorService;
	
	/**
	 * @param trazaGestorService the trazaGestorService to set
	 */
	public void setTrazaGestorService(TrazaGestorService trazaGestorService) {
		this.trazaGestorService = trazaGestorService;
	}
	
	/**
	 * Constructor.
	 */
	public ComunicacionController() {
		idComunicacion = FacesUtil.getParameter( "idComunicacion" );
		
		solModalidad = FacesUtil.getParameter( "solModalidad" );
		solNumero = FacesUtil.getParameter( "solNumero" );
		fromDetExp = Boolean.valueOf( FacesUtil.getParameter( "fromDetExp" ) );
	}
	
	public String getSolModalidad() {
		return solModalidad;
	}
	
	public String getSolNumero() {
		return solNumero;
	}
	
	public boolean getFromDetExp() {
		return fromDetExp;
	}

	@Override
	public MensajeVO getFilter() {
		if( ( filter == null || filter.getId() == null ) && idComunicacion != null ) {
			try {
				// Si se trata de un usuario gestor insertamos la traza
				if (SessionContext.getTipoUsuario().equals(TipoUsuario.GESTOR.name())) {
					interGestorTraze();
				}
				
				filter = mensajesService.recuperarComunicacionById( idComunicacion );
			} catch( Exception e ) {
				OepmLogger.error(e);
				FacesUtil.addErrorMessage( "comynot.comunicaciones.error.recuperarComunicacion" );
			}
		}
		
		return filter;
	}
	
	/**
	 * Inserta la traza del usuario gestor.
	 * @throws BusinessException 
	 */
	private void interGestorTraze() throws BusinessException {
		StringBuilder detalle = new StringBuilder();
		if (fromDetExp) {
			detalle.append("Expediente: ").append(solModalidad)
					.append(solNumero).append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}
		detalle.append("idComunicacion: ").append(idComunicacion);
		
		trazaGestorService.insertTraze(MaoTrazaGestor.DETALLE_CONSULTA_COM, detalle.toString());
	}

	@Override
	public void setFilter( BaseVO filter ) {
		this.filter = ( MensajeVO )filter;
	}
	
	public String back() {
		return JSFPages.COMYNOT_COM_LIST;
	}

//	public void actionDownloadAdjunto( int index ) {
//		if( filter != null && filter.getAdjuntos() != null && index < filter.getAdjuntos().size() ) {
//			AdjuntoVO adjunto = filter.getAdjuntos().get( index );
//			
//			FacesContext fc = FacesContext.getCurrentInstance();
//			ExternalContext ec = fc.getExternalContext();
//			
//			try {
//				ec.responseReset();
//				ec.setResponseContentType( "application/octet-stream" );
//				ec.setResponseContentLength( ( int )adjunto.getTamanio() );
//				ec.setResponseHeader( "Content-Disposition", String.format( "attachment; filename=%s", adjunto.getNombre() ) );
//				
//				OutputStream os = ec.getResponseOutputStream();
//				
//				os.write( adjunto.getContenido() );
//				os.flush();
//				os.close();
//				
//				fc.responseComplete();
//			} catch (IOException e) {
//				FacesUtil.addErrorMessage( "comynot.notificaciones.error.descargarAdjunto" );
//			}
//		}
//	}
	
	public StreamedContent actionDownloadAdjunto( int index ) {
		StreamedContent file = null;
		
		if( filter != null && filter.getAdjuntos() != null && index < filter.getAdjuntos().size() ) {
			AdjuntoVO adjunto = filter.getAdjuntos().get( index );
			
			try {
				InputStream stream = new ByteArrayInputStream( adjunto.getContenido() );
					
				file = new DefaultStreamedContent( stream, "application/octet-stream", adjunto.getNombre() );
			} catch( Exception e ) {
				FacesUtil.addErrorMessage( "comynot.comunicaciones.error.descargarAdjunto" );
			}
		}
		
        return file;  
    }
	
}