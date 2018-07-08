package es.oepm.mao.business.vo;

import java.util.Date;


import es.oepm.core.business.BaseVO;
import es.oepm.sireco.webservice.TasaMaoBean;

public class PagoPrevioVO extends BaseVO {

	private static final long serialVersionUID = -1914180342393071573L;
	
	private String codigoBarras;
	private TasaMaoBean tasa;
	private double importe;
	private Date fechaPago;

	public String getCodigoBarras() {
		return codigoBarras;
	}

	public void setCodigoBarras( String codigoBarras ) {
		this.codigoBarras = codigoBarras;
	}

	public TasaMaoBean getTasa() {
		return tasa;
	}

	public void setTasa( TasaMaoBean tasa ) {
		this.tasa = tasa;
	}

	public double getImporte() {
		return importe;
	}

	public void setImporte( double importe ) {
		this.importe = importe;
	}

	public Date getFechaPago() {
		return fechaPago;
	}

	public void setFechaPago( Date fechaPago ) {
		this.fechaPago = fechaPago;
	}
	
	public String getTasaLiteral() {
		return tasa.getLiteral();
	}
	
}