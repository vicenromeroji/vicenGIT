package es.oepm.mao.view.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.apache.cxf.common.util.CollectionUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import es.oepm.core.business.BaseVO;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.session.SessionContext;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.controller.BaseController;
import es.oepm.core.view.controller.ListController;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.core.view.faces.SelectItemList;
import es.oepm.core.view.util.HTMLUtil;
import es.oepm.mao.business.service.PagosService;
import es.oepm.mao.business.vo.ConsultaTasasFilterVO;
import es.oepm.mao.view.controller.util.GeneradorValoresCombo;
import es.oepm.mao.view.controller.util.JSFPages;
import es.oepm.sireco.webservice.TasaMaoBean;
import es.oepm.sireco.ws.SirecoService.beans.RespuestaTasasMao;

@ManagedBean( name = "consultaTasasController" )
@ViewScoped
public class ConsultaTasasController extends BaseController implements ListController {
	
	private static final long serialVersionUID = -337810791123403619L;

	@ManagedProperty( "#{pagosService}" )
	private PagosService pagosService;
	
	private boolean mustLoadList;
	private ConsultaTasasFilterVO filter;
	private List<TasaMaoBean> tasas;
	
	private List<SelectItem> modalidades;
	
	public ConsultaTasasController() {
		super();
		
		BaseVO sessionValue = SessionUtil.getSearchFilter( true );
		
		if( ( sessionValue != null ) && ( sessionValue instanceof ConsultaTasasFilterVO ) ) {
			setFilter( sessionValue );

			mustLoadList = true;
		} else {			
			SessionUtil.clearSearchFilter();
		}
		
		List<Modalidad> listModalidades = GeneradorValoresCombo
				.getListaModalidadesConsultaTasas((Collection<SimpleGrantedAuthority>) SessionContext
						.getUserDetails().getAuthorities());
		
		modalidades = HTMLUtil.getSelectItemListByGroup( SelectItemList.createDefault(), 
														 listModalidades, 
														 Modalidad.class, "textoModalidad", "modalidad", "" );
	}
	
	public void setPagosService( PagosService pagosService ) {
		this.pagosService = pagosService;
	}
	
	public List<SelectItem> getModalidades() {
		return modalidades;
	}
	
	public List<TasaMaoBean> getTasas() {
		return tasas;
	}
	
	@Override
	public String actionSearch() {
		tasas = Collections.emptyList();
		
		try {
				RespuestaTasasMao resTasasMao = pagosService.getTasa( filter.getFecha(), filter.getCodigo(), filter.getDescripcion(), Modalidad.find( filter.getModalidad() ) );
				
				if( resTasasMao != null && resTasasMao.getTasasMao() != null ) {
					tasas = Arrays.asList( resTasasMao.getTasasMao() );
				}
				
				// Sin resultados.
				if (CollectionUtils.isEmpty(tasas)) {
					FacesUtil.addOKMessage("busqueda.sin.resultados");
				}
				
			} catch( BusinessException e ) {
				FacesUtil.addErrorMessage( "pagos.tasas.error.recuperarDatos" );
			}
		
		return null;
	}
	
	/**
	 * Action para el boton de limpiar la busqueda de tasas. 
	 * Creado para MAO-459
	 * @param to Parametro opcional para especificar una jsp de destino 
	 * @return
	 */
	public String actionLimpiar(String to) {
		filter = new ConsultaTasasFilterVO();
		//saveFilterInSession(filter);
		this.tasas = new ArrayList<TasaMaoBean>();
		if(StringUtils.isEmptyOrNull(to)){
			return JSFPages.PAGOS_TASAS;
		}
		else {
			return to;
		}
	}
	
	public String actionLimpiar() {
		return actionLimpiar(null);
	}

	@Override
	public String actionEdit() {
		return null;
	}

	@Override
	public String actionDelete() {
		return null;
	}

	@Override
	public String actionNew() {
		return null;
	}

	@Override
	public ConsultaTasasFilterVO getFilter() {
		try {
			if( filter == null ) {
				filter = new ConsultaTasasFilterVO();
			} else {
				if( mustLoadList ) {
					actionSearch();
					
					mustLoadList = false;
				}
			}
		} catch( Exception e ) {
			FacesUtil.addErrorMessage( ExceptionUtil.getMessage( e ) );
		}
		
		return filter;
	}

	@Override
	public void setFilter( BaseVO filter ) {
		this.filter = ( ConsultaTasasFilterVO )filter;
	}

}