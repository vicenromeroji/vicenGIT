package es.oepm.mao.application;

import java.io.Serializable;

import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;

public class TrazaOpsMAO implements Serializable {

	private static final long serialVersionUID = -7541706989596519388L;
	
	private static String PTRN_TRAZA_OP = "%s %s - %s";
	private static String PTRN_OP_CONSULTA_EXP = "Consulta Exp. %s%s";
	private static String PTRN_OP_CONSULTA_DOC_EXP = "Consulta Doc. Exp. %s%s Doc. %s";
	
	private static void trazaOperacion( String message ) {
		OepmLogger.info(String.format( PTRN_TRAZA_OP, SessionContext.getDocumento(), SessionContext.getLoginUsuario(), message));
	}
	
	public static void trazaOperacionConsultaExp(String modalidad,
			String solicitud) {
		trazaOperacion(String.format(PTRN_OP_CONSULTA_EXP, modalidad, solicitud));
	}

	public static void trazaOperacionConsultaDocExp(String modalidad,
			String solicitud, String documento) {
		trazaOperacion(String.format(PTRN_OP_CONSULTA_DOC_EXP, modalidad,solicitud, documento));
	}
	
}