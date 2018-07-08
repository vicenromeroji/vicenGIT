package es.oepm.mao.view.controller;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.core.business.ceo.vo.CeDirDisenadoresMvVO;
import es.oepm.core.business.ceo.vo.CeDirDisenosMvVO;
import es.oepm.core.business.ceo.vo.CeDirExpedientesMvVO;
import es.oepm.core.business.ceo.vo.CeDirHijosMvVO;
import es.oepm.core.business.ceo.vo.CeDirLicenciasMvVO;
import es.oepm.core.business.ceo.vo.CeDirPagosMvVO;
import es.oepm.core.business.ceo.vo.CeDirProductosMvVO;
import es.oepm.core.business.ceo.vo.CeDirTitularesMvVO;
import es.oepm.core.business.ceo.vo.DIActoTramitacionDTO;
import es.oepm.core.business.ceo.vo.ListaVariantes;
import es.oepm.core.business.vo.DetalleExpedienteResponseVO;
import es.oepm.core.business.vo.DocumentoExpedienteVO;
import es.oepm.core.constants.Modalidad;
import es.oepm.core.constants.Roles;
import es.oepm.core.constants.Sistemas;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.session.SessionContext;
import es.oepm.core.session.SessionUtil;
import es.oepm.core.util.DateUtils;
import es.oepm.core.util.StringUtils;
import es.oepm.core.view.controller.TituloDetalleExp;
import es.oepm.core.view.controller.TituloDetalleExpGral;
import es.oepm.core.view.faces.FacesUtil;
import es.oepm.mao.application.TrazaOpsMAO;
import es.oepm.mao.business.service.ExpedientesService;
import es.oepm.mao.business.vo.ExpedienteVO;
import es.oepm.mao.constants.MaoPropiedadesConf;
import es.oepm.mao.view.servlet.ImagenFiguraInternoServlet;
import es.oepm.mao.view.servlet.ImagenFiguraServlet;
import es.oepm.maoceo.comun.view.controller.IDetalleExpedienteSitamodController;

@ManagedBean(name = "detalleSitamodController")
@ViewScoped
public class DetalleExpedienteSitamodController extends DetalleExpedienteController implements IDetalleExpedienteSitamodController{
	
	private static final long serialVersionUID = -4034206748253087193L;
	
	@ManagedProperty( "#{expedientesService}" )
	private ExpedientesService expedientesService;
	
	public void setExpedientesService(ExpedientesService expedientesService) {
		this.expedientesService = expedientesService;
	}

	// ---------------
	private CeDirExpedientesMvVO expediente = null;

	// ---------------
	//private String codModalidad = null;
	//private String numExpediente = null;

	/**
	 * Constructor por defecto de la clase.
	 */
	public DetalleExpedienteSitamodController() {		
		ExpedienteVO exp = (ExpedienteVO)SessionUtil.getFromSession("expedienteSeleccionado");
		idExpediente = exp.getId();
		modalidad = exp.getModalidad().trim();
		expediente = null;
	}

	@PostConstruct
	public void init() {
		if (expediente == null) {
			if (!SessionContext.getUserDetails().getAuthorities().contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "D"))){
				FacesUtil.addErrorMessage("busquedaRapidaPorNumero.error.sitamod");
			}
			else{
				try {
					// Recuperamos el expediente
					DetalleExpedienteResponseVO<CeDirExpedientesMvVO> response = expedientesService
							.getDetalleExpedienteSitamod(idExpediente, modalidad);
					expediente = response.getDetalleExpediente();
					procesarMensajesDetalleExpediente(response.getMensajes());

					// Lo añadimos a sesion
					SessionUtil.addToSession("detalleExpediente",expediente );
					SessionUtil.addToSession("tituloDetalle",getTituloDetalle());
					
					// Escribimos la traza
					TrazaOpsMAO.trazaOperacionConsultaExp( expediente.getModalidad(), expediente.getNumero() );
				} catch (final Exception e) {
					FacesUtil.addErrorMessage("busqueda.error");
				}
			}
		}
	}

	public CeDirExpedientesMvVO getExpediente() {
		return this.expediente;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.maoceo.comun.view.controller.IDetalleExpedienteSitamodController#mostrarFigurasDiseno(es.oepm.core.business.ceo.vo.CeDirDisenosMvVO)
	 */
	@Override
	public void mostrarFigurasDiseno(CeDirDisenosMvVO diseno) {
		diseno.setMostrarFiguras(Boolean.TRUE);
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.maoceo.comun.view.controller.IDetalleExpedienteSitamodController#ocultarFigurasDiseno(es.oepm.core.business.ceo.vo.CeDirDisenosMvVO)
	 */
	@Override
	public void ocultarFigurasDiseno(CeDirDisenosMvVO diseno) {
		diseno.setMostrarFiguras(Boolean.FALSE);
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.maoceo.comun.view.controller.IDetalleExpedienteSitamodController#isImpresionPDF()
	 */
	@Override
	public boolean isImpresionPDF() {
		String renderOutputType = FacesUtil.getParameter("RenderOutputType");
		if (renderOutputType != null && renderOutputType.equals("pdf")) {
			return true;
		} 
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.maoceo.comun.view.controller.IDetalleExpedienteSitamodController#isConsultaExterna()
	 */
	@Override
	public boolean isConsultaExterna() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.maoceo.comun.view.controller.IDetalleExpedienteSitamodController#getRutaLinkServletImagenesFiguras()
	 */
	@Override
	public String getRutaLinkServletImagenesFiguras() {
		return ImagenFiguraServlet.RUTA_SERVLET;
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.maoceo.comun.view.controller.IDetalleExpedienteSitamodController#
	 * getRutaImagenServletImagenesFiguras(es.oepm.core.business.vo.DocumentoExpedienteVO)
	 */
	@Override
	public String getRutaImagenServletImagenesFiguras(DocumentoExpedienteVO figura) {
		String renderOutputType = FacesUtil.getParameter("RenderOutputType");
		StringBuilder ruta = new StringBuilder();
		if (renderOutputType != null && renderOutputType.equals("pdf")) {
			ruta.append(Configuracion.getPropertyAsString(MaoPropiedadesConf.URL_HOME));
			ruta.append(ImagenFiguraInternoServlet.RUTA_SERVLET);
			ruta.append("?idContenido=").append(figura.getIdContenido());
			ruta.append("&modalidad=").append(expediente.getModalidad());
			ruta.append("&numero=").append(expediente.getNumero());
			ruta.append("&numeroDiseno=").append(figura.getNumeroDiseno());
		} else {
			ruta.append(ImagenFiguraServlet.RUTA_SERVLET);
			ruta.append("?idContenido=").append(figura.getIdContenido());
			ruta.append("&modalidad=").append(expediente.getModalidad());
			ruta.append("&numero=").append(expediente.getNumero());
			ruta.append("&numeroDiseno=").append(figura.getNumeroDiseno());
		}
		ruta.append("&thumbnail=true");
		
		return ruta.toString();
	}

	public String getTituloDetalle() {
		String titulo = getTituloProducto();

		if(titulo==null){
			if (expediente.isDisenosNLey()) {
				titulo = getTituloNLey();
			} else if (expediente.isDisenosIntern()) {
			//	titulo = getTituloInter();
			//} else if (expediente.isDisenosEstad()) {
			//	titulo = getTituloProducto();
			}
		}

		return getTitulo(titulo);
	}

	private String getTitulo(String strTitulo) {
		final String mod = expediente.getModalidad();
		String descMod = Modalidad.find(mod).getDescModalidad();
		
		Integer n = new Integer(expediente.getNumero().trim());
		if (expediente.isDiseno() && n < Sistemas.SITAMOD_NUMERO_500000) { //Es dibujo industrial
			descMod = Modalidad.DI_DIBUJO.getDescModalidad();
		}

		final TituloDetalleExp titulo = new TituloDetalleExpGral();
		titulo.setTextoModalidad(descMod);
		titulo.setLetraModalidad(mod);
		titulo.setNumero(expediente.getNumero());
		titulo.setDigito(expediente.getDigExpe());
		titulo.setTitulo(strTitulo);

		return titulo.toString();
	}

	private String getTituloNLey() {
		String titulo = null;
		final List<CeDirDisenosMvVO> disenos = expediente.getDisenos();
		if (!disenos.isEmpty() && !Modalidad.DI_DISENO.getModalidad().equals(expediente.getModalidad())) {
			titulo = disenos.get(0).getTitulo();
		}

		return titulo;
	}

//	private String getTituloInter() {
//		String titulo = null;
//		final List<CeDirDisenosMvVO> disenos = expediente.getDisenosInter();
//		if (!disenos.isEmpty()) {
//			titulo = disenos.get(0).getTitulo();
//		}
//
//		return titulo;
//	}

	private String getTituloProducto() {
		String titulo = null;
		if (null != expediente.getProducto()) {
			titulo = expediente.getProducto().getTitulo();
		}

		return titulo;
	}

	public List<CeDirHijosMvVO> getDisenosDerivados() {
		return this.expediente.getDisenosDerivados();		
	}
	
	//---------------
	public CeDirTitularesMvVO getTitular() {
		return this.expediente.getTitular();		
	}
	
	public List<CeDirTitularesMvVO> getOtrosTitulares() {
		return this.expediente.getOtrosTitulares();		
	}

	//---------------
	public List<CeDirDisenosMvVO> getDisenos() {
		return this.expediente.getDisenos();
		
	}	

	// ***************************
	public List<CeDirLicenciasMvVO> getLicencias() throws BusinessException {
		return this.expediente.getLicencias();
		
	}
	
	//--------------------

	public List<CeDirPagosMvVO> getPagos() {
		return this.expediente.getPagos();		
	}
	
	public CeDirProductosMvVO getProducto() {
		return this.expediente.getProducto();		
	}
	
	public List<CeDirDisenadoresMvVO> getDisenadores() {
		return this.expediente.getDisenadores();		
	}

	// ***********************************************************
	public boolean isPublicable(){	
		return this.expediente != null ? 
				this.expediente.isPublicable() 
				: false;	
	}
	
	public boolean isPublicable(String codVariante){
		boolean publicable = false;
		if (getDisenos()!=null) {
			for (CeDirDisenosMvVO diseno : getDisenos()) {
				if (codVariante.equals(diseno.getCodVariante())) {
					if (diseno.getActosTramites()!=null) {
						for (DIActoTramitacionDTO tramite : diseno.getActosTramites()) {
							if ("170   ".equals(tramite.getCodTramiteMvto())
									|| "160   ".equals(tramite.getCodTramiteMvto())
									|| "190   ".equals(tramite.getCodTramiteMvto())
									|| "810170".equals(tramite.getCodTramiteMvto())
									|| "510170".equals(tramite.getCodTramiteMvto())) {
								publicable = true;
								break;
							}
						}
					}
					break;
				}
			}
		}
		return publicable;
	}
	
	public List<CeDirDisenosMvVO> getDisenosInter() {
		return this.expediente.getDisenosInter();		
	}

	public List<CeDirDisenosMvVO> getDisenosEsta() {
		return this.expediente.getDisenosEsta();		
	}	

	public boolean isTratarDisenosNLey() {
		return this.expediente.isDisenosNLey();		
	}

	public boolean isTratarDisenosInter() {
		return this.expediente.isDisenosIntern();		
	}

	public boolean isTratarDisenosEsta() {
		return this.expediente.isDisenosEstad();		
	}	

	public List<ListaVariantes> getVariantesDiseno() {
		return this.expediente.getLineasVariantes();		
	}

	public boolean isFechaPresentacionDistinta(CeDirDisenosMvVO diseno)
			throws ParseException {		
		String strFExp = this.expediente.getFechaHoraPresenta();
		String strFDiseno = diseno.getFechaPresenta();

		if (null == strFExp && null == strFDiseno) {
			return false;
		}

		// uno de los dos !=null diseno?
		if (null == strFDiseno) {
			return false;
		}
		
		if (null == strFExp) {
			return true;
		}

		// si no: ambos son !=null
		final Date fexp = DateUtils.parseFecha(strFExp);
		final Date fdiseno = DateUtils.parseFecha(strFDiseno);

		return !fdiseno.equals(fexp);
	}

	public String getTextoModalidad() {
		return this.expediente.getTextoModalidad();		
	}

	public String getTextoDigExpediente() {
		return this.expediente.getTextoDigExpediente();		
	}

	public boolean isDiseno() {
		return this.expediente.isDiseno();		
	}

	public boolean isModeloIndustrial() {
		return this.expediente.isModeloIndustrial();		
	}

	public boolean isModeloInternacional() {
		return this.expediente.isModeloInternacional();		
	}	

	// -------------

	public boolean isMostrarTextoDesgloseModalidad() {
		return this.expediente.isMostrarTextoDesgloseModalidad();		
	}

	public String getTextoDesgloseModalidad() {
		return this.expediente.getTextoDesgloseModalidad();		
	}	

	public String getTextoEmbargo() {
		return this.expediente.getTextoEmbargo();		
	}	

	public String getTextoTransferenciaTitular() {
		return this.expediente.getTextoTransferenciaTitular();		
	}

	public String getReferenciaInvenes(){
		if ("D".equals(expediente.getModalidad())) {
//			Integer numero = Integer.parseInt(expediente.getNumero());
			//if (numero<500000) {
				return expediente.getModalidad()+ expediente.getNumero();
			//} else { //diseños
			//	return expediente.getModalidad()+ expediente.getNumero()+ "-01";
			//}
		} else {
			return expediente.getModalidad()+ expediente.getNumero();
		}
	}	
	
	/**
	 * Devuelve el valor correcto para design view
	 * 
	 * Para la busqueda en DesignView, teneis que convertir el numero de diseño en ST13.
	 * Para convertir a ST13: modalidad=D, Numero=0506532, Variante=14
	 * Seria ES700000000506532-0014
	 * El ES es fijo, el 70 equivale a la modalidad, D = 70, I = 71, 6 ceros fijos y el numero del diseño (7 digitos)
	 * un guion el numero de variante con 4 digitos rellenando con ceros.
	 * 
	 * Si el diseño es del EPI (numero < 0500000), la variante del diseño se identifica por una letra de la A-J o X,
	 * y la conversion es A = 01, B =02 ... J = 10 y la X = 24. 
	 * 
	 * @return String
	 */
	public String getReferenciaDesignView () {
		String st13 = "ES";
		
		String modalidad = expediente.getModalidad();
		String numero = expediente.getNumero();
		String variante = "01";
		
		try { 
			if (expediente.getDisenos()!=null && expediente.getDisenos().size()>0) {
				variante = expediente.getDisenos().get(expediente.getDisenos().size()-1).getCodVariante().trim();
			} else if (expediente.getLineasVariantes()!=null && expediente.getLineasVariantes().size()>0) {
				for (ListaVariantes variantes : expediente.getLineasVariantes()) {
					if ("VARIANTES CONCEDIDAS".equals(variantes.getKey())) {
						if (variantes.getLista()!=null && variantes.getLista().size()>0) {
							variante = variantes.getLista().get(variantes.getLista().size()-1).trim(); 
							break;
						}
					}
				}
				
			}
			
			//Si es numero
			variante = String.valueOf(Integer.parseInt(variante)); 
			
		} catch (Exception e) {
			//si es letra
			if ("A".equals(variante)) {
				variante = "01";
			} else if ("B".equals(variante)) {
				variante = "02";
			} else if ("C".equals(variante)) {
				variante = "03";
			} else if ("D".equals(variante)) {
				variante = "04";
			} else if ("E".equals(variante)) {
				variante = "05";
			} else if ("F".equals(variante)) {
				variante = "06";
			} else if ("G".equals(variante)) {
				variante = "07";
			} else if ("H".equals(variante)) {
				variante = "08";
			} else if ("I".equals(variante)) {
				variante = "09";
			} else if ("J".equals(variante)) {
				variante = "10";
			} else if ("X".equals(variante)) {
				variante = "24";
			}
		}
		
		try {
			st13 += ("D".equals(modalidad) ? "70" : "71") + "000000" + numero + "-" + StringUtils.lcouch(variante, 4, '0');
		} catch (Exception ignored) {}
		
		return st13;
	}

	@Override
	public String getReferenciaDesignViewOLD() {
		String st13 = "ES";

		String modalidad = expediente.getModalidad();
		String numero = expediente.getNumero();
		String variante = "01";

		try {
			if (expediente.getDisenos() != null
					&& expediente.getDisenos().size() > 0) {
				variante = expediente.getDisenos()
						.get(expediente.getDisenos().size() - 1)
						.getCodVariante().trim();
			} else if (expediente.getLineasVariantes() != null
					&& expediente.getLineasVariantes().size() > 0) {
				for (ListaVariantes variantes : expediente.getLineasVariantes()) {
					if ("VARIANTES CONCEDIDAS".equals(variantes.getKey())) {
						if (variantes.getLista() != null
								&& variantes.getLista().size() > 0) {
							variante = variantes.getLista()
									.get(variantes.getLista().size() - 1)
									.trim();
							break;
						}
					}
				}

			}

			// Si es numero
			variante = String.valueOf(Integer.parseInt(variante));

		} catch (Exception e) {
			// si es letra
			if ("A".equals(variante)) {
				variante = "01";
			} else if ("B".equals(variante)) {
				variante = "02";
			} else if ("C".equals(variante)) {
				variante = "03";
			} else if ("D".equals(variante)) {
				variante = "04";
			} else if ("E".equals(variante)) {
				variante = "05";
			} else if ("F".equals(variante)) {
				variante = "06";
			} else if ("G".equals(variante)) {
				variante = "07";
			} else if ("H".equals(variante)) {
				variante = "08";
			} else if ("I".equals(variante)) {
				variante = "09";
			} else if ("J".equals(variante)) {
				variante = "10";
			} else if ("X".equals(variante)) {
				variante = "24";
			}
		}

		try {
			st13 += ("D".equals(modalidad) ? "70" : "71") + "000000" + numero
					+ "-" + StringUtils.lcouch(variante, 4, '0');
		} catch (Exception ignored) {
		}

		return st13;
	}
	
	public String numFigura(String figura)
	{
		String numFigura="";
		String numFiguratemp= figura.replaceAll("[^0-9]+", " "); // to get just digits
		List<String> list=Arrays.asList(numFiguratemp.trim().split(" ")); // split string numbers in a list
		for(int i=0; i<list.size();i++)
		{
			if(i<list.size()-1)
			{
				numFigura+=list.get(i).replaceFirst("^0+", "")+".";
			}
			else
			{
				numFigura+=list.get(i).replaceFirst("^0+", "");
			}
		}
		return numFigura;
	}

	@Override
	public void setExpediente(CeDirExpedientesMvVO exp) {
		expediente = exp;
	}	

}
