package es.oepm.mao.constants;

import java.io.Serializable;

/**
 * Propiedades de configuracion de MAO
 * 
 * @author AYESA AT
 */
public class MaoPropiedadesConf implements Serializable {
	
	private static final long serialVersionUID = -5609621453283035248L;
	
	// Maximo de resultados por sistema
	public static final String MAX_RESULTADOS_SISTEMA = "MAX_RESULTADOS_SISTEMA";
	public static final String MIN_CARACTERES_CAMPO_NUMERO = "MIN_CARACTERES_CAMPO_NUMERO";
	public static final String MIN_CARACTERES_CAMPO_CADENA = "MIN_CARACTERES_CAMPO_CADENA";
	public static final String URL_RSS = "URL_RSS_NOTICIAS";
	
	/**
	 * El entorno en el que está desplegada la aplicación
	 */
	public static final String ENTORNO = "ENTORNO";
	
	/**
	 * Indica si se ha configurado la aplicacion para mostrar u comunicaciones y notificaciones
	 */
	public static final String FUNCIONALIDAD_ACTIVA_NOTIFICACIONES_COMUNICACIONES = "FUNCIONALIDAD_ACTIVA.NOTIFICACIONES_COMUNICACIONES";
	
	/**
	 * Indica si se ha configurado la aplicacion para mostrar u ocultar los bonos
	 */
	public static final String FUNCIONALIDAD_ACTIVA_BONOS = "FUNCIONALIDAD_ACTIVA.BONOS";
	
	/**
	 * Indica si la aplicación soporta modalidades de disenyos
	 */
	public static final String FUNCIONALIDAD_ACTIVA_ALFA = "FUNCIONALIDAD_ACTIVA.ALFA";
		
	/**
	 * Constante del parametro de configuracion que indica si se muestra la integracion con BOPI
	 */
	public static final String FUNCIONALIDAD_ACTIVA_BOPI = "FUNCIONALIDAD_ACTIVA.BOPI";
	
	/**
	 * Constante del parametro de configuracion que indica si se muestra la funcionalidad de signos
	 */
	public static final String FUNCIONALIDAD_ACTIVA_SIGNOS = "FUNCIONALIDAD_ACTIVA.SIGNOS";
	
	/**
	 * Constante del parametro de configuracion que indica si se muestra la funcionalidad de diseños
	 */
	public static final String FUNCIONALIDAD_ACTIVA_DISENOS = "FUNCIONALIDAD_ACTIVA.DISENOS";
	
	/**
	 * Nombre del parametro que contiene la url de login del acceso con clave
	 */
	public static final String CLAVE_URL_LOGIN = "CLAVE.URL_LOGIN";
	/**
	 * Nombre del parametro que contiene el campo urlRetorno del acceso con clave
	 */
	public static final String CLAVE_URL_RETORNO = "CLAVE.URL_RETORNO";
	/**
	 * Nombre del parametro que contiene el campo aplicacion para el acceso con clave
	 */
	public static final String CLAVE_APLICACION = "CLAVE.APLICACION";
	/**
	 * Nombre del parametro que contiene el campo nqaa para el acceso con clave
	 */
	public static final String CLAVE_NQAA = "CLAVE.NQAA";
	/**
	 * Nombre del parametro que contiene el campo codigoApp para el acceso con clave
	 */
	public static final String CLAVE_CODIGO_APP = "CLAVE.CODIGO_APP";
	/**
	 * Nombre del parametro que contiene el campo idpAfirma para el acceso con clave
	 */
	public static final String CLAVE_IDP_AFIRMA = "CLAVE.IDP_AFIRMA";
	/**
	 * Nombre del parametro que contiene el campo idpStork para el acceso con clave
	 */
	public static final String CLAVE_IDP_STORK = "CLAVE.IDP_STORK";
	/**
	 * Nombre del parametro que contiene el campo idpAEAT para el acceso con clave
	 */
	public static final String CLAVE_IDP_AEAT = "CLAVE.IDP_AEAT";
	/**
	 * Nombre del parametro que contiene el campo idpGISS para el acceso con clave
	 */
	public static final String CLAVE_IDP_GISS = "CLAVE.IDP_GISS";
	/**
	 * Nombre del atributo de sesion que indica desde donde se llama a la pasera de clave
	 */
	public static final String CLAVE_URL_LLAMADA = "CLAVE_URL_LLAMADA";
	/**
	 * Valor del atributo de sesion CLAVE_URL_LLAMADA cuando la pasarela se llama desde la pantalla de login
	 */
	public static final String CLAVE_URL_LLAMADA_LOGIN = "LOGIN";
	/**
	 * Valor del atributo de sesion CLAVE_URL_LLAMADA cuando la pasarela se llama desde la pantalla de verificar cuenta
	 */
	public static final String CLAVE_URL_LLAMADA_VERIFICAR = "VERIFICARCUENTA";
	/**
	 * Valor del atributo de sesion CLAVE_URL_LLAMADA cuando la pasarela se llama desde la pantalla de login para registrar un usuario
	 */
	public static final String CLAVE_URL_LLAMADA_REGISTRO = "REGISTROUSUARIO";
	/**
	 * Nombre del atributo de sesion que indica si se ha de realizar la llamada a clave
	 */
	public static final String CLAVE_REALIZAR_LLAMADA = "CLAVE_REALIZAR_LLAMADA";
	/**
	 * Nombre del atributo de sesion que indica si el registro se realiza con autentificacion mediante Cl@ve
	 */
	public static final String REGISTRO_CLAVE = "REGISTRO_CLAVE";
	/**
	 * Nombre del atributo de sesion que informa el numero de documento del usuario para el registro
	 */
	public static final String REGISTRO_NUMERO_DOCUMENTO = "REGISTRO_NUMERO_DOCUMENTO";
	/**
	 * Nombre del atributo de sesion que informa el nombre del usuario para el registro
	 */
	public static final String REGISTRO_NOMBRE = "REGISTRO_NOMBRE";
	/**
	 * Nombre del atributo de sesion que informa los apellidos del usuario para el registro
	 */
	public static final String REGISTRO_APELLIDOS = "REGISTRO_APELLIDOS";
	
	/**
	 * Nombre del parametro que contiene el nombre de la aplicacion
	 */
	public static final String APLICATION_NAME = "aplicationName";
	
	/**
	 * Nombre del parametro que contiene la url del servicio web de la pasarela de pagos
	 */
	public static final String PASARELA2_URL_SERVICIOWEB = "PASARELA2.URL_SERVICIOWEB";
	
	/**
	 * Nombre del parametro que contiene la url de la pasarela de pagos
	 */
	public static final String PASARELA2_URL_POSTACTION = "PASARELA2.URL_POSTACTION";
	
	/**
	 * Nombre del parametro que contiene la url a la que retornara la pasarela de pagos
	 */
	public static final String PASARELA2_URL_RETORNO = "PASARELA2.URL_RETORNO";
	
	/**
	 * Nombre de la variable de session para la informacion de pagos de la pasarela
	 */
	public static final String PASARELA2_INFOPAGOS = "infoPagos";
	
	/**
	 * Constante que indica el valor del tipo de identificacion represntante legal "R"
	 */
	public static final String PASARELA2_TIP_IDE_REPRESENTANTE_LEGAL = "R";
	
	/**
	 * Constante que indica el valor del tipo de identificacion sujeto pasivo "P"
	 */
	public static final String PASARELA2_TIP_IDE_SUJETO_PASIVO = "P";
	
	/**
	 * Constante que indica el valor del tipo de pasarela caixa "C"
	 */
	public static final String PASARELA2_TIP_PASARELA_CAIXA = "C";
	
	/**
	 * Constante que indica el valor del tipo de pasarela AEAT "P"
	 */
	public static final String PASARELA2_TIP_PASARELA_AEAT = "P";
	
	/**
	 * Constante que indica el valor del tipo de pasarela Tarjeta de credito "T"
	 */
	public static final String PASARELA2_TIP_PASARELA_TARJETA = "T";
	
	/**
	 * Constante para el codigo de representante de los representantes
	 */
	public static final String PASARELA2_COD_REPRESENTANTE = "0000";
	
	/**
	 * Constante para permitir el acceso a la aplicacion con usuario y contraseña
	 */
	public static final String ACCESO_CON_USUARIO = "FUNCIONALIDAD_ACTIVA.ACCESO_CON_USUARIO";
	
	/**
	 * Constante para recuperar la url del logo del ministerio para la cabecera de la aplicación
	 */
	public static final String LOGO_MINISTERIO = "ENLACE_LOGO_MINISTERIO";
	
	/**
	 * Constante para recuperar la url del logo de la OEPM para la cabecera de la aplicación
	 */
	public static final String LOGO_OEPM = "ENLACE_LOGO_OEPM";
	
	/**
	 * Constante para recuperar la url del logo del ministerio para los pdf
	 */
	public static final String LOGO_MINISTERIO_REPORT = "ENLACE_LOGO_MINISTERIO_REPORT";
	
	/**
	 * Constante para recuperar la url del logo de la OEPM para los pdf
	 */
	public static final String LOGO_OEPM_REPORT = "ENLACE_LOGO_OEPM_REPORT";
	
	/**
	 * Constante para recuperar la url del logo de la MINISTERIO y OEPM para la cabecera de la aplicación
	 */
	public static final String LOGO_MIN_OEPM = "ENLACE_LOGO_MIN_OEPM";
		
	/**
	 * Nombre del parametro que contiene la url de búsqueda de marcas de la
	 * OAMI.
	 */
	public static final String URL_BUSQUEDA_OAMI = "OAMI.URL_BUSQUEDA";
	
	/**
	 * Constante para recuperar la url del logo de la sede
	 */
	public static final String LOGO_SEDE = "ENLACE_LOGO_SEDE";
	
	/**
	 * Constante para recuperar la url de la sede
	 */
	public static final String ENLACE_SEDE = "ENLACE_SEDE";
	
	/**
	 * Constante para recuperar el mensaje que se incluirá en el logo de la página principal
	 */
	public static final String MENSAJE_ENLACE = "MENSAJE_ENLACE";
	
	/**
	 * Constante para recuperar la url local de la aplicación MAO
	 */
	public static final String URL_HOME = "url.mao.home";
	
	/**
	 * xteve019 - [MAO-501] - v1.8.4 - Desactivar pestaña de pagos en Mao
	 * Indica si se ha configurado la aplicacion para mostrar u ocultar los pagos
	 */
	public static final String FUNCIONALIDAD_ACTIVA_PAGOS = "FUNCIONALIDAD_ACTIVA.PAGOS";
}
