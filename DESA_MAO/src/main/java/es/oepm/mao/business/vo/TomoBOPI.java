package es.oepm.mao.business.vo;

import es.oepm.core.view.faces.MessagesUtil;

public enum TomoBOPI {

	TOMO1( "bopi.tomos.tomo1.key", "bopi.tomos.tomo1.value" ),
	TOMO2( "bopi.tomos.tomo2.key", "bopi.tomos.tomo2.value" ),
	TOMO3( "bopi.tomos.tomo3.key", "bopi.tomos.tomo3.value" );
	
	private String tomoKey;
	private String tomoValue;
	
	private TomoBOPI( String tomoKey, String tomoValue ) {
		this.tomoKey = tomoKey;
		this.tomoValue = tomoValue;
	}

	public String getTomoKey() {
		return MessagesUtil.getMessage( tomoKey );
	}

	public void setTomoKey( String tomoKey ) {
		this.tomoKey = tomoKey;
	}

	public String getTomoValue() {
		return MessagesUtil.getMessage( tomoValue );
	}

	public void setTomoValue( String tomoValue ) {
		this.tomoValue = tomoValue;
	}
	
}