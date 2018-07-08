package es.oepm.mao.business.vo;

import es.oepm.core.business.BaseVO;

public class CabeceraPasarelaVO extends BaseVO {

	private static final long serialVersionUID = 2863678512640198564L;
	
	private String codRepresentante;
	private String email;
	private String idSesion;
	private String nif;
	private String nomApe;
	private String tipoIdentificacion;
	private String tipoPasarela;

	public CabeceraPasarelaVO() {
		super();
	}

	public String getCodRepresentante() {
		return codRepresentante;
	}

	public void setCodRepresentante(String codRepresentante) {
		this.codRepresentante = codRepresentante;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getIdSesion() {
		return idSesion;
	}

	public void setIdSesion(String idSesion) {
		this.idSesion = idSesion;
	}

	public String getNif() {
		return nif;
	}

	public void setNif(String nif) {
		this.nif = nif;
	}

	public String getNomApe() {
		return nomApe;
	}

	public void setNomApe(String nomApe) {
		this.nomApe = nomApe;
	}

	public String getTipoIdentificacion() {
		return tipoIdentificacion;
	}

	public void setTipoIdentificacion(String tipoIdentificacion) {
		this.tipoIdentificacion = tipoIdentificacion;
	}

	public String getTipoPasarela() {
		return tipoPasarela;
	}

	public void setTipoPasarela(String tipoPasarela) {
		this.tipoPasarela = tipoPasarela;
	}
}