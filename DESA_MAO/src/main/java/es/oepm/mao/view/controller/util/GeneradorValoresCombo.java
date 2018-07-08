package es.oepm.mao.view.controller.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.model.SelectItem;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.CollectionUtils;

import es.oepm.core.constants.Modalidad;
import es.oepm.core.constants.Roles;
import es.oepm.core.view.faces.MessagesUtil;
import es.oepm.mao.business.vo.UsuarioLogado.TipoUsuario;

/**
 * Lista para generar los combos de modalidades de las distintas pantallas.
 * @author jugonzalez
 *
 */
public class GeneradorValoresCombo implements Serializable {
	
	private static final long serialVersionUID = -4219235813146251502L;

	/**
	 * Devuelve una lista de modalidades para la pantalla de busqueda de expedientes adecuada a los permisos del usuario.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Modalidad> getListaModalidadesCompleta(Collection<SimpleGrantedAuthority> authList) {
		Modalidad[] modalidades = Modalidad.values();
		List<Modalidad> listModalidades = (List<Modalidad>) CollectionUtils.arrayToList(modalidades);
		List<Modalidad> listModalidadesFinal = new ArrayList<Modalidad>();
		
		/** Comprobar pemisos de modalidades de usuario */
		for (Modalidad modalidad : listModalidades) {
			if (modalidad.isAlfa()) {
				// Permiso para moadlidad invenciones
				if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "I"))) {
					listModalidadesFinal.add(modalidad);
				}
			} else if (modalidad.isSitamod()) {
				//Permiso para modalidad diseño
				if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "D"))) {
					listModalidadesFinal.add(modalidad);
				}
			} else if (modalidad.isSitamar()) {
				//Permiso para modalidad marcas
				if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "S"))) {
					listModalidadesFinal.add(modalidad);
				}
			}
		}
		
		// HCS: MAO-337
		listModalidadesFinal = filtrarModalidades(listModalidadesFinal);
		
		return listModalidadesFinal;
	}	
	
	/**
	 * Devuelve una lista de modalidades para la pantalla de pagos esperados adecuada a los permisos del usuario.
	 * @return
	 */
	public static List<Modalidad> getListaModalidadesPagosEsperados(Collection<SimpleGrantedAuthority> authList) {
		List<Modalidad> listModalidades = new ArrayList<Modalidad>();
		
		// Permiso para moadlidad invenciones
		if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "I"))) {
			listModalidades.add( Modalidad.ALFA );
			listModalidades.add( Modalidad.AL_PATENTE_NACIONAL );
			listModalidades.add( Modalidad.AL_MODELO_UTILIDAD );
			listModalidades.add( Modalidad.AL_PATENTE_EUROPEA );
			listModalidades.add( Modalidad.AL_PATENTE_INVENCION );
		}
		//Permiso para modalidad marcas
		if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "S"))) {
			listModalidades.add( Modalidad.SITAMAR );
			listModalidades.add( Modalidad.MN_MARCA_NACIONAL );
			listModalidades.add( Modalidad.MN_NOMBRE_COMERCIAL );
		}
		
		// HCS: MAO-337
		listModalidades = filtrarModalidades(listModalidades);
		
		return listModalidades;
	}
	
	/**
	 * Devuelve una lista de modalidades para la pantalla de consulta de bonos adecuada a los permisos del usuario.
	 * @return
	 */
	public static List<Modalidad> getListaModalidadesConsultaBonos(Collection<SimpleGrantedAuthority> authList) {
		List<Modalidad> listModalidades = new ArrayList<Modalidad>();
		
		// Permiso para moadlidad invenciones
		if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "I"))) {
			listModalidades.add( Modalidad.ALFA );
			listModalidades.add( Modalidad.AL_PATENTE_INVENCION );
			listModalidades.add( Modalidad.AL_PATENTE_EUROPEA );
			//listModalidades.add( Modalidad.AL_TRANSMISION ); // F
			//listModalidades.add( Modalidad.AL_LICENCIA ); // L
			listModalidades.add( Modalidad.AL_PATENTE_NACIONAL );
			listModalidades.add( Modalidad.AL_SEMICONDUCTOR );
			listModalidades.add( Modalidad.AL_MODELO_UTILIDAD );
			listModalidades.add( Modalidad.AL_PATENTE_PCT );
		}
			
		// HCS: MAO-337
		listModalidades = filtrarModalidades(listModalidades);
		
		return listModalidades;
	}
	
	/**
	 * Devuelve una lista de modalidades para la pantalla de consulta de tasas adecuada a los permisos del usuario.
	 * @return
	 */
	public static List<Modalidad> getListaModalidadesConsultaTasas(Collection<SimpleGrantedAuthority> authList) {
		List<Modalidad> listModalidades = new ArrayList<Modalidad>();
		
		// Permiso para moadlidad invenciones
		if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "I"))) {
			listModalidades.add( Modalidad.ALFA );
			listModalidades.add( Modalidad.AL_PATENTE_INVENCION ); // C
			listModalidades.add( Modalidad.AL_PATENTE_EUROPEA ); // E
			listModalidades.add( Modalidad.AL_TRANSMISION ); // F
			listModalidades.add( Modalidad.AL_PATENTE_NACIONAL ); // P
			listModalidades.add( Modalidad.AL_SEMICONDUCTOR ); // T
			listModalidades.add( Modalidad.AL_MODELO_UTILIDAD ); // U
			listModalidades.add( Modalidad.AL_PATENTE_PCT ); // W
		}
		
		//Permiso para modalidad marcas
		if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "S"))) {
			listModalidades.add( Modalidad.SITAMAR );
			listModalidades.add( Modalidad.MN_MARCA_INTERNACIONAL ); // H
			listModalidades.add( Modalidad.MN_MARCA_NACIONAL ); // M
			listModalidades.add( Modalidad.MN_NOMBRE_COMERCIAL ); // N
			listModalidades.add( Modalidad.MN_ROTULO ); // R
		}
		
		//Permiso para modalidad diseño
		if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "D"))) {
			listModalidades.add( Modalidad.SITAMOD );
			listModalidades.add( Modalidad.DI_DISENO ); // D
			listModalidades.add( Modalidad.DI_MODELO ); // I
		}
		
		// HCS: MAO-337
		listModalidades = filtrarModalidades(listModalidades);
		
		return listModalidades;
	}
	
	
	/**
	 * Devuelve una lista de tomos de modalidades para la pantalla de bopi adecuada a los permisos del usuario.
	 * @return
	 */
	public static List<SelectItem> getListaTomosCompleta(Collection<SimpleGrantedAuthority> authList,  List<SelectItem> listTomos) {
		List<SelectItem> listTomosFinal = new ArrayList<SelectItem>();
		
		/** Comprobar pemisos de modalidades de usuario */
		for (SelectItem item : listTomos) {
			if (MessagesUtil.getMessage("bopi.tomos.tomo2.key").equals((String) item.getValue())) {
				//Permiso para modalidad invenciones
				if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "I"))) {
					if (!MAOConfiguracion.getAlfaIsDisabled()) {
						listTomosFinal.add(item);
					}
				}
				
			} 
			else if (MessagesUtil.getMessage("bopi.tomos.tomo1.key").equals((String) item.getValue())) {
				
				// Permiso para moadlidad marcas
				if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "S"))) {
					if (!MAOConfiguracion.getSitamarIsDisabled()) {
						listTomosFinal.add(item);
					}
				}				
			} else if (MessagesUtil.getMessage("bopi.tomos.tomo3.key").equals((String) item.getValue())) {
				
				//Permiso para modalidad diseño
				if (authList.contains(new SimpleGrantedAuthority(Roles.ROLE_MAO_MODALIDAD_ + "D"))) {
					if (!MAOConfiguracion.getSitamodIsDisabled()) {
						listTomosFinal.add(item);
					}
				}
			}
		}
		
		return listTomosFinal;
	}
	
	/**
	 * Devuelve una lista con los tipos de usuarios (sin usuario Gestor ni Asociado)
	 * @return
	 */
	public static List<String> getListaTipoUsuariosPrincipales() {
		List<String> lista = new ArrayList<String>();
		
		lista.add(TipoUsuario.AGENTE.name());
		lista.add(TipoUsuario.REPRESENTANTE.name());
		lista.add(TipoUsuario.TITULAR.name());
		
		return lista;
	}
	
	
	/**
	 * MAO-337
	 * Función auxiliar (y esperemos que temporar) para ocultar modalidaes que no queremos que se usen
	 * por una razón un otra.
	 * @param modalidades
	 * @author hcanosor
	 * @return
	 */
	private static List<Modalidad> filtrarModalidades(List<Modalidad> modalidades)
	{
		// HCS: Eliminamos la modalides que no queremos mostrar por la razón
		// que sea. De momento quitaremos todas menos las de invenciones, pero
		// en un futuro se prevee que se mostrarán todas
		
		List<Modalidad> result = new ArrayList<Modalidad>();
		
		for (Modalidad modalidad : modalidades) {
			
			// Nos quedamos unicamente con Patentes e invenciones de momento
			if (modalidad.isAlfa()) {
				if (!MAOConfiguracion.getAlfaIsDisabled()) {
					result.add(modalidad);
				}
			} else if (modalidad.isSitamar()) {
				if (!MAOConfiguracion.getSitamarIsDisabled()) {
					//Evitamos las modalidades que no queremos
					if (!modalidad.equals(Modalidad.MN_TRANSFERENCIA) &&
						!modalidad.equals(Modalidad.MN_CAMBIO_NOMBRE) && 
						!modalidad.equals(Modalidad.MN_LICENCIA)) {
						result.add(modalidad);
					}
				}		
			} else if (modalidad.isSitamod()) {
				if (!MAOConfiguracion.getSitamodIsDisabled()) {
					
					//Evitamos las modalidades que no queremos
					if (!modalidad.equals(Modalidad.DI_DISENOTEXTO) &&
						!modalidad.equals(Modalidad.DI_DIBUJO) && 
						!modalidad.equals(Modalidad.DI_DIBUJO)) {
						result.add(modalidad);
					}
				}		
			}		
		}
		
		return result;
	}	
	
}
