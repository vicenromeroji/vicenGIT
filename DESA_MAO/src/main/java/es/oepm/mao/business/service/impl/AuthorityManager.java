package es.oepm.mao.business.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import es.oepm.busmule.ws.client.ceo.BusSistemas;
import es.oepm.core.constants.Roles;
import es.oepm.mao.view.controller.util.MAOConfiguracion;

/**
 * Clase para realizar las operaciones sobre los permisos de los usuarios.
 * 
 * @author jugonzalez
 *
 */
public class AuthorityManager implements Serializable {

	private static final long serialVersionUID = 775952099593472584L;

	/**
	 * Obtiene la lista de sistemas a las que tiene acceso al usuario.
	 * 
	 * @return
	 */
	public static List<BusSistemas> obtenerSistemasUsuario(Collection<SimpleGrantedAuthority> authList) {
		List<BusSistemas> resultado = new ArrayList<BusSistemas>();

		// Introducimos los sistemas a los que el usuario tiene permiso en la
		// lista
		// Permiso para moadlidad invenciones
		if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "I"))) {
			if (!MAOConfiguracion.getAlfaIsDisabled()) {
				resultado.add(BusSistemas.ALFA);
			}
		}

		// Permiso para modalidad diseño
		if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "D"))) {	
			if (!MAOConfiguracion.getSitamodIsDisabled()) {
				resultado.add(BusSistemas.SITAMOD);
			}
		}

		// Permiso para modalidad marcas
		if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "S"))) {
			if (!MAOConfiguracion.getSitamarIsDisabled()) {
				resultado.add(BusSistemas.SITAMAR);
			}
		}

		return resultado;
	}

}
