package es.oepm.mao.view.controller.util;

import java.io.Serializable;

import com.ayesa.utilities.iconfiguration.Configuracion;

import es.oepm.mao.constants.MaoPropiedadesConf;

public final class MAOConfiguracion implements Serializable {
	
	private static final long serialVersionUID = -8849533489353767059L;
	
	// Interruptor para Alfa
	private static Boolean alfaIsDisabled;
	
	// Interruptor para Sitamod (disenos)
	private static Boolean sitamodIsDisabled;
	
	// Interruptor para Sitamar (Marcas)
	private static Boolean sitamarIsDisabled;
	
	// Interruptor para seccion de Bonos
	private static Boolean bonosIsDisabled;
	
	// Interruptor para comunicaciones y notificaciones
	private static Boolean comNotifIsDisabled;
	
	// Interruptor para comunicaciones y notificaciones
	private static Boolean pagosIsDisabled;
	
	
	public static Boolean getAlfaIsDisabled() {
		
		if (alfaIsDisabled == null) {
			if (!Configuracion.getPropertyAsBoolean(MaoPropiedadesConf.FUNCIONALIDAD_ACTIVA_ALFA)) {
				alfaIsDisabled = true;
			} else {
				alfaIsDisabled = false;
			}	
		}
		
		return alfaIsDisabled;
	}
	
	public static Boolean getSitamarIsDisabled() {
		
		if (sitamarIsDisabled == null) {
			if (Configuracion.getPropertyAsBoolean(MaoPropiedadesConf.FUNCIONALIDAD_ACTIVA_SIGNOS)) {
				sitamarIsDisabled = false;
			} else {
				sitamarIsDisabled = true;
			}	
		}
		
		return sitamarIsDisabled;
	}
	
	public static Boolean getSitamodIsDisabled() {
		
		if (sitamodIsDisabled == null) {
			if (Configuracion.getPropertyAsBoolean(MaoPropiedadesConf.FUNCIONALIDAD_ACTIVA_DISENOS)) {
				sitamodIsDisabled = false;
			} else {
				sitamodIsDisabled = true;
			}	
		}	
		
		return sitamodIsDisabled;
	}

	public static Boolean getBonosIsDisabled() {
		
		if (bonosIsDisabled == null) {
			if (!Configuracion.getPropertyAsBoolean(MaoPropiedadesConf.FUNCIONALIDAD_ACTIVA_BONOS)) {
				bonosIsDisabled = true;
			} else {
				bonosIsDisabled = false;
			}	
		}
		
		return bonosIsDisabled;
	}

	public static Boolean getComNotifIsDisabled() {
		
		if (comNotifIsDisabled == null) {
			if (!Configuracion.getPropertyAsBoolean(MaoPropiedadesConf.FUNCIONALIDAD_ACTIVA_NOTIFICACIONES_COMUNICACIONES)) {
				comNotifIsDisabled = true;
			} else {
				comNotifIsDisabled = false;
			}
		}
		
		return comNotifIsDisabled;
	}
	
	
	/**
	 *  xteve019 - [MAO-501] - v1.8.4 - Desactivar pestaña de pagos en Mao
	 **/	
	public static Boolean getPagosIsDisabled() {
		
		if (pagosIsDisabled == null) {
			if (!Configuracion.getPropertyAsBoolean(MaoPropiedadesConf.FUNCIONALIDAD_ACTIVA_PAGOS)) {
				pagosIsDisabled = true;
			} else {
				pagosIsDisabled = false;
			}
		}
		
		return pagosIsDisabled;
	}



}
