package es.oepm.mao.business.vo;

import java.io.Serializable;

/**
 * Usuario logado
 * 
 * @author AYESA AT
 *
 */
public class UsuarioLogado implements Serializable {
	
	private static final long serialVersionUID = -9217552479583990743L;

	public static enum TipoUsuario {
		AGENTE, ASOCIADO, TITULAR, REPRESENTANTE, GESTOR
	}; 

	private String idUsuario;
	private String txLogin;
	private String txClave;
	private String txNombre;
	private String txApellido1;
	private String txApellido2;
	private String txDocumento;
	private String txEmail;
	private TipoUsuario tipoUsuario;
	private Object usuario;
	private String txRol;

	public String getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(String idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getTxLogin() {
		return txLogin;
	}

	public void setTxLogin(String txLogin) {
		this.txLogin = txLogin;
	}

	public String getTxClave() {
		return txClave;
	}

	public void setTxClave(String txClave) {
		this.txClave = txClave;
	}

	public String getTxNombre() {
		return txNombre;
	}

	public void setTxNombre(String txNombre) {
		this.txNombre = txNombre;
	}

	public String getTxApellido1() {
		return txApellido1;
	}

	public void setTxApellido1(String txApellido1) {
		this.txApellido1 = txApellido1;
	}

	public String getTxApellido2() {
		return txApellido2;
	}

	public void setTxApellido2(String txApellido2) {
		this.txApellido2 = txApellido2;
	}

	public String getTxDocumento() {
		return txDocumento;
	}

	public void setTxDocumento(String txDocumento) {
		this.txDocumento = txDocumento;
	}

	public String getTxEmail() {
		return txEmail;
	}

	public void setTxEmail(String txEmail) {
		this.txEmail = txEmail;
	}

	public TipoUsuario getTipoUsuario() {
		return tipoUsuario;
	}

	public void setTipoUsuario( TipoUsuario tipoUsuario ) {
		this.tipoUsuario = tipoUsuario;
	}

	public Object getUsuario() {
		return usuario;
	}

	public void setUsuario(Object usuario) {
		this.usuario = usuario;
	}

	public String getTxRol() {
		return txRol;
	}

	public void setTxRol(String txRol) {
		this.txRol = txRol;
	}

}
