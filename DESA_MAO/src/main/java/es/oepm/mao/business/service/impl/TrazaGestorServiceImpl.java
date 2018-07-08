package es.oepm.mao.business.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import es.oepm.core.business.mao.vo.TrazaGestorVO;
import es.oepm.core.exceptions.BusinessException;
import es.oepm.core.exceptions.ExceptionUtil;
import es.oepm.core.logger.OepmLogger;
import es.oepm.core.session.SessionContext;
import es.oepm.mao.business.service.TrazaGestorService;
import es.oepm.mao.contenido.business.transformers.TrazaGestorTransformer;
import es.oepm.persistencia.mao.MaoTrazaGestor;
import es.oepm.persistencia.mao.dao.MaoTrazaGestorDAO;

@Service( value = "trazaGestorService" )
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = BusinessException.class)
public class TrazaGestorServiceImpl implements TrazaGestorService {
	
	private static final long serialVersionUID = -1755402636507722181L;
	
	@Autowired
	private MaoTrazaGestorDAO maoTrazaGestorDAO;
	
	/**
	 * Inserta la traza en bbdd.
	 * 
	 * @param trazaGestor
	 *            Objeto con los datos de la traza a insertar.
	 * @throws BusinessException
	 */
	private void insertTraze(TrazaGestorVO trazaGestor) throws BusinessException {
		OepmLogger.info("TrazaGestorService - insertTraze: Inicio insertar traza.  IdGestor:" + trazaGestor.getIdGestor());
		MaoTrazaGestor maoTrazaGestor = TrazaGestorTransformer.trazaGestorVOToMaoTrazaGestor(trazaGestor);
		
		try {
			maoTrazaGestorDAO.create(maoTrazaGestor);
			
			OepmLogger.info("TrazaGestorService - insertTraze: Traza insertada. IdTrazaGestor:" + maoTrazaGestor.getIdTraza());
		} catch (final Exception e) {
			OepmLogger.error(e);
			ExceptionUtil.throwBusinessException(e);
		}
		OepmLogger.info("TrazaGestorService - insertTraze: Fin insertar traza.  IdGestor:" + trazaGestor.getIdGestor());
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see es.oepm.mao.business.service.TrazaGestorService#insertTraze(java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = BusinessException.class)
	public void insertTraze(String accion, String detalle) throws BusinessException {
		OepmLogger.info("TrazaGestorService - insertTraze: Inicio insertar traza.  Accion:" + accion);
		// Creamos el vo de la traza
		TrazaGestorVO trazaGestorVO = new TrazaGestorVO();
		trazaGestorVO.setIdGestor(Long.valueOf(SessionContext.getIdUsuario()));
		trazaGestorVO.setFechaAccion(new Date());
		
		// Seteamos la acción realizada
		trazaGestorVO.setAccion(accion);
		// Seteamos el detalle de la acción realizada
		trazaGestorVO.setDetalle(detalle);
		
		// Insertamos en bbdd
		this.insertTraze(trazaGestorVO);
		OepmLogger.info("TrazaGestorService - insertTraze: Fin insertar traza.  Accion:" + accion);
	}

}
