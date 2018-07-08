package es.oepm.mao.view;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;


import es.oepm.core.business.vo.MensajeVO;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.service.MensajesService;
import es.oepm.platanot.vo.ResultadoFiltroVO;

public class MensajesLazyDataModel extends LazyDataModel<MensajeVO> implements Serializable {
	
	private static final long serialVersionUID = -1490020364827255814L;
	
	private boolean notificaciones;
	private String documento;
	private String asunto;
	private String origen;
	private String solicitud;
	private String estado;
	
	private MensajesService mensajesService;
	
	private List<MensajeVO> datasource;
	
	public MensajesLazyDataModel( MensajesService mensajesService, boolean notificaciones ) {
		this.mensajesService = mensajesService;
		
		this.notificaciones = notificaciones;
	}
	
	public MensajesLazyDataModel( MensajesService mensajesService, boolean notificaciones, String documento ) {
		this( mensajesService, notificaciones );
		
		this.documento = documento;
	}
	
	public MensajesLazyDataModel( MensajesService mensajesService, boolean notificaciones, MensajeVO filter ) {
		this( mensajesService, notificaciones );
		
		if( filter != null ) {
			if( filter.getDestinatario() != null ) {
				this.documento = filter.getDestinatario().getDocumento();
			}
			
			this.asunto = filter.getAsunto();
			this.origen = filter.getOrigen().getNombre();
			this.solicitud = filter.getSolicitud();
			this.estado = filter.getEstado();
		}
	}

	@Override
	public Object getRowKey( MensajeVO mensaje ) {
		return mensaje.getId();
	}
	
	@Override
    public MensajeVO getRowData( String rowKey ) {
		MensajeVO rowData = null;
		
		for( MensajeVO mensaje : datasource ) {
			if( mensaje.getId().equals( rowKey ) ) {
				rowData = mensaje;
			}
		}
		
		return rowData;
    }
	
	@Override
	public Object getWrappedData() {
		return datasource;
	}

	@Override
	public List<MensajeVO> load( int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters ) {
		int page = ( first / pageSize ) + 1;
		
		String ordenCampo = null;
		
		if( sortField != null ) {
			if( sortField.equals( "origen.nombre" ) ) {
				ordenCampo = "nombre_tramite";
			} else if( sortField.equals( "fechaEnvio" ) ) {
				ordenCampo = "fecha_envio";
			} else {
				ordenCampo = sortField;
			}
		}
		
		String ordenDireccion = ( ( sortOrder == SortOrder.ASCENDING ) ? "asc" : ( ( sortOrder == SortOrder.DESCENDING ) ? "desc" : null ) );
		
		try {
			ResultadoFiltroVO mensajes = mensajesService.recuperarMensajesPaginados( notificaciones, documento, asunto, origen, solicitud, estado, ordenCampo, ordenDireccion, page );
			
			datasource = mensajesService.parseResultados( mensajes, false, true );
			
			setRowCount( mensajes != null ? mensajes.getTamanioLista() : 0 );
		} catch( BusinessException be ) {
			datasource = null;
			
			setRowCount( 0 );
		}
		
		return datasource;
	}
	
}