package es.oepm.mao.business.service;

import java.io.Serializable;





import es.oepm.core.business.vo.LoginVO;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.mao.business.vo.UsuarioLogado;

/**
 * Interfaz de servicios de login
 * 
 * @author josema.talavera
 */
public interface LoginService extends Serializable {

	/**
	 * Login de usuario mediante usuario y contraseña.
	 * 
	 * @param usuario
	 * @param clave
	 * @return UsuarioLogado
	 * @throws BusinessException
	 */
	public abstract UsuarioLogado login(LoginVO login) throws BusinessException;
	
	/**
	 * Busca un usuario mediante su numero de documento.
	 * 
	 * @param numeroDocumento
	 *            Numero de documento del usuario
	 * @return El usuario que corresponde con ese numero de documento
	 * @throws BusinessException
	 */
	void loginByNumeroDocumento(String numeroDocumento) throws BusinessException;

	/**
	 * Obtiene un usuario por el login.
	 * 
	 * @param usuario Nombre de usuario
	 * @return UsuarioLogado Objeto que representa al usuario
	 * @throws BusinessException
	 */
	public abstract UsuarioLogado findByLogin(String usuario) throws BusinessException;

	/**
	 * Valida un usuario logado mediante su numero de documento.
	 * 
	 * @param numDocumento
	 *            Numero de documento de la cuenta a verificar.
	 * @throws BusinessException
	 */
	void verificarCuentaUsuarioLogado(String numDocumento) throws BusinessException;
	
	/**
	 * Crea un acceso incorrecto para un usuario si este existe.
	 * 
	 * @param usuario
	 *            El usuario para el que se creara el acceso incorrecto
	 * @return El nº de intentos realizados -1 si no se encuentra el usuario
	 * @throws BusinessException
	 */
	Integer createIncorrectAcces(String usuario) throws BusinessException;

}