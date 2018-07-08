package es.oepm.mao.business.vo;

import es.oepm.core.view.faces.MessagesUtil;

public enum FormaPagoSIRECO {
	
	FICHERO_CARGO( "pagos.formasPago.fContable.key", "pagos.formasPago.fContable.value" ), 
	PASARELA_AEAT( "pagos.formasPago.pAEAT.key", "pagos.formasPago.pAEAT.value" ), 
	PASARELA_CAIXA( "pagos.formasPago.pCaixa.key", "pagos.formasPago.pCaixa.value" ), 
	TARJETA_CREDITO( "pagos.formasPago.tCredito.key", "pagos.formasPago.tCredito.value" ), 
	TODAS( "pagos.formasPago.todas.key", "pagos.formasPago.todas.value" ), 
	VENTANILLA_CAIXA( "pagos.formasPago.vCaixa.key", "pagos.formasPago.vCaixa.value" );
	
	private String formaPagoKey;
	private String formaPagoValue;
	
	private FormaPagoSIRECO( String formaPagoKey, String formaPagoValue ) {
		this.formaPagoKey = formaPagoKey;
		this.formaPagoValue = formaPagoValue;
	}
	
	public String getFormaPagoKey() {
		return MessagesUtil.getMessage( formaPagoKey );
	}
	
	public void setFormaPagoKey( String formaPagoKey ) {
		this.formaPagoKey = formaPagoKey;
	}
	
	public String getFormaPagoValue() {
		return MessagesUtil.getMessage( formaPagoValue );
	}
	
	public void setFormaPagoValue( String formaPagoValue ) {
		this.formaPagoValue = formaPagoValue;
	}
	
}