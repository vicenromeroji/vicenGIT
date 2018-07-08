package es.oepm.mao.ws.parameters;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import es.oepm.wservices.core.BaseWSResponse;
import es.oepm.wservices.core.mensajes.Mensajes;

/**
 * Login con MAO Response
 * 
 * @author AYESA AT
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LogWithMAOResponse", namespace = "http://parameters.logwithmao.ws.mao.oepm.es/", propOrder = {
		"cookieName", "cookie", "login", "nombre", "apellido1", "apellido2", "documento", "email",
		"tipoUsuario", "codAgente", "domicilio", "codPostal", "municipio",
		"codProvincia", "provincia", "codPais", "pais", "cuentaBancaria" })
public class LogWithMAOResponse extends BaseWSResponse {

	private static final long serialVersionUID = -3229254766154983218L;
	
	private String cookieName;
	private String cookie;
	private String login;
	private String nombre;
	private String apellido1;
	private String apellido2;
	private String documento;
	private String email;
	private String tipoUsuario;
	private String codAgente;
	private String domicilio;
	private String codPostal;
	private String municipio;
	private String codProvincia;
	private String provincia;
	private String codPais;
	private String pais;
	private String cuentaBancaria;

	public LogWithMAOResponse() {
		super();
	}

	public LogWithMAOResponse(int resultado, Mensajes[] mensajes) {
		super(resultado, mensajes);
	}

	/**
	 * @return the cookieName
	 */
	public String getCookieName() {
		return cookieName;
	}

	/**
	 * @param cookieName the cookieName to set
	 */
	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

	/**
	 * @return the cookie
	 */
	public String getCookie() {
		return cookie;
	}

	/**
	 * @param cookie
	 *            the cookie to set
	 */
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	/**
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @param login
	 *            the login to set
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @return the nombre
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * @param nombre
	 *            the nombre to set
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * @return the apellido1
	 */
	public String getApellido1() {
		return apellido1;
	}

	/**
	 * @param apellido1
	 *            the apellido1 to set
	 */
	public void setApellido1(String apellido1) {
		this.apellido1 = apellido1;
	}

	/**
	 * @return the apellido2
	 */
	public String getApellido2() {
		return apellido2;
	}

	/**
	 * @param apellido2
	 *            the apellido2 to set
	 */
	public void setApellido2(String apellido2) {
		this.apellido2 = apellido2;
	}

	/**
	 * @return the documento
	 */
	public String getDocumento() {
		return documento;
	}

	/**
	 * @param documento
	 *            the documento to set
	 */
	public void setDocumento(String documento) {
		this.documento = documento;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the tipoUsuario
	 */
	public String getTipoUsuario() {
		return tipoUsuario;
	}

	/**
	 * @param tipoUsuario
	 *            the tipoUsuario to set
	 */
	public void setTipoUsuario(String tipoUsuario) {
		this.tipoUsuario = tipoUsuario;
	}

	/**
	 * @return the domicilio
	 */
	public String getDomicilio() {
		return domicilio;
	}

	/**
	 * @param domicilio
	 *            the domicilio to set
	 */
	public void setDomicilio(String domicilio) {
		this.domicilio = domicilio;
	}

	/**
	 * @return the codAgente
	 */
	public String getCodAgente() {
		return codAgente;
	}

	/**
	 * @param codAgente
	 *            the codAgente to set
	 */
	public void setCodAgente(String codAgente) {
		this.codAgente = codAgente;
	}

	/**
	 * @return the codPostal
	 */
	public String getCodPostal() {
		return codPostal;
	}

	/**
	 * @param codPostal
	 *            the codPostal to set
	 */
	public void setCodPostal(String codPostal) {
		this.codPostal = codPostal;
	}

	/**
	 * @return the municipio
	 */
	public String getMunicipio() {
		return municipio;
	}

	/**
	 * @param municipio
	 *            the municipio to set
	 */
	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	/**
	 * @return the codProvincia
	 */
	public String getCodProvincia() {
		return codProvincia;
	}

	/**
	 * @param codProvincia
	 *            the codProvincia to set
	 */
	public void setCodProvincia(String codProvincia) {
		this.codProvincia = codProvincia;
	}

	/**
	 * @return the provincia
	 */
	public String getProvincia() {
		return provincia;
	}

	/**
	 * @param provincia
	 *            the provincia to set
	 */
	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}

	/**
	 * @return the codPais
	 */
	public String getCodPais() {
		return codPais;
	}

	/**
	 * @param codPais
	 *            the codPais to set
	 */
	public void setCodPais(String codPais) {
		this.codPais = codPais;
	}

	/**
	 * @return the pais
	 */
	public String getPais() {
		return pais;
	}

	/**
	 * @param pais
	 *            the pais to set
	 */
	public void setPais(String pais) {
		this.pais = pais;
	}

	/**
	 * @return the cuentaBancaria
	 */
	public String getCuentaBancaria() {
		return cuentaBancaria;
	}

	/**
	 * @param cuentaBancaria the cuentaBancaria to set
	 */
	public void setCuentaBancaria(String cuentaBancaria) {
		this.cuentaBancaria = cuentaBancaria;
	}
}