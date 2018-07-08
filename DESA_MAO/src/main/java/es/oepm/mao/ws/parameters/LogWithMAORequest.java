package es.oepm.mao.ws.parameters;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import es.oepm.wservices.core.BaseWSRequest;

/**
 * Login con MAO Request
 * 
 * cookie example = aWzX76yzrpQ+LDIqb5obVAEql1eTIoNxOsBQfhfWV8M=
 * 
 * @author AYESA AT
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LogWithMAORequest", namespace = "http://parameters.logwithmao.ws.mao.oepm.es/", propOrder = {
		"cookie", "login", "password" })
public class LogWithMAORequest extends BaseWSRequest {

	private static final long serialVersionUID = -3935641052491548062L;
	
	private String cookie;
	private String login;
	private String password;

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
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

}