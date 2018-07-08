package es.oepm.mao.view.controller.util;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.util.StringUtils;
import es.oepm.sireco.bl.beans.TasaPasarelaBean;
import es.oepm.sireco.webservice.ConsultaModalidadClavesOut;
import es.oepm.sireco.webservice.ModalidadClavesBean;
import es.oepm.sireco.webservice.ServiceSireco;
import es.oepm.sireco.webservice.ServiceSirecoService;
import es.oepm.sireco.webservice.ServiceSirecoServiceLocator;
import es.oepm.sireco.ws.SirecoService.beans.RespuestaConsultaTasasPasarela;

/**
 * Clase singleton para almacenar las tasas por modalidad.
 * De esta manera nos ahorramos un montón de invocaciones al WS de Sireco Interno 
 * @author hcanosor
 *
 */
public class TasasListado implements Serializable {

	private static final long serialVersionUID = 1048989437978204576L;

	private ServiceSireco wsSirecoInter;

	private static TasasListado instance;
	
	private List<TasaPasarelaBean> tasas;
	
	private List<TasaPasarelaBean> tasasE;
	private List<TasaPasarelaBean> tasasP;
	private List<TasaPasarelaBean> tasasU;
	private List<TasaPasarelaBean> tasasW;
	private List<TasaPasarelaBean> tasasC;
	private List<TasaPasarelaBean> tasasT;
	private List<TasaPasarelaBean> tasasF;
	private List<TasaPasarelaBean> tasasL;
	 
	private TasasListado(){}
		
	
	/**
	 * Comprueba si está instanciado el cliente de sireco.
	 * Si no es el caso, lo instancia.
	 * @throws BusinessException
	 */
	private void comprobarClienteSirecoInter() throws BusinessException {
		String urlWSSirecoInter="";
		try {
			if(wsSirecoInter==null){							
				urlWSSirecoInter=Configuracion.getPropertyAsString("url.bus.sirecoInter");
					
				if(!StringUtils.isEmptyOrNull(urlWSSirecoInter)){
					ServiceSirecoService wsSirecoLocator = new ServiceSirecoServiceLocator();
					wsSirecoInter = wsSirecoLocator.getServiceSirecoPort( new URL(urlWSSirecoInter));
					
				}else
					OepmLogger.error("Error al instanciar el wsclient de SIRECO interno. No esta definida en la configuracion la variable url.bus.sirecoInter");
				}
		} catch( Exception e ) {
			wsSirecoInter=null;
			OepmLogger.error("Error al instanciar el wsclient de SIRECO interno para la direccion " + urlWSSirecoInter, e);
			throw new BusinessException(e,"");

		}
	}	
	
	/**
	 * Procesa la respuesta devuelta por el metodo consultaModalidadClaves de
	 * Sireco Interno.
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private List<TasaPasarelaBean> procesarRespuesta (ConsultaModalidadClavesOut response) throws Exception
	{
		List<TasaPasarelaBean> result = new ArrayList<TasaPasarelaBean>();
		List<ModalidadClavesBean> auxTasas = Arrays.asList(response.getTasas()); 
		
		// Lo llamamos a través del getter para inicializarlas si no existen
		List<TasaPasarelaBean> todasTasas = getTasas();
		
		for (ModalidadClavesBean auxTasa : auxTasas) {
			for (TasaPasarelaBean tasaPasarela : todasTasas) {
				
				if ( tasaPasarela.getCodigo().equals(auxTasa.getCodClave()) )
				{
					result.add(tasaPasarela);
				}	
			}
		}
			
		return result;
	}
	
	public static TasasListado getInstance() {
		if(instance==null){
			instance = new TasasListado();
		}
		return instance;
	}

	/**
	 * Obtiene todas las tasas disponibles de Sireco Interno
	 * @return
	 * @throws Exception
	 */
	public List<TasaPasarelaBean> getTasas() throws Exception
	{
		if (tasas == null) {
			comprobarClienteSirecoInter();
			
			RespuestaConsultaTasasPasarela response = wsSirecoInter.consultaTasas();
			tasas = Arrays.asList(response.getTasas());	
    	}
			
		return tasas;
	}
	
	/**
	 * Obtiene todas las tasas disponibels para una modalidad especificada por parámetro
	 * @param modalidad La modalidad
	 * @return
	 * @throws Exception
	 */
	public List<TasaPasarelaBean> getTasaPorModalidad(String modalidad) throws Exception
	{	
		// Modalidades de Alfa
		
		if ("C".equalsIgnoreCase(modalidad)) {
			return this.getTasasC();
		} else if ("E".equalsIgnoreCase(modalidad)) {
			return this.getTasasE();
		} else if ("F".equalsIgnoreCase(modalidad)) {
			return this.getTasasF();
		} else if ("L".equalsIgnoreCase(modalidad)) {
			return this.getTasasL();
		} else if ("P".equalsIgnoreCase(modalidad)) {
			return this.getTasasP();
		} else if ("T".equalsIgnoreCase(modalidad)) {
			return this.getTasasT();
		} else if ("U".equalsIgnoreCase(modalidad)) {
			return this.getTasasU();
		} else if ("W".equalsIgnoreCase(modalidad)) {
			return this.getTasasW();
		}
		
		// Todos los demas casos...
		return new ArrayList<TasaPasarelaBean>();		
	}
	
	
	//////////////////////////////
	// ------ Tasas ALFA ------ //
	//////////////////////////////
	
	/**
	 * Obtiene todas las tasas disponibles de Sireco Interno para
	 * trámites de modalidad E
	 * @return
	 * @throws Exception
	 */
	public List<TasaPasarelaBean> getTasasE() throws Exception
	{
		if (tasasE == null) {
						
			comprobarClienteSirecoInter();
			ConsultaModalidadClavesOut response = wsSirecoInter.consultaModalidadClaves("E");
			tasasE = procesarRespuesta(response);	
    	}
		
		return tasasE;  	 
	}
	
	/**
	 * Obtiene todas las tasas disponibles de Sireco Interno para
	 * trámites de modalidad P
	 * @return
	 * @throws Exception
	 */
	public List<TasaPasarelaBean> getTasasP() throws Exception
	{
		if (tasasP == null) {
						
			comprobarClienteSirecoInter();
			ConsultaModalidadClavesOut response = wsSirecoInter.consultaModalidadClaves("P");
			tasasP = procesarRespuesta(response);	
    	}
		
		return tasasP;  	 
	}
  
	/**
	 * Obtiene todas las tasas disponibles de Sireco Interno para
	 * trámites de modalidad U
	 * @return
	 * @throws Exception
	 */
	public List<TasaPasarelaBean> getTasasU() throws Exception
	{
		if (tasasU == null) {
						
			comprobarClienteSirecoInter();
			ConsultaModalidadClavesOut response = wsSirecoInter.consultaModalidadClaves("U");
			tasasU = procesarRespuesta(response);	
    	}
		
		return tasasU;  	 
	}
	 
	/**
	 * Obtiene todas las tasas disponibles de Sireco Interno para
	 * trámites de modalidad W
	 * @return
	 * @throws Exception
	 */
	public List<TasaPasarelaBean> getTasasW() throws Exception
	{
		if (tasasW == null) {
						
			comprobarClienteSirecoInter();
			ConsultaModalidadClavesOut response = wsSirecoInter.consultaModalidadClaves("W");
			tasasW = procesarRespuesta(response);	
    	}
		
		return tasasW;  	 
	}
	
	/**
	 * Obtiene todas las tasas disponibles de Sireco Interno para
	 * trámites de modalidad C
	 * @return
	 * @throws Exception
	 */
	public List<TasaPasarelaBean> getTasasC() throws Exception
	{
		if (tasasC == null) {
						
			comprobarClienteSirecoInter();
			ConsultaModalidadClavesOut response = wsSirecoInter.consultaModalidadClaves("C");
			tasasC = procesarRespuesta(response);	
    	}
		
		return tasasC;  	 
	}
	
	/**
	 * Obtiene todas las tasas disponibles de Sireco Interno para
	 * trámites de modalidad T
	 * @return
	 * @throws Exception
	 */
	public List<TasaPasarelaBean> getTasasT() throws Exception
	{
		if (tasasT == null) {
						
			comprobarClienteSirecoInter();
			ConsultaModalidadClavesOut response = wsSirecoInter.consultaModalidadClaves("T");
			tasasT = procesarRespuesta(response);	
    	}
		
		return tasasT;  	 
	}
	
	/**
	 * Obtiene todas las tasas disponibles de Sireco Interno para
	 * trámites de modalidad F
	 * @return
	 * @throws Exception
	 */
	public List<TasaPasarelaBean> getTasasF() throws Exception
	{
		if (tasasF == null) {
						
			comprobarClienteSirecoInter();
			ConsultaModalidadClavesOut response = wsSirecoInter.consultaModalidadClaves("F");
			tasasF = procesarRespuesta(response);	
    	}
		
		return tasasF;  	 
	}
	
	/**
	 * Obtiene todas las tasas disponibles de Sireco Interno para
	 * trámites de modalidad L
	 * @return
	 * @throws Exception
	 */
	public List<TasaPasarelaBean> getTasasL() throws Exception
	{
		if (tasasL == null) {
						
			comprobarClienteSirecoInter();
			ConsultaModalidadClavesOut response = wsSirecoInter.consultaModalidadClaves("L");
			tasasL = procesarRespuesta(response);	
    	}
		
		return tasasL;  	 
	}
	 
	 
	

}
