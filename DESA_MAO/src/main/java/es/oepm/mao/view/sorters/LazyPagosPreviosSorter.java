package es.oepm.mao.view.sorters;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;

import org.primefaces.model.SortOrder;

import es.oepm.mao.business.vo.PagoPrevioVO;

public class LazyPagosPreviosSorter implements Comparator<PagoPrevioVO>,Serializable {
	
	private static final long serialVersionUID = -6966365173895001962L;

	private String sortField;

	private SortOrder sortOrder;
	
	
	public LazyPagosPreviosSorter(String sortField, SortOrder sortOrder) {
		super();
		this.sortField = sortField;
		this.sortOrder = sortOrder;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int compare(PagoPrevioVO obj1, PagoPrevioVO obj2) {
		try {
			Method method = PagoPrevioVO.class.getMethod("get" + this.sortField.substring(0, 1).toUpperCase() + this.sortField.substring(1));
			
			Object value1 = method.invoke(obj1);
			Object value2 = method.invoke(obj2);
			int value;
			
			// HCS: Chapa para que haga comparaciones sin case sensitive con los strings
			if (value1.getClass().equals(String.class) && value2.getClass().equals(String.class)) {
				
				String string1 = ((String) value1).toUpperCase();
				String string2 = ((String) value2).toUpperCase();
				
				value = ((Comparable) string1).compareTo(string2);
			}
			else {
				value = ((Comparable) value1).compareTo(value2);
			}

			return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

}
