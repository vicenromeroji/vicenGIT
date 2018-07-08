package es.oepm.mao.view.sorters;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.Comparator;

import org.primefaces.model.SortOrder;

import es.oepm.mao.business.vo.PagoPrevioVO;

public class LazyDataSorter<T> implements Comparator<T>,Serializable {
	
	private Class<?> type;
	
	private static final long serialVersionUID = 1139156667440692968L;

	private String sortField;

	private SortOrder sortOrder;
	
	public LazyDataSorter(Class<?> type ,String sortField, SortOrder sortOrder) {
		super();
		this.sortField = sortField;
		this.sortOrder = sortOrder;
		this.type = type;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int compare(T obj1, T obj2) {
		try {
			
			Method method = type.getMethod("get" + this.sortField.substring(0, 1).toUpperCase() + this.sortField.substring(1));
			
			Object value1 = method.invoke(obj1);
			Object value2 = method.invoke(obj2);
			int value;
			
			// HCS: Chapa para que haga comparaciones sin case sensitive con los strings
			if (value1.getClass().equals(String.class) && value2.getClass().equals(String.class)) {
				
				String string1 = ((String) value1).toUpperCase();
				String string2 = ((String) value2).toUpperCase();
				
				value = ((Comparable<String>) string1).compareTo(string2);
			}
			else {
				value = ((Comparable<Object>) value1).compareTo(value2);
			}

			return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

}
