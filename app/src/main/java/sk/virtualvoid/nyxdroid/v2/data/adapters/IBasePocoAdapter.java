package sk.virtualvoid.nyxdroid.v2.data.adapters;

/**
 * 
 * @author Juraj
 *
 */
public interface IBasePocoAdapter {
	Object getItem(int pos);
	Object getItemById(long id);
	
	Object getFirstItem();
	Object getLastItem();
	
	void addItem(Object item);
	void addItem(int index, Object item);
	void removeItem(Object item);

	void notifyDataSetChanged();
}
