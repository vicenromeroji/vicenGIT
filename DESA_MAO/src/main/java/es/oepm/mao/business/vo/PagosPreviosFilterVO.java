package es.oepm.mao.business.vo;

import es.oepm.core.business.BaseVO;
import es.oepm.mao.constants.MaoTrazaGestor;

public class PagosPreviosFilterVO extends BaseVO {

	private static final long serialVersionUID = 1155428515885966261L;
	
	private String codigoAgente;
	private String documento;
	private String loginMAO;
	
	/**
	 * Obtiene los parámetros de la clase con formato para las trazas.
	 * 
	 * @return
	 */
	public String toTraze() {
		StringBuilder traze = new StringBuilder();

		if (codigoAgente != null) {
			traze.append("Cod. agente: ").append(codigoAgente)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (documento != null) {
			traze.append("Documento: ").append(documento)
					.append(MaoTrazaGestor.TRAZA_SEPARADOR);
		}

		if (loginMAO != null) {
			traze.append("Login MAO: ").append(loginMAO);
		}

		// Eliminamos la última coma si existe
		if (traze.lastIndexOf(MaoTrazaGestor.TRAZA_SEPARADOR) == traze.length() - 2) {
			traze.delete(traze.length() - 2, traze.length());
		}

		return traze.toString();
	}

	public String getCodigoAgente() {
		return codigoAgente;
	}

	public void setCodigoAgente( String codigoAgente ) {
		this.codigoAgente = codigoAgente;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento( String documento ) {
		this.documento = documento;
	}

	public String getLoginMAO() {
		return loginMAO;
	}

	public void setLoginMAO( String loginMAO ) {
		this.loginMAO = loginMAO;
	}
	
}