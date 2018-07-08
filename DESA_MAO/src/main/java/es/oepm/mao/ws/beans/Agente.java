package es.oepm.mao.ws.beans;

import java.io.Serializable;

public class Agente implements Serializable {

	private static final long serialVersionUID = -2726509207350944745L;
	
	private String codigo;
	private String digito;
	private String apellido1;
	private String apellido2;
	private String nombre;
	private String documento;
	private String senage;
	private String direccion;
	private String codigoPostal;
	private String poblacion;
	private String codigoProvincia;
	private String telefono;
	private String fax;
	private String email;
	private String codigoMedioNotificacion;

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo( String codigo ) {
		this.codigo = codigo;
	}

	public String getDigito() {
		return digito;
	}

	public void setDigito( String digito ) {
		this.digito = digito;
	}

	public String getApellido1() {
		return apellido1;
	}

	public void setApellido1( String apellido1 ) {
		this.apellido1 = apellido1;
	}

	public String getApellido2() {
		return apellido2;
	}

	public void setApellido2( String apellido2 ) {
		this.apellido2 = apellido2;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre( String nombre ) {
		this.nombre = nombre;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento( String documento ) {
		this.documento = documento;
	}

	public String getSenage() {
		return senage;
	}

	public void setSenage( String senage ) {
		this.senage = senage;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion( String direccion ) {
		this.direccion = direccion;
	}

	public String getCodigoPostal() {
		return codigoPostal;
	}

	public void setCodigoPostal( String codigoPostal ) {
		this.codigoPostal = codigoPostal;
	}

	public String getPoblacion() {
		return poblacion;
	}

	public void setPoblacion( String poblacion ) {
		this.poblacion = poblacion;
	}

	public String getCodigoProvincia() {
		return codigoProvincia;
	}

	public void setCodigoProvincia( String codigoProvincia ) {
		this.codigoProvincia = codigoProvincia;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono( String telefono ) {
		this.telefono = telefono;
	}

	public String getFax() {
		return fax;
	}

	public void setFax( String fax ) {
		this.fax = fax;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail( String email ) {
		this.email = email;
	}

	public String getCodigoMedioNotificacion() {
		return codigoMedioNotificacion;
	}

	public void setCodigoMedioNotificacion( String codigoMedioNotificacion ) {
		this.codigoMedioNotificacion = codigoMedioNotificacion;
	}

}