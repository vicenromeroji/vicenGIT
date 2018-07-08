package es.oepm.mao.view.controller.util;

import java.io.Serializable;

public class JSFPages implements Serializable {
	
	private static final long serialVersionUID = 4749631153641447783L;
	
	public static final String PATH_PUBLIC = "/jsp/";
	public static final String PATH_PRIVATE = "/jsp/privado/";
	
	public static final String LOGIN = PATH_PUBLIC + "login/loginpage";
	public static final String LOGIN_OUT = PATH_PUBLIC + "login/logout";
	public static final String LOGIN_DENEGADO = PATH_PUBLIC + "deniedpage";
	
	public static final String INICIO_PRIVADO = PATH_PRIVATE + "inicio";
	
	//Autoregistro
	public static final String REGISTRARSE = PATH_PUBLIC + "publico/registrarNuevoUsuario/registrarUsuario";
	
	//Usuarios Asociados
	public static final String USUARIOSASOCIADOS_NEW = PATH_PRIVATE + "contenido/usuariosAutorizados/mantUsuariosAsociados";
	public static final String USUARIOSASOCIADOS_MODIFY= PATH_PRIVATE + "contenido/usuariosAutorizados/mantUsuariosAsociados";
	public static final String USUARIOSASOCIADOS_LIST = PATH_PRIVATE + "contenido/usuariosAutorizados/consultaUsuariosAsociados";
	
	// Comunicaciones y Notificaciones
	public static final String COMYNOT_COM_LIST = PATH_PRIVATE + "contenido/comunicacionesynotificaciones/comunicaciones";
	public static final String COMYNOT_NOT_LIST = PATH_PRIVATE + "contenido/comunicacionesynotificaciones/notificaciones";
	public static final String COMYNOT_COM_VIEW = PATH_PRIVATE + "contenido/comunicacionesynotificaciones/consultaComunicacion";
	public static final String COMYNOT_NOT_VIEW = PATH_PRIVATE + "contenido/comunicacionesynotificaciones/consultaNotificacion";
	
	// Pagos
	public static final String PAGOS_TASAS= PATH_PRIVATE + "contenido/pagos/consultaTasas";
	public static final String PAGOS_ESPERADOS= PATH_PRIVATE + "contenido/pagos/pagosEsperados";
	public static final String PAGOS_PREVIOS= PATH_PRIVATE + "contenido/pagos/pagosPrevios";
	public static final String PAGOS_REALIZADOS= PATH_PRIVATE + "contenido/pagos/historialPagosRealizados";
	public static final String PAGOS_BONOS_LIST = PATH_PRIVATE + "contenido/pagos/consultaBonos";
	public static final String PAGOS_BONOS_VIEW = PATH_PRIVATE + "contenido/pagos/consultaBono";
	public static final String PAGOS_REDIRECCION_PASARELA = PATH_PRIVATE + "contenido/pagos/redireccionPasarela";
	
	//Documentos
	public static final String DOCUMENTOS_LIST = PATH_PRIVATE + "contenido/documentos/documentosExp";
	
	//Pagina principal
	public static final String PRINCIPAL_CATALOGO =  PATH_PRIVATE + "tramites";
	public static final String PRINCIPAL_TRAMITE = PATH_PRIVATE + "tramite";
	
	//Busquedas de expedientes
	public static final String BUSQ_EXP_RESULTADOS = PATH_PRIVATE +  "contenido/expedientes/listadoExpedientes";
	// Redireccion a login con clave
	public static final String PASARELA_CLAVE = "/comun/pasarelaClave";
	
	// Verificacion de cuenta
	public static final String VERIFICAR_CUENTA = PATH_PRIVATE +  "verificar";
	
	// Busqueda de BOPI
	public static final String BUSQUEDA_BOPI = PATH_PRIVATE +  "contenido/bopi/listadoBOPI";

}
