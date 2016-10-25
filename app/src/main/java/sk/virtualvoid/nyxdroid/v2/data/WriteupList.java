package sk.virtualvoid.nyxdroid.v2.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author suchan_j
 * 
 */
public class WriteupList extends ArrayList<Writeup> {
	private static final long serialVersionUID = -377621254763536759L;

	public WriteupList() {
		super();
	}

	public WriteupList(Collection<Writeup> collection) {
		super(collection);
	}


	public List<Writeup> filter(Collection<Writeup> inputCollection) {
		ArrayList<Writeup> outputCollection = new ArrayList<Writeup>();
		for (Writeup item : inputCollection) {
			if (exists(item.Id)) {
				continue;
			}
			add(item);
			outputCollection.add(item);
		}
		return outputCollection;
	}
	
	public Writeup getItem(long id) {
		for (Writeup item : this) {
			if (item.Id == id) {
				return item;
			}
		}
		return null;
	}

	public boolean exists(long id) {
		for (Writeup item : this) {
			if (item.Id == id) {
				return true;
			}
		}
		return false;
	}
}
