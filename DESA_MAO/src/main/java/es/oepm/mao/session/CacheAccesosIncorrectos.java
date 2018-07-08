package es.oepm.mao.session;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache de accesos incorrectos
 */
public class CacheAccesosIncorrectos implements Serializable {

	private static final long serialVersionUID = 3895116667244990528L;
	
	private static final Map<String, Integer> accesosIncorrectos = new HashMap<String, Integer>();
	
	public synchronized static int addAccesoIncorrecto (String login) {
		Integer numAccesosIncorrectos = 1;
		if (accesosIncorrectos.get(login) == null) {
			accesosIncorrectos.put(login, numAccesosIncorrectos);
		} else {
			numAccesosIncorrectos = accesosIncorrectos.get(login);
			numAccesosIncorrectos ++;
			accesosIncorrectos.put(login, numAccesosIncorrectos);
		}
		
		return numAccesosIncorrectos;
	}
	
	public synchronized static void limpiarAccesosIncorrectos (String login) {
		if (accesosIncorrectos.get(login) != null) {
			accesosIncorrectos.remove(login);
		}
	}
}
