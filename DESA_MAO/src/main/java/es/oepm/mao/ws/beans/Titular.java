package es.oepm.mao.ws.beans;

import java.io.Serializable;
import java.util.Date;

public class Titular implements Serializable {

	private static final long serialVersionUID = -1343972644988963643L;
	
	private long id;
	private String login;
	private String password;
	private String nombre;
	private String apellido1;
	private String apellido2;
	private String tipoDocumento;
	private String documento;
	private String email;
	private boolean cuentaVerificada;
	private boolean emailVerificado;
	private String cnae;
	private boolean pyme;
	private String codigoEstadoNacionalidad;
	private String codigoEstadoResidencia;
	private String codigoEstadoEstablecimiento;
	private String direccion;
	private String codigoPostal;
	private String poblacion;
	private String codigoProvincia;
	private String codigoPais;
	private String direccionNotificaciones;
	private String codigoPostalNotificaciones;
	private String poblacionNotificaciones;
	private String codigoProvinciaNotificaciones;
	private String codigoPaisNotificaciones;
	private String emailNotificaciones;
	private int fax;
	private int telefono;
	private String codigoMedioNotificacion;
	private boolean cuentaBloqueada;
	private Date fechaCuentaAutoBloqueada;

	public long getId() {
		return id;
	}

	public void setId( long id ) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin( String login ) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword( String password ) {
		this.password = password;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre( String nombre ) {
		this.nombre = nombre;
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

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento( String tipoDocumento ) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento( String documento ) {
		this.documento = documento;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail( String email ) {
		this.email = email;
	}

	public boolean isCuentaVerificada() {
		return cuentaVerificada;
	}

	public void setCuentaVerificada( boolean cuentaVerificada ) {
		this.cuentaVerificada = cuentaVerificada;
	}

	public boolean isEmailVerificado() {
		return emailVerificado;
	}

	public void setEmailVerificado( boolean emailVerificado ) {
		this.emailVerificado = emailVerificado;
	}

	public String getCnae() {
		return cnae;
	}

	public void setCnae( String cnae ) {
		this.cnae = cnae;
	}

	public boolean isPyme() {
		return pyme;
	}

	public void setPyme( boolean pyme ) {
		this.pyme = pyme;
	}

	public String getCodigoEstadoNacionalidad() {
		return codigoEstadoNacionalidad;
	}

	public void setCodigoEstadoNacionalidad( String codigoEstadoNacionalidad ) {
		this.codigoEstadoNacionalidad = codigoEstadoNacionalidad;
	}

	public String getCodigoEstadoResidencia() {
		return codigoEstadoResidencia;
	}

	public void setCodigoEstadoResidencia( String codigoEstadoResidencia ) {
		this.codigoEstadoResidencia = codigoEstadoResidencia;
	}

	public String getCodigoEstadoEstablecimiento() {
		return codigoEstadoEstablecimiento;
	}

	public void setCodigoEstadoEstablecimiento( String codigoEstadoEstablecimiento ) {
		this.codigoEstadoEstablecimiento = codigoEstadoEstablecimiento;
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

	public String getCodigoPais() {
		return codigoPais;
	}

	public void setCodigoPais( String codigoPais ) {
		this.codigoPais = codigoPais;
	}

	public String getDireccionNotificaciones() {
		return direccionNotificaciones;
	}

	public void setDireccionNotificaciones( String direccionNotificaciones ) {
		this.direccionNotificaciones = direccionNotificaciones;
	}

	public String getCodigoPostalNotificaciones() {
		return codigoPostalNotificaciones;
	}

	public void setCodigoPostalNotificaciones( String codigoPostalNotificaciones ) {
		this.codigoPostalNotificaciones = codigoPostalNotificaciones;
	}

	public String getPoblacionNotificaciones() {
		return poblacionNotificaciones;
	}

	public void setPoblacionNotificaciones( String poblacionNotificaciones ) {
		this.poblacionNotificaciones = poblacionNotificaciones;
	}

	public String getCodigoProvinciaNotificaciones() {
		return codigoProvinciaNotificaciones;
	}

	public void setCodigoProvinciaNotificaciones( String codigoProvinciaNotificaciones ) {
		this.codigoProvinciaNotificaciones = codigoProvinciaNotificaciones;
	}

	public String getCodigoPaisNotificaciones() {
		return codigoPaisNotificaciones;
	}

	public void setCodigoPaisNotificaciones( String codigoPaisNotificaciones ) {
		this.codigoPaisNotificaciones = codigoPaisNotificaciones;
	}

	public String getEmailNotificaciones() {
		return emailNotificaciones;
	}

	public void setEmailNotificaciones( String emailNotificaciones ) {
		this.emailNotificaciones = emailNotificaciones;
	}

	public int getFax() {
		return fax;
	}

	public void setFax( int fax ) {
		this.fax = fax;
	}

	public int getTelefono() {
		return telefono;
	}

	public void setTelefono( int telefono ) {
		this.telefono = telefono;
	}

	public String getCodigoMedioNotificacion() {
		return codigoMedioNotificacion;
	}

	public void setCodigoMedioNotificacion( String codigoMedioNotificacion ) {
		this.codigoMedioNotificacion = codigoMedioNotificacion;
	}

	public boolean isCuentaBloqueada() {
		return cuentaBloqueada;
	}

	public void setCuentaBloqueada( boolean cuentaBloqueada ) {
		this.cuentaBloqueada = cuentaBloqueada;
	}

	public Date getFechaCuentaAutoBloqueada() {
		return fechaCuentaAutoBloqueada;
	}

	public void setFechaCuentaAutoBloqueada( Date fechaCuentaAutoBloqueada ) {
		this.fechaCuentaAutoBloqueada = fechaCuentaAutoBloqueada;
	}

}